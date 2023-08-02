package ee.oyatl.ime.make.keyboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Keyboard(
    config: KeyboardConfig,
    onKeyClick: (String) -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            Modifier.padding(8.dp)
        ) {
            config.rows.forEach { row -> KeyRow(
                configs = row.keys,
                spacingLeft = row.spacingLeft,
                spacingRight = row.spacingRight,
                onKeyClick = onKeyClick,
            ) }
            BottomRow(
                left = listOf(),
                right = listOf(),
                onKeyClick = onKeyClick,
            )
        }
    }
}

@Composable
fun KeyRow(configs: List<KeyConfig>, spacingLeft: Float, spacingRight: Float, onKeyClick: (String) -> Unit) {
    return Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if(spacingLeft > 0) KeySpacer(
            onClick = { onKeyClick(configs.firstOrNull()?.output ?: "") },
            modifier = Modifier
                .weight(spacingLeft)
        )
        configs.forEach { config -> Key(
            config = config,
            onClick = onKeyClick,
            modifier = Modifier
                .weight(config.width)
        ) }
        if(spacingRight > 0) KeySpacer(
            onClick = { onKeyClick(configs.lastOrNull()?.output ?: "") },
            modifier = Modifier
                .weight(spacingRight)
        )
    }
}

@Composable
fun BottomRow(left: List<KeyConfig>, right: List<KeyConfig>, onKeyClick: (String) -> Unit) {
    return Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        left.forEach { config -> Key(
            config = config,
            onClick = onKeyClick,
            modifier = Modifier
                .clickable { onKeyClick(config.output) }
                .weight(config.width)
        ) }
        Key(
            config = KeyConfig(" ", KeyLabel.None, width = 4f),
            onClick = onKeyClick,
            modifier = Modifier
                .weight(4f)
        )
        right.forEach { config -> Key(
            config = config,
            onClick = onKeyClick,
            modifier = Modifier
                .clickable { onKeyClick(config.output) }
                .weight(config.width)
        ) }
    }
}

@Composable
fun Key(modifier: Modifier, config: KeyConfig, onClick: (String) -> Unit) {
    val containerColor = when(config.type) {
        KeyConfig.Type.Alphanumeric -> MaterialTheme.colorScheme.surface
        KeyConfig.Type.Modifier -> MaterialTheme.colorScheme.primaryContainer
        KeyConfig.Type.Space -> MaterialTheme.colorScheme.surface
        else -> Color.Transparent
    }
    val contentColor = MaterialTheme.colorScheme.onSurface
    return Button(
        onClick = { onClick(config.output) },
        contentPadding = PaddingValues(0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        modifier = modifier
            .height(config.height.dp)
            .noRippleClickable() { onClick(config.output) }
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
fun KeySpacer(modifier: Modifier, onClick: () -> Unit) {
    return Button(
        onClick = onClick,
        modifier = modifier
            .alpha(0f)
    ) {
    }
}

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}
