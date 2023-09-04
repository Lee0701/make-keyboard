package ee.oyatl.ime.make

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.KeyCharacterMap
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import ee.oyatl.ime.make.data.Layouts
import ee.oyatl.ime.make.data.MoreKeysTables
import ee.oyatl.ime.make.data.SoftKeyboardLayouts
import ee.oyatl.ime.make.data.SymbolTables
import ee.oyatl.ime.make.model.KeyOutput
import ee.oyatl.ime.make.modifier.DefaultShiftKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyStateSet
import ee.oyatl.ime.make.preset.KeyboardPreset
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.KeyEvent
import ee.oyatl.ime.make.view.candidates.CandidatesViewManager
import ee.oyatl.ime.make.view.keyboard.FlickDirection
import ee.oyatl.ime.make.view.keyboard.KeyboardListener
import ee.oyatl.ime.make.view.keyboard.StackedViewKeyboardView
import ee.oyatl.ime.make.view.keyboard.Themes

class IMEService: InputMethodService(), KeyboardListener {
    private val handler: Handler = Handler(Looper.getMainLooper())
//    private val ioScope: CoroutineScope = CoroutineScope(Job() + Dispatchers.IO)
//    private val mainScope: CoroutineScope = CoroutineScope(Dispatchers.Main)
//    var job: Job? = null

    private var mainView: ViewGroup? = null
    private var inputView: ViewGroup? = null
    private var candidatesView: CandidatesViewManager? = null

    private val doubleTapGap = 500
    private val longPressDelay = 500
    private val repeatDelay = 50

    private val shiftHandler: ModifierKeyHandler = DefaultShiftKeyHandler(doubleTapGap)
    private val modifierState: ModifierKeyStateSet get() = ModifierKeyStateSet(
        shift = shiftHandler.state,
    )

    private val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
    private val keyboardPresets: MutableList<KeyboardPreset> = mutableListOf()
    private var currentKeyboardPresetIndex = 0
    private val currentKeyboardPreset: KeyboardPreset?
        get() = keyboardPresets.getOrNull(currentKeyboardPresetIndex)

    private val mainViewHeight by lazy { mainView?.height ?: 0 }
    private val inputViewHeight by lazy { inputView?.height ?: 0 }
    private val candidatesViewHeight by lazy { candidatesView?.getView()?.height ?: 0 }

    override fun onCreate() {
        super.onCreate()
        val rowHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 55f, resources.displayMetrics).toInt()

