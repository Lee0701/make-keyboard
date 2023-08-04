package ee.oyatl.ime.make.keyboard

data class KeyEvent(
    val action: Action,
    val output: String,
) {
    enum class Action {
        Press, Release, Repeat,
    }
}