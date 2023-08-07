package ee.oyatl.ime.make

import android.inputmethodservice.InputMethodService
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import ee.oyatl.ime.make.keyboard.KeyEvent
import ee.oyatl.ime.make.keyboard.KeyIcons
import ee.oyatl.ime.make.keyboard.KeyLabel
import ee.oyatl.ime.make.keyboard.KeyOutput
import ee.oyatl.ime.make.keyboard.Keyboard
import ee.oyatl.ime.make.keyboard.KeyboardConfig
import ee.oyatl.ime.make.modifier.DefaultShiftKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyHandler
import ee.oyatl.ime.make.modifier.ModifierKeyState

class IMEService: InputMethodService() {
    private val handler: Handler = Handler(Looper.getMainLooper())

    private var inputView: View? = null
    private val inputViewLifecycleOwner = InputViewLifecycleOwner()

    private val shiftHandler: ModifierKeyHandler = DefaultShiftKeyHandler(500)

    private val initialKeyboardConfig = KeyboardConfigs.defaultDvorak()

    override fun onCreate() {
        super.onCreate()
        inputViewLifecycleOwner.onCreate()
    }

    override fun onCreateInputView(): View {
        inputViewLifecycleOwner.attachToDecorView(window?.window?.decorView)
        return ComposeView(this).apply {
            setContent {
                InputView(initialKeyboardConfig)
            }
            this@IMEService.inputView = this
        }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        inputViewLifecycleOwner.onResume()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        inputViewLifecycleOwner.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        inputViewLifecycleOwner.onDestroy()
    }

    private fun onKeyEvent(event: KeyEvent) {
        val inputConnection = currentInputConnection ?: return
        when(event.action) {
            KeyEvent.Action.Press -> {
                when(event.output) {
                    is KeyOutput.Special -> onSpecialKeyPress(event.output)
                    is KeyOutput.Text -> {
                        val output = if(shiftHandler.state.active) event.output.text.uppercase()
                        else event.output.text.lowercase()
                        inputConnection.commitText(output, 1)
                        shiftHandler.autoUnlock()
                        shiftHandler.onInput()
                    }
                    else -> Unit
                }
                performKeyFeedback(event.output)
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
    }

    private fun onSpecialKeyPress(output: KeyOutput.Special) {
        val inputConnection = currentInputConnection ?: return
        when(output) {
            is KeyOutput.Special.Delete -> {
                inputConnection.deleteSurroundingText(1, 0)
                fun repeat() {
                    this.onKeyEvent(KeyEvent(KeyEvent.Action.Repeat, output))
                    handler.postDelayed({ repeat() }, 50)
                }
                handler.postDelayed({ repeat() }, 500)
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

    @Composable
    private fun InputView(keyboardConfig: KeyboardConfig) {
        val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        val darkTheme = isSystemInDarkTheme()
        val colorScheme = when {
            dynamicColor && darkTheme -> dynamicDarkColorScheme(this)
            dynamicColor && !darkTheme -> dynamicLightColorScheme(this)
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        }
        val shapes = Shapes(
            extraSmall = RoundedCornerShape(4.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp),
            large = RoundedCornerShape(16.dp),
            extraLarge = RoundedCornerShape(24.dp),
        )

        var shiftState by remember { mutableStateOf(ModifierKeyState()) }
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = shapes,
        ) {
            Keyboard(
                config = keyboardConfig.mapTextLabels {
                    if(shiftState.active) it.uppercase()
                    else it.lowercase()
                }.map { key ->
                      if(key.output is KeyOutput.Special.Shift) {
                          key.copy(label = KeyLabel.Icon { KeyIcons.Shift(shiftState) })
                      } else {
                          key
                      }
                },
                onKeyEvent = {
                    this.onKeyEvent(it)
                    shiftState = this.shiftHandler.state
                },
            )
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

    override fun onComputeInsets(outInsets: Insets?) {
        super.onComputeInsets(outInsets)
        outInsets?.contentTopInsets = outInsets?.visibleTopInsets
    }

    override fun onEvaluateFullscreenMode(): Boolean {
        super.onEvaluateFullscreenMode()
        return false
    }
}