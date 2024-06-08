package ee.oyatl.ime.make.preset.table

import ee.oyatl.ime.make.preset.serialization.HexIntSerializer
import kotlinx.serialization.Serializable

@Serializable
data class JamoCombinationTable(
    val list: List<List<@Serializable(with = HexIntSerializer::class) Int>> = listOf(),
) {
    val map: Map<Pair<Int, Int>, Int> = list.associate { (a, b, result) -> (a to b) to result }

    operator fun plus(another: JamoCombinationTable): JamoCombinationTable {
        return JamoCombinationTable(this.list + another.list)
    }
}