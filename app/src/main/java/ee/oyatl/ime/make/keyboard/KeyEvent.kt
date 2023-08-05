package ee.oyatl.ime.make.keyboard

import ee.oyatl.ime.make.modifier.ModifierKeyState

data class KeyEvent(
    val action: Action,
    val output: String,
) {
    enum class Action {
        Press, Release, Repeat,
    }
}