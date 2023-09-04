package ee.oyatl.ime.make.model

import android.content.Context
import android.util.TypedValue
import ee.oyatl.ime.make.data.SoftKeyboardLayouts
import ee.oyatl.ime.make.profile.KeyboardProfile
import ee.oyatl.ime.make.table.CodeConvertTable
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.view.keyboard.KeyboardListener
import ee.oyatl.ime.make.view.keyboard.StackedViewKeyboardView
import ee.oyatl.ime.make.view.keyboard.Themes

data class KeyboardProfilePreset(
    val keyboardLayout: KeyboardLayout,
    val convertTable: CodeConvertTable,
    val moreKeysTable: MoreKeysTable,
    val unifyHeight: Boolean = true,
    val rowHeight: Float = 55f,
    val doubleTapGap: Int = 500,
    val longPressDelay: Int = 500,
    val autoUnlockShift: Boolean = true,
) {
    fun inflate(context: Context, listener: KeyboardListener): KeyboardProfile {
        val rowHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.rowHeight, context.resources.displayMetrics).toInt()
        val keyboardView = StackedViewKeyboardView(
            context = context,
            attrs = null,
            listener = listener,
            keyboard = SoftKeyboardLayouts.LAYOUT_QWERTY_MOBILE,
            theme = Themes.Dynamic,
            popupOffsetY = 0,
            unifyHeight = this.unifyHeight,
            rowHeight = rowHeight
        )
        return KeyboardProfile(keyboardView, convertTable, moreKeysTable, doubleTapGap, autoUnlockShift)
    }
}
