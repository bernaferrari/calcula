package org.solovyev.android.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jscl.NumeralBase
import kotlin.text.uppercaseChar

internal data class ProgrammerAction(
    val label: String,
    val action: String
)

internal fun baseInputHint(base: NumeralBase): String = when (base) {
    NumeralBase.bin -> "Only 0-1 digits"
    NumeralBase.oct -> "Only 0-7 digits"
    NumeralBase.dec -> "0-9 and decimal point"
    NumeralBase.hex -> "0-9 and A-F digits"
}

internal fun isDigitAllowedForBase(input: String, base: NumeralBase): Boolean {
    if (input.isEmpty()) return false
    if (input == ".") return base == NumeralBase.dec
    val acceptable = base.getAcceptableCharacters()
    return input.all { ch ->
        if (ch == '.') {
            base == NumeralBase.dec
        } else {
            acceptable.contains(ch.uppercaseChar())
        }
    }
}

@Composable
internal fun ProgrammerBaseSelector(
    selectedBase: NumeralBase,
    onBaseSelected: (NumeralBase) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProgrammerBaseChip(
            label = "DEC",
            selected = selectedBase == NumeralBase.dec,
            onClick = { onBaseSelected(NumeralBase.dec) }
        )
        ProgrammerBaseChip(
            label = "HEX",
            selected = selectedBase == NumeralBase.hex,
            onClick = { onBaseSelected(NumeralBase.hex) }
        )
        ProgrammerBaseChip(
            label = "OCT",
            selected = selectedBase == NumeralBase.oct,
            onClick = { onBaseSelected(NumeralBase.oct) }
        )
        ProgrammerBaseChip(
            label = "BIN",
            selected = selectedBase == NumeralBase.bin,
            onClick = { onBaseSelected(NumeralBase.bin) }
        )
    }
}

@Composable
internal fun ProgrammerWordConfigRow(
    wordSize: Int,
    signed: Boolean,
    onWordSizeSelected: (Int) -> Unit,
    onSignedSelected: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(8, 16, 32, 64).forEach { size ->
            ProgrammerTinyChip(
                label = "${size}b",
                selected = wordSize == size,
                onClick = { onWordSizeSelected(size) }
            )
        }
        ProgrammerTinyChip(
            label = "S",
            selected = signed,
            onClick = { onSignedSelected(true) }
        )
        ProgrammerTinyChip(
            label = "U",
            selected = !signed,
            onClick = { onSignedSelected(false) }
        )
    }
}

@Composable
private fun RowScope.ProgrammerTinyChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = CalculatorFontFamily
        )
    }
}

@Composable
private fun RowScope.ProgrammerBaseChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = CalculatorFontFamily
        )
    }
}

@Composable
internal fun ProgrammerActionRow(
    actions: List<ProgrammerAction>,
    onAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach { action ->
            ProgrammerActionChip(
                label = action.label,
                onClick = { onAction(action.action) }
            )
        }
    }
}

@Composable
private fun RowScope.ProgrammerActionChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = CalculatorFontFamily
        )
    }
}

@Composable
internal fun ProgrammerHexRow(
    onDigitClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf("A", "B", "C", "D", "E", "F").forEach { digit ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .clickable { onDigitClick(digit) }
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = digit,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = CalculatorFontFamily
                )
            }
        }
    }
}

@Composable
internal fun ProgrammerInputHint(
    base: NumeralBase,
    modifier: Modifier = Modifier
) {
    val model = programmerHintUiModel(base)
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = model.primaryHint,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = CalculatorFontFamily
        )
        Text(
            text = model.secondaryHint,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = CalculatorFontFamily
        )
    }
}

internal fun Modifier.disabledAlpha(enabled: Boolean): Modifier {
    return if (enabled) this else this.alpha(0.34f)
}
