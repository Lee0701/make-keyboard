package ee.oyatl.ime.make.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup

@Composable
fun KeyPreviewPopup(visible: Boolean, params: PopupParams) {
    val output = params.config.output
    if(output !is KeyOutput.Text) return
    val (x, y) = params.position
    val (width, height) = params.size

    var bounds by remember { mutableStateOf(params.position) }

    Popup(
        offset = IntOffset((x - width/2).toInt(), (y - height).toInt()),
        alignment = Alignment.TopStart,
    ) {
        val enterAnim =
            slideInVertically(initialOffsetY = { it / 16 }, animationSpec = tween(80)) +
                    fadeIn(initialAlpha = 0f, animationSpec = tween(40))
        val exitAnim =
            slideOutVertically(targetOffsetY = { it / 16 }, animationSpec = tween(80)) +
                    fadeOut(targetAlpha = 0f, animationSpec = tween(40))
        AnimatedVisibility(
            visible = visible,
            enter = enterAnim,
            exit = exitAnim,
            modifier = Modifier
        ) {
            KeyPreviewPopupContent(params, output.text)
        }
    }
}

@Composable
fun KeyPreviewPopupContent(params: PopupParams, text: String) {
    val (width, height) = params.size
    with(LocalDensity.current) {
        Card(
            modifier = Modifier
                .size(width.toDp(), height.toDp())
                .onGloballyPositioned {
                    val position = it.positionInParent()
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                Text(
                    text = text,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .weight(1f)
                )
                Spacer(modifier = Modifier
                    .weight(1f)
                )
            }
        }
    }
}

data class PopupParams(
    val position: Offset = Offset.Unspecified,
    val size: Size = Size.Unspecified,
    val config: KeyConfig = KeyConfig(KeyOutput.None),
)
