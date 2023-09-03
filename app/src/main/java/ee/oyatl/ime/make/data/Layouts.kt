package ee.oyatl.ime.make.data

import android.view.KeyEvent
import ee.oyatl.ime.make.table.SimpleCodeConvertTable

object Layouts {

    val CONVERT_QWERTY = SimpleCodeConvertTable(
        (KeyEvent.KEYCODE_A .. KeyEvent.KEYCODE_Z).associateWith { code ->
            val ordinal = code - KeyEvent.KEYCODE_A
            SimpleCodeConvertTable.Entry(
                ordinal + 'a'.code,
                ordinal + 'A'.code,
            )
        }
    )

}