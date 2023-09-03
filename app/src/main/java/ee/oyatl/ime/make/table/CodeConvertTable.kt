package ee.oyatl.ime.make.table

import ee.oyatl.ime.make.modifier.ModifierKeyStateSet

sealed interface CodeConvertTable {
    fun getAllForState(state: ModifierKeyStateSet): Map<Int, Int>
    fun getReversed(charCode: Int, state: ModifierKeyStateSet): Int?

    operator fun get(keyCode: Int, state: ModifierKeyStateSet): Int?
    operator fun plus(table: CodeConvertTable): CodeConvertTable
}