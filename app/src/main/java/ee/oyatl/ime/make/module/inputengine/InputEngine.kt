package ee.oyatl.ime.make.module.inputengine

import android.content.Context
import android.view.KeyCharacterMap
import android.view.View
import ee.oyatl.ime.make.modifiers.DefaultShiftKeyHandler
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet
import ee.oyatl.ime.make.module.candidates.Candidate
import ee.oyatl.ime.make.module.component.InputViewComponent
import ee.oyatl.ime.make.module.keyboardview.Theme
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard

interface InputEngine {
    val listener: Listener
    val keyCharacterMap: KeyCharacterMap
    var shiftKeyHandler: DefaultShiftKeyHandler
    var components: List<InputViewComponent>
    var symbolsInputEngine: InputEngine?
    var alternativeInputEngine: InputEngine?

    fun initView(context: Context): View?
    fun getView(): View?
    fun updateView()
    fun onReset()
    fun onResetComponents()

    fun onKey(code: Int, modifiers: ModifierKeyStateSet)
    fun onDelete()
    fun onTextAroundCursor(before: String, after: String)


    fun getLabels(state: ModifierKeyStateSet): Map<Int, CharSequence>
    fun getIcons(state: ModifierKeyStateSet, theme: Theme): Map<Int, Int>
    fun getMoreKeys(state: ModifierKeyStateSet): Map<Int, Keyboard>

    interface Listener {
        fun onComposingText(text: CharSequence)
        fun onFinishComposing()
        fun onCommitText(text: CharSequence)
        fun onDeleteText(beforeLength: Int, afterLength: Int)
        fun onCandidates(list: List<Candidate>)
        fun onNonPrintingKey(code: Int): Boolean
        fun onEditorAction(code: Int)
        fun onDefaultAction(code: Int)
    }
}