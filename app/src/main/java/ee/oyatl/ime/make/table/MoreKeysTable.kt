package ee.oyatl.ime.make.table

import ee.oyatl.ime.make.model.KeyboardLayout

data class MoreKeysTable(
    val map: Map<Int, KeyboardLayout> = mapOf(),
) {
    constructor(vararg entries: Pair<Int, KeyboardLayout>): this(entries.toMap())

    operator fun plus(another: MoreKeysTable): MoreKeysTable {
        val keys = this.map.keys + another.map.keys
        val values = keys.associateWith { k ->
            listOfNotNull(this.map[k], another.map[k]).reduce { a, c -> a + c } }
        return MoreKeysTable(values)
    }

    operator fun times(another: MoreKeysTable): MoreKeysTable {
        val keys = this.map.keys + another.map.keys
        val values = keys.associateWith { k ->
            KeyboardLayout(listOfNotNull(this.map[k]?.rows, another.map[k]?.rows)
                .flatten()
                .reduce { a, c -> a + c }
            ) }
        return MoreKeysTable(values)
    }

    operator fun get(keyCode: Int): KeyboardLayout? {
        return map[keyCode]
    }
}