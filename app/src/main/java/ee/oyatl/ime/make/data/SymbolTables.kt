package ee.oyatl.ime.make.data

import android.view.KeyEvent
import ee.oyatl.ime.make.table.MoreKeysTable
import ee.oyatl.ime.make.table.SimpleCodeConvertTable
import ee.oyatl.ime.make.table.SimpleCodeConvertTable.Entry
import ee.oyatl.ime.make.model.KeyboardLayout
import ee.oyatl.ime.make.model.Row

object SymbolTables {

    val LAYOUT_SYMBOLS_G = SimpleCodeConvertTable(
        KeyEvent.KEYCODE_Q to Entry('1'.code, '~'.code),
        KeyEvent.KEYCODE_W to Entry('2'.code, '`'.code),
        KeyEvent.KEYCODE_E to Entry('3'.code, '|'.code),
        KeyEvent.KEYCODE_R to Entry('4'.code, '•'.code),
        KeyEvent.KEYCODE_T to Entry('5'.code, '√'.code),
        KeyEvent.KEYCODE_Y to Entry('6'.code, 'π'.code),
        KeyEvent.KEYCODE_U to Entry('7'.code, '÷'.code),
        KeyEvent.KEYCODE_I to Entry('8'.code, '×'.code),
        KeyEvent.KEYCODE_O to Entry('9'.code, '¶'.code),
        KeyEvent.KEYCODE_P to Entry('0'.code, '∆'.code),

        KeyEvent.KEYCODE_A to Entry('@'.code, '€'.code),
        KeyEvent.KEYCODE_S to Entry('#'.code, '¥'.code),
        KeyEvent.KEYCODE_D to Entry('£'.code, '$'.code),
        KeyEvent.KEYCODE_F to Entry('_'.code, '¢'.code),
        KeyEvent.KEYCODE_G to Entry('&'.code, '^'.code),
        KeyEvent.KEYCODE_H to Entry('-'.code, '°'.code),
        KeyEvent.KEYCODE_J to Entry('+'.code, '='.code),
        KeyEvent.KEYCODE_K to Entry('('.code, '{'.code),
        KeyEvent.KEYCODE_L to Entry(')'.code, '}'.code),
        KeyEvent.KEYCODE_SEMICOLON to Entry('/'.code, '\\'.code),

        KeyEvent.KEYCODE_Z to Entry('*'.code, '%'.code),
        KeyEvent.KEYCODE_X to Entry('"'.code, '©'.code),
        KeyEvent.KEYCODE_C to Entry('\''.code, '®'.code),
        KeyEvent.KEYCODE_V to Entry(':'.code, '™'.code),
        KeyEvent.KEYCODE_B to Entry(';'.code, '✓'.code),
        KeyEvent.KEYCODE_N to Entry('!'.code, '['.code),
        KeyEvent.KEYCODE_M to Entry('?'.code, ']'.code),
    )

    val MORE_KEYS_1 = KeyboardLayout(
        Row.ofOutputs("⅙⅐⅛⅑⅒"),
        Row.ofOutputs("¹½⅓¼⅕"),
    )

    val MORE_KEYS_2 = KeyboardLayout(
        Row.ofOutputs("²⅔⅖"),
    )

    val MORE_KEYS_3 = KeyboardLayout(
        Row.ofOutputs("⅗³¾⅜"),
    )

    val MORE_KEYS_4 = KeyboardLayout(
        Row.ofOutputs("⁴⅘"),
    )

    val MORE_KEYS_5 = KeyboardLayout(
        Row.ofOutputs("⅝⁵⅚"),
    )

    val MORE_KEYS_6 = KeyboardLayout(
        Row.ofOutputs("⁶"),
    )

    val MORE_KEYS_7 = KeyboardLayout(
        Row.ofOutputs("⁷⅞"),
    )

    val MORE_KEYS_8 = KeyboardLayout(
        Row.ofOutputs("⁸"),
    )

    val MORE_KEYS_9 = KeyboardLayout(
        Row.ofOutputs("⁹"),
    )

