package ee.oyatl.ime.make.module.hangul

import ee.oyatl.ime.make.preset.table.JamoCombinationTable

class DefaultHangulCombiner(
    private val jamoCombinationTable: JamoCombinationTable,
    private val correctOrders: Boolean
) {
    fun combine(state: State, input: Int): Pair<CharSequence, State> {
        // The unicode codepoint of input, without any extended parts
        val inputCodepoint = input and 0x1fffff
        var newState: State? = state
        var composed = ""
        if(Hangul.isCho(inputCodepoint)) {
            if(state.cho != null) {
                val combination = jamoCombinationTable.map[state.cho to input]
                if(combination != null) {
                    if(state.last != null && !Hangul.isCho(state.last)) {
                        composed += state.composed
                        newState = State(cho = input, previous = newState)
                    } else {
                        newState = state.copy(cho = combination, previous = newState)
                    }
                } else {
                    composed += state.composed
                    newState = State(cho = input, previous = newState)
                }
            } else if(correctOrders) {
                newState = state.copy(cho = input, previous = newState)
            } else {
                composed += state.composed
                newState = State(cho = input, previous = newState)
            }
        } else if(Hangul.isJung(inputCodepoint)) {
            if(state.jung != null) {
                val combination = jamoCombinationTable.map[state.jung to input]
                if(combination != null) newState = state.copy(jung = combination, previous = newState)
                else {
                    composed += state.composed
                    newState = State(jung = input, previous = newState)
                }
            } else if(correctOrders || state.last == null || Hangul.isCho(state.last)) {
                newState = state.copy(jung = input, previous = newState)
            } else {
                composed += state.composed
                newState = State(jung = input, previous = newState)
            }
        } else if(Hangul.isJong(inputCodepoint)) {
            val newStateJong = state.jong
            if(newStateJong != null) {
                val combination = jamoCombinationTable.map[newStateJong to input]
                if(combination != null) newState = state.copy(
                    jong = combination,
                    jongCombination = newStateJong to input,
                    previous = newState
                )
                else {
                    composed += state.composed
                    newState = State(jong = input, previous = newState)
                }
            } else if(correctOrders || state.last == null || Hangul.isJung(state.last) && state.cho != null) {
                newState = state.copy(jong = input, previous = newState)
            } else if(state.cho == null || state.jung == null) {
                composed += state.composed
                newState = State(jong = input, previous = newState)
            } else {
                composed += state.composed
                newState = State(jong = input, previous = newState)
            }
        } else if(Hangul.isConsonant(inputCodepoint)) {
            val cho = Hangul.consonantToCho(input and 0xffff)
            val jong = Hangul.consonantToJong(input and 0xffff)
            if(state.cho != null && state.jung != null) {
                if(state.jong != null) {
                    val combination = jamoCombinationTable.map[state.jong to jong]
                    if(combination != null) newState = state.copy(
                        jong = combination,
                        jongCombination = state.jong to jong,
                        previous = newState
                    )
                    else {
                        composed += state.composed
                        newState = State(cho = cho, previous = newState)
                    }
                } else if(jong != 0) {
                    newState = state.copy(jong = jong, previous = newState)
                } else {
                    composed += state.composed
                    newState = State(cho = cho, previous = newState)
                }
            } else if(state.cho != null) {
                if(state.last != null && !Hangul.isConsonant(state.last)) {
                    composed += state.composed
                    newState = State(cho = cho, previous = newState)
                } else {
                    val combination = jamoCombinationTable.map[state.cho to cho]
                    if(combination != null) newState = state.copy(cho = combination, previous = newState)
                    else {
                        composed += state.composed
                        newState = State(cho = cho, previous = newState)
                    }
                }
            } else if(correctOrders) {
                newState = state.copy(cho = cho, previous = newState)
            } else {
                composed += state.composed
                newState = State(cho = cho, previous = newState)
            }
        } else if(Hangul.isVowel(inputCodepoint)) {
            val jung = Hangul.vowelToJung(input and 0xffff)
            val newStateJong = state.jong
            val jongCombination = state.jongCombination
            if(newStateJong != null) {
                if(jongCombination != null) {
                    val promotedCho = Hangul.ghostLight(jongCombination.second)
                    composed += state.copy(jong = jongCombination.first).composed
                    newState = State(cho = promotedCho, previous = newState)
                    newState = State(cho = promotedCho, jung = jung, previous = newState)
                } else {
                    val promotedCho = Hangul.ghostLight(newStateJong)
                    composed += state.copy(jong = null).composed
                    newState = State(cho = promotedCho, previous = newState)
                    newState = State(cho = promotedCho, jung = jung, previous = newState)
                }
            } else if(state.jung != null) {
                val combination = jamoCombinationTable.map[state.jung to jung]
                if(combination != null) newState = state.copy(jung = combination, previous = newState)
                else {
                    composed += state.composed
                    newState = State(jung = jung, previous = newState)
                }
            } else {
                newState = state.copy(jung = jung, previous = newState)
            }
        } else {
            composed += state.composed
            composed += input.toChar()
            newState = null
        }
        if(newState == null) newState = State()
        return composed to newState
    }

    data class State(
        val cho: Int? = null,
        val jung: Int? = null,
        val jong: Int? = null,
        val last: Int? = null,
        val jongCombination: Pair<Int, Int>? = null,
        private val previous: State? = null,
    ) {
        val choChar: Char? = cho?.and(0xffff)?.toChar()
        val jungChar: Char? = jung?.and(0xffff)?.toChar()
        val jongChar: Char? = jong?.and(0xffff)?.toChar()

        private val ordinalCho: Int? = cho?.and(0xffff)?.minus(0x1100)
        private val ordinalJung: Int? = jung?.and(0xffff)?.minus(0x1161)
        private val ordinalJong: Int? = jong?.and(0xffff)?.minus(0x11a7)

        val nfc: Char? =
            if(ordinalCho != null && ordinalJung != null && listOfNotNull(cho, jung, jong).all {
                    Hangul.isModernJamo(
                        it.and(0xffff)
                    )
                })
                Hangul.combineNFC(ordinalCho, ordinalJung, ordinalJong)
            else null
        val nfd: CharSequence =
            Hangul.combineNFD(choChar, jungChar, jongChar)

        val composed: CharSequence =
            if(cho == null && jung == null && jong == null) ""
            else if(listOfNotNull(cho, jung, jong).let { it.size == 1 && it.all { c ->
                    Hangul.isModernJamo(
                        c and 0xffff
                    )
                } })
                (choChar?.let { Hangul.choToCompatConsonant(it) } ?:
                jungChar?.let { Hangul.jungToCompatVowel(it) } ?:
                jongChar?.let { Hangul.jongToCompatConsonant(it) })?.toString().orEmpty()
            else
                nfc?.toString() ?: nfd
    }
}