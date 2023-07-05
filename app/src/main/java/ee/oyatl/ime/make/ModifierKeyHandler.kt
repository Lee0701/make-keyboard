package ee.oyatl.ime.make

interface ModifierKeyHandler {
    fun onDown()
    fun onUp()
    fun onInput()
    fun autoUnlock()
    fun isActive(): Boolean
}