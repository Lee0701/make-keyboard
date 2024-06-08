package ee.oyatl.ime.make.service

data class ModifierState(
    val pressed: Boolean = false,
    val locked: Boolean = false,
    val pressing: Boolean = pressed,
)