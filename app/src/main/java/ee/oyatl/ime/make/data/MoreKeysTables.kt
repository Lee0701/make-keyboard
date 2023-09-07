package ee.oyatl.ime.make.data

import ee.oyatl.ime.make.model.KeyboardLayout
import ee.oyatl.ime.make.table.MoreKeysTable

object MoreKeysTables {
    val MOREKEYS_MOBILE_NUMBERS = MoreKeysTable(
        'q'.code to KeyboardLayout("1"),
        'w'.code to KeyboardLayout("2"),
        'e'.code to KeyboardLayout("3"),
        'r'.code to KeyboardLayout("4"),
        't'.code to KeyboardLayout("5"),
        'y'.code to KeyboardLayout("6"),
        'u'.code to KeyboardLayout("7"),
        'i'.code to KeyboardLayout("8"),
        'o'.code to KeyboardLayout("9"),
        'p'.code to KeyboardLayout("0"),
    )

    val MOREKEYS_KOREAN_M_R = MoreKeysTable(
        'e'.code to KeyboardLayout("ë"),
        'o'.code to KeyboardLayout("ŏ"),
        'u'.code to KeyboardLayout("ŭ"),
        'E'.code to KeyboardLayout("Ë"),
        'O'.code to KeyboardLayout("Ŏ"),
        'U'.code to KeyboardLayout("Ŭ"),
    )
}