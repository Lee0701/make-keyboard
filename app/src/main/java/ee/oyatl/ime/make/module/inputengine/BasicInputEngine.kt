package ee.oyatl.ime.make.module.inputengine

import android.content.Context
import android.view.KeyCharacterMap
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.widget.LinearLayoutCompat
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet
import ee.oyatl.ime.make.module.component.InputViewComponent

abstract class BasicInputEngine(
    override var components: List<InputViewComponent> = listOf(),
    override val keyCharacterMap: KeyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
): InputEngine {
    private var view: View? = null

    override fun initView(context: Context): View? {
        val componentViews = components.map { it.initView(context) }
        view = LinearLayoutCompat(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayoutCompat.VERTICAL
            componentViews.forEach { addView(it) }
        }
        return view
    }

    override fun getView(): View? {
        return view
    }

    override fun updateView() {
        components.forEach { component -> component.updateView() }
    }

    override fun onResetComponents() {
        components.forEach { it.reset() }
    }

    override fun onKey(code: Int, modifiers: ModifierKeyStateSet) {
        val char = keyCharacterMap.get(code, modifiers.asMetaState())
        if(char > 0) listener.onCommitText(char.toChar().toString())
    }
}