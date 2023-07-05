package ee.oyatl.ime.make.modifier

interface ModifierKeyHandler {
    fun onDown()
    fun onUp()
    fun onInput()
    fun autoUnlock()
    fun isActive(): Boolean
}