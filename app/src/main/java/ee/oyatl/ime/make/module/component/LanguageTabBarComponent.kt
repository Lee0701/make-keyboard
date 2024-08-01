package ee.oyatl.ime.make.module.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.StringRes
import ee.oyatl.ime.make.databinding.ComponentLanguageTabbarBinding
import ee.oyatl.ime.make.databinding.ComponentLanguageTabbarTabBinding

class LanguageTabBarComponent(
    var tabs: List<Tab> = listOf(),
    var listener: Listener? = null
): InputViewComponent {
    private var inflater: LayoutInflater? = null
    private var view: ComponentLanguageTabbarBinding? = null

    override fun initView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        val view = ComponentLanguageTabbarBinding.inflate(inflater, null, false)
        view.voiceBtn.setOnClickListener { listener?.onVoiceButtonClick() }
        this.view = view
        this.inflater = inflater
        return view.root
    }

    override fun updateView() {
        val view = view ?: return
        val inflater = this.inflater ?: return
        tabs = listener?.onUpdateLanguageTabs().orEmpty()
        view.tabs.removeAllViews()
        tabs.forEach { (index, label, selected) ->
            val tab = ComponentLanguageTabbarTabBinding.inflate(inflater, null, false)
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
        val selected: Boolean
    )
}