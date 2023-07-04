package ee.oyatl.ime.make

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView

class IMEService: InputMethodService() {
    private val inputViewLifecycleOwner = InputViewLifecycleOwner()

    private val layout: KeyboardConfig = KeyboardConfig(
        "QWERTYUIOP".toRowConfig(),
        "ASDFGHJKL".toRowConfig(0.5f, 0.5f),
        "ZXCVBNM".toRowConfig(1.5f) + KeyConfig("\b", "DEL", width = 1.5f),
    )

    override fun onCreate() {
        super.onCreate()
        inputViewLifecycleOwner.onCreate()
    }

    override fun onCreateInputView(): View {
        inputViewLifecycleOwner.attachToDecorView(window?.window?.decorView)
        return ComposeView(this).apply {
            setContent {
                MaterialTheme {
                    Keyboard(
                        layout = layout,
                        onClick = { onKeyClick(it) },
                    )
                }
            }
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

    private fun onKeyClick(output: String) {
        val inputConnection = currentInputConnection ?: return
        when(output) {
            "\b" -> {
                inputConnection.deleteSurroundingText(1, 0)
            }
            else -> {
                inputConnection.commitText(output, 1)
            }
        }
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