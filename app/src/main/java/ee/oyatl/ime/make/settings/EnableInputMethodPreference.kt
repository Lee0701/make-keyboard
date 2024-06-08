package ee.oyatl.ime.make.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.make.R

class EnableInputMethodPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
    override fun onClick() {
        context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }
}