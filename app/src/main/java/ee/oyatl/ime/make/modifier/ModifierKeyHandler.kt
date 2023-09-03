package ee.oyatl.ime.make.modifier

interface ModifierKeyHandler {
    val state: ModifierKeyState
    fun onPress()
    fun onRelease()
    fun onInput()
}