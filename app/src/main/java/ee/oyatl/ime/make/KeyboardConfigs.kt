package ee.oyatl.ime.make

import ee.oyatl.ime.make.keyboard.BottomRowConfig
import ee.oyatl.ime.make.keyboard.KeyConfig
import ee.oyatl.ime.make.keyboard.KeyIcons
import ee.oyatl.ime.make.keyboard.KeyLabel
import ee.oyatl.ime.make.keyboard.KeyboardConfig
import ee.oyatl.ime.make.keyboard.RowConfig
import ee.oyatl.ime.make.keyboard.toRowConfig

object KeyboardConfigs {

    private val commaKey = KeyConfig(",", KeyLabel.Text(","), type = KeyConfig.Type.Modifier)
    private val periodKey = KeyConfig(".", KeyLabel.Text("."), type = KeyConfig.Type.Modifier)

    private val shiftKey = KeyConfig("<<SHIFT>>", KeyLabel.Icon { KeyIcons.Shift() }, type = KeyConfig.Type.Modifier)
    private val deleteKey = KeyConfig("<<DELETE>>", KeyLabel.Icon { KeyIcons.Delete() }, type = KeyConfig.Type.Modifier)
    private val symbolKey = KeyConfig("<<SYMBOL>>", KeyLabel.Icon { KeyIcons.Symbol() }, type = KeyConfig.Type.Symbol)
    private val returnKey = KeyConfig("<<RETURN>>", KeyLabel.Icon { KeyIcons.Return() }, type = KeyConfig.Type.Return)

    fun defaultQwerty(): KeyboardConfig {
        val shiftKey = shiftKey.copy(width = 1.5f)
        val deleteKey = deleteKey.copy(width = 1.5f)
        return KeyboardConfig(
            listOf(
                "QWERTYUIOP".toRowConfig(),
                "ASDFGHJKL".toRowConfig(0.5f, 0.5f),
                RowConfig(shiftKey) + "ZXCVBNM".toRowConfig() + RowConfig(deleteKey),
            ),
            defaultBottomRow(),
        )
    }

    private fun defaultBottomRow(): BottomRowConfig {
        val symbolKey = symbolKey.copy(width = 2.0f)
        val returnKey = returnKey.copy(width = 2.0f)
        return BottomRowConfig(
            spaceWidth = 4f,
            leftKeys = listOf(symbolKey, commaKey),
            rightKeys = listOf(periodKey, returnKey),
        )
    }

}