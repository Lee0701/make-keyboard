package ee.oyatl.ime.make.data

import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.model.KeyboardLayout
import ee.oyatl.ime.make.model.Row

object MoreKeysTables {
    val MORE_KEYS_M_R_E = KeyboardLayout(
        Row.ofOutputs("ë"),
    )
    val MORE_KEYS_M_R_O = KeyboardLayout(
        Row.ofOutputs("ŏ"),
    )
    val MORE_KEYS_M_R_U = KeyboardLayout(
        Row.ofOutputs("ŭ"),
    )

    val MORE_KEYS_M_R_CAP_E = KeyboardLayout(
        Row.ofOutputs("Ë"),
    )
    val MORE_KEYS_M_R_CAP_O = KeyboardLayout(
        Row.ofOutputs("Ŏ"),
    )
    val MORE_KEYS_M_R_CAP_U = KeyboardLayout(
        Row.ofOutputs("Ŭ"),
    )

    val MORE_KEYS_TABLE_M_R = MoreKeysTable(
        'e'.code to MORE_KEYS_M_R_E,
        'o'.code to MORE_KEYS_M_R_O,
        'u'.code to MORE_KEYS_M_R_U,
        'E'.code to MORE_KEYS_M_R_CAP_E,
        'O'.code to MORE_KEYS_M_R_CAP_O,
        'U'.code to MORE_KEYS_M_R_CAP_U,
    )
}