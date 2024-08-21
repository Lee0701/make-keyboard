package ee.oyatl.ime.make.service

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import ee.oyatl.ime.make.preset.serialization.KeyCodeSerializer
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.table.CodeConvertTable
import ee.oyatl.ime.make.preset.table.SimpleCodeConvertTable
import kotlinx.serialization.modules.EmptySerializersModule
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ImportExportActivity: AppCompatActivity() {

    private val config = YamlConfiguration(encodeDefaults = false)
    private val yaml = Yaml(EmptySerializersModule(), config)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val tables = listOf(
            "hangul_2set/table_ks5002.yaml",
            "table_old_hangul.yaml",
            "hangul_3set/table_390.yaml",
            "hangul_3set/table_391.yaml",
            "hangul_3set/table_391_strict.yaml",
            "latin/table_latin_colemak.yaml",
            "latin/table_latin_dvorak.yaml",
            "symbol/table_symbol_g.yaml",
        )
//        upgradeTables(tables)

        val keyboards = listOf(
//            "common/soft_mobile_qwerty.yaml",
            "hangul_3set/soft_%s_390.yaml",
            "hangul_3set/soft_%s_391.yaml",
//            "latin/soft_mobile_qwerty_dvorak_custom.yaml",
//            "soft_qwerty_mobile_with_num.yaml",
//            "common/soft_mobile_qwerty_with_semicolon.yaml",
        )
//        upgradeKeyboards(keyboards)
//        generatePreset()

        Button(this).apply {
            text = "Import Layouts"
            setOnClickListener {
                // 신세벌식 계열 변환 후에는 이중모음용 가상 낱자 코드를 필히 확인할것.
                val layouts: Map<String, Array<IntArray>> = mapOf(
//                    "3set_shin_original_chojong" to JAMO_SEBUL_SHIN_ORIGINAL_CHOJONG,
//                    "3set_shin_original_chojung" to JAMO_SEBUL_SHIN_ORIGINAL_CHOJUNG,
//                    "3set_shin_edit_chojong" to OpenWnnKoreanLayouts.JAMO_SEBUL_SHIN_EDIT_CHOJONG,
//                    "3set_shin_edit_chojung" to OpenWnnKoreanLayouts.JAMO_SEBUL_SHIN_EDIT_CHOJUNG,
//                    "3set_393_old_hangul" to OpenWnnKoreanLayouts.JAMO_SEBUL_393Y,
                )
                layouts.forEach { (name, layout) ->
                    val table = importLayout(layout)
                    val file = File(filesDir, "layout_$name.yaml")
                    yaml.encodeToStream(table, file.outputStream())
                }
            }
            contentView.addView(this)
        }

        setContentView(contentView)
    }

    private fun upgradeTables(names: List<String>) {
        names.forEach { name ->
            upgradeTable(
                assets.open(name.split('.').first()),
                File(filesDir, name).outputStream(),
            )
        }
    }

    private fun upgradeTable(input: InputStream, output: OutputStream) {
        val table = yaml.decodeFromStream<CodeConvertTable>(input)
//        val newTable = CodeConvertTable(map2 = table.map.map { (k, v) -> KeyEvent.keyCodeToString(k) to v.convert() }.toMap())
        val newTable = table
        yaml.encodeToStream(newTable, output)
    }

    private fun upgradeKeyboards(names: List<String>) {
        names.forEach { name ->
            val input = assets.open(name)
            val file = File(filesDir, name)
            file.parentFile?.mkdirs()
            val output = file.outputStream()
            upgradeKeyboard(input, output)
        }
    }

    private fun upgradeKeyboard(input: InputStream, output: OutputStream) {
        val keyboard = yaml.decodeFromStream<Keyboard>(input)
        yaml.encodeToStream(keyboard, output)
    }

    private fun generatePreset() {
//        val h2 = InputEnginePreset.Hangul(
//            softKeyboard = listOf("common/soft_tablet_qwerty.yaml"),
//            codeConvertTable = listOf("hangul_2set/table_ks5002.yaml"),
//            combinationTable = listOf("hangul_2set/comb_ks5002.yaml"),
//        )
//        Yaml.default.encodeToStream(h2, File(filesDir, "test.yaml").outputStream())
    }

    private fun importLayout(layout: Array<IntArray>): CodeConvertTable {
        val map = layout.associate { (code, base, shift) ->
            KeyEvent.keyCodeFromString(convertKeycode(code) ?: "") to SimpleCodeConvertTable.Entry(base, shift) }
        return SimpleCodeConvertTable(map = map)
    }

    private fun convertKeycode(code: Int): String? {
        val converted = when(code) {
            in '0'.code .. '9'.code -> code - '0'.code + KeyEvent.KEYCODE_0
            in 'A'.code .. 'Z'.code -> code - 'A'.code + KeyEvent.KEYCODE_A
            in 'a'.code .. 'z'.code -> code - 'a'.code + KeyEvent.KEYCODE_A
            '-'.code, '_'.code -> KeyEvent.KEYCODE_MINUS
            '='.code, '+'.code -> KeyEvent.KEYCODE_EQUALS
            '\\'.code, '|'.code -> KeyEvent.KEYCODE_BACKSLASH
            '['.code, '{'.code -> KeyEvent.KEYCODE_LEFT_BRACKET
            ']'.code, '}'.code -> KeyEvent.KEYCODE_RIGHT_BRACKET
            ';'.code, ':'.code -> KeyEvent.KEYCODE_SEMICOLON
            '\''.code, '"'.code -> KeyEvent.KEYCODE_APOSTROPHE
            ','.code, '<'.code -> KeyEvent.KEYCODE_COMMA
            '.'.code, '>'.code -> KeyEvent.KEYCODE_PERIOD
            '/'.code, '?'.code -> KeyEvent.KEYCODE_SLASH
            else -> null
        }
        return converted?.let { KeyCodeSerializer.keyCodeToString(it) }
    }

}