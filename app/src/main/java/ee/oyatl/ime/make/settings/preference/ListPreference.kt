package ee.oyatl.ime.make.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import ee.oyatl.ime.make.R

class ListPreference(
    context: Context,
    attrs: AttributeSet?,
): ListPreference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
}