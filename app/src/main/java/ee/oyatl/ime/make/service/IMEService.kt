package ee.oyatl.ime.make.service

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.CursorAnchorInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import android.view.inputmethod.InputConnection
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.charleskorn.kaml.decodeFromStream
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.modifiers.ModifierKeyState
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet
import ee.oyatl.ime.make.module.candidates.Candidate
import ee.oyatl.ime.make.module.candidates.CandidateListener
import ee.oyatl.ime.make.module.inputengine.InputEngine
import ee.oyatl.ime.make.preset.InputEnginePreset
import ee.oyatl.ime.make.preset.PresetLoader
import ee.oyatl.ime.make.preset.table.CustomKeyCode
import ee.oyatl.ime.make.settings.HotkeyDialogPreference
import java.io.File
import kotlin.math.abs

class IMEService: InputMethodService(), InputEngine.Listener, CandidateListener {
    private var composingText: CharSequence = ""
    private var cursorAnchorInfo: CursorAnchorInfo? = null

    private val clipboard: ClipboardManager by lazy { getSystemService(CLIPBOARD_SERVICE) as ClipboardManager }
    private var inputEngineSwitcher: InputEngineSwitcher? = null

    private var languageSwitchModifiers: Int = HotkeyDialogPreference.DEFAULT_MODIFIER
    private var languageSwitchKeycode: Int = HotkeyDialogPreference.DEFAULT_KEYCODE

    override fun onCreate() {
        super.onCreate()
        reload()
    }

    private fun reload() {
        val pref = PreferenceManager.getDefaultSharedPreferences(this)
        val loader = PresetLoader(this)

        val (latinPreset, hangulPreset, symbolPreset) = loadPresets(this)

        val latinModule = loader.modLatin(latinPreset)
        val latinSymbolModule = loader.modSymbol(symbolPreset, "en")
        val hangulModule = loader.modHangul(hangulPreset)
        val hangulSymbolModule = loader.modSymbol(symbolPreset, "ko")

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
        reloadView()
    }

    override fun onCreateInputView(): View {
        return inputEngineSwitcher?.initView(this) ?: View(this)
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

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        resetCurrentEngine()
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        event ?: return false
        if(event.isSystem) return super.onKeyDown(keyCode, event)
        val currentEngine = inputEngineSwitcher?.getCurrentEngine()
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
                else inputEngineSwitcher?.getCurrentEngine()?.onDelete() != null
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
                resetCurrentEngine()
                inputEngineSwitcher?.nextLanguage()
                reloadView()
                updateView()
                true
            }
            KeyEvent.KEYCODE_SYM -> {
                resetCurrentEngine()
                inputEngineSwitcher?.nextExtra()
                reloadView()
                updateView()
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
        val engine = inputEngineSwitcher?.getCurrentEngine() ?: return
        engine.onReset()
        engine.onResetComponents()
        updateTextAroundCursor()
    }

    private fun updateTextAroundCursor() {
        val engine = inputEngineSwitcher?.getCurrentEngine() ?: return
        val inputConnection = currentInputConnection ?: return
        val before = inputConnection.getTextBeforeCursor(100, 0).toString()
        val after = inputConnection.getTextAfterCursor(100, 0).toString()
        engine.onTextAroundCursor(before, after)
    }

    private fun reloadView() {
        setInputView(onCreateInputView())
    }

    private fun updateView() {
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val reload = intent?.getBooleanExtra(ACTION_RELOAD, false) == true
        if(reload) reload()
        return START_STICKY
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }

    override fun onEvaluateInputViewShown(): Boolean {
        return super.onEvaluateInputViewShown()
    }

    companion object {
        const val ACTION_RELOAD = "ee.oyatl.ime.make.IMEService.ACTION_RELOAD"

        fun sendReloadIntent(activity: Activity) {
            val intent = Intent(activity, IMEService::class.java)
            intent.putExtra(ACTION_RELOAD, true)
            activity.startService(intent)
        }

        fun loadPresets(context: Context): Triple<InputEnginePreset, InputEnginePreset, InputEnginePreset> {
            fun showToast(fileName: String) {
                val msg = context.getString(R.string.msg_preset_load_failed, fileName)
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }

            val latinFileName = "preset_latin.yaml"
            val hangulFileName = "preset_hangul.yaml"
            val symbolFileName = "preset_symbol.yaml"

            val latinPreset = loadPreset(context, latinFileName, "preset/preset_latin_qwerty.yaml")
                ?: InputEnginePreset().apply { showToast(latinFileName) }
            val hangulPreset = loadPreset(context, hangulFileName, "preset/preset_3set_390.yaml")
                ?: InputEnginePreset().apply { showToast(hangulFileName) }
            val symbolPreset = loadPreset(context, symbolFileName, "preset/preset_symbol_g.yaml")
                ?: InputEnginePreset().apply { showToast(symbolFileName) }
            return Triple(latinPreset, hangulPreset, symbolPreset)
        }

        private fun loadPreset(context: Context, fileName: String, defaultFilename: String): InputEnginePreset? {
            val fromFilesDir = kotlin.runCatching {
                InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(File(context.filesDir, fileName).inputStream())
            }
            if(fromFilesDir.isSuccess) return fromFilesDir.getOrNull()
            val fromAssets = kotlin.runCatching {
                InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(context.assets.open(fileName))
            }
            if(fromAssets.isSuccess) return fromAssets.getOrNull()
            val defaultFromAssets = kotlin.runCatching {
                InputEnginePreset.yaml.decodeFromStream<InputEnginePreset>(context.assets.open(defaultFilename))
            }
            if(defaultFromAssets.isSuccess) return defaultFromAssets.getOrNull()
            return null
        }
    }
}