package ee.oyatl.ime.make.preset

import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.keyboard.KeyboardView

data class KeyboardPreset(
    val keyboardView: KeyboardView,
    val convertTable: CodeConvertTable,
    val moreKeysTable: MoreKeysTable,
)
