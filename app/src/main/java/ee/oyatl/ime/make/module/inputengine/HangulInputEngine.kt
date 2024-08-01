package ee.oyatl.ime.make.module.inputengine

import android.graphics.drawable.Drawable
import ee.oyatl.ime.make.charset.Hangul
import ee.oyatl.ime.make.module.kokr.HangulCombiner
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.table.JamoCombinationTable
import ee.oyatl.ime.make.preset.table.LayeredCodeConvertTable
import ee.oyatl.ime.make.preset.table.LayeredCodeConvertTable.Companion.BASE_LAYER_NAME
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet

data class HangulInputEngine(
    private val engine: TableInputEngine,
    private val jamoCombinationTable: JamoCombinationTable,
    private val correctOrders: Boolean,
    override val listener: InputEngine.Listener
): BasicTableInputEngine(engine.convertTable, engine.overrideTable, engine.moreKeysTable, engine.shiftKeyHandler, listener) {
    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    private val hangulCombiner = HangulCombiner(jamoCombinationTable, correctOrders)
    private var combinerState: HangulCombiner.State = HangulCombiner.State()
    // TODO: Use combiner status code instead of this calculation
    private val layerIdByHangulState: String get() {
        val syllable = combinerState.syllable
        val cho = syllable.cho?.extra
        val jung = syllable.jung?.extra
        val jong = syllable.jong?.extra

        return if(jong != null && jong == 0) "\$jong"
        else if(jung != null && jung == 0) "\$jung"
        else if(cho != null && cho == 0) "\$cho"
        else "base"
    }

    override fun onKey(code: Int, state: ModifierKeyStateSet) {
        val converted =
            if(convertTable is LayeredCodeConvertTable) convertTable.get(layerIdByHangulState, code, state)
            else convertTable.get(code, state)
        if(converted == null) {
            val char = keyCharacterMap.get(code, state.asMetaState())
            if(char > 0) {
                onReset()
                listener.onCommitText(char.toChar().toString())
            }
        } else {
            val override = overrideTable.get(converted)
            val result = hangulCombiner.combine(combinerState, override ?: converted)
            this.combinerState = result.state
            if(result.text.isNotEmpty()) listener.onCommitText(result.text)
            listener.onComposingText(result.state.syllable.composed)
        }
    }

    override fun onDelete() {
        val previous = combinerState.previous
        if(previous != null) {
            combinerState = previous
            listener.onComposingText(previous.syllable.composed)
        }
        else listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
        combinerState = HangulCombiner.State()
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