package ee.oyatl.ime.make.preset.softkeyboard

import androidx.annotation.DrawableRes
import ee.oyatl.ime.make.R
import kotlinx.serialization.Serializable

@Serializable
enum class KeyBackgroundType(
    @DrawableRes val resId: Int,
    val extendTop: Boolean = false,
    val extendBottom: Boolean = false,
) {
    Normal(R.drawable.key_bg),
    MergeUp(R.drawable.key_bg_extend_up, extendTop = true),
}