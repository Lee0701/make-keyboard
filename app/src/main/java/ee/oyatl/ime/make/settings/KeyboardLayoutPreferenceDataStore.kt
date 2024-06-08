package ee.oyatl.ime.make.settings

import android.content.Context
import androidx.preference.PreferenceDataStore
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import ee.oyatl.ime.make.preset.InputEnginePreset
import ee.oyatl.ime.make.preset.InputViewComponentType
import java.io.File
import java.util.Collections

class KeyboardLayoutPreferenceDataStore(
    private val context: Context,
    private val file: File,
    private val onChangeListener: OnChangeListener,
): PreferenceDataStore() {

    private val mutablePreset: InputEnginePreset.Mutable = kotlin.runCatching {
        InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(file.inputStream()).mutable() }.getOrNull()
            ?: InputEnginePreset.Mutable()
    val preset: InputEnginePreset get() = mutablePreset.commit()

    init {
        update()
    }

    fun addComponent(componentType: InputViewComponentType) {
        this.mutablePreset.components.add(componentType)
    }

    fun insertComponent(position: Int, componentType: InputViewComponentType) {
        this.mutablePreset.components.add(position, componentType)
    }

    fun removeComponent(position: Int): InputViewComponentType {
        return mutablePreset.components.removeAt(position)
    }

    fun swapComponents(a: Int, b: Int) {
        Collections.swap(mutablePreset.components, a, b)
    }

    override fun putString(key: String?, value: String?) {
        when(key) {
            KEY_ENGINE_TYPE -> mutablePreset.type = InputEnginePreset.Type.valueOf(value ?: "Latin")
            KEY_LAYOUT_PRESET -> {
                val assetFileName = value ?: return
                val newLayout = InputEnginePreset.yaml
                    .decodeFromStream<InputEnginePreset>(context.assets.open(assetFileName)).layout
                mutablePreset.layout = newLayout.mutable()
            }
        }
        write()
        update()
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        when(key) {
            KEY_HANJA_ADDITIONAL_DICTIONARIES -> {
                mutablePreset.hanja.additionalDictionaries = values ?: mutableSetOf()
            }
        }
        write()
        update()
    }

    override fun putInt(key: String?, value: Int) {
        super.putInt(key, value)
    }

    override fun putLong(key: String?, value: Long) {
        super.putLong(key, value)
    }

    override fun putFloat(key: String?, value: Float) {
        when(key) {
            KEY_ROW_HEIGHT -> mutablePreset.size.rowHeight = value.toInt()
        }
        write()
        update()
    }

    override fun putBoolean(key: String?, value: Boolean) {
        when(key) {
            KEY_DEFAULT_HEIGHT -> mutablePreset.size.defaultHeight = value
            KEY_HANJA_CONVERSION -> mutablePreset.hanja.conversion = value
        }
        write()
        update()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return when(key) {
            KEY_ENGINE_TYPE -> mutablePreset.type.name
            KEY_LAYOUT_PRESET -> null
            else -> defValue
        }
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return when(key) {
            KEY_HANJA_ADDITIONAL_DICTIONARIES -> mutablePreset.hanja.additionalDictionaries
            else -> defValues
        }
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return super.getInt(key, defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return super.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return when(key) {
            KEY_ROW_HEIGHT -> mutablePreset.size.rowHeight.toFloat()
            else -> defValue
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return when(key) {
            KEY_DEFAULT_HEIGHT -> mutablePreset.size.defaultHeight
            KEY_HANJA_CONVERSION -> mutablePreset.hanja.conversion
            else -> defValue
        }
    }

    fun write() {
        InputEnginePreset.yaml.encodeToStream(mutablePreset.commit(), file.outputStream())
    }

    fun update() {
        onChangeListener.onChange(mutablePreset.commit())
    }

    interface OnChangeListener {
        fun onChange(preset: InputEnginePreset)
    }

    companion object {
        const val KEY_INPUT_HEADER = "input_layer"

        const val KEY_DEFAULT_HEIGHT = "soft_keyboard_default_height"
        const val KEY_ROW_HEIGHT = "soft_keyboard_row_height"

        const val KEY_ENGINE_TYPE = "input_engine_type"
        const val KEY_LAYOUT_PRESET = "input_layout_preset"

        const val KEY_HANJA_CONVERSION = "input_hanja_conversion"
        const val KEY_HANJA_ADDITIONAL_DICTIONARIES = "input_hanja_additional_dictionaries"
    }
}