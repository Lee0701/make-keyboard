package ee.oyatl.ime.make.keyboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    if(visible) {
        Popup(
            offset = IntOffset((x - width/2).toInt(), (y - height).toInt()),
            alignment = Alignment.TopStart,
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
    val position: Pair<Float, Float> = 0f to 0f,
    val size: Pair<Float, Float> = 0f to 0f,
    val config: KeyConfig = KeyConfig(KeyOutput.None),
)
