package ee.oyatl.ime.make.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.PreferenceCategory
import ee.oyatl.ime.make.R

class PreferenceCategory(
    context: Context,
    attrs: AttributeSet?,
): PreferenceCategory(context, attrs) {
    init {
        layoutResource = R.layout.preference_category
    }
}