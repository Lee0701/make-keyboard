package ee.oyatl.ime.make.settings

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import androidx.preference.Preference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import ee.oyatl.ime.make.R

class OssLicensesMenuPreference(
    context: Context,
    atts: AttributeSet?,
): Preference(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }
    override fun onClick() {
        context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        super.onClick()
    }
}