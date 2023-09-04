package ee.oyatl.ime.make.profile

import ee.oyatl.ime.make.modifier.DefaultShiftKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyStateSet
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.keyboard.KeyboardView

class KeyboardProfile(
    val keyboardView: KeyboardView,
    val convertTable: CodeConvertTable,
    val moreKeysTable: MoreKeysTable,
    doubleTapGap: Int,
    autoUnlockShift: Boolean = true,
) {
    val shiftHandler: ModifierKeyHandler = DefaultShiftKeyHandler(doubleTapGap, autoUnlockShift)
    val modifierState: ModifierKeyStateSet
        get() = ModifierKeyStateSet(shift = shiftHandler.state)
}
