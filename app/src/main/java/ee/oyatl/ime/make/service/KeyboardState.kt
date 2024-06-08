package ee.oyatl.ime.make.service

import android.view.KeyEvent

data class KeyboardState(
    val shiftState: ModifierState = ModifierState(),
    val altState: ModifierState = ModifierState(),
    val controlState: ModifierState = ModifierState(),
    val metaState: ModifierState = ModifierState(),
) {
    fun asMetaState(): Int {
        var result = 0
        result = result or if(shiftState.pressed || shiftState.pressing) KeyEvent.META_SHIFT_ON else 0
        result = result or if(altState.pressed || altState.pressing) KeyEvent.META_ALT_ON else 0
        result = result or if(controlState.pressed || controlState.pressing) KeyEvent.META_CTRL_ON else 0
        result = result or if(metaState.pressed || metaState.pressing) KeyEvent.META_META_ON else 0
        return result
    }
}
