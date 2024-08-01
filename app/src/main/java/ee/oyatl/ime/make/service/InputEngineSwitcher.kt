package ee.oyatl.ime.make.service

import android.content.Context
import android.view.View
import ee.oyatl.ime.make.module.candidates.Candidate
import ee.oyatl.ime.make.module.component.CandidatesComponent
import ee.oyatl.ime.make.module.inputengine.InputEngine

class InputEngineSwitcher(
    val engines: List<InputEngine>,
    val table: Array<IntArray>,
) {
    private var _languageIndex = 0
    private var _extraIndex = 0

    val languageIndex: Int get() = _languageIndex
    val extraIndex: Int get() = _extraIndex

    fun initView(context: Context): View? {
        val currentEngine = getCurrentEngine()
        val view = currentEngine.initView(context)
        currentEngine.components.forEach { it.reset() }
        return view
    }

    fun updateView() {
        getCurrentEngine().components.forEach { it.updateView() }
    }

    fun getCurrentEngine(): InputEngine {
        return engines[table[_languageIndex][_extraIndex]]
    }

    fun showCandidates(list: List<Candidate>) {
        getCurrentEngine().components.filterIsInstance<CandidatesComponent>()
            .forEach { it.showCandidates(list) }
    }

    fun setLanguage(index: Int) {
        _languageIndex = index
        _extraIndex = 0
    }

    fun nextLanguage() {
        getCurrentEngine().components.filterIsInstance<CandidatesComponent>()
            .forEach { it.showCandidates(listOf()) }
        _languageIndex += 1
        if(_languageIndex >= table.size) _languageIndex = 0
        _extraIndex = 0
    }

    fun nextExtra() {
        _extraIndex += 1
        if(_extraIndex >= table[_languageIndex].size) _extraIndex = 0
    }
}