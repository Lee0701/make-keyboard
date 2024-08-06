package ee.oyatl.ime.make.module.keyboardview

import ee.oyatl.ime.make.preset.softkeyboard.KeyIconType
import ee.oyatl.ime.make.preset.softkeyboard.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
    val tabBackground: Int
)