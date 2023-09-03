package ee.oyatl.ime.make.view.keyboard

import com.google.android.material.color.DynamicColors
import ee.oyatl.ime.make.model.KeyType.*
import ee.oyatl.ime.make.model.KeyIconType.*
import ee.oyatl.ime.make.R

// TODO: Replace this with xml theme
object Themes {
    val Static = Theme(
        R.style.Theme_MakeKeyboard_Keyboard,
        mapOf(
            Alphanumeric to R.style.Theme_MakeKeyboard_Keyboard_Key,
            AlphanumericAlt to R.style.Theme_MakeKeyboard_Keyboard_Key_Mod,
            Modifier to R.style.Theme_MakeKeyboard_Keyboard_Key_Mod,
            ModifierAlt to R.style.Theme_MakeKeyboard_Keyboard_Key,
            Space to R.style.Theme_MakeKeyboard_Keyboard_Key,
            Action to R.style.Theme_MakeKeyboard_Keyboard_Key_Return,
        ),
        mapOf(
            Shift to R.drawable.keyic_shift,
            ShiftLock to R.drawable.keyic_shift_lock,
            Caps to R.drawable.keyic_caps,
            Symbol to R.drawable.keyic_numbers,
            Tab to R.drawable.keyic_tab,
            Backspace to R.drawable.keyic_backspace,
            Language to R.drawable.keyic_language,
            Return to R.drawable.keyic_return,
        ),
        R.style.Theme_MakeKeyboard_Keyboard_KeyPopup,
    )

    val Dynamic = Theme(
        R.style.Theme_MakeKeyboard_Keyboard_Overlay,
        mapOf(
            Alphanumeric to R.style.Theme_MakeKeyboard_Keyboard_Key_Overlay,
            AlphanumericAlt to R.style.Theme_MakeKeyboard_Keyboard_Key_Mod_Overlay,
            Modifier to R.style.Theme_MakeKeyboard_Keyboard_Key_Mod_Overlay,
            ModifierAlt to R.style.Theme_MakeKeyboard_Keyboard_Key_Overlay,
            Space to R.style.Theme_MakeKeyboard_Keyboard_Key_Overlay,
            Action to R.style.Theme_MakeKeyboard_Keyboard_Key_Return_Overlay,
        ),
        mapOf(
            Shift to R.drawable.keyic_shift,
            ShiftLock to R.drawable.keyic_shift_lock,
            Caps to R.drawable.keyic_caps,
            Symbol to R.drawable.keyic_numbers,
            Tab to R.drawable.keyic_tab,
            Backspace to R.drawable.keyic_backspace,
            Language to R.drawable.keyic_language,
            Return to R.drawable.keyic_return,
        ),
        R.style.Theme_MakeKeyboard_Keyboard_KeyPopup_Overlay,
    )

    val map: Map<String, Theme> = mapOf(
        "theme_static" to Static,
        "theme_dynamic" to Dynamic,
    )

    fun of(id: String?): Theme {
        return (map[id] ?: Static).let {
            if(!DynamicColors.isDynamicColorAvailable() && it == Dynamic) Static
            else it
        }
    }
}