package ee.oyatl.ime.make.state

import android.view.KeyEvent

data class KeyboardState(
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