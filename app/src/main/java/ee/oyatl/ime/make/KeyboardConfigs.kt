package ee.oyatl.ime.make

import ee.oyatl.ime.make.keyboard.BottomRowConfig
import ee.oyatl.ime.make.keyboard.KeyConfig
import ee.oyatl.ime.make.keyboard.KeyIcons
import ee.oyatl.ime.make.keyboard.KeyLabel
import ee.oyatl.ime.make.keyboard.KeyboardConfig
import ee.oyatl.ime.make.keyboard.RowConfig
import ee.oyatl.ime.make.keyboard.toRowConfig
import ee.oyatl.ime.make.modifier.ModifierKeyState

object KeyboardConfigs {

    fun generate(shiftState: ModifierKeyState): KeyboardConfig {
        val commaKey = KeyConfig(",", KeyLabel.Text(","), type = KeyConfig.Type.Modifier)
        val periodKey = KeyConfig(".", KeyLabel.Text("."), type = KeyConfig.Type.Modifier)

        val shiftKey = KeyConfig("<<SHIFT>>", KeyLabel.Icon { KeyIcons.Shift(shiftState) }, width = 1.5f, type = KeyConfig.Type.Modifier)
        val deleteKey = KeyConfig("<<DELETE>>", KeyLabel.Icon { KeyIcons.Delete() }, width = 1.5f, type = KeyConfig.Type.Modifier)
        val symbolKey = KeyConfig("<<SYMBOL>>", KeyLabel.Icon { KeyIcons.Symbol() }, width = 2f, type = KeyConfig.Type.Symbol)
        val returnKey = KeyConfig("<<RETURN>>", KeyLabel.Icon { KeyIcons.Return() }, width = 2f, type = KeyConfig.Type.Return)

        return KeyboardConfig(
            listOf(
                "QWERTYUIOP".toRowConfig(),
                "ASDFGHJKL".toRowConfig(0.5f, 0.5f),
                RowConfig(shiftKey) + "ZXCVBNM".toRowConfig(1.5f) + RowConfig(deleteKey),
            ),
            BottomRowConfig(
                spaceWidth = 4f,
                leftKeys = listOf(symbolKey, commaKey),
                rightKeys = listOf(periodKey, returnKey),
            )
        )
    }

}