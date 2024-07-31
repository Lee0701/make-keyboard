package ee.oyatl.ime.make.settings.preference

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.make.BuildConfig
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.service.ImportExportActivity

class StartActivityPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }
    override fun onClick() {
        if(BuildConfig.DEBUG) {
            val intent = Intent(context, ImportExportActivity::class.java)
            context.startActivity(intent)
        }
    }
}