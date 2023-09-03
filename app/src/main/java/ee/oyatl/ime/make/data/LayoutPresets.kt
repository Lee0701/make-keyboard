package ee.oyatl.ime.make.data

import ee.oyatl.ime.make.data.SoftKeyboardLayouts.LAYOUT_QWERTY_MOBILE

object LayoutPresets {
    val PRESET_MOBILE = mapOf(
        "qwerty" to LAYOUT_QWERTY_MOBILE,
        "dvorak" to SoftKeyboardLayouts.LAYOUT_DVORAK_MOBILE,
    )
}