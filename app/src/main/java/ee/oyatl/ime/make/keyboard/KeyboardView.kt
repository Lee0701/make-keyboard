package ee.oyatl.ime.make.keyboard

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val handler = Handler(Looper.getMainLooper())

@Composable
fun Keyboard(
    config: KeyboardConfig,
    onKeyEvent: (KeyEvent) -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseOnSurface,
        ),
    ) {
        Column(
            Modifier.padding(8.dp)
        ) {
            config.rows.forEach { row -> KeyRow(
                configs = row.keys,
                spacingLeft = row.spacingLeft,
                spacingRight = row.spacingRight,
                onKeyEvent = onKeyEvent,
            ) }
            BottomRow(
                bottomRowConfig = config.bottomRow,
                onKeyEvent = onKeyEvent,
            )
        }
    }
}

@Composable
fun KeyRow(configs: List<KeyConfig>, spacingLeft: Float, spacingRight: Float, onKeyEvent: (KeyEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val modifier = Modifier
        if(spacingLeft > 0) KeySpacer(
            modifier = modifier
                .weight(spacingLeft)
        )
        configs.forEach { config -> Key(
            config = config,
            onKeyEvent = onKeyEvent,
            modifier = modifier
                .weight(config.width)
        ) }
        if(spacingRight > 0) KeySpacer(
            modifier = modifier
                .weight(spacingRight)
        )
    }
}

@Composable
fun BottomRow(bottomRowConfig: BottomRowConfig, onKeyEvent: (KeyEvent) -> Unit) {
    Row(
        modifier = Modifier
    ) {
        val modifier = Modifier
        val spaceKey = KeyConfig(KeyOutput.Special.Space, KeyLabel.None, width = bottomRowConfig.spaceWidth)
        val keys = bottomRowConfig.leftKeys + listOf(spaceKey) + bottomRowConfig.rightKeys
        keys.forEach { config -> Key(
            config = config,
            onKeyEvent = onKeyEvent,
            modifier = modifier
                .weight(config.width)
                .height(bottomRowConfig.height.dp)
        ) }
    }
}

@Composable
fun Key(config: KeyConfig, modifier: Modifier, onKeyEvent: (KeyEvent) -> Unit) {
    val containerColor = when(config.type) {
        KeyConfig.Type.Alphanumeric -> MaterialTheme.colorScheme.surface
        KeyConfig.Type.Space -> MaterialTheme.colorScheme.surface
        KeyConfig.Type.Modifier -> MaterialTheme.colorScheme.secondaryContainer
        KeyConfig.Type.Symbol -> MaterialTheme.colorScheme.primaryContainer
        KeyConfig.Type.Return -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when(config.type) {
        KeyConfig.Type.Return -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    var popupControl by remember { mutableStateOf(false) }
    var popupParams by remember(config) {
        mutableStateOf(PopupParams())
    }
    KeyPreviewPopup(popupControl, popupParams)
    Button(
        onClick = { },
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier
            .onGloballyPositioned {
                val rect = it.boundsInParent()
                val width = rect.width * 1.4f
                val height = rect.height * 2f
                val x = rect.center.x
                val y = rect.bottom
                val position = x to y
                val size = width to height
                popupParams = PopupParams(position, size, config)
            }
            .pressAndRelease(config) {
                onKeyEvent(it)
                if(it.output is KeyOutput.Text) {
                    if(it.action == KeyEvent.Action.Press) popupControl = true
                    else if(it.action == KeyEvent.Action.Release) handler.postDelayed({
                        popupControl = false
                    }, 160)
                }
            }
            .height(config.height.dp)
            .padding(2.dp, 4.dp)
    ) {
        when(config.label) {
            is KeyLabel.Text -> Text(
                text = config.label.text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
            )
            is KeyLabel.Icon -> config.label.icon()
            else -> Unit
        }
    }
}

@Composable
fun KeySpacer(modifier: Modifier) {
    Button(
        onClick = { },
        modifier = modifier
            .alpha(0f)
    ) {
    }
}

fun Modifier.pressAndRelease(config: KeyConfig, onKeyEvent: (KeyEvent) -> Unit): Modifier {
    return this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            // Press
            onKeyEvent(KeyEvent(KeyEvent.Action.Press, config.output))
            do {
                val event = awaitPointerEvent()
            } while(event.changes.any { it.pressed })
            // Release
            onKeyEvent(KeyEvent(KeyEvent.Action.Release, config.output))
        }
    }
}
