package ee.oyatl.ime.make.module.hangul

import ee.oyatl.ime.make.preset.table.JamoCombinationTable

class DefaultHangulCombiner(
    private val jamoCombinationTable: JamoCombinationTable,
    private val correctOrders: Boolean
) {
    fun combine(state: State, input: Int): Pair<CharSequence, State> {
        // The unicode codepoint of input, without any extended parts
        val inputCodepoint = input and 0x1fffff
        var newState: State = state
        var composed = ""
        if(Hangul.isCho(inputCodepoint)) {
            if(newState.cho != null) {
                val combination = jamoCombinationTable.map[newState.cho to input]
                if(combination != null) {
                    val newStateLast = newState.last
                    if(newStateLast != null && !Hangul.isCho(newStateLast)) {
                        composed += newState.composed
                        newState = State(cho = input)
                    } else {
                        newState = newState.copy(cho = combination, previous = newState)
                    }
                } else {
                    composed += newState.composed
                    newState = State(cho = input)
                }
            } else if(correctOrders) {
                newState = newState.copy(cho = input, previous = newState)
            } else {
                composed += newState.composed
                newState = State(cho = input)
            }
        } else if(Hangul.isJung(inputCodepoint)) {
            val newStateLast = newState.last
            if(newState.jung != null) {
                val combination = jamoCombinationTable.map[newState.jung to input]
                if(combination != null) newState = newState.copy(jung = combination, previous = newState)
                else {
                    composed += newState.composed
                    newState = State(jung = input)
                }
            } else if(correctOrders || newStateLast == null || Hangul.isCho(newStateLast)) {
                newState = newState.copy(jung = input, previous = newState)
            } else {
                composed += newState.composed
                newState = State(jung = input)
            }
        } else if(Hangul.isJong(inputCodepoint)) {
            val newStateLast = newState.last
            val newStateJong = newState.jong
            if(newStateJong != null) {
                val combination = jamoCombinationTable.map[newStateJong to input]
                if(combination != null) newState = newState.copy(
                    jong = combination,
                    jongCombination = newStateJong to input,
                    previous = newState
                )
                else {
                    composed += newState.composed
                    newState = State(jong = input)
                }
            } else if(correctOrders || newStateLast == null || Hangul.isJung(newStateLast) && newState.cho != null) {
                newState = newState.copy(jong = input, previous = newState)
            } else {
                composed += newState.composed
                newState = State(jong = input)
            }
        } else if(Hangul.isConsonant(inputCodepoint)) {
            val cho = Hangul.consonantToCho(input and 0xffff)
            val jong = Hangul.consonantToJong(input and 0xffff)
            val newStateJong = newState.jong
            if(newState.cho != null && newState.jung != null) {
                if(newStateJong != null) {
                    val combination = jamoCombinationTable.map[newStateJong to jong]
                    if(combination != null) newState = newState.copy(
                        jong = combination,
                        jongCombination = newStateJong to jong,
                        previous = newState
                    )
                    else {
                        composed += newState.composed
                        newState = State(cho = cho)
                    }
                } else if(jong != 0) {
                    newState = newState.copy(jong = jong, previous = newState)
                } else {
                    composed += newState.composed
                    newState = State(cho = cho)
                }
            } else if(newState.cho != null) {
                val newStateLast = newState.last
                if(newStateLast != null && !Hangul.isConsonant(newStateLast)) {
                    composed += newState.composed
                    newState = State(cho = cho, previous = newState)
                } else {
                    val combination = jamoCombinationTable.map[newState.cho to cho]
                    if(combination != null) newState = newState.copy(cho = combination, previous = newState)
                    else {
                        composed += newState.composed
                        newState = State(cho = cho)
                    }
                }
            } else if(correctOrders) {
                newState = newState.copy(cho = cho, previous = newState)
            } else {
                composed += newState.composed
                newState = State(cho = cho)
            }
        } else if(Hangul.isVowel(inputCodepoint)) {
            val jung = Hangul.vowelToJung(input and 0xffff)
            val newStateJong = newState.jong
            val jongCombination = newState.jongCombination
            if(newStateJong != null) {
                if(jongCombination != null) {
                    val promotedCho = Hangul.ghostLight(jongCombination.second)
                    composed += newState.copy(jong = jongCombination.first).composed
                    newState = State(cho = promotedCho)
                    newState = State(cho = promotedCho, jung = jung, previous = newState)
                } else {
                    val promotedCho = Hangul.ghostLight(newStateJong)
                    composed += newState.copy(jong = null).composed
                    newState = State(cho = promotedCho)
                    newState = State(cho = promotedCho, jung = jung, previous = newState)
                }
            } else if(newState.jung != null) {
                val combination = jamoCombinationTable.map[newState.jung to jung]
                if(combination != null) newState = newState.copy(jung = combination, previous = newState)
                else {
                    composed += newState.composed
                    newState = State(jung = jung)
                }
            } else {
                newState = newState.copy(jung = jung, previous = newState)
            }
        } else {
            composed += newState.composed
            composed += input.toChar()
            newState = State.INITIAL
        }
        return composed to newState.copy(last = input)
    }

    data class State(
        val cho: Int? = null,
        val jung: Int? = null,
        val jong: Int? = null,
        val last: Int? = null,
        val jongCombination: Pair<Int, Int>? = null,
        val previous: State? = INITIAL,
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

        companion object {
            val INITIAL = State()
        }
    }
}