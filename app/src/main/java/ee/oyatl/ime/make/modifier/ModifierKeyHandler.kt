package ee.oyatl.ime.make.modifier

interface ModifierKeyHandler {
    var state: ModifierKeyState
    fun onPress()
    fun onRelease()
    fun onInput()
}