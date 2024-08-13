package ee.oyatl.ime.make.module.keyboardview

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.color.DynamicColors
import ee.oyatl.ime.make.preset.softkeyboard.KeyIconType
import ee.oyatl.ime.make.preset.softkeyboard.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
    val tabBarBackground: Int,
    val tabBackground: Int
) {
    fun wrapKeyboardBackground(context: Context): Context {
        return wrapContext(context, keyboardBackground)
    }

    fun wrapKeyBackground(context: Context, type: KeyType): Context {
        return wrapContext(context, keyBackground[type] ?: return context)
    }

    fun wrapKeyIcon(context: Context, type: KeyIconType): Context {
        return wrapContext(context, keyIcon[type] ?: return context)
    }

    fun wrapKeyPopupBackground(context: Context): Context {
        return wrapContext(context, popupBackground)
    }

    fun wrapTabBarBackground(context: Context): Context {
        return wrapContext(context, tabBarBackground)
    }

    fun wrapTabBackground(context: Context): Context {
        return wrapContext(context, tabBackground)
    }

    private fun wrapContext(context: Context, theme: Int): Context {
        return wrapContextDynamic(wrapContextStatic(context, theme), theme)
    }

    private fun wrapContextDynamic(context: Context, theme: Int): Context {
        return DynamicColors.wrapContextIfAvailable(context, theme)
    }

    private fun wrapContextStatic(context: Context, theme: Int): Context {
        return ContextThemeWrapper(context, theme)
    }
}