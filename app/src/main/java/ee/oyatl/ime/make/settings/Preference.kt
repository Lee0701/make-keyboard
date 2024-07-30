package ee.oyatl.ime.make.settings

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.make.R

class Preference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
}