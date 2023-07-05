package ee.oyatl.ime.make

data class ModifierKeyState(
    val pressed: Boolean = false,
    val locked: Boolean = false,
    val pressing: Boolean = pressed,
)