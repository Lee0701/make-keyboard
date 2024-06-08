package ee.oyatl.ime.make.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.INVISIBLE
import androidx.recyclerview.widget.RecyclerView.VISIBLE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.snackbar.Snackbar
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.preset.InputEnginePreset
import ee.oyatl.ime.make.preset.InputViewComponentType
import ee.oyatl.ime.make.preset.PresetLoader
import ee.oyatl.ime.make.service.IMEService
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_DEFAULT_HEIGHT
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ENGINE_TYPE
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_HANJA_ADDITIONAL_DICTIONARIES
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_HANJA_CONVERSION
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_INPUT_HEADER
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_LAYOUT_PRESET
import ee.oyatl.ime.make.settings.KeyboardLayoutPreferenceDataStore.Companion.KEY_ROW_HEIGHT
import ee.oyatl.ime.make.settings.KeyboardLayoutSettingsActivity.Companion.emptyInputEngineListener
import java.io.File

class KeyboardLayoutSettingsFragment(
    private val fileName: String,
    private val template: String,
): PreferenceFragmentCompat(), KeyboardLayoutPreferenceDataStore.OnChangeListener {

    private var preferenceDataStore: KeyboardLayoutPreferenceDataStore? = null
    private var adapter: KeyboardComponentsAdapter? = null
    private var loader: PresetLoader? = null

    private var keyboardViewType: String = "canvas"
    private var themeName: String = "theme_dynamic"

    private var previewMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.keyboard_layout_preferences, rootKey)
        val context = context ?: return
        val loader = PresetLoader(context)
        val rootPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        this.loader = loader

        val file = File(context.filesDir, fileName)
        if(!file.exists()) {
            file.outputStream().write(context.assets.open(template).readBytes())
        }

        keyboardViewType = rootPreferences.getString("appearance_keyboard_view_type", "canvas") ?: keyboardViewType
        themeName = rootPreferences.getString("appearance_theme", "theme_dynamic") ?: themeName
        val pref = KeyboardLayoutPreferenceDataStore(context, file, this)
        this.preferenceDataStore = pref
        preferenceManager.preferenceDataStore = pref

        val rootPreference = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val defaultHeightValue = rootPreference.getFloat("appearance_keyboard_height", 55f)

        val defaultHeight = findPreference<SwitchPreference>(KEY_DEFAULT_HEIGHT)
        val rowHeight = findPreference<SliderPreference>(KEY_ROW_HEIGHT)

        val engineType = findPreference<ListPreference>(KEY_ENGINE_TYPE)
        val layoutPreset = findPreference<ListPreference>(KEY_LAYOUT_PRESET)
        val inputHeader = findPreference<PreferenceCategory>(KEY_INPUT_HEADER)

        val hanjaConversion = findPreference<SwitchPreference>(KEY_HANJA_CONVERSION)
        val additionalDictionaries = findPreference<MultiSelectListPreference>(KEY_HANJA_ADDITIONAL_DICTIONARIES)

        hanjaConversion?.isChecked = preferenceDataStore?.getBoolean(KEY_HANJA_CONVERSION, false) == true
        additionalDictionaries?.values = preferenceDataStore?.getStringSet(KEY_HANJA_ADDITIONAL_DICTIONARIES, mutableSetOf())

        fun updateByDefaultHeight(newValue: Any?) {
            val enabled = newValue != true
            defaultHeight?.isChecked = !enabled
            rowHeight?.isEnabled = enabled
            if(!enabled) rowHeight?.value = defaultHeightValue
        }
        defaultHeight?.setOnPreferenceChangeListener { _, newValue ->
            updateByDefaultHeight(newValue)
            true
        }
        updateByDefaultHeight(pref.getBoolean(KEY_DEFAULT_HEIGHT, true))

        fun updateByEngineType(newValue: Any?) {
            inputHeader?.isVisible = newValue == InputEnginePreset.Type.Hangul.name
            val (entries, values) = when(newValue) {
                InputEnginePreset.Type.Hangul.name -> {
                    R.array.preset_hangul_entries to R.array.preset_hangul_values
                }
                InputEnginePreset.Type.Latin.name -> {
                    R.array.preset_latin_entries to R.array.preset_latin_values
                }
                InputEnginePreset.Type.Symbol.name -> {
                    R.array.preset_symbol_entries to R.array.preset_symbol_values
                }
                else -> return
            }
            layoutPreset?.setEntries(entries)
            layoutPreset?.setEntryValues(values)
        }
        engineType?.setOnPreferenceChangeListener { _, newValue ->
            updateByEngineType(newValue)
            layoutPreset?.setValueIndex(0)
            true
        }
        updateByEngineType(pref.getString(KEY_ENGINE_TYPE, "Latin"))
        engineType?.isVisible = false
        updateKeyboardView()
    }

    override fun onStart() {
        super.onStart()
        checkPreferences()
    }

    override fun onStop() {
        super.onStop()
        checkPreferences()
        IMEService.sendReloadIntent(activity ?: return)
    }

    private fun checkPreferences() {
        val preset = preferenceDataStore?.preset ?: return
        val hasMainKeyboardComponent = preset.components.any { it == InputViewComponentType.MainKeyboard }
        val hasCandidatesComponent = preset.components.any { it == InputViewComponentType.Candidates }
        val hanjaConversionIsOn = preset.hanja.conversion
        if(!hasMainKeyboardComponent) {
            Snackbar.make(requireView(), R.string.msg_main_keyboard_component_missing, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_add_component) {
                    val pref = preferenceDataStore ?: return@setAction
                    pref.addComponent(InputViewComponentType.MainKeyboard)
                    updateComponents()
                    checkPreferences()
                }
                .show()
        } else if(hanjaConversionIsOn && !hasCandidatesComponent) {
            Snackbar.make(requireView(), R.string.msg_hanja_candidates_component_missing, Snackbar.LENGTH_LONG)
                .setAction(R.string.action_add_component) {
                    val pref = preferenceDataStore ?: return@setAction
                    pref.insertComponent(0, InputViewComponentType.Candidates)
                    updateComponents()
                    checkPreferences()
                }
                .show()
        }
    }

    private fun updateKeyboardView() {
        activity?.findViewById<FrameLayout>(R.id.preview_mode_frame)?.visibility = INVISIBLE
        activity?.findViewById<RecyclerView>(R.id.reorder_mode_recycler_view)?.visibility = INVISIBLE
        if(previewMode) updatePreviewMode()
        else updateReorderMode()
    }

    private fun updatePreviewMode() {
        val context = context ?: return
        val frame = activity?.findViewById<FrameLayout>(R.id.preview_mode_frame) ?: return
        val preset = preferenceDataStore?.preset ?: return
        val engine = loader?.mod(preset)?.inflate(context, emptyInputEngineListener) ?: return
        frame.removeAllViews()
        frame.addView(engine.initView(context))
        engine.onReset()
        engine.onResetComponents()
        frame.visibility = VISIBLE
    }

    private fun updateReorderMode() {
        val context = context ?: return
        val preferenceDataStore = preferenceDataStore ?: return
        val recyclerView = activity?.findViewById<RecyclerView>(R.id.reorder_mode_recycler_view)

        val adapter = KeyboardComponentsAdapter(context)
        this.adapter = adapter
        val onMove = { from: ViewHolder, to: ViewHolder ->
            preferenceDataStore.swapComponents(from.adapterPosition, to.adapterPosition)
            adapter.notifyItemMoved(from.adapterPosition, to.adapterPosition)
            preferenceDataStore.write()
            true
        }
        val onSwipe = { viewHolder: ViewHolder, direction: Int ->
            preferenceDataStore.removeComponent(viewHolder.adapterPosition)
            adapter.notifyItemRemoved(viewHolder.adapterPosition)
            preferenceDataStore.write()
            preferenceDataStore.update()
        }
        val touchHelper = ItemTouchHelper(TouchCallback(onMove, onSwipe))
        adapter.onItemLongPress = { viewHolder ->
            touchHelper.startDrag(viewHolder)
        }
        adapter.onItemMenuPress = { type, viewHolder ->
            val components = preferenceDataStore.preset.components
            when(type) {
                KeyboardComponentsAdapter.ItemMenuType.Remove -> {
                    val position = viewHolder.adapterPosition
                    preferenceDataStore.removeComponent(position)
                    adapter.notifyItemRemoved(position)
                    preferenceDataStore.write()
                    preferenceDataStore.update()
                }
                KeyboardComponentsAdapter.ItemMenuType.MoveUp -> {
                    val position = viewHolder.adapterPosition
                    if(position - 1 in components.indices) {
                        preferenceDataStore.swapComponents(position, position - 1)
                        adapter.notifyItemMoved(position, position - 1)
                        preferenceDataStore.write()
                    }
                }
                KeyboardComponentsAdapter.ItemMenuType.MoveDown -> {
                    val position = viewHolder.adapterPosition
                    if(position + 1 in components.indices) {
                        preferenceDataStore.swapComponents(position, position + 1)
                        adapter.notifyItemMoved(position, position + 1)
                        preferenceDataStore.write()
                    }
                }
                else -> Unit
            }
        }
        recyclerView?.apply {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
            touchHelper.attachToRecyclerView(this)
            updateComponents()
            this.visibility = VISIBLE
        }
    }

    private fun updateComponents() {
        val pref = preferenceDataStore ?: return
        adapter?.submitList(pref.preset.components
            .map { pref.preset.copy(components = listOf(it)) })
        pref.write()
    }

    override fun onChange(preset: InputEnginePreset) {
        val rootPreference = PreferenceManager.getDefaultSharedPreferences(context ?: return)
        preferenceDataStore?.write()
        rootPreference.edit().putBoolean("requested_restart", true).apply()
        rootPreference.edit().putBoolean("requested_restart", false).apply()
        updateKeyboardView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_keyboard_layout_setting, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val previewMode = menu.findItem(R.id.preview_mode)
        val changeOrdersMode = menu.findItem(R.id.reorder_mode)
        previewMode.isVisible = false
        changeOrdersMode.isVisible = false
        if(this.previewMode) changeOrdersMode.isVisible = true
        else previewMode.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.preview_mode -> {
                previewMode = true
                updateKeyboardView()
                true
            }
            R.id.reorder_mode -> {
                previewMode = false
                updateKeyboardView()
                true
            }
            R.id.add_component -> {
                val preferenceDataStore = preferenceDataStore ?: return true
                val adapter = adapter ?: return true
                val bottomSheet = ChooseNewComponentBottomSheetFragment { componentType ->
                    val index = preferenceDataStore.preset.components.size
                    preferenceDataStore.insertComponent(index, componentType)
                    adapter.notifyItemInserted(index)
                    preferenceDataStore.write()
                    preferenceDataStore.update()
                }
                bottomSheet.show(childFragmentManager, ChooseNewComponentBottomSheetFragment.TAG)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class TouchCallback(
        val onMove: (ViewHolder, ViewHolder) -> Boolean,
        val onSwipe: (ViewHolder, Int) -> Unit,
    ): ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.START or ItemTouchHelper.END
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: ViewHolder,
            target: ViewHolder
        ): Boolean {
            return onMove(viewHolder, target)
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            onSwipe(viewHolder, direction)
        }

        // false as long-clicks are detected by gesture detector.
        override fun isLongPressDragEnabled(): Boolean = false
    }

    companion object {
        const val NUMBER_SOFT_ID = "common/soft_%s_number.yaml"
        const val TEXT_EDIT_SOFT_ID = "common/soft_%s_text_edit.yaml"
    }
}