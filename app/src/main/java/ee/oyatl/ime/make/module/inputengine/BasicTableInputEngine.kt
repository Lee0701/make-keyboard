package ee.oyatl.ime.make.module.inputengine

import android.graphics.drawable.Drawable
import ee.oyatl.ime.make.modifiers.DefaultShiftKeyHandler
import ee.oyatl.ime.make.preset.softkeyboard.Keyboard
import ee.oyatl.ime.make.preset.table.CharOverrideTable
import ee.oyatl.ime.make.preset.table.CodeConvertTable
import ee.oyatl.ime.make.preset.table.MoreKeysTable
import ee.oyatl.ime.make.preset.table.SimpleCodeConvertTable
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet

abstract class BasicTableInputEngine(
    override val convertTable: CodeConvertTable,
    override val overrideTable: CharOverrideTable,
    override val moreKeysTable: MoreKeysTable,
    override var shiftKeyHandler: DefaultShiftKeyHandler,
    override val listener: InputEngine.Listener,
): BasicInputEngine(), TableInputEngine {
    override var alternativeInputEngine: InputEngine? = null
    override var symbolsInputEngine: InputEngine? = null

    override fun onKey(code: Int, modifiers: ModifierKeyStateSet) {
        val converted = convertTable.get(code, modifiers)
            ?: keyCharacterMap.get(code, modifiers.asMetaState())
        val override = overrideTable.get(converted) ?: converted
        if(override > 0) listener.onCommitText(override.toChar().toString())
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

    override fun getLabels(state: ModifierKeyStateSet): Map<Int, CharSequence> {
        val codeMap = convertTable.getAllForState(state)
            .mapValues { (_, code) -> overrideTable.get(code) ?: code }
            .mapValues { (_, code) -> code.toChar().toString() }
        return DirectInputEngine.getLabels(keyCharacterMap, state) + codeMap
    }

    override fun getIcons(state: ModifierKeyStateSet): Map<Int, Drawable> {
        return emptyMap()
    }

    override fun getMoreKeys(state: ModifierKeyStateSet): Map<Int, Keyboard> {
        return moreKeysTable.map.mapNotNull { (code, value) ->
            val key = convertTable.getReversed(code, SimpleCodeConvertTable.EntryKey.fromKeyboardState(state))
            if(key == null) null
            else key to value
        }.toMap()
    }
}