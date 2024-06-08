package ee.oyatl.ime.make.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat
import ee.oyatl.ime.make.R

class SwitchPreference(
    context: Context,
    attrs: AttributeSet?,
): SwitchPreferenceCompat(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }

}