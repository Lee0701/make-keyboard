package ee.oyatl.ime.make

data class KeyboardConfig(
    val rows: List<RowConfig>,
    val bottomLeft: List<KeyConfig> = listOf(),
    val bottomRight: List<KeyConfig> = listOf(),
) {
    constructor(vararg rows: RowConfig): this(rows.toList())

    fun map(transform: (KeyConfig) -> KeyConfig): KeyboardConfig = this.copy(
        rows = this.rows.map { row -> row.copy(keys = row.keys.map(transform)) })
}

data class RowConfig(
    val keys: List<KeyConfig>,
    val spacingLeft: Float = 0f,
    val spacingRight: Float = 0f,
) {
    constructor(vararg keys: KeyConfig): this(keys.toList())
    operator fun plus(another: RowConfig): RowConfig {
        return RowConfig(
            keys = this.keys + another.keys,
            spacingLeft = this.spacingLeft,
            spacingRight = another.spacingRight,
        )
    }
    operator fun plus(key: KeyConfig): RowConfig {
        return this.copy(
            keys = this.keys + key,
        )
    }
}

data class KeyConfig(
    val output: String,
    val label: String,
    val height: Int = 60,
    val width: Float = 1f,
    val type: Type = Type.Alphanumeric,
) {
    enum class Type {
        Alphanumeric,
        Modifier,
        Space,
    }
}

fun String.toRowConfig(
    spacingLeft: Float = 0f,
    spacingRight: Float = 0f,
): RowConfig {
    val keys = this.map { KeyConfig(it.toString(), it.toString()) }
    return RowConfig(
        keys = keys,
        spacingLeft = spacingLeft,
        spacingRight = spacingRight,
    )
}
