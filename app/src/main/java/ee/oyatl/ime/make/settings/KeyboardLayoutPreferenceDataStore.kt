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

    var preset: InputEnginePreset = kotlin.runCatching {
        InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(file.inputStream())
    }.getOrElse { InputEnginePreset() }

    init {
        update()
    }

    fun addComponent(componentType: InputViewComponentType) {
        preset = preset.copy(components = preset.components + componentType)
    }

    fun insertComponent(position: Int, componentType: InputViewComponentType) {
        val components = preset.components.toMutableList()
        components.add(position, componentType)
        preset = preset.copy(components = components)
    }

    fun removeComponent(position: Int): InputViewComponentType {
        val components = preset.components.toMutableList()
        val removed =components.removeAt(position)
        preset = preset.copy(components = components)
        return removed
    }

    fun swapComponents(a: Int, b: Int) {
        val components = preset.components.toMutableList()
        Collections.swap(components, a, b)
        preset = preset.copy(components = components)
    }

    override fun putString(key: String?, value: String?) {
        when(key) {
            KEY_ENGINE_TYPE -> {
                preset = preset.copy(type = InputEnginePreset.Type.valueOf(value ?: "Latin"))
            }
            KEY_LAYOUT_PRESET -> {
                val assetFileName = value ?: return
                val newLayout = InputEnginePreset.yaml
                    .decodeFromStream<InputEnginePreset>(context.assets.open(assetFileName)).layout
                preset = preset.copy(layout = newLayout)
            }
        }
        write()
        update()
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        when(key) {
            KEY_HANJA_ADDITIONAL_DICTIONARIES -> {
                val hanja = preset.hanja.copy(additionalDictionaries = values.orEmpty())
                preset = preset.copy(hanja = hanja)
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
            KEY_ROW_HEIGHT -> {
                val size = preset.size.copy(rowHeight = value.toInt())
                preset = preset.copy(size = size)
            }
        }
        write()
        update()
    }

    override fun putBoolean(key: String?, value: Boolean) {
        when(key) {
            KEY_DEFAULT_HEIGHT -> {
                preset = preset.copy(size = preset.size.copy(defaultHeight = value))
            }
            KEY_HANGUL_CORRECT_ORDERS -> {
                preset = preset.copy(hangul = preset.hangul.copy(correctOrders = value))
            }
            KEY_HANJA_CONVERSION -> {
                preset = preset.copy(hanja = preset.hanja.copy(conversion = value))
            }
        }
        write()
        update()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return when(key) {
            KEY_ENGINE_TYPE -> preset.type.name
            KEY_LAYOUT_PRESET -> null
            else -> defValue
        }
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): Set<String>? {
        return when(key) {
            KEY_HANJA_ADDITIONAL_DICTIONARIES -> preset.hanja.additionalDictionaries
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
            KEY_ROW_HEIGHT -> preset.size.rowHeight.toFloat()
            else -> defValue
        }
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return when(key) {
            KEY_DEFAULT_HEIGHT -> preset.size.defaultHeight
            KEY_HANGUL_CORRECT_ORDERS -> preset.hangul.correctOrders
            KEY_HANJA_CONVERSION -> preset.hanja.conversion
            else -> defValue
        }
    }

    fun write() {
        InputEnginePreset.yaml.encodeToStream(preset, file.outputStream())
    }

    fun update() {
        onChangeListener.onChange(preset)
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

        const val KEY_HANGUL_CORRECT_ORDERS = "input_hangul_correct_orders"

        const val KEY_HANJA_CONVERSION = "input_hanja_conversion"
        const val KEY_HANJA_ADDITIONAL_DICTIONARIES = "input_hanja_additional_dictionaries"
    }
}