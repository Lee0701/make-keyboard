package ee.oyatl.ime.make.modifier

data class ModifierKeyState(
    val pressed: Boolean = false,
    val locked: Boolean = false,
    val pressing: Boolean = pressed,
) {
    val active: Boolean get() = pressed || pressing
}