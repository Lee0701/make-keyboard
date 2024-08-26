package ee.oyatl.ime.make.preset

import kotlinx.serialization.Serializable

@Serializable
data class SoftKeyboardSelector(
    val mobile: String? = null,
    val tablet: String? = null,
    val full: String? = null,
    val television: String? = null,
)