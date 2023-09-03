package ee.oyatl.ime.make.model

data class Row(
    val keys: List<RowItem> = listOf(),
) {
    constructor(vararg keys: RowItem): this(keys.toList())

    operator fun plus(another: Row): Row {
        return Row(
            keys = this.keys + another.keys,
        )
    }

    companion object {
        fun ofOutputs(outputs: String): Row {
            return Row(outputs.map { Key(output = it.toString()) })
        }
    }
}