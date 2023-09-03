package ee.oyatl.ime.make.view

import ee.oyatl.ime.make.model.KeyOutput

data class KeyEvent(
    val action: Action,
    val output: KeyOutput,
) {
    enum class Action {
        Press, Release, Repeat,
    }
}