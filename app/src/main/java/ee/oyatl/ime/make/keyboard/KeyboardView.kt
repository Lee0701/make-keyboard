package ee.oyatl.ime.make.keyboard

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        if(spacingLeft > 0) KeySpacer(
            modifier = Modifier
                .weight(spacingLeft)
                .pressAndRelease(configs.firstOrNull() ?: return@Row, onKeyEvent)
        )
        configs.forEach { config -> Key(
            config = config,
            onKeyEvent = onKeyEvent,
            modifier = Modifier
                .weight(config.width)
        ) }
        if(spacingRight > 0) KeySpacer(
            modifier = Modifier
                .weight(spacingRight)
                .pressAndRelease(configs.lastOrNull() ?: return@Row, onKeyEvent)
        )
    }
}

@Composable
fun BottomRow(bottomRowConfig: BottomRowConfig, onKeyEvent: (KeyEvent) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        bottomRowConfig.leftKeys.forEach { config -> Key(
            config = config,
            onKeyEvent = onKeyEvent,
            modifier = Modifier
                .weight(config.width)
                .height(bottomRowConfig.height.dp)
        ) }
        Key(
            config = KeyConfig(" ", KeyLabel.None, width = 4f),
            onKeyEvent = onKeyEvent,
            modifier = Modifier
                .weight(bottomRowConfig.spaceWidth)
                .height(bottomRowConfig.height.dp)
        )
        bottomRowConfig.rightKeys.forEach { config -> Key(
            config = config,
            onKeyEvent = onKeyEvent,
            modifier = Modifier
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
        else -> Color.Transparent
    }
    val contentColor = when(config.type) {
        KeyConfig.Type.Return -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Button(
        onClick = { },
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier
            .pressAndRelease(config, onKeyEvent)
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