        run {
            val keyboard = StackedViewKeyboardView(
                context = this,
                attrs = null,
                listener = this,
                keyboard = SoftKeyboardLayouts.LAYOUT_QWERTY_MOBILE,
                theme = Themes.Dynamic,
                popupOffsetY = candidatesViewHeight,
                unifyHeight = true,
                rowHeight = rowHeight,
            )
            val convertTable: CodeConvertTable = Layouts.CONVERT_QWERTY
            val moreKeysTable: MoreKeysTable = MoreKeysTables.MORE_KEYS_TABLE_M_R
            val preset = KeyboardPreset(keyboard, convertTable, moreKeysTable)
            keyboardPresets += preset
        }
        run {
            val keyboard = StackedViewKeyboardView(
                context = this,
                attrs = null,
                listener = this,
                keyboard = SoftKeyboardLayouts.LAYOUT_QWERTY_MOBILE_SEMICOLON,
                theme = Themes.Dynamic,
                popupOffsetY = candidatesViewHeight,
                unifyHeight = true,
                rowHeight = rowHeight,
            )
            val convertTable: CodeConvertTable = SymbolTables.LAYOUT_SYMBOLS_G
            val moreKeysTable: MoreKeysTable = SymbolTables.MORE_KEYS_TABLE_SYMBOLS_G
            val preset = KeyboardPreset(keyboard, convertTable, moreKeysTable)
            keyboardPresets += preset
        }
    }

    override fun onCreateInputView(): View {
        val mainView = LinearLayoutCompat(this).apply {
            orientation = LinearLayoutCompat.VERTICAL
        }
        val inputView = FrameLayout(this)
        val candidatesViewManager = CandidatesViewManager(object: CandidatesViewManager.Listener {
            override fun onCandidateClick(position: Int) {
                updateInput()
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

        keyboardPresets.forEach { inputView.addView(it.keyboardView) }
        currentKeyboardPreset?.keyboardView?.bringToFront()

//        mainView.addView(candidatesViewManager.initView(this))
        mainView.addView(inputView)

        this.candidatesView = candidatesViewManager
        this.inputView = inputView
        this.mainView = mainView
        return mainView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        resetInput()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onKeyClick(code: Int, output: String?) {
        val char = keyCharacterMap[code, modifierState.asMetaState()]
        val isPrintingKey = codeIsPrintingKey(code)
        val isSystemKey = when(code) {
            android.view.KeyEvent.KEYCODE_DEL -> onDeleteKey()
            android.view.KeyEvent.KEYCODE_SPACE -> onSpace()
            android.view.KeyEvent.KEYCODE_ENTER -> onActionKey()
            android.view.KeyEvent.KEYCODE_LANGUAGE_SWITCH -> onLanguageKey()
            android.view.KeyEvent.KEYCODE_SYM -> onSymbolsKey()
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT, android.view.KeyEvent.KEYCODE_SHIFT_RIGHT -> true
            else -> false
        }
        if(!isSystemKey) {
            if(code == 0 && output != null) {
                onKeyText(output)
            } else if(char == 0) {
                sendDownUpKeyEvents(code)
            } else if(isPrintingKey) {
                if(code != 0) onKeyCode(code)
                else onKeyText(char.toChar().toString())
            }
            shiftHandler.onInput()
            updateInput()
        }
    }

    override fun onKeyLongClick(code: Int, output: String?) {
    }

    override fun onKeyDown(code: Int, output: String?) {
        when(code) {
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
            android.view.KeyEvent.KEYCODE_SHIFT_RIGHT -> shiftHandler.onPress()
            else -> return
        }
        updateInput()
    }

    override fun onKeyUp(code: Int, output: String?) {
        when(code) {
            android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
            android.view.KeyEvent.KEYCODE_SHIFT_RIGHT -> shiftHandler.onRelease()
            else -> return
        }
        updateInput()
    }

    override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) {
    }

    private fun updateInput() {
        updateLabelsAndIcons()
        updateMoreKeys()
    }

    private fun updateLabelsAndIcons() {
        val keyboardView = currentKeyboardPreset?.keyboardView ?: return
        val labelsToUpdate = android.view.KeyEvent.KEYCODE_UNKNOWN .. android.view.KeyEvent.KEYCODE_SEARCH
        val labels = labelsToUpdate.associateWith { code ->
            val label = currentKeyboardPreset?.convertTable?.get(code, modifierState)?.toChar()?.toString()
            label ?: keyCharacterMap.get(code, modifierState.asMetaState()).toChar().toString()
        }
        val icons = mapOf<Int, Drawable>()
        keyboardView.updateLabelsAndIcons(labels, getIcons() + icons)
    }

    private fun updateMoreKeys() {
        val preset = currentKeyboardPreset ?: return
        val moreKeysTable = preset.moreKeysTable.map.map { (char, value) ->
            val keyCode = preset.convertTable.getReversed(char, modifierState)
            if(keyCode != null) keyCode to value else null
        }.filterNotNull().toMap()
        preset.keyboardView.updateMoreKeyKeyboards(moreKeysTable)
    }

    private fun convert(): List<String> {
        return emptyList()
    }

    private fun resetInput() {
        val inputConnection = currentInputConnection ?: return
        inputConnection.finishComposingText()
        updateInput()
    }

    private fun commitText(text: String) {
        val inputConnection = currentInputConnection ?: return
        resetInput()
        inputConnection.commitText(text, 1)
    }

    private fun commitCandidate(text: String) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    private fun onDeleteKey(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        resetInput()
        inputConnection.deleteSurroundingText(1, 0)
        return true
    }

    private fun onSpace(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        resetInput()
        inputConnection.commitText(" ", 1)
        return true
    }

    private fun onActionKey(): Boolean {
        resetInput()
        sendDownUpKeyEvents(android.view.KeyEvent.KEYCODE_ENTER)
        return true
    }

    private fun onLanguageKey(): Boolean {
        return true
    }

    private fun onSymbolsKey(): Boolean {
        currentKeyboardPresetIndex += 1
        if(currentKeyboardPresetIndex >= keyboardPresets.size) currentKeyboardPresetIndex = 0
        currentKeyboardPreset?.keyboardView?.bringToFront()
        updateInput()
        return true
    }

    private fun onKeyCode(code: Int) {
        val output = currentKeyboardPreset?.convertTable?.get(code, modifierState)?.toChar()?.toString()
        if(output != null) onKeyText(output)
        else sendDownUpKeyEvents(code)
    }

    private fun onKeyText(text: String) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.commitText(text, 1)
    }

    private fun onSpecialKey(event: KeyEvent) {
        when(event.action) {
            KeyEvent.Action.Press -> {
                if(event.output is KeyOutput.Special) {
                    onSpecialKeyPress(event.output)
                    performKeyFeedback(event.output)
                }
            }
            KeyEvent.Action.Release -> {
                if(event.output is KeyOutput.Special) {
                    onSpecialKeyRelease(event.output)
                }
            }
            KeyEvent.Action.Repeat -> {
                if(event.output is KeyOutput.Special) {
                    onSpecialKeyRepeat(event.output)
                }
            }
        }
        updateInput()
    }

    private fun onSpecialKeyPress(output: KeyOutput.Special) {
        val inputConnection = currentInputConnection ?: return
        when(output) {
            is KeyOutput.Special.Delete -> {
                inputConnection.deleteSurroundingText(1, 0)
                fun repeat() {
                    this.onSpecialKey(KeyEvent(KeyEvent.Action.Repeat, output))
                    handler.postDelayed({ repeat() }, repeatDelay.toLong())
                }
                handler.postDelayed({ repeat() }, longPressDelay.toLong())
            }
            is KeyOutput.Special.Shift -> {
                shiftHandler.onPress()
            }
            is KeyOutput.Special.Space -> {
                inputConnection.commitText(" ", 1)
            }
            is KeyOutput.Special.Return -> {
                sendDefaultEditorAction(true)
            }
            else -> Unit
        }
    }

    private fun onSpecialKeyRelease(output: KeyOutput.Special) {
        val inputConnection = currentInputConnection ?: return
        when(output) {
            is KeyOutput.Special.Delete -> {
                handler.removeCallbacksAndMessages(null)
            }
            is KeyOutput.Special.Shift -> {
                shiftHandler.onRelease()
            }
            else -> Unit
        }
    }

    private fun onSpecialKeyRepeat(output: KeyOutput.Special) {
        val inputConnection = currentInputConnection ?: return
        when(output) {
            is KeyOutput.Special.Delete -> {
                inputConnection.deleteSurroundingText(1, 0)
            }
            else -> Unit
        }
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

    private fun getIcons(): Map<Int, Drawable> {
        val shiftIconID = if(modifierState.shift.locked) R.drawable.keyic_shift_lock else R.drawable.keyic_shift
        val shiftIcon = ContextCompat.getDrawable(this, shiftIconID)
        val icons = shiftIcon?.let { mapOf(android.view.KeyEvent.KEYCODE_SHIFT_LEFT to it, android.view.KeyEvent.KEYCODE_SHIFT_RIGHT to it) }.orEmpty()
        return icons
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