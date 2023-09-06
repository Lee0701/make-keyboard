package ee.oyatl.ime.make.profile

import android.view.KeyEvent
import ee.oyatl.ime.make.IMEService
import ee.oyatl.ime.make.model.KeyOutput
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.keyboard.FlickDirection
import ee.oyatl.ime.make.view.keyboard.KeyboardView

open class BasicKeyboardProfile(
    override val keyboardView: KeyboardView,
    override val convertTable: CodeConvertTable,
    override val moreKeysTable: MoreKeysTable,
    override val listener: Listener
): CommonKeyboardProfile() {

    override val doubleTapGap: Int = 500
    override val longPressDelay: Int = 300
    override val repeatDelay: Int = 20
    override val autoUnlockShift: Boolean = true

    init {
        updateInputView()
    }

    override fun onKeyClick(code: Int, output: String?) {
        val char = keyCharacterMap[code, modifierState.asMetaState()]
        val isPrintingKey = IMEService.codeIsPrintingKey(code)
        val specialKey = when(code) {
            KeyEvent.KEYCODE_DEL -> onDeleteKey()
            KeyEvent.KEYCODE_SPACE -> onSpace()
            KeyEvent.KEYCODE_ENTER -> onActionKey()
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> onLanguageKey()
            KeyEvent.KEYCODE_SYM -> onSymbolsKey()
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> true
            else -> false
        }
        if(!specialKey) {
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
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_SHIFT_RIGHT -> shiftHandler.onPress()
            else -> return
        }
        updateInputView()
    }

    override fun onKeyUp(code: Int, output: String?) {
        when(code) {
            KeyEvent.KEYCODE_SHIFT_LEFT,
            KeyEvent.KEYCODE_SHIFT_RIGHT -> shiftHandler.onRelease()
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
        shiftHandler.onInput()
        updateInputView()
        return true
    }

    private fun onActionKey(): Boolean {
        resetInput()
        listener.onRawKeyCode(KeyEvent.KEYCODE_ENTER)
        shiftHandler.onInput()
        return true
    }

    private fun onLanguageKey(): Boolean {
        return true
    }

    private fun onSymbolsKey(): Boolean {
        listener.onSpecialKey(KeyOutput.Special.Symbol)
        return true
    }

    private fun resetInput() {

    }

    class Symbols(
        keyboardView: KeyboardView,
        convertTable: CodeConvertTable,
        moreKeysTable: MoreKeysTable,
        listener: Listener
    ): BasicKeyboardProfile(
        keyboardView,
        convertTable,
        moreKeysTable,
        listener
    ) {
        override val autoUnlockShift: Boolean = false
        override fun onKeyClick(code: Int, output: String?) {
            if(code == KeyEvent.KEYCODE_SPACE || code == KeyEvent.KEYCODE_ENTER) {
                if(shiftHandler.state.active) shiftHandler.onPress()
                super.onKeyClick(code, output)
                shiftHandler.onRelease()
            } else {
                super.onKeyClick(code, output)
            }
        }
    }
}