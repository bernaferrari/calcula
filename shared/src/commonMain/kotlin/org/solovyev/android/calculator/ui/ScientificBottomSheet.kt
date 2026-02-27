@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package org.solovyev.android.calculator.ui

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
    val pageTitles = listOf("Trig", "Power", "More")

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(Res.string.cpp_scientific_functions),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth()
            ) {
                pageTitles.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = pageTitles.size)
                    ) {
                        Text(title)
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 420.dp)
            ) { page ->
                when (page) {
                    0 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScientificGroup(title = "Trigonometry") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ScientificButton(
                                    text = "asin",
                                    style = ScientificButtonStyle.Secondary,
                                    onClick = { onFunctionClick("asin") }
                                )
                                ScientificButton(
                                    text = "acos",
                                    style = ScientificButtonStyle.Secondary,
                                    onClick = { onFunctionClick("acos") }
                                )
                                ScientificButton(
                                    text = "atan",
                                    style = ScientificButtonStyle.Secondary,
                                    onClick = { onFunctionClick("atan") }
                                )
                            }
                        }
                    }

                    1 -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScientificGroup(title = "Power & Logarithms") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

                    else -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScientificGroup(title = "Constants") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                ScientificButton(
                                    text = "π",
                                    style = ScientificButtonStyle.Primary,
                                    onClick = { onConstantClick("π") }
                                )
                                ScientificButton(
                                    text = "e",
                                    style = ScientificButtonStyle.Primary,
                                    onClick = { onConstantClick("e") }
                                )
                                ScientificButton(
                                    text = "i",
                                    style = ScientificButtonStyle.Primary,
                                    onClick = { onConstantClick("i") }
                                )
                            }
                        }

                        ScientificGroup(title = "Variables") {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            }
        }
    }
}

@Composable
private fun ScientificGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                content()
            }
        )
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
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
