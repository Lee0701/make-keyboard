package ee.oyatl.ime.make.model

sealed interface KeyOutput {
    object None: KeyOutput
    data class Text(
        val text: String,
    ): KeyOutput
    sealed interface Special: KeyOutput {
        data class Delete(
            val beforeLength: Int,
            val afterLength: Int,
        ): Special
        data class Shift(
            val side: Side = Side.Any,
        ): Special {
            enum class Side {
                Left, Right, Any, Both
            }
        }
        object Space: Special
        object Return: Special
        object Symbol: Special
        object Language: Special
    }
}