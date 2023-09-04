package ee.oyatl.ime.make.table

import ee.oyatl.ime.make.modifier.ModifierKeyStateSet

class SimpleCodeConvertTable(
    val map: Map<Int, Entry> = mapOf(),
): CodeConvertTable {

    constructor(vararg entries: Pair<Int, Entry>): this(entries.toMap())

    private val reversedMap: Map<Pair<Int, EntryKey>, Int> = map.flatMap { (key, value) ->
        value.explode().map { (entryKey, charCode) -> (charCode to entryKey) to key }
    }.toMap()

    override fun getAllForState(state: ModifierKeyStateSet): Map<Int, Int> {
        return map.map { (k, v) -> v.withKeyboardState(state)?.let { k to it } }
            .filterNotNull()
            .toMap()
    }

    override fun getReversed(charCode: Int, state: ModifierKeyStateSet): Int? {
        return reversedMap[charCode to EntryKey.fromModifierKeyState(state)]
    }

    override operator fun get(keyCode: Int, state: ModifierKeyStateSet): Int? {
        return map[keyCode]?.withKeyboardState(state)
    }

    override operator fun plus(table: CodeConvertTable): CodeConvertTable {
        return when(table) {
            is SimpleCodeConvertTable -> this + table
            is LayeredCodeConvertTable -> this + table
        }
    }

    operator fun plus(table: SimpleCodeConvertTable): SimpleCodeConvertTable {
        return SimpleCodeConvertTable(map = this.map + table.map)
    }

    operator fun plus(table: LayeredCodeConvertTable): LayeredCodeConvertTable {
        return LayeredCodeConvertTable(table.layers.mapValues { (_, table) ->
            this + table
        })
    }

    data class Entry(
        val base: Int? = null,
        val shift: Int? = base,
        val capsLock: Int? = shift,
        val alt: Int? = base,
        val altShift: Int? = shift,
    ) {
        fun withKeyboardState(keyboardState: ModifierKeyStateSet): Int? {
            val shiftPressed = keyboardState.shift.pressed || keyboardState.shift.active
            val altPressed = keyboardState.alt.pressed || keyboardState.alt.active
            return if(keyboardState.shift.locked) capsLock
            else if(shiftPressed && altPressed) altShift
            else if(shiftPressed) shift
            else if(altPressed) alt
            else base
        }
        fun forKey(key: EntryKey): Int? {
            return when(key) {
                EntryKey.Base -> base
                EntryKey.Shift -> shift ?: base
                EntryKey.CapsLock -> capsLock ?: shift ?: base
                EntryKey.Alt -> alt ?: base
                EntryKey.AltShift -> altShift ?: alt
            }
        }
        fun explode(): Map<EntryKey, Int> {
            return listOfNotNull(
                base?.let { EntryKey.Base to it },
                shift?.let { EntryKey.Shift to it },
                capsLock?.let { EntryKey.CapsLock to it },
                alt?.let { EntryKey.Alt to it },
                altShift?.let { EntryKey.AltShift to it },
            ).toMap()
        }
    }

    enum class EntryKey {
        Base, Shift, CapsLock, Alt, AltShift;
        companion object {
            fun fromModifierKeyState(modifierKeyState: ModifierKeyStateSet): EntryKey {
                return if(modifierKeyState.alt.pressed && modifierKeyState.shift.pressed) AltShift
                else if(modifierKeyState.alt.pressed) Alt
                else if(modifierKeyState.shift.locked) CapsLock
                else if(modifierKeyState.shift.pressed) Shift
                else Base
            }
        }
    }
}