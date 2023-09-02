package ee.oyatl.ime.make.keyboard

import androidx.compose.ui.geometry.Offset

data class KeyEvent(
    val action: Action,
    val output: KeyOutput,
    val position: Offset,
) {
    enum class Action {
        Press, Release, Repeat,
    }
}