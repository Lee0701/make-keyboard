package ee.oyatl.ime.make

import android.animation.ValueAnimator
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import ee.oyatl.ime.make.data.ConvertTables
import ee.oyatl.ime.make.data.MoreKeysTables
import ee.oyatl.ime.make.data.SoftKeyboardLayouts
import ee.oyatl.ime.make.data.SymbolTables
import ee.oyatl.ime.make.model.KeyOutput
import ee.oyatl.ime.make.model.KeyboardProfilePreset
import ee.oyatl.ime.make.profile.CommonKeyboardProfile
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.candidates.CandidatesViewManager
import ee.oyatl.ime.make.view.keyboard.FlickDirection
import ee.oyatl.ime.make.view.keyboard.KeyboardListener

class IMEService: InputMethodService(), KeyboardListener, CommonKeyboardProfile.Listener {

    private var mainView: ViewGroup? = null
    private var inputView: ViewGroup? = null
    private var candidatesView: CandidatesViewManager? = null

    private val keyboardProfiles: MutableList<CommonKeyboardProfile> = mutableListOf()
    private var currentKeyboardProfileIndex = 0
    private val currentKeyboardProfile: CommonKeyboardProfile
        get() = keyboardProfiles[currentKeyboardProfileIndex]

    private val mainViewHeight by lazy { mainView?.height ?: 0 }
    private val inputViewHeight by lazy { inputView?.height ?: 0 }

    override fun onCreate() {
        super.onCreate()
        val keyboardPresets = mutableListOf<KeyboardProfilePreset>()
        run {
            val convertTable: CodeConvertTable = ConvertTables.CONVERT_QWERTY
            val moreKeysTable: MoreKeysTable = MoreKeysTables.MORE_KEYS_TABLE_M_R
            val preset = KeyboardProfilePreset(
                KeyboardProfilePreset.Type.Alphabetic,
                SoftKeyboardLayouts.LAYOUT_QWERTY_MOBILE, convertTable, moreKeysTable
            )
            keyboardPresets += preset
        }
        run {
            val convertTable: CodeConvertTable = SymbolTables.LAYOUT_SYMBOLS_G
            val moreKeysTable: MoreKeysTable = SymbolTables.MORE_KEYS_TABLE_SYMBOLS_G
            val preset = KeyboardProfilePreset(
                KeyboardProfilePreset.Type.Symbol,
                SoftKeyboardLayouts.LAYOUT_QWERTY_MOBILE_SEMICOLON, convertTable, moreKeysTable
            )
            keyboardPresets += preset
        }
        keyboardProfiles += keyboardPresets.map { it.inflate(this, this, this) }
        keyboardProfiles.forEach { inputView?.addView(it.keyboardView) }
    }

    override fun onCreateInputView(): View {
        val mainView = LinearLayoutCompat(this).apply {
            orientation = LinearLayoutCompat.VERTICAL
        }
        val inputView = FrameLayout(this)
        val candidatesViewManager = CandidatesViewManager(object: CandidatesViewManager.Listener {
            override fun onCandidateClick(position: Int) {
                updateInputView()
            }

            val candidatesViewHeight by lazy { 0 }
            fun getAnimator(inputView: View, candidatesView: View): ValueAnimator {
                return ValueAnimator.ofInt(0, inputViewHeight).apply {
                    this.duration = 200
                    this.interpolator = DecelerateInterpolator()
                    addUpdateListener {
                        candidatesView.layoutParams.height = (it.animatedValue as Int) + candidatesViewHeight
                        inputView.layoutParams.height = mainViewHeight - (it.animatedValue as Int) - candidatesViewHeight
                        candidatesView.requestLayout()
                        inputView.requestLayout()
                    }
                }
            }

            override fun onExpand(candidatesView: View) {
                getAnimator(inputView, candidatesView).start()
            }
            override fun onCollapse(candidatesView: View) {
                getAnimator(inputView, candidatesView).reverse()
            }
        })

        keyboardProfiles.forEach { inputView.addView(it.keyboardView) }
        currentKeyboardProfile.keyboardView.bringToFront()

//        mainView.addView(candidatesViewManager.initView(this))
        mainView.addView(inputView)

        this.candidatesView = candidatesViewManager
        this.inputView = inputView
        this.mainView = mainView
        return mainView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        resetInputView()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun convert(): List<String> {
        return emptyList()
    }

    override fun onKeyClick(code: Int, output: String?) {
        currentKeyboardProfile.onKeyClick(code, output)
    }

    override fun onKeyLongClick(code: Int, output: String?) {
        currentKeyboardProfile.onKeyLongClick(code, output)
    }

    override fun onKeyDown(code: Int, output: String?) {
        currentKeyboardProfile.onKeyDown(code, output)
    }

    override fun onKeyUp(code: Int, output: String?) {
        currentKeyboardProfile.onKeyUp(code, output)
    }

    override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) {
        currentKeyboardProfile.onKeyFlick(direction, code, output)
    }

    private fun resetInputView() {
        val inputConnection = currentInputConnection ?: return
        inputConnection.finishComposingText()
        updateInputView()
    }

    private fun commitText(text: String) {
        val inputConnection = currentInputConnection ?: return
        resetInputView()
        inputConnection.commitText(text, 1)
    }

    private fun commitCandidate(text: String) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    private fun nextProfile() {
        currentKeyboardProfileIndex += 1
        if(currentKeyboardProfileIndex >= keyboardProfiles.size) currentKeyboardProfileIndex = 0
        currentKeyboardProfile.keyboardView.bringToFront()
        updateInputView()
    }

    fun updateInputView() {

    }

    override fun onText(text: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.finishComposingText()
        inputConnection.commitText(text, 1)
    }

    override fun onDelete(before: Int, after: Int) {
        currentInputConnection?.deleteSurroundingText(before, after)
    }

    override fun onSpecialKey(output: KeyOutput.Special) {
        when(output) {
            KeyOutput.Special.Symbol -> nextProfile()
            else -> Unit
        }
    }

    override fun onRawKeyCode(keyCode: Int) {
        sendDownUpKeyEvents(keyCode)
    }

    override fun onEditorAction(fromEnterKey: Boolean) {
        sendDefaultEditorAction(fromEnterKey)
    }

    override fun onFeedback(output: KeyOutput) {
        performKeyFeedback(output)
    }

    override fun onInputViewUpdate() {
        updateInputView()
    }

    override fun onInputViewReset() {
        resetInputView()
    }

    private fun performKeyFeedback(output: KeyOutput) {
        if(output != KeyOutput.None) {
            performHapticFeedback(output)
            performSoundFeedback(output)
        }
    }

    private fun performHapticFeedback(output: KeyOutput) {
        val inputView = inputView ?: return
        inputView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    private fun performSoundFeedback(output: KeyOutput) {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val fx = when(output) {
            is KeyOutput.Special.Delete -> AudioManager.FX_KEYPRESS_DELETE
            is KeyOutput.Special.Return -> AudioManager.FX_KEYPRESS_RETURN
            is KeyOutput.Special.Space -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
        audioManager.playSoundEffect(fx, 1f)
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        outInsets?.contentTopInsets = outInsets?.visibleTopInsets
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    companion object {
        fun codeIsPrintingKey(keyCode: Int): Boolean = android.view.KeyEvent(
            android.view.KeyEvent.ACTION_DOWN,
            keyCode
        ).isPrintingKey
    }
}