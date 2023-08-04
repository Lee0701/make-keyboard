package ee.oyatl.ime.make.modifier

class DefaultShiftKeyHandler(
    private val doubleTapGap: Int,
): ModifierKeyHandler {

    override var state: ModifierKeyState = ModifierKeyState()
    private var clickedTime: Long = 0L
    private var inputEvent: Boolean = false

    override fun onPress() {
        val lastState = state
        val newState = lastState.copy(
            pressing = true
        )
        state = newState
        inputEvent = false
    }

    override fun onRelease() {
        val lastState = state
        val currentState = lastState.copy(pressing = false)

        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - clickedTime

        val newState =
            if(currentState.locked) {
                ModifierKeyState()
            } else if(currentState.pressed) {
                if(timeDiff < doubleTapGap) {
                    ModifierKeyState(pressed = true, locked = true)
                } else {
                    ModifierKeyState()
                }
            } else if(inputEvent) {
                ModifierKeyState()
            } else {
                ModifierKeyState(pressed = true)
            }

        state = newState.copy(pressing = false)
        clickedTime = currentTime
        inputEvent = false
    }

    override fun onInput() {
        inputEvent = true
    }

    override fun autoUnlock() {
        if(state.pressing && inputEvent) return
        val lastState = state
        if(!lastState.locked && !lastState.pressing) state = ModifierKeyState()
    }
}