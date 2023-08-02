package ee.oyatl.ime.make.keyboard

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ee.oyatl.ime.make.R
import ee.oyatl.ime.make.modifier.ModifierKeyState

object KeyIcons {

    @Composable
    fun Shift(state: ModifierKeyState) = when {
        state.locked -> ShiftLocked()
        state.active -> ShiftPressed()
        else -> ShiftReleased()
    }

    @Composable
    fun ShiftReleased() = Icon(
        painter = painterResource(id = R.drawable.keyic_shift),
        contentDescription = stringResource(id = R.string.key_desc_shift)
    )

    @Composable
    fun ShiftPressed() = Icon(
        painter = painterResource(id = R.drawable.keyic_shift_pressed),
        contentDescription = stringResource(id = R.string.key_desc_shift)
    )

    @Composable
    fun ShiftLocked() = Icon(
        painter = painterResource(id = R.drawable.keyic_shift_locked),
        contentDescription = stringResource(id = R.string.key_desc_shift_locked)
    )

    @Composable
    fun Delete() = Icon(
        painter = painterResource(id = R.drawable.keyic_delete),
        contentDescription = stringResource(id = R.string.key_desc_delete)
    )

    @Composable
    fun Return() = Icon(
        painter = painterResource(id = R.drawable.keyic_return),
        contentDescription = stringResource(id = R.string.key_desc_return)
    )

    @Composable
    fun OfOutput(output: String) = when(output) {
        "<<SHIFT>>" -> Shift(ModifierKeyState())
        "<<SHIFT:PRESSED>>" -> ShiftPressed()
        "<<SHIFT:LOCKED>>" -> ShiftLocked()
        "<<DELETE>>" -> Delete()
        "<<RETURN>>" -> Return()
        else -> Unit
    }
}