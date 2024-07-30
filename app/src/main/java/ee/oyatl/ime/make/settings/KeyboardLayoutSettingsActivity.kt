package ee.oyatl.ime.make.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.module.candidates.Candidate
import ee.oyatl.ime.make.module.candidates.CandidateListener
import ee.oyatl.ime.make.module.inputengine.InputEngine
import ee.oyatl.ime.make.module.keyboardview.FlickDirection
import ee.oyatl.ime.make.module.keyboardview.KeyboardListener
import ee.oyatl.ime.make.service.IMEService

class KeyboardLayoutSettingsActivity: AppCompatActivity() {

    private val fileName: String by lazy { intent.getStringExtra("fileName") ?: "default.yaml" }
    private val template: String by lazy { intent.getStringExtra("template") ?: "default.yaml" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DynamicColors.applyToActivityIfAvailable(this)

        setContentView(R.layout.activity_keyboard_layout_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, KeyboardLayoutSettingsFragment(fileName, template))
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        IMEService.sendReloadIntent(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val emptyKeyboardListener = object: KeyboardListener {
            override fun onKeyClick(code: Int, output: String?) = Unit
            override fun onKeyLongClick(code: Int, output: String?) = Unit
            override fun onKeyDown(code: Int, output: String?) = Unit
            override fun onKeyUp(code: Int, output: String?) = Unit
            override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) = Unit
        }

        val emptyInputEngineListener = object: InputEngine.Listener {
            override fun onComposingText(text: CharSequence) = Unit
            override fun onFinishComposing() = Unit
            override fun onCommitText(text: CharSequence) = Unit
            override fun onDeleteText(beforeLength: Int, afterLength: Int) = Unit
            override fun onCandidates(list: List<Candidate>) = Unit
            override fun onNonPrintingKey(code: Int): Boolean = false
            override fun onDefaultAction(code: Int) = Unit
            override fun onEditorAction(code: Int) = Unit
        }

        val emptyCandidateListener = object: CandidateListener {
            override fun onItemClicked(candidate: Candidate) = Unit
        }
    }

}