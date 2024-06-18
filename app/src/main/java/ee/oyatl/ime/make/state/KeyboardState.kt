package ee.oyatl.ime.make.state

import android.view.KeyEvent

data class KeyboardState(
    val shift: ModifierKeyState = ModifierKeyState(),
    val alt: ModifierKeyState = ModifierKeyState(),
    val control: ModifierKeyState = ModifierKeyState(),
    val meta: ModifierKeyState = ModifierKeyState()
) {
    fun asMetaState(): Int {
        return if(shift.active) KeyEvent.META_SHIFT_ON else 0 or
                if(alt.active) KeyEvent.META_ALT_ON else 0 or
                if(control.active) KeyEvent.META_CTRL_ON else 0 or
                if(meta.active) KeyEvent.META_META_ON else 0
    }
}