package ee.oyatl.ime.make.preset

import android.content.Context
import android.content.SharedPreferences
import android.util.TypedValue
import androidx.preference.PreferenceManager
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import java.io.File
import kotlin.math.roundToInt

class PresetLoader(
    val context: Context
) {
    private val pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val screenType: String = pref.getString("layout_screen_type", "mobile") ?: "mobile"

    private val unifyHeight: Boolean = pref.getBoolean("appearance_unify_height", false)
    private val rowHeight: Int = pref.getFloat("appearance_keyboard_height", 55f).roundToInt()

    fun load(fileName: String, defaultFileName: String): InputEnginePreset {
        return loadFromFilesDir(fileName)
            ?: loadFromAssets(fileName)
            ?: loadFromAssets(defaultFileName)
            ?: InputEnginePreset()
    }

    fun loadHardware(fileName: String, defaultFileName: String): InputEnginePreset {
        return loadFromFilesDir(fileName)
            ?: loadFromAssets(fileName)
            ?: loadFromAssets(defaultFileName)
                ?.copy(components = listOf(InputViewComponentType.LanguageTabBar))
            ?: InputEnginePreset()
    }

    private fun loadFromFilesDir(fileName: String): InputEnginePreset? {
        val result = kotlin.runCatching {
            InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(File(context.filesDir, fileName).inputStream())
        }
        return result.getOrNull()
    }

    private fun loadFromAssets(fileName: String): InputEnginePreset? {
        val fromAssets = kotlin.runCatching {
            InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(context.assets.open(fileName))
        }
        return fromAssets.getOrNull()
    }

    private fun modHeight(height: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            height.toFloat(),
            context.resources.displayMetrics
        ).roundToInt()
    }

    private fun modSize(size: InputEnginePreset.Size): InputEnginePreset.Size {
        val rowHeight: Int = modHeight(if(size.defaultHeight) rowHeight else size.rowHeight)
        return size.copy(
            unifyHeight = unifyHeight,
            rowHeight = rowHeight
        )
    }

    fun resolveSoftKeyboardSelect(fileName: String): String? {
        val selector = kotlin.runCatching {
            Yaml.default.decodeFromStream<SoftKeyboardSelector>(context.assets.open(fileName))
        }.getOrNull() ?: kotlin.runCatching {
            val file = File(context.filesDir, fileName)
            Yaml.default.decodeFromStream<SoftKeyboardSelector>(file.inputStream())
        }.getOrNull() ?: return null
        return when(screenType) {
            "mobile" -> selector.mobile
            "tablet" -> selector.tablet
            "full" -> selector.full
            "television" -> selector.television
            else -> selector.mobile
        }
    }

    fun modPreset(preset: InputEnginePreset): InputEnginePreset {
        val moreKeysTable = mutableListOf<String>()
        val overrideTable = mutableListOf<String>()
        moreKeysTable += "symbol/morekeys_common.yaml"
        if(preset.type == InputEnginePreset.Type.Symbol) {
            when(preset.language) {
                "ko" -> {
                    moreKeysTable += "symbol/morekeys_symbols_hangul.yaml"
                    overrideTable += "symbol/override_currency_won.yaml"
                }
            }
        }
        val layout = preset.layout.copy(
            softKeyboard = preset.layout.softKeyboard.mapNotNull { resolveSoftKeyboardSelect(it) },
            moreKeysTable = preset.layout.moreKeysTable + moreKeysTable,
            codeConvertTable = preset.layout.codeConvertTable,
            overrideTable = preset.layout.overrideTable + overrideTable,
            combinationTable = preset.layout.combinationTable,
        )
        return preset.copy(
            layout = layout,
            size = modSize(preset.size),
        )
    }
}