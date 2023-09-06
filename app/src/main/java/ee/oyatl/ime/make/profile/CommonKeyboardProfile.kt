package ee.oyatl.ime.make.profile

import android.os.Handler
import android.os.Looper
import android.view.KeyCharacterMap
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.model.KeyOutput
import ee.oyatl.ime.make.modifier.DefaultShiftKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyStateSet
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.KeyEvent
import ee.oyatl.ime.make.view.keyboard.KeyboardListener
import ee.oyatl.ime.make.view.keyboard.KeyboardView

abstract class CommonKeyboardProfile: KeyboardListener {
    abstract val keyboardView: KeyboardView
    abstract val convertTable: CodeConvertTable
    abstract val moreKeysTable: MoreKeysTable
    abstract val listener: Listener

    private val doubleTapGap = 500
    private val longPressDelay = 500
    private val repeatDelay = 50
    private val autoUnlockShift = true

    protected val handler: Handler = Handler(Looper.getMainLooper())
    protected val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    val shiftHandler: ModifierKeyHandler = DefaultShiftKeyHandler(doubleTapGap, autoUnlockShift)
    val modifierState: ModifierKeyStateSet
        get() = ModifierKeyStateSet(shift = shiftHandler.state)

    fun onKeyCode(code: Int) {
        val modifierState = modifierState
        val output = convertTable.get(code, modifierState)?.toChar()?.toString()
        if(output != null) onKeyText(output)
        else listener.onRawKeyCode(code)
    }

    fun onKeyText(text: String) {
        listener.onText(text)
    }

    private fun onSpecialKey(event: KeyEvent) {
        when(event.action) {
            KeyEvent.Action.Press -> {
                if(event.output is KeyOutput.Special) {
                    onSpecialKeyPress(event.output)
                    listener.onFeedback(event.output)
                }
            }
            KeyEvent.Action.Release -> {
                if(event.output is KeyOutput.Special) {
                    onSpecialKeyRelease(event.output)
                }
            }
            KeyEvent.Action.Repeat -> {
                if(event.output is KeyOutput.Special) {
                    onSpecialKeyRepeat(event.output)
                }
            }
        }
        listener.onInputViewUpdate()
    }

    private fun onSpecialKeyPress(output: KeyOutput.Special) {
        when(output) {
            is KeyOutput.Special.Delete -> {
                listener.onDelete(1, 0)
                fun repeat() {
                    this.onSpecialKey(KeyEvent(KeyEvent.Action.Repeat, output))
                    handler.postDelayed({ repeat() }, repeatDelay.toLong())
                }
                handler.postDelayed({ repeat() }, longPressDelay.toLong())
            }
            is KeyOutput.Special.Shift -> {
                shiftHandler.onPress()
            }
            is KeyOutput.Special.Space -> {
                listener.onText(" ")
            }
            is KeyOutput.Special.Return -> {
                listener.onEditorAction(true)
            }
            else -> Unit
        }
    }

    private fun onSpecialKeyRelease(output: KeyOutput.Special) {
        when(output) {
            is KeyOutput.Special.Delete -> {
                handler.removeCallbacksAndMessages(null)
            }
            is KeyOutput.Special.Shift -> {
                shiftHandler.onRelease()
            }
            else -> Unit
        }
    }

    private fun onSpecialKeyRepeat(output: KeyOutput.Special) {
        when(output) {
            is KeyOutput.Special.Delete -> {
                listener.onDelete(1, 0)
            }
            else -> Unit
        }
    }

    protected fun updateInputView() {
        updateLabelsAndIcons()
        updateMoreKeys()
    }

    private fun updateLabelsAndIcons() {
        val labels = mapOf<Int, String>()
        val icons = mapOf<Int, Int>()
        keyboardView.updateLabelsAndIcons(getLabels() + labels, getIcons() + icons)
    }

    private fun updateMoreKeys() {
        val moreKeysTable = moreKeysTable.map.map { (char, value) ->
            val modifierState = modifierState
            val keyCode = convertTable.getReversed(char, modifierState)
            if(keyCode != null) keyCode to value else null
        }.filterNotNull().toMap()
        keyboardView.updateMoreKeyKeyboards(moreKeysTable)
    }

    private fun getLabels(): Map<Int, String> {
        val labelsToUpdate = android.view.KeyEvent.KEYCODE_UNKNOWN .. android.view.KeyEvent.KEYCODE_SEARCH
        val labels = labelsToUpdate.associateWith { code ->
            val modifierState = modifierState
            val label = convertTable.get(code, modifierState)?.toChar()?.toString()
            label ?: keyCharacterMap.get(code, modifierState.asMetaState()).toChar().toString()
        }
        return labels
    }

    private fun getIcons(): Map<Int, Int> {
        val shiftIconID = if(modifierState.shift.locked) R.drawable.keyic_shift_lock else R.drawable.keyic_shift
        return mapOf(
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT to shiftIconID,
            android.view.KeyEvent.KEYCODE_SHIFT_RIGHT to shiftIconID,
        )
    }

    interface Listener {
        fun onText(text: CharSequence)
        fun onDelete(before: Int, after: Int)
        fun onRawKeyCode(keyCode: Int)
        fun onEditorAction(fromEnterKey: Boolean)
        fun onFeedback(output: KeyOutput)
        fun onInputViewUpdate()
        fun onInputViewReset()
    }
}
