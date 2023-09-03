package ee.oyatl.ime.make.table

import ee.oyatl.ime.make.modifier.ModifierKeyStateSet

class LayeredCodeConvertTable(
    val layers: Map<String, CodeConvertTable>,
): CodeConvertTable {

    fun get(layerId: String): CodeConvertTable? {
        return layers[layerId]
    }

    fun get(layerId: String, keyCode: Int, state: ModifierKeyStateSet): Int? {
        return get(layerId)?.get(keyCode, state) ?: get(BASE_LAYER_NAME)?.get(keyCode, state)
    }

    fun getAllForState(layerId: String, state: ModifierKeyStateSet): Map<Int, Int> {
        return layers[layerId]?.getAllForState(state)?: mapOf()
    }

    override fun getAllForState(state: ModifierKeyStateSet): Map<Int, Int> {
        return getAllForState(BASE_LAYER_NAME, state)
    }

    override fun getReversed(charCode: Int, state: ModifierKeyStateSet): Int? {
        return get(BASE_LAYER_NAME)?.getReversed(charCode, state)
    }

    fun getReversed(layerId: String, charCode: Int, state: ModifierKeyStateSet): Int? {
        return get(layerId)?.getReversed(charCode, state)
    }

    override operator fun get(keyCode: Int, state: ModifierKeyStateSet): Int? {
        return get(BASE_LAYER_NAME)?.get(keyCode, state)
    }

    override operator fun plus(table: CodeConvertTable): CodeConvertTable {
        return when(table) {
            is SimpleCodeConvertTable -> this + table
            is LayeredCodeConvertTable -> this + table
        }
    }

    operator fun plus(table: LayeredCodeConvertTable): LayeredCodeConvertTable {
        val keys = layers.keys + table.layers.keys
        return LayeredCodeConvertTable(keys.map { key ->
            val a = table.layers[key]
            val b = this.layers[key]
            if(a != null && b != null) key to LayeredCodeConvertTable(mapOf(key to (a + b)))
            else null
        }.filterNotNull().toMap())
    }

    operator fun plus(table: SimpleCodeConvertTable): LayeredCodeConvertTable {
        return LayeredCodeConvertTable(this.layers.mapValues { (_, layer) -> layer + table })
    }

    companion object {
        const val BASE_LAYER_NAME = "base"
    }
}