    val MORE_KEYS_0 = KeyboardLayout(
        Row.ofOutputs("∅ⁿ⁰"),
    )

    val MORE_KEYS_APOSTROPHE = KeyboardLayout(
        Row.ofOutputs("‚‘’‹›"),
    )

    val MORE_KEYS_ASTERISK = KeyboardLayout(
        Row.ofOutputs("★†‡"),
    )

    val MORE_KEYS_BRACE_LEFT = KeyboardLayout(
        Row.ofOutputs("[<{"),
    )

    val MORE_KEYS_BRACE_RIGHT = KeyboardLayout(
        Row.ofOutputs("]}>"),
    )

    val MORE_KEYS_BULLET = KeyboardLayout(
        Row.ofOutputs("♣♠♪♥♦"),
    )

    val MORE_KEYS_CARET = KeyboardLayout(
        Row.ofOutputs("←↓↑→"),
    )

    val MORE_KEYS_DEGREE = KeyboardLayout(
        Row.ofOutputs("′″"),
    )

    val MORE_KEYS_EQUALS = KeyboardLayout(
        Row.ofOutputs("∞≠≈"),
    )

    val MORE_KEYS_EXCLAMATION = KeyboardLayout(
        Row.ofOutputs("¡"),
    )

    val MORE_KEYS_HYPHEN = KeyboardLayout(
        Row.ofOutputs("—_–·"),
    )

    val MORE_KEYS_NUMBER = KeyboardLayout(
        Row.ofOutputs("№"),
    )

    val MORE_KEYS_PERCENT = KeyboardLayout(
        Row.ofOutputs("‰℅"),
    )

    val MORE_KEYS_PI = KeyboardLayout(
        Row.ofOutputs("ΩΠμ"),
    )

    val MORE_KEYS_PILCROW = KeyboardLayout(
        Row.ofOutputs("§"),
    )

    val MORE_KEYS_PLUS = KeyboardLayout(
        Row.ofOutputs("±"),
    )

    val MORE_KEYS_POUND = KeyboardLayout(
        Row.ofOutputs("₹¥₱"),
        Row.ofOutputs("€¢\$")
    )

    val MORE_KEYS_QUESTION = KeyboardLayout(
        Row.ofOutputs("¿‽"),
    )

    val MORE_KEYS_QUOTE = KeyboardLayout(
        Row.ofOutputs("„“”«»"),
    )

    val MORE_KEYS_TABLE_SYMBOLS_G = MoreKeysTable(
        0x31 to MORE_KEYS_1,
        0x32 to MORE_KEYS_2,
        0x33 to MORE_KEYS_3,
        0x34 to MORE_KEYS_4,
        0x35 to MORE_KEYS_5,
        0x36 to MORE_KEYS_6,
        0x37 to MORE_KEYS_7,
        0x38 to MORE_KEYS_8,
        0x39 to MORE_KEYS_9,
        0x30 to MORE_KEYS_0,
        0x27 to MORE_KEYS_APOSTROPHE,
        0x2a to MORE_KEYS_ASTERISK,
        0x2022 to MORE_KEYS_BULLET,
        0x28 to MORE_KEYS_BRACE_LEFT,
        0x29 to MORE_KEYS_BRACE_RIGHT,
        0x5e to MORE_KEYS_CARET,
        0x80 to MORE_KEYS_DEGREE,
        0x3d to MORE_KEYS_EQUALS,
        0x21 to MORE_KEYS_EXCLAMATION,
        0x2d to MORE_KEYS_HYPHEN,
        0x23 to MORE_KEYS_NUMBER,
        0x25 to MORE_KEYS_PERCENT,
        0x03c0 to MORE_KEYS_PI,
        0xb6 to MORE_KEYS_PILCROW,
        0x2b to MORE_KEYS_PLUS,
        0xa3 to MORE_KEYS_POUND,
        0x3f to MORE_KEYS_QUESTION,
        0x22 to MORE_KEYS_QUOTE,
    )

}