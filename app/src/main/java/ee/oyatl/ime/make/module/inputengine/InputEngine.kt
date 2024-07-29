package ee.oyatl.ime.make.module.inputengine

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import android.view.View
import ee.oyatl.ime.make.module.candidates.Candidate
import ee.oyatl.ime.make.module.component.InputViewComponent
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet

interface InputEngine {
    val listener: Listener
    val keyCharacterMap: KeyCharacterMap
    var components: List<InputViewComponent>
    var symbolsInputEngine: InputEngine?
    var alternativeInputEngine: InputEngine?

    fun initView(context: Context): View?
    fun onReset()
    fun onResetComponents()

    fun onKey(code: Int, state: ModifierKeyStateSet)
    fun onDelete()
    fun onTextAroundCursor(before: String, after: String)


    fun getLabels(state: ModifierKeyStateSet): Map<Int, CharSequence>
    fun getIcons(state: ModifierKeyStateSet): Map<Int, Drawable>
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