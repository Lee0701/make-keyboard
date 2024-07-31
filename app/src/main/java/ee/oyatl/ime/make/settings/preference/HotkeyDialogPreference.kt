package ee.oyatl.ime.make.settings.preference

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.databinding.DialogSetHotkeyBinding
import ee.oyatl.ime.make.modifiers.ModifierKeyStateSet

class HotkeyDialogPreference(
    context: Context,
    attrs: AttributeSet
): Preference(context, attrs) {
    private var modifiers: Int = DEFAULT_MODIFIER
    private var keycode: Int = DEFAULT_KEYCODE

    init {
        layoutResource = R.layout.preference_inline
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val str = getPersistedString("")
        modifiers = parseModifiers(str)
        keycode = parseKeycode(str)
    }

    override fun onClick() {
        super.onClick()
        val inflater = LayoutInflater.from(context)

        val view = DialogSetHotkeyBinding.inflate(inflater)
        updateView(view)

        view.keycode.setOnKeyListener { _, keyCode, event ->
            if(keyCode in ModifierKeyStateSet.MODIFIER_KEYS) true
            else {
                this.keycode = keyCode
                this.modifiers = event.modifiers
                updateView(view)
                true
            }
        }

        setOf(view.modShift, view.modAlt, view.modControl, view.modMeta).forEach { check ->
            check.setOnCheckedChangeListener { _, _ ->
                val shift = if(view.modShift.isChecked) KeyEvent.META_SHIFT_ON else 0
                val alt = if(view.modAlt.isChecked) KeyEvent.META_ALT_ON else 0
                val control = if(view.modControl.isChecked) KeyEvent.META_CTRL_ON else 0
                val meta = if(view.modMeta.isChecked) KeyEvent.META_META_ON else 0
                this.modifiers = shift or alt or control or meta
            }
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                persistString("$modifiers,$keycode")
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .show()
        view.keycode.requestFocus()
    }

    private fun updateView(view: DialogSetHotkeyBinding) {
        view.keycode.setText(KeyEvent.keyCodeToString(keycode))
        view.modShift.isChecked = modifiers and KeyEvent.META_SHIFT_MASK != 0
        view.modAlt.isChecked = modifiers and KeyEvent.META_ALT_MASK != 0
        view.modControl.isChecked = modifiers and KeyEvent.META_CTRL_MASK != 0
        view.modMeta.isChecked = modifiers and KeyEvent.META_META_MASK != 0
    }

    companion object {
        const val DEFAULT_MODIFIER = KeyEvent.META_SHIFT_ON
        const val DEFAULT_KEYCODE = KeyEvent.KEYCODE_SPACE

        fun parseModifiers(str: String): Int {
            val tok = str.split(",").map { it.toIntOrNull() }
            if(tok.size != 2 || null in tok) return DEFAULT_MODIFIER
            return tok[0] ?: DEFAULT_MODIFIER
        }
        fun parseKeycode(str: String): Int {
            val tok = str.split(",").map { it.toIntOrNull() }
            if(tok.size != 2 || null in tok) return DEFAULT_KEYCODE
            return tok[1] ?: DEFAULT_KEYCODE
        }
    }
}