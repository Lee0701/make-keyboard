package ee.oyatl.ime.make.profile

import ee.oyatl.ime.make.IMEService
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.keyboard.FlickDirection
import ee.oyatl.ime.make.view.keyboard.KeyboardView

class KeyboardProfile(
    override val keyboardView: KeyboardView,
    override val convertTable: CodeConvertTable,
    override val moreKeysTable: MoreKeysTable,
    override val listener: Listener,
): CommonKeyboardProfile() {

    init {
        updateInputView()
    }

    override fun onKeyClick(code: Int, output: String?) {
        val char = keyCharacterMap[code, modifierState.asMetaState()]
        val isPrintingKey = IMEService.codeIsPrintingKey(code)
        val systemKey = when(code) {
            android.view.KeyEvent.KEYCODE_DEL -> onDeleteKey()
            android.view.KeyEvent.KEYCODE_SPACE -> onSpace()
            android.view.KeyEvent.KEYCODE_ENTER -> onActionKey()
            android.view.KeyEvent.KEYCODE_LANGUAGE_SWITCH -> onLanguageKey()
            android.view.KeyEvent.KEYCODE_SYM -> onSymbolsKey()
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT, android.view.KeyEvent.KEYCODE_SHIFT_RIGHT -> true
            else -> false
        }
        if(!systemKey) {
            if(code == 0 && output != null) {
                onKeyText(output)
            } else if(char == 0) {
                listener.onRawKeyCode(code)
            } else if(isPrintingKey) {
                if(code != 0) onKeyCode(code)
                else onKeyText(char.toChar().toString())
            }
            shiftHandler.onInput()
            updateInputView()
        }
    }

    override fun onKeyLongClick(code: Int, output: String?) {
    }

    override fun onKeyDown(code: Int, output: String?) {
        when(code) {
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
            android.view.KeyEvent.KEYCODE_SHIFT_RIGHT -> shiftHandler.onPress()
            else -> return
        }
        updateInputView()
    }

    override fun onKeyUp(code: Int, output: String?) {
        when(code) {
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
            android.view.KeyEvent.KEYCODE_SHIFT_RIGHT -> shiftHandler.onRelease()
            else -> return
        }
        updateInputView()
    }

    override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) {
    }

    private fun onDeleteKey(): Boolean {
        resetInput()
        listener.onDelete(1, 0)
        return true
    }

    private fun onSpace(): Boolean {
        resetInput()
        listener.onText(" ")
        return true
    }

    private fun onActionKey(): Boolean {
        resetInput()
        listener.onRawKeyCode(android.view.KeyEvent.KEYCODE_ENTER)
        return true
    }

    private fun onLanguageKey(): Boolean {
        return true
    }

    private fun onSymbolsKey(): Boolean {
        return true
    }

    private fun resetInput() {

    }

}