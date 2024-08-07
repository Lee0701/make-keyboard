package ee.oyatl.ime.make.module.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.databinding.ComponentLanguageTabbarBinding
import ee.oyatl.ime.make.databinding.ComponentLanguageTabbarTabBinding
import ee.oyatl.ime.make.module.keyboardview.Themes
import ee.oyatl.ime.make.settings.preference.TouchInterceptingFrameLayout

class LanguageTabBarComponent(
    var tabs: List<Tab> = listOf(),
    var listener: Listener? = null,
    val width: Int = LayoutParams.MATCH_PARENT,
    private val disableTouch: Boolean = false,
): InputViewComponent {
    private var tabInflater: LayoutInflater? = null
    private var view: ComponentLanguageTabbarBinding? = null

    override fun initView(context: Context): View {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val name = preferences.getString("appearance_theme", "theme_dynamic")
        val theme = Themes.ofName(name)

        val wrappedContext = ContextThemeWrapper(context, theme.tabBarBackground)
        val inflater = LayoutInflater.from(wrappedContext)

        val view = ComponentLanguageTabbarBinding.inflate(inflater, null, false)
        val height = context.resources.getDimensionPixelSize(R.dimen.tabbar_height)
        view.root.layoutParams = LayoutParams(width, height)
        view.voiceBtn.setOnClickListener { listener?.onVoiceButtonClick() }
        this.view = view

        tabInflater = LayoutInflater.from(DynamicColors.wrapContextIfAvailable(context, theme.tabBackground))

        val wrapper = TouchInterceptingFrameLayout(context, null, disableTouch)
        wrapper.addView(view.root)
        return wrapper
    }

    override fun updateView() {
        val view = view ?: return
        val tabInflater = tabInflater ?: return
        tabs = listener?.onUpdateLanguageTabs() ?: tabs
        view.tabs.removeAllViews()
        tabs.forEach { (index, label, selected) ->
            val tab = ComponentLanguageTabbarTabBinding.inflate(tabInflater, null, false)
            tab.root.layoutParams = LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
            )
            tab.root.isSelected = selected
            tab.label.setText(label)
            tab.root.setOnClickListener {
                listener?.onLanguageTabClick(index)
            }
            view.tabs.addView(tab.root)
        }
    }

    override fun reset() {
        updateView()
    }

    interface Listener {
        fun onUpdateLanguageTabs(): List<Tab>
        fun onVoiceButtonClick()
        fun onLanguageTabClick(index: Int)
    }

    data class Tab(
        val index: Int,
        @StringRes val label: Int,
        val selected: Boolean = false
    )
}