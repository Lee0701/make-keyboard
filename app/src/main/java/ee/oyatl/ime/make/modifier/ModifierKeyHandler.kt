package ee.oyatl.ime.make.modifier

interface ModifierKeyHandler {
    val state: ModifierKeyState
    fun reset()
    fun onPress()
    fun onRelease()
    fun onInput()
}