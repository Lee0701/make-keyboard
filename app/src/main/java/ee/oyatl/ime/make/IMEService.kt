package ee.oyatl.ime.make

import android.inputmethodservice.InputMethodService
import android.view.View

class IMEService: InputMethodService() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        return super.onCreateInputView()
    }

    override fun onDestroy() {
        super.onDestroy()
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