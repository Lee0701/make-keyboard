package ee.oyatl.ime.make.state

interface ModifierKeyHandler {
    val state: ModifierKeyState
    fun reset()
    fun onPress()
    fun onRelease()
    fun onLock()
    fun onInput()
}