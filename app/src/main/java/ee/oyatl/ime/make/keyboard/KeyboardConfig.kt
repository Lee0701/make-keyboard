package ee.oyatl.ime.make.keyboard

import androidx.compose.runtime.Composable

data class KeyboardConfig(
    val rows: List<RowConfig>,
    val bottomRow: BottomRowConfig,
) {
    fun mapTextLabels(transform: (String) -> String): KeyboardConfig = this.mapLabels {
        if(it is KeyLabel.Text) KeyLabel.Text(transform(it.text))
        else it
    }

    fun mapLabels(transform: (KeyLabel) -> KeyLabel): KeyboardConfig = this.map {
        it.copy(label = transform(it.label))
    }

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

data class BottomRowConfig(
    val height: Int = 50,
    val spaceWidth: Float = 4f,
    val leftKeys: List<KeyConfig> = listOf(),
    val rightKeys: List<KeyConfig> = listOf(),
)

data class KeyConfig(
    val output: String,
    val label: KeyLabel,
    val height: Int = 55,
    val width: Float = 1f,
    val type: Type = Type.Alphanumeric,
) {
    enum class Type {
        Alphanumeric,
        Modifier,
        Symbol,
        Return,
        Space,
    }
}

sealed interface KeyLabel {
    object None: KeyLabel
    data class Icon(
        val icon: @Composable () -> Unit,
    ): KeyLabel
    data class Text(
        val text: String,
    ): KeyLabel {
        fun uppercase(): Text = Text(text.uppercase())
        fun lowercase(): Text = Text(text.lowercase())
    }
}

fun String.toRowConfig(
    spacingLeft: Float = 0f,
    spacingRight: Float = 0f,
): RowConfig {
    val keys = this.map { KeyConfig(it.toString(), KeyLabel.Text(it.toString())) }
    return RowConfig(
        keys = keys,
        spacingLeft = spacingLeft,
        spacingRight = spacingRight,
    )
}

typealias KeyOutput = String
val KeyOutput.commandOutput: String? get() =
    if(this.startsWith("<<") && this.endsWith(">>"))
        this.uppercase().substring(2, this.length - 2)
    else null
