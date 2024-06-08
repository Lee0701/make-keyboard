package ee.oyatl.ime.make.module.candidates

data class DefaultCandidate(
    override val text: String,
    override val score: Float = 0f,
): Candidate