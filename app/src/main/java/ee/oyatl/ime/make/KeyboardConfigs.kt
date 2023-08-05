package ee.oyatl.ime.make

import ee.oyatl.ime.make.keyboard.BottomRowConfig
import ee.oyatl.ime.make.keyboard.KeyConfig
import ee.oyatl.ime.make.keyboard.KeyIcons
import ee.oyatl.ime.make.keyboard.KeyLabel
import ee.oyatl.ime.make.keyboard.KeyOutput
import ee.oyatl.ime.make.keyboard.KeyboardConfig
import ee.oyatl.ime.make.keyboard.RowConfig
import ee.oyatl.ime.make.keyboard.plus
import ee.oyatl.ime.make.keyboard.toRowConfig

object KeyboardConfigs {

    fun defaultQwerty(): KeyboardConfig {
        val shiftKey = shiftKey.copy(width = 1.5f)
        val deleteKey = deleteKey.copy(width = 1.5f)
        return KeyboardConfig(
            listOf(
                "QWERTYUIOP".toRowConfig(),
                "ASDFGHJKL".toRowConfig(0.5f, 0.5f),
                shiftKey + "ZXCVBNM".toRowConfig() + deleteKey,
            ),
            defaultBottomRow(),
        )
    }

    fun defaultDvorak(): KeyboardConfig {
        val shiftKey = shiftKey.copy(width = 1.5f)
        val deleteKey = deleteKey.copy(width = 1.5f)
        return KeyboardConfig(
            listOf(
                "'.,PYFGCRL".toRowConfig(),
                "AOEUIDHTNS".toRowConfig(),
                RowConfig(shiftKey) + "QJKXBMZ".toRowConfig() + RowConfig(deleteKey),
            ),
            dvorakBottomRow(),
        )
    }

    private val commaKey = KeyConfig(KeyOutput.Text(","), type = KeyConfig.Type.Modifier)
    private val periodKey = KeyConfig(KeyOutput.Text("."), type = KeyConfig.Type.Modifier)

    private val shiftKey = KeyConfig(KeyOutput.Special.Shift(), KeyLabel.Icon { KeyIcons.Shift() }, type = KeyConfig.Type.Modifier)
    private val deleteKey = KeyConfig(KeyOutput.Special.Delete(1, 0), KeyLabel.Icon { KeyIcons.Delete() }, type = KeyConfig.Type.Modifier)
    private val symbolKey = KeyConfig(KeyOutput.Special.Symbol, KeyLabel.Icon { KeyIcons.Symbol() }, type = KeyConfig.Type.Symbol)
    private val returnKey = KeyConfig(KeyOutput.Special.Return, KeyLabel.Icon { KeyIcons.Return() }, type = KeyConfig.Type.Return)

    private fun defaultBottomRow(): BottomRowConfig {
        val symbolKey = symbolKey.copy(width = 2.0f)
        val returnKey = returnKey.copy(width = 2.0f)
        return BottomRowConfig(
            spaceWidth = 4f,
            leftKeys = listOf(symbolKey, commaKey),
            rightKeys = listOf(periodKey, returnKey),
        )
    }

    private fun dvorakBottomRow(): BottomRowConfig {
        val symbolKey = symbolKey.copy(width = 2.0f)
        val returnKey = returnKey.copy(width = 2.0f)
        val wKey = KeyConfig(KeyOutput.Text("W"), type = KeyConfig.Type.Modifier)
        val vKey = KeyConfig(KeyOutput.Text("V"), type = KeyConfig.Type.Modifier)
        return BottomRowConfig(
            spaceWidth = 4f,
            leftKeys = listOf(symbolKey, wKey),
            rightKeys = listOf(vKey, returnKey),
        )
    }

}