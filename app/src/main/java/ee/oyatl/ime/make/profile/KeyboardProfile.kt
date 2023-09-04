package ee.oyatl.ime.make.profile

import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.keyboard.KeyboardView

class KeyboardProfile(
    val keyboardView: KeyboardView,
    val convertTable: CodeConvertTable,
    val moreKeysTable: MoreKeysTable,
)
