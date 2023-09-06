package ee.oyatl.ime.make.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

data class Theme(
    val keyboardBackground: Int,
    val keyBackground: Map<KeyType, Int> = mapOf(),
    val keyIcon: Map<KeyIconType, Int> = mapOf(),
    val popupBackground: Int,
) {
    fun inflateIcons(context: Context): Map<KeyIconType, Drawable> {
        return keyIcon.entries
            .mapNotNull { (k, v) -> ContextCompat.getDrawable(context, v)?.let { k to it } }
            .toMap()
    }
}
