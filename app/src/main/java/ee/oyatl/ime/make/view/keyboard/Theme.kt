package ee.oyatl.ime.make.view.keyboard

import ee.oyatl.ime.make.model.KeyIconType
import ee.oyatl.ime.make.model.KeyType

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
)