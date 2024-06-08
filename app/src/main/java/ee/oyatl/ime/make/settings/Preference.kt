package ee.oyatl.ime.make.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.make.R

class Preference(
    context: Context,
    atts: AttributeSet?,
): Preference(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }
}