package ee.oyatl.ime.make.keyboard

data class KeyEvent(
    val action: Action,
    val output: KeyOutput,
) {
    enum class Action {
        Press, Release, Repeat,
    }
}