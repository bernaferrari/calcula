@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package org.solovyev.android.calculator.ui

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
// Material icons not available in commonMain - using text alternatives
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun ScientificBottomSheet(
    onFunctionClick: (String) -> Unit,
    onConstantClick: (String) -> Unit,
    onDismissRequest: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "f(x)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.cpp_scientific_functions),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Tab selector
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                SegmentedButton(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = pagerState.currentPage == 0) {
                            Text("sin", modifier = Modifier.size(18.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                ) {
                    Text("Trig")
                }
                SegmentedButton(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = pagerState.currentPage == 1) {
                            Text("x²", modifier = Modifier.size(18.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                ) {
                    Text("Power")
                }
                SegmentedButton(
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = pagerState.currentPage == 2) {
                            Text("•••", modifier = Modifier.size(18.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                ) {
                    Text("More")
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 280.dp, max = 400.dp)
            ) { page ->
                when (page) {
                    0 -> TrigonometryPage(
                        onFunctionClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFunctionClick(it)
                        }
                    )
                    1 -> PowerPage(
                        onFunctionClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFunctionClick(it)
                        }
                    )
                    else -> ConstantsPage(
                        onConstantClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onConstantClick(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrigonometryPage(onFunctionClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScientificGroup(title = "Trigonometric Functions") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton(
                    text = "sin",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onFunctionClick("sin") }
                )
                ScientificButton(
                    text = "cos",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onFunctionClick("cos") }
                )
                ScientificButton(
                    text = "tan",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onFunctionClick("tan") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton(
                    text = "asin",
                    description = "arcsin",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("asin") }
                )
                ScientificButton(
                    text = "acos",
                    description = "arccos",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("acos") }
                )
                ScientificButton(
                    text = "atan",
                    description = "arctan",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("atan") }
                )
            }
        }
    }
}

@Composable
private fun PowerPage(onFunctionClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScientificGroup(title = "Power & Logarithms") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton(
                    text = "ln",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onFunctionClick("ln") }
                )
                ScientificButton(
                    text = "log",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onFunctionClick("log") }
                )
                ScientificButton(
                    text = "√",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("sqrt") }
                )
                ScientificButton(
                    text = "^",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("^") }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton(
                    text = "x²",
                    style = ScientificButtonStyle.Tertiary,
                    onClick = { onFunctionClick("^2") }
                )
                ScientificButton(
                    text = "x³",
                    style = ScientificButtonStyle.Tertiary,
                    onClick = { onFunctionClick("^3") }
                )
                ScientificButton(
                    text = "!",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("!") }
                )
                ScientificButton(
                    text = "%",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onFunctionClick("%") }
                )
            }
        }
    }
}

@Composable
private fun ConstantsPage(onConstantClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScientificGroup(title = "Mathematical Constants") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton(
                    text = "π",
                    description = "pi",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onConstantClick("π") }
                )
                ScientificButton(
                    text = "e",
                    description = "Euler's number",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onConstantClick("e") }
                )
                ScientificButton(
                    text = "i",
                    description = "imaginary",
                    style = ScientificButtonStyle.Primary,
                    onClick = { onConstantClick("i") }
                )
            }
        }

        ScientificGroup(title = "Variables") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScientificButton(
                    text = "x",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onConstantClick("x") }
                )
                ScientificButton(
                    text = "y",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onConstantClick("y") }
                )
                ScientificButton(
                    text = "t",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onConstantClick("t") }
                )
                ScientificButton(
                    text = "j",
                    style = ScientificButtonStyle.Secondary,
                    onClick = { onConstantClick("j") }
                )
            }
        }
    }
}

@Composable
private fun ScientificGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content
            )
        }
    }
}

private enum class ScientificButtonStyle {
    Primary,
    Secondary,
    Tertiary
}

@Composable
private fun RowScope.ScientificButton(
    text: String,
    icon: String? = null,
    description: String? = null,
    style: ScientificButtonStyle,
    onClick: () -> Unit
) {
    val colors = when (style) {
        ScientificButtonStyle.Primary -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
        ScientificButtonStyle.Secondary -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        ScientificButtonStyle.Tertiary -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }

    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = colors,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        if (icon != null) {
            Text(text = icon)
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
