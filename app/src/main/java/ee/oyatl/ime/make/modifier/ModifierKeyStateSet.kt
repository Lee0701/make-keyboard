package ee.oyatl.ime.make.modifier

import android.view.KeyEvent

data class ModifierKeyStateSet(
    val shift: ModifierKeyState = ModifierKeyState(),
    val alt: ModifierKeyState = ModifierKeyState(),
) {
    fun asMetaState(): Int {
        var result = 0
        result = result or if(shift.active) KeyEvent.META_SHIFT_ON else 0
        result = result or if(alt.active) KeyEvent.META_ALT_ON else 0
        return result
    }
}