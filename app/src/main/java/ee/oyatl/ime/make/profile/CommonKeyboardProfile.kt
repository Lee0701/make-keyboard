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

    abstract val doubleTapGap: Int
    abstract val longPressDelay: Int
    abstract val repeatDelay: Int
    abstract val autoUnlockShift: Boolean

    protected val handler: Handler = Handler(Looper.getMainLooper())
    protected val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    val shiftHandler: ModifierKeyHandler by lazy { DefaultShiftKeyHandler(doubleTapGap, autoUnlockShift) }
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

    fun onKeyRepeat(output: KeyOutput.Special) {
        when(output) {
            is KeyOutput.Special.Delete -> {
                listener.onDelete(1, 0)
            }
            else -> Unit
        }
    }

    fun reset() {
        this.shiftHandler.reset()
    }

    fun updateInputView() {
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
        fun onSpecialKey(output: KeyOutput.Special)
        fun onRawKeyCode(keyCode: Int)
        fun onEditorAction(fromEnterKey: Boolean)
        fun onFeedback(output: KeyOutput)
        fun onInputViewUpdate()
        fun onInputViewReset()
    }
}
