package ee.oyatl.ime.make.model

sealed interface RowItem {
    val width: Float
}

data class Spacer(
    override val width: Float = 1f,
): RowItem

data class Key(
    val code: Int = 0,
    val output: String? = null,
    val label: String? = output,
    val backgroundType: KeyBackgroundType? = null,
    val iconType: KeyIconType? = null,
    override val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: KeyType = KeyType.Alphanumeric,
): RowItem

data class Include(
    val name: String,
): RowItem {
    override val width: Float = 0f
}