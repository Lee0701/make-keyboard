package ee.oyatl.ime.make.settings.preference

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import androidx.preference.Preference
import ee.oyatl.ime.make.R

class OpenStorePreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {
    init {
        layoutResource = R.layout.preference_inline
    }

    private val uri: String? = attrs?.getAttributeValue(null, "uri")

    override fun onClick() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uri)
            setPackage("com.android.vending")
        }
        kotlin.runCatching { context.startActivity(intent) }
        super.onClick()
    }

}