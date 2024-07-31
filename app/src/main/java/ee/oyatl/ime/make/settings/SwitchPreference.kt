package ee.oyatl.ime.make.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.switchmaterial.SwitchMaterial
import ee.oyatl.ime.make.R

class SwitchPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {

    private var valueSet: Boolean = false

    var isChecked: Boolean
        get() = getPersistedBoolean(false)
        set(v) {
            persistBoolean(v)
            notifyChanged()
        }

    init {
        layoutResource = R.layout.preference_inline
        widgetLayoutResource = R.layout.pref_widget_frame
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val widgetView = holder.itemView.findViewById<LinearLayoutCompat>(android.R.id.widget_frame)
        if(widgetView is ViewGroup) {
            widgetView.removeAllViews()
            val switch = LayoutInflater.from(context).inflate(R.layout.pref_switch_widget_content, null, false) as SwitchMaterial
            try {
                switch.isEnabled = this.isEnabled
                switch.isChecked = getPersistedBoolean(false)
                switch.setOnCheckedChangeListener { _, value ->
                    this.isChecked = value
                    onPreferenceChangeListener?.onPreferenceChange(this, value)
                }
            } catch(ex: IllegalStateException) {
                ex.printStackTrace()
            }
            widgetView.addView(switch)
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getBoolean(index, false)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        setInitialValue(getPersistedBoolean(defaultValue as? Boolean ?: false))
    }

    private fun setInitialValue(value: Boolean) {
        // Always persist/notify the first time.
        val changed = this.isChecked != value
        if(changed || !this.valueSet) {
            this.valueSet = true
            this.isChecked = value
        }
    }
}