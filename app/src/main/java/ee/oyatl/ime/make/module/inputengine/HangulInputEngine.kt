package ee.oyatl.ime.make.module.inputengine

import android.graphics.drawable.Drawable
import ee.oyatl.ime.make.module.combiner.Hangul
import ee.oyatl.ime.make.module.combiner.HangulCombiner
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.table.JamoCombinationTable
import ee.oyatl.ime.make.preset.table.LayeredCodeConvertTable
import ee.oyatl.ime.make.preset.table.LayeredCodeConvertTable.Companion.BASE_LAYER_NAME
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet
import ee.oyatl.ime.make.module.combiner.Combiner

data class HangulInputEngine(
    private val engine: TableInputEngine,
    private val jamoCombinationTable: JamoCombinationTable,
    private val correctOrders: Boolean,
    override val listener: InputEngine.Listener
): BasicTableInputEngine(
    engine.convertTable,
    engine.overrideTable,
    engine.moreKeysTable,
    engine.shiftKeyHandler,
    listener
) {
    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    private val hangulCombiner = HangulCombiner(jamoCombinationTable, correctOrders)
    private var state: Combiner.State = HangulCombiner.State.Initial
    private val layerIdByHangulState: String get() {
        val state = state
        if(state is HangulCombiner.State) {
            val cho = state.cho
            val jung = state.jung
            val jong = state.jong

            if(jong != null && jong and 0xff00000 == 0) return "\$jong"
            else if(jung != null && jung and 0xff00000 == 0) return "\$jung"
            else if(cho != null && cho and 0xff00000 == 0) return "\$cho"
        }
        return "base"
    }

    override fun onKey(code: Int, modifiers: ModifierKeyStateSet) {
        val converted =
            if(convertTable is LayeredCodeConvertTable) convertTable.get(layerIdByHangulState, code, modifiers)
            else convertTable.get(code, modifiers)
        if(converted == null) {
            val char = keyCharacterMap.get(code, modifiers.asMetaState())
            if(char > 0) {
                onReset()
                listener.onCommitText(char.toChar().toString())
            }
        } else {
            val override = overrideTable.get(converted) ?: converted
            val (text, newState) = hangulCombiner.combine(this.state, override)
            if(text.isNotEmpty()) {
                this.state = HangulCombiner.State.Initial
                listener.onCommitText(text)
            }
            if(newState is HangulCombiner.State) {
                this.state = newState
                listener.onComposingText(newState.combined)
            }
        }
    }

    override fun onDelete() {
        val previous = state.previous
        if(previous != null) {
            state = previous
            listener.onComposingText(previous.combined)
        }
        else listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
        state = HangulCombiner.State.Initial
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