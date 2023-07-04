package ee.oyatl.ime.make

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp

@Composable
fun Keyboard(layout: KeyboardConfig, onClick: (String) -> Unit) {
    Card {
        Column(
            Modifier.padding(8.dp)
        ) {
            layout.rows.forEachIndexed { i, row -> KeyRow(
                configs = row.keys,
                spacingLeft = row.spacingLeft,
                spacingRight = row.spacingRight,
                onClick = onClick,
            ) }
            BottomRow(
                left = listOf(),
                right = listOf(),
                onClick = onClick
            )
        }
    }
}

@Composable
fun KeyRow(configs: List<KeyConfig>, spacingLeft: Float, spacingRight: Float, onClick: (String) -> Unit) {
    return Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        if(spacingLeft > 0) KeySpacer(
            modifier = Modifier
                .weight(spacingLeft)
        )
        configs.forEach { config -> Key(
            keyTop = config.keyTop,
            output = config.output,
            onClick = { onClick(config.output) },
            modifier = Modifier
                .weight(config.width)
        ) }
        if(spacingRight > 0) KeySpacer(
            modifier = Modifier
                .weight(spacingRight)
        )
    }
}

@Composable
fun BottomRow(left: List<KeyConfig>, right: List<KeyConfig>, onClick: (String) -> Unit) {
    return Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        left.forEach { config -> Key(
            keyTop = config.keyTop,
            output = config.output,
            onClick = { onClick(config.output) },
            modifier = Modifier
                .weight(config.width)
        ) }
        Key(
            keyTop = "",
            output = " ",
            onClick = { onClick(" ") },
            modifier = Modifier
                .weight(4f)
        )
        right.forEach { config -> Key(
            keyTop = config.keyTop,
            output = config.output,
            onClick = { onClick(config.output) },
            modifier = Modifier
                .weight(config.width)
        ) }
    }
}

@Composable
fun Key(modifier: Modifier, keyTop: String, output: String, onClick: () -> Unit) {
    return Button(
        onClick = onClick,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .height(60.dp)
            .padding(2.dp, 4.dp)
    ) {
        Text(text = keyTop)
    }
}

@Composable
fun KeySpacer(modifier: Modifier) {
    return Button(
        onClick = { /*TODO*/ },
        modifier = modifier
            .alpha(0f)
    ) {
    }
}
