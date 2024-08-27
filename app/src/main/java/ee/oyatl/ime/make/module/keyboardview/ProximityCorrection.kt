package ee.oyatl.ime.make.module.keyboardview

import android.content.Context

class ProximityCorrection {
    private val map = mutableMapOf<String, MutableMap<Char, Int>>()
    private val history: MutableList<Char> = mutableListOf()

    fun onKey(keys: Map<Char, Int>): Char {
        val historyCandidates = map[history.joinToString("")] ?: emptyMap()
        val filteredCandidates = historyCandidates.filter { it.key in keys.keys }
        if(filteredCandidates.isEmpty()) {
            history.clear()
            val result = keys.minBy { it.value }.key
            history += result
            return result
        } else {
            val modelCandidatesMaxFreq = filteredCandidates.values.max()
            val adjacentCandidatesMaxFreq = keys.values.min()
            val modelCandidates = filteredCandidates.mapValues { (_, v) -> v.toFloat() / modelCandidatesMaxFreq }
            val adjacentCandidates = keys.mapValues { (_, v) -> adjacentCandidatesMaxFreq / v.toFloat() }
            val result = modelCandidates.keys.filter { it in adjacentCandidates }
                .map { it to ((modelCandidates[it] ?: 0f) * (adjacentCandidates[it] ?: 0f)) }
                .maxBy { it.second }.first
            history += result
            return result
        }
    }

    fun onReset() {
        history.clear()
    }

    fun loadModel(context: Context) {
        map.clear()
        val reader = context.assets.open("proximity_correction.tsv").bufferedReader()
        reader.forEachLine { line ->
            if('\t' !in line) return@forEachLine
            val (seq, freq) = line.split('\t')
            val key = seq.dropLast(1)
            val value = seq.last()
            if(key !in map) map += key to mutableMapOf(value to freq.toInt())
            else {
                val m = map[key] ?: return@forEachLine
                m += value to freq.toInt()
            }
        }
    }

}