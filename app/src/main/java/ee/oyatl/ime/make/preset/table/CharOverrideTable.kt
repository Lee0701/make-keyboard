package ee.oyatl.ime.make.preset.table

import ee.oyatl.ime.make.preset.serialization.HexIntSerializer
import ee.oyatl.ime.make.state.KeyboardState
import kotlinx.serialization.Serializable

@Serializable
class CharOverrideTable(
    @Serializable val map: Map<
            @Serializable(with = HexIntSerializer::class) Int,
            @Serializable(with = HexIntSerializer::class) Int> = mapOf(),
) {

    private val reversedMap: Map<Int, Int> = map.map { (key, value) ->
        value to key
    }.toMap()

    fun get(charCode: Int): Int? {
        return map[charCode]
    }

    fun getAllForState(state: KeyboardState): Map<Int, Int> {
        return map
    }

    fun getReversed(charCode: Int): Int? {
        return reversedMap[charCode]
    }

    operator fun plus(table: CharOverrideTable): CharOverrideTable {
        return CharOverrideTable(map = this.map + table.map)
    }
}