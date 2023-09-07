package ee.oyatl.ime.make.modifier

class DefaultShiftKeyHandler(
    private val doubleTapGap: Int,
    private val autoUnlock: Boolean = true,
): ModifierKeyHandler {

    override var state: ModifierKeyState = ModifierKeyState()
    private var clickedTime: Long = 0L
    private var inputEventExists: Boolean = false

    override fun reset() {
        state = ModifierKeyState()
        clickedTime = 0L
        inputEventExists = false
    }

    override fun onPress() {
        val lastState = state
        val newState = lastState.copy(
            pressing = true
        )
        state = newState
        inputEventExists = false
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
            } else if(inputEventExists) {
                ModifierKeyState()
            } else {
                ModifierKeyState(pressed = true)
            }

        state = newState.copy(pressing = false)
        clickedTime = currentTime
        inputEventExists = false
    }

    override fun onInput() {
        autoUnlock()
        inputEventExists = true
    }

    private fun autoUnlock() {
        if(!autoUnlock) return
        if(state.pressing && inputEventExists) return
        val lastState = state
        if(!lastState.locked && !lastState.pressing) state = ModifierKeyState()
    }

}