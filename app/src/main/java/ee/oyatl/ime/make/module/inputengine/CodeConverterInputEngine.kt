package ee.oyatl.ime.make.module.inputengine

import android.graphics.drawable.Drawable
import android.view.KeyCharacterMap
import ee.oyatl.ime.make.module.component.InputViewComponent
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.table.CharOverrideTable
import ee.oyatl.ime.make.preset.table.CodeConvertTable
import ee.oyatl.ime.make.preset.table.MoreKeysTable
import ee.oyatl.ime.make.preset.table.SimpleCodeConvertTable
import ee.oyatl.ime.make.state.KeyboardState

class CodeConverterInputEngine(
    private val convertTable: CodeConvertTable,
    private val overrideTable: CharOverrideTable,
    private val moreKeysTable: MoreKeysTable,
    override val listener: InputEngine.Listener,
): InputEngine {
    override var components: List<InputViewComponent> = listOf()

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)

    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    override fun onKey(code: Int, state: KeyboardState) {
        val converted = convertTable.get(code, state) ?: keyCharacterMap.get(code, state.asMetaState())
        val override = overrideTable.get(converted) ?: converted
        listener.onCommitText(override.toChar().toString())
    }

    override fun onDelete() {
        listener.onDeleteText(1, 0)
    }

    override fun onTextAroundCursor(before: String, after: String) {
    }

    override fun onReset() {
        listener.onFinishComposing()
        listener.onCandidates(listOf())
    }

    override fun getLabels(state: KeyboardState): Map<Int, CharSequence> {
        val codeMap = convertTable.getAllForState(state)
            .mapValues { (_, code) -> overrideTable.get(code) ?: code }
            .mapValues { (_, code) -> code.toChar().toString() }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: KeyboardState): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: KeyboardState): Map<Int, Keyboard> {
        return moreKeysTable.map.mapNotNull { (code, value) ->
            val key = convertTable.getReversed(code, SimpleCodeConvertTable.EntryKey.fromKeyboardState(state))
            if(key == null) null
            else key to value
        }.toMap()
    }
}