package ee.oyatl.ime.make.module.kokr

import ee.oyatl.ime.make.charset.Hangul
import ee.oyatl.ime.make.preset.table.JamoCombinationTable

class HangulCombiner(
    private val jamoCombinationTable: JamoCombinationTable,
    private val correctOrders: Boolean
) {
    private val ordered = object: Automata {
        override fun test(current: Int, choCode: Int?, jungCode: Int?, jongCode: Int?): Int {
            val (cho, jung, jong) = listOf(choCode, jungCode, jongCode).map { it != null }
            return when(current) {
                0 -> if(cho) 1 else if(jung) 2 else if(jong) 3 else 0
                1 -> if(cho) 1 else if(jung) 2 else if(jong) 3 else 0
                2 -> if(jung) 2 else if(jong) 3 else 0
                3 -> if(jong) 3 else 0
                else -> 0
            }
        }
    }

    fun combine(current: State, input: Int): CombinedHangul {
        val inputChar = input and 0xffff
        val inputExtra = input and 0xffff.inv()
        val cho = if(Hangul.isCho(inputChar)) Hangul.HangulJamo.Choseong(inputChar - 0x1100, inputExtra) else null
        val jung = if(Hangul.isJung(inputChar)) Hangul.HangulJamo.Jungseong(inputChar - 0x1161, inputExtra) else null
        val jong = if(Hangul.isJong(inputChar)) Hangul.HangulJamo.Jongseong(inputChar - 0x11a8, inputExtra) else null
        if(cho == null && jung == null && jong == null) {
            val text = current.syllable.composed.toString() + inputChar.toChar().toString()
            return CombinedHangul(text, State())
        }
        val next = ordered.test(current.statusCode, cho?.ordinal, jung?.ordinal, jong?.ordinal)
        return if(next == 0) {
            val state = State(input, State(), next, Hangul.HangulSyllable(cho, jung, jong))
            CombinedHangul(current.syllable.composed, state)
        } else {
            val syllable = current.syllable + Hangul.HangulSyllable(cho, jung, jong)
            val state = State(input, current, next, syllable)
            CombinedHangul("", state)
        }
    }

    data class State(
        val input: Int? = null,
        val previous: State? = null,
        val statusCode: Int = 0,
        val syllable: Hangul.HangulSyllable = Hangul.HangulSyllable()
    )

    data class CombinedHangul(
        val text: CharSequence,
        val state: State
    )

    interface Automata {
        fun test(current: Int, choCode: Int?, jungCode: Int?, jongCode: Int?): Int
    }
}