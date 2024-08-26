package ee.oyatl.ime.make.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.modifiers.ModifierKeyState
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet
import ee.oyatl.ime.make.module.candidates.Candidate
import ee.oyatl.ime.make.module.candidates.CandidateListener
import ee.oyatl.ime.make.module.component.LanguageTabBarComponent
import ee.oyatl.ime.make.module.inputengine.InputEngine
import ee.oyatl.ime.make.preset.InputEnginePreset
import ee.oyatl.ime.make.preset.PresetLoader
import ee.oyatl.ime.make.preset.table.CustomKeyCode
import ee.oyatl.ime.make.settings.SettingsActivity
import ee.oyatl.ime.make.settings.preference.HotkeyDialogPreference
import kotlin.math.abs

class IMEService: InputMethodService(), InputEngine.Listener, CandidateListener, LanguageTabBarComponent.Listener {
    private var composingText: CharSequence = ""
    private var cursorAnchorInfo: CursorAnchorInfo? = null

    private val clipboard: ClipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }
    private var inputEngineSwitcher: InputEngineSwitcher? = null

    private var languageSwitchModifiers: Int = HotkeyDialogPreference.DEFAULT_MODIFIER
    private var languageSwitchKeycode: Int = HotkeyDialogPreference.DEFAULT_KEYCODE

    private var screenType: String = "mobile"
    private val softKeyboardHidden
        get() = resources.configuration.keyboardHidden == Configuration.KEYBOARDHIDDEN_YES
    private val hardKeyboardHidden
        get() = resources.configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        SettingsActivity.setDefaultValues(this)
        reload()
    }

    private fun reload() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val presetLoader = PresetLoader(this)

        val presets = loadPresets(presetLoader)
        val (latinPreset, hangulPreset, symbolPreset) = presets

        val latinModule = presetLoader.modPreset(latinPreset)
        val latinSymbolModule = presetLoader.modPreset(symbolPreset.copy(language = "en"))
        val hangulModule = presetLoader.modPreset(hangulPreset)
        val hangulSymbolModule = presetLoader.modPreset(symbolPreset.copy(language = "ko"))

        val latinInputEngine = latinModule.inflate(this, this)
        val latinSymbolInputEngine = latinSymbolModule.inflate(this, this)
        val hangulInputEngine = hangulModule.inflate(this, this)
        val hangulSymbolInputEngine = hangulSymbolModule.inflate(this, this)

        latinInputEngine.symbolsInputEngine = latinSymbolInputEngine
        latinInputEngine.alternativeInputEngine = hangulInputEngine

        hangulInputEngine.symbolsInputEngine = hangulSymbolInputEngine
        hangulInputEngine.alternativeInputEngine = latinInputEngine

        val engines = listOf(
            latinInputEngine,
            hangulInputEngine,
            latinSymbolInputEngine,
            hangulSymbolInputEngine,
        )
        val table = arrayOf(
            intArrayOf(0, 2),
            intArrayOf(1, 3),
        )
        val switcher = InputEngineSwitcher(engines, table)
        this.inputEngineSwitcher = switcher

        val languageSwitchHotkey = pref.getString(
            "behaviour_hardware_lang_switch_hotkey", "") ?: ""
        languageSwitchModifiers = HotkeyDialogPreference.parseModifiers(languageSwitchHotkey)
        languageSwitchKeycode = HotkeyDialogPreference.parseKeycode(languageSwitchHotkey)

        screenType = pref.getString("layout_screen_type", screenType) ?: screenType
    }

    override fun onCreateInputView(): View {
        val inputView = inputEngineSwitcher?.initView(this) ?: View(this)
        val inputViewWrapper = LinearLayout(this).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        if(screenType == "television") {
            val width = resources.getDimensionPixelSize(R.dimen.input_view_width)
            inputView.layoutParams = LinearLayout.LayoutParams(
                width,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        inputViewWrapper.addView(inputView)
        return inputViewWrapper
    }

    override fun onCandidates(list: List<Candidate>) {
//        val sorted = list.sortedByDescending { it.score }
        inputEngineSwitcher?.showCandidates(list)
    }

    override fun onItemClicked(candidate: Candidate) {
        val newComposingText = composingText.drop(candidate.text.length)
        onComposingText(candidate.text)
        onFinishComposing()
        resetCurrentEngine()
        onCandidates(listOf())
        onComposingText(newComposingText)
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        val engine = inputEngineSwitcher?.currentEngine ?: return
        engine.shiftKeyHandler.reset()
        resetCurrentEngine()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        if(event.isSystem) return super.onKeyDown(keyCode, event)
        val currentEngine = inputEngineSwitcher?.currentEngine
        currentEngine ?: return super.onKeyDown(keyCode, event)
        val modifiers = getModifierKeyStateSet(event)
        if(modifiers.asMetaState() == languageSwitchModifiers && keyCode == languageSwitchKeycode) {
            onNonPrintingKey(KeyEvent.KEYCODE_LANGUAGE_SWITCH)
            return true
        }
        if(modifiers.alt.active || modifiers.control.active || modifiers.meta.active) {
            return super.onKeyDown(keyCode, event)
        }
        if(event.isPrintingKey) {
            currentEngine.onKey(keyCode, modifiers)
        } else if(!onNonPrintingKey(keyCode)) {
            return super.onKeyDown(keyCode, event)
        }
        return true
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        if(event.isSystem) return super.onKeyUp(keyCode, event)
        if(!event.isPrintingKey) return super.onKeyUp(keyCode, event)
        return true
    }

    override fun onNonPrintingKey(code: Int): Boolean {
        if(onSelectionKeys(code)) {
            resetCurrentEngine()
            return false
        }
        return when(code) {
            KeyEvent.KEYCODE_DEL -> {
                if(deleteSelection()) true
                else inputEngineSwitcher?.currentEngine?.onDelete() != null
            }
            KeyEvent.KEYCODE_SPACE -> {
                resetCurrentEngine()
                onCommitText(" ")
                true
            }
            KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                resetCurrentEngine()
                onEditorAction(code)
                true
            }
            KeyEvent.KEYCODE_LANGUAGE_SWITCH -> {
                resetShiftState()
                resetCurrentEngine()
                inputEngineSwitcher?.nextLanguage()
                reloadView()
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                resetShiftState()
                resetCurrentEngine()
                inputEngineSwitcher?.nextExtra()
                reloadView()
                true
            }
            else -> false
        }
    }

    private fun onSelectionKeys(code: Int): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val extractedText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)
        return when(code) {
            KeyEvent.KEYCODE_DPAD_LEFT,
            KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN -> {
                true
            }
            KeyEvent.KEYCODE_MOVE_HOME,
            KeyEvent.KEYCODE_MOVE_END,
            KeyEvent.KEYCODE_PAGE_UP,
            KeyEvent.KEYCODE_PAGE_DOWN -> {
                true
            }
            CustomKeyCode.KEYCODE_COPY.code -> {
                val selectedText = inputConnection.getSelectedText(0)?.toString().orEmpty()
                val clip = ClipData.newPlainText(selectedText, selectedText)
                clipboard.setPrimaryClip(clip)
                true
            }
            CustomKeyCode.KEYCODE_CUT.code -> {
                val selectedText = inputConnection.getSelectedText(0)?.toString().orEmpty()
                val clip = ClipData.newPlainText(selectedText, selectedText)
                clipboard.setPrimaryClip(clip)
                inputConnection.commitText("", 1)
                true
            }
            CustomKeyCode.KEYCODE_PASTE.code -> {
                val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
                inputConnection.commitText(text, 1)
                true
            }
            CustomKeyCode.KEYCODE_SELECT_ALL.code -> {
                extractedText ?: return true
                extractedText.selectionStart = 0
                extractedText.selectionEnd = extractedText.text.length
                inputConnection.setSelection(extractedText.selectionStart, extractedText.selectionEnd)
                true
            }
            CustomKeyCode.KEYCODE_EXPAND_SELECTION_LEFT.code -> {
                extractedText ?: return true
                extractedText.selectionStart -= 1
                inputConnection.setSelection(extractedText.selectionStart, extractedText.selectionEnd)
                true
            }
            CustomKeyCode.KEYCODE_EXPAND_SELECTION_RIGHT.code -> {
                extractedText ?: return true
                extractedText.selectionEnd += 1
                inputConnection.setSelection(extractedText.selectionStart, extractedText.selectionEnd)
                true
            }
            else -> false
        }
    }

    private fun deleteSelection(): Boolean {
        val inputConnection = currentInputConnection ?: return false
        val extractedText = inputConnection.getExtractedText(ExtractedTextRequest(), 0)
        extractedText ?: return true
        val start = extractedText.startOffset + extractedText.selectionStart
        val end = extractedText.startOffset + extractedText.selectionEnd
        val selectionLength = abs(end - start)
        if(selectionLength != 0) {
            resetCurrentEngine()
            inputConnection.setSelection(start, start)
            if(start < end) {
                inputConnection.deleteSurroundingText(0, selectionLength)
            } else {
                inputConnection.deleteSurroundingText(selectionLength, 0)
            }
            return true
        }
        return false
    }

    override fun onEditorAction(code: Int) {
        if(!sendDefaultEditorAction(true)) sendDownUpKeyEvents(code)
    }

    override fun onDefaultAction(code: Int) {
        sendDownUpKeyEvents(code)
    }

    override fun onComposingText(text: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        if(text.isEmpty() && inputConnection.getSelectedText(0)?.isNotEmpty() == true) return
        composingText = text
        inputConnection.setComposingText(text, 1)
    }

    override fun onFinishComposing() {
        val inputConnection = currentInputConnection ?: return
        composingText = ""
        inputConnection.finishComposingText()
        updateTextAroundCursor()
    }

    override fun onCommitText(text: CharSequence) {
        val inputConnection = currentInputConnection ?: return
        resetCurrentEngine()
        inputConnection.commitText(text, 1)
        updateTextAroundCursor()
    }

    override fun onDeleteText(beforeLength: Int, afterLength: Int) {
        val inputConnection = currentInputConnection ?: return
        inputConnection.deleteSurroundingText(beforeLength, afterLength)
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart,
            oldSelEnd,
            newSelStart,
            newSelEnd,
            candidatesStart,
            candidatesEnd
        )
        currentInputConnection?.requestCursorUpdates(InputConnection.CURSOR_UPDATE_MONITOR)
    }

    override fun onUpdateCursorAnchorInfo(cursorAnchorInfo: CursorAnchorInfo?) {
        super.onUpdateCursorAnchorInfo(cursorAnchorInfo)
        cursorAnchorInfo ?: return
        val end = cursorAnchorInfo.selectionEnd
        if(end == -1) return
        val composingText = cursorAnchorInfo.composingText
        val composingTextStart = cursorAnchorInfo.composingTextStart
        // Skip if no text is being composed
        if(composingText != null && composingTextStart != -1) {
            // Reset input if expected and actual composing text end positions differ
            // Used to detect if cursor is in unexpected position, away from the composing text
            // For example when the cursor is moved by the user manually by tapping on the text view
            val expectedComposingTextEnd = composingTextStart + composingText.length
            if(end != expectedComposingTextEnd) resetCurrentEngine()
        }
        this.cursorAnchorInfo = cursorAnchorInfo
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reload()
    }

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        outInsets?.contentTopInsets = outInsets?.visibleTopInsets ?: return
    }

    private fun resetCurrentEngine() {
        val engine = inputEngineSwitcher?.currentEngine ?: return
        engine.onReset()
        engine.onResetComponents()
        engine.components.forEach { it.updateView() }
        updateTextAroundCursor()
    }

    private fun resetShiftState() {
        val engine = inputEngineSwitcher?.currentEngine ?: return
        engine.shiftKeyHandler.reset()
    }

    private fun updateTextAroundCursor() {
        val engine = inputEngineSwitcher?.currentEngine ?: return
        val inputConnection = currentInputConnection ?: return
        val before = inputConnection.getTextBeforeCursor(100, 0).toString()
        val after = inputConnection.getTextAfterCursor(100, 0).toString()
        engine.onTextAroundCursor(before, after)
    }

    private fun reloadView() {
        setInputView(onCreateInputView())
        inputEngineSwitcher?.updateView()
    }

    private fun getModifierKeyStateSet(event: KeyEvent): ModifierKeyStateSet {
        return ModifierKeyStateSet(
            shift = ModifierKeyState(pressed = event.isShiftPressed, locked = event.isCapsLockOn),
            alt = ModifierKeyState(pressed = event.isAltPressed),
            control = ModifierKeyState(pressed = event.isCtrlPressed),
            meta = ModifierKeyState(pressed = event.isMetaPressed)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onUpdateLanguageTabs(): List<LanguageTabBarComponent.Tab> {
        val currentLanguage = inputEngineSwitcher?.languageIndex ?: 0
        val languageTabs = listOf(
            LanguageTabBarComponent.Tab(0, R.string.lang_label_en, currentLanguage == 0),
            LanguageTabBarComponent.Tab(1, R.string.lang_label_ko, currentLanguage == 1)
        )
        return languageTabs
    }

    override fun onVoiceButtonClick() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val voiceSubtypes = imm.enabledInputMethodList
            .flatMap { method -> (0 until method.subtypeCount).map { method to method.getSubtypeAt(it) } }
            .filter { (_, subtype) -> subtype.mode == "voice" }
        if(voiceSubtypes.isNotEmpty()) {
            val (method, subtype) = voiceSubtypes.first()
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                switchInputMethod(method.id, subtype)
            } else {
                val token = window.window?.attributes?.token ?: return
                @Suppress("DEPRECATION")
                imm.setInputMethodAndSubtype(token, method.id, subtype)
            }
        }
    }

    override fun onLanguageTabClick(index: Int) {
        inputEngineSwitcher?.currentEngine?.shiftKeyHandler?.reset()
        inputEngineSwitcher?.setLanguage(index)
        reloadView()
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        super.onEvaluateInputViewShown()
        return true
    }

    private fun loadPresets(presetLoader: PresetLoader): List<InputEnginePreset> {
        val latinPreset: InputEnginePreset
        val hangulPreset: InputEnginePreset
        val symbolPreset: InputEnginePreset

        if(hardKeyboardHidden) {
            val latinFileName = getString(R.string.preset_file_latin_soft)
            val hangulFileName = getString(R.string.preset_file_hangul_soft)
            val symbolFileName = getString(R.string.preset_file_symbol_soft)

            latinPreset = presetLoader.load(latinFileName, "preset/preset_latin_qwerty.yaml")
            hangulPreset = presetLoader.load(hangulFileName, "preset/preset_3set_390.yaml")
            symbolPreset = presetLoader.load(symbolFileName, "preset/preset_symbol_g.yaml")
        } else {
            val latinFileName = getString(R.string.preset_file_latin_hard)
            val hangulFileName = getString(R.string.preset_file_hangul_hard)

            latinPreset = presetLoader.loadHardware(latinFileName, "preset/preset_latin_qwerty.yaml")
            hangulPreset = presetLoader.loadHardware(hangulFileName, "preset/preset_3set_390.yaml")
            symbolPreset = InputEnginePreset()
        }

        return listOf(
            latinPreset.copy(language = "en"),
            hangulPreset.copy(language = "ko"),
            symbolPreset
        )
    }

    companion object {
        private var INSTANCE: IMEService? = null
        fun restartService() {
            val instance = INSTANCE ?: return
            instance.reload()
            instance.reloadView()
        }
    }
}