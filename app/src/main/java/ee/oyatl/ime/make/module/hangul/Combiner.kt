package ee.oyatl.ime.make.module.hangul

interface Combiner {

    fun combine(state: State, input: Int): Result

    interface State {
        val previous: State?
        val combined: CharSequence
    }

    data class Result(
        val textToCommit: CharSequence,
        val newState: State
    )
}