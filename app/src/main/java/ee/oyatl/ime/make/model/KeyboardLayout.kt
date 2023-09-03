package ee.oyatl.ime.make.model

data class KeyboardLayout(
    val rows: List<Row> = listOf(),
    val height: Float = 1f,
) {
    constructor(vararg rows: Row): this(rows.toList())
    operator fun plus(another: KeyboardLayout): KeyboardLayout {
        return KeyboardLayout(
            rows = this.rows + another.rows,
            height = (this.height + another.height) / 2f,
        )
    }
}