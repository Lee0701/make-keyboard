package ee.oyatl.ime.make.modifier

interface ModifierKeyHandler {
    var state: ModifierKeyState
    fun onDown()
    fun onUp()
    fun onInput()
    fun autoUnlock()
}