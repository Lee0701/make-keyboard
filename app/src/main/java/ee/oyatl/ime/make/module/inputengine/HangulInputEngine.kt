package ee.oyatl.ime.make.module.inputengine

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import ee.oyatl.ime.make.module.component.InputViewComponent
import ee.oyatl.ime.make.charset.Hangul
import ee.oyatl.ime.make.module.kokr.HangulCombiner
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.table.JamoCombinationTable
import ee.oyatl.ime.make.preset.table.LayeredCodeConvertTable
import ee.oyatl.ime.make.preset.table.LayeredCodeConvertTable.Companion.BASE_LAYER_NAME
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet
import ee.oyatl.ime.make.preset.table.CharOverrideTable
import ee.oyatl.ime.make.preset.table.CodeConvertTable
import ee.oyatl.ime.make.preset.table.MoreKeysTable

data class HangulInputEngine(
    override val convertTable: CodeConvertTable,
    override val overrideTable: CharOverrideTable,
    override val moreKeysTable: MoreKeysTable,
    private val jamoCombinationTable: JamoCombinationTable,
    private val correctOrders: Boolean,
    override val listener: InputEngine.Listener
): BasicTableInputEngine(convertTable, overrideTable, moreKeysTable, listener) {
    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    private val hangulCombiner = HangulCombiner(jamoCombinationTable, correctOrders)
    private val stateStack: MutableList<HangulCombiner.State> = mutableListOf()
    private val hangulState: HangulCombiner.State get() = stateStack.lastOrNull() ?: HangulCombiner.State()
    private val layerIdByHangulState: String get() {
        val cho = hangulState.cho
        val jung = hangulState.jung
        val jong = hangulState.jong

        return if(jong != null && jong and 0xff00000 == 0) "\$jong"
        else if(jung != null && jung and 0xff00000 == 0) "\$jung"
        else if(cho != null && cho and 0xff00000 == 0) "\$cho"
        else "base"
    }

    override fun onKey(code: Int, state: ModifierKeyStateSet) {
        val converted =
            if(convertTable is LayeredCodeConvertTable) convertTable.get(layerIdByHangulState, code, state)
            else convertTable.get(code, state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            if(char > 0) listener.onCommitText(char.toChar().toString())
        } else {
            val override = overrideTable.get(converted)
            val (text, hangulStates) = hangulCombiner.combine(hangulState, override ?: converted)
            if(text.isNotEmpty()) clearStack()
            this.stateStack += hangulStates
            if(text.isNotEmpty()) listener.onCommitText(text)
            listener.onComposingText(hangulStates.lastOrNull()?.composed ?: "")
        }
    }

    override fun onDelete() {
        if(stateStack.size >= 1) {
            stateStack.removeLastOrNull()
            listener.onComposingText(stateStack.lastOrNull()?.composed ?: "")
        }
        else listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
        clearStack()
    }

    fun clearStack() {
        stateStack.clear()
    }

    override fun getLabels(state: ModifierKeyStateSet): Map<Int, CharSequence> {
        val table =
            if(convertTable is LayeredCodeConvertTable)
                convertTable.get(layerIdByHangulState) ?: convertTable.get(BASE_LAYER_NAME)
            else convertTable
        val codeMap = table?.getAllForState(state).orEmpty()
            .mapValues { (_, code) -> overrideTable.get(code) ?: code }
            .mapValues { (_, output) ->
                val ch = output and 0xffffff
                if(Hangul.isModernJamo(ch)) {
                    if(Hangul.isCho(ch)) Hangul.choToCompatConsonant(ch.toChar()).toString()
                    else if(Hangul.isJung(ch)) Hangul.jungToCompatVowel(ch.toChar()).toString()
                    else if(Hangul.isJong(ch)) Hangul.jongToCompatConsonant(ch.toChar()).toString()
                    else ch.toChar().toString()
                } else ch.toChar().toString()
            }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: ModifierKeyStateSet): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: ModifierKeyStateSet): Map<Int, Keyboard> {
        return mapOf()
    }
}