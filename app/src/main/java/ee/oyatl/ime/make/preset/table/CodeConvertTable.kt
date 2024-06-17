package ee.oyatl.ime.make.preset.table

import ee.oyatl.ime.make.state.KeyboardState
import kotlinx.serialization.Serializable

@Serializable
sealed interface CodeConvertTable {
    fun get(keyCode: Int, state: KeyboardState): Int?
    fun getAllForState(state: KeyboardState): Map<Int, Int>
    fun getReversed(charCode: Int, entryKey: SimpleCodeConvertTable.EntryKey): Int?

    operator fun plus(table: CodeConvertTable): CodeConvertTable
}