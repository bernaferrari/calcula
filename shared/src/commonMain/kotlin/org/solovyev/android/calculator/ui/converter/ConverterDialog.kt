package org.solovyev.android.calculator.ui.converter

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import org.solovyev.android.calculator.converter.*
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.tokens.CalculatorCornerRadius
import org.solovyev.android.calculator.ui.tokens.CalculatorElevation
import org.solovyev.android.calculator.ui.tokens.CalculatorPadding
import org.solovyev.android.calculator.ui.tokens.CalculatorSpacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterDialog(
    onDismissRequest: () -> Unit,
    viewModel: ConverterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.saveLastUsed()
            onDismissRequest()
        },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        ConverterSheetContent(
            state = state,
            onDimensionSelected = viewModel::onDimensionSelected,
            onFromUnitSelected = viewModel::onFromUnitSelected,
            onToUnitSelected = viewModel::onToUnitSelected,
            onInputChanged = viewModel::onInputChanged,
            onInputCommitted = viewModel::onInputCommitted,
            onSwap = viewModel::onSwap,
            onCopy = {
                viewModel.getTextToCopy()?.let { text ->
                    clipboardManager.setText(AnnotatedString(text))
                }
                viewModel.saveLastUsed()
                onDismissRequest()
            },
            onUse = {
                viewModel.onUse()
                viewModel.saveLastUsed()
                onDismissRequest()
            },
            onCancel = {
                viewModel.saveLastUsed()
                onDismissRequest()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConverterSheetContent(
    state: ConverterUiState,
    onDimensionSelected: (Int) -> Unit,
    onFromUnitSelected: (Int) -> Unit,
    onToUnitSelected: (Int) -> Unit,
    onInputChanged: (String) -> Unit,
    onInputCommitted: () -> Unit,
    onSwap: () -> Unit,
    onCopy: () -> Unit,
    onUse: () -> Unit,
    onCancel: () -> Unit
) {
    var swapRotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = swapRotation,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "swap_rotation"
    )
    
    val listState = rememberLazyListState()
    
    LaunchedEffect(state.selectedDimensionIndex) {
        listState.animateScrollToItem(
            index = state.selectedDimensionIndex.coerceAtLeast(0),
            scrollOffset = -50
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalculatorPadding.Large)
            .padding(bottom = CalculatorPadding.XLarge),
        verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = stringResource(Res.string.cpp_back)
                )
            }
            Spacer(modifier = Modifier.size(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp, CalculatorPadding.XLarge)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    )
            )
            Spacer(modifier = Modifier.size(CalculatorSpacing.Medium))
            Text(
                text = stringResource(Res.string.c_conversion_tool),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(state.dimensions) { index, dimension ->
                val isSelected = index == state.selectedDimensionIndex
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.95f,
                    animationSpec = spring(dampingRatio = 0.7f),
                    label = "chip_scale"
                )
                
                FilterChip(
                    selected = isSelected,
                    onClick = { onDimensionSelected(index) },
                    label = {
                        Text(
                            text = getDimensionName(dimension),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.scale(scale),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    border = if (isSelected) null else FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = false,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = CalculatorElevation.Subtle
        ) {
            Column(
                modifier = Modifier.padding(CalculatorPadding.Standard),
                verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.XSmall)
            ) {
                ConversionRow(
                    label = stringResource(Res.string.cpp_from),
                    inputContent = {
                        OutlinedTextField(
                            value = state.input,
                            onValueChange = onInputChanged,
                            isError = state.inputError,
                            singleLine = true,
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (state.isNumeralBase) KeyboardType.Ascii else KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onInputCommitted() }),
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focus ->
                                    if (!focus.isFocused) onInputCommitted()
                                },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            )
                        )
                    },
                    unitContent = {
                        CompactUnitDropdown(
                            options = state.unitsFrom.map { getConvertibleName(it) },
                            selectedIndex = state.selectedFromIndex,
                            onSelected = onFromUnitSelected
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CalculatorPadding.Small),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        onClick = {
                            swapRotation += 180f
                            onSwap()
                        },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.rotate(animatedRotation)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ScreenRotation,
                                contentDescription = stringResource(Res.string.cpp_swap),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                ConversionRow(
                    label = stringResource(Res.string.cpp_to),
                    inputContent = {
                        AnimatedContent(
                            targetState = state.output.ifEmpty { "-" },
                            transitionSpec = {
                                (fadeIn(tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(200)))
                                    .togetherWith(fadeOut(tween(150)) + scaleOut(targetScale = 0.92f, animationSpec = tween(150)))
                            },
                            label = "result_animation",
                            modifier = Modifier.weight(1f)
                        ) { result ->
                            Text(
                                text = result,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = if (state.output.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    unitContent = {
                        CompactUnitDropdown(
                            options = state.unitsTo.map { getConvertibleName(it) },
                            selectedIndex = state.selectedToIndex,
                            onSelected = onToUnitSelected
                        )
                    },
                    isResult = true
                )
            }
        }

        Spacer(modifier = Modifier.height(CalculatorSpacing.XSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(text = stringResource(Res.string.cpp_back))
            }

            Spacer(modifier = Modifier.weight(1f))

            FilledTonalButton(
                onClick = onCopy,
                enabled = state.output.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.TextFields,
                    contentDescription = stringResource(Res.string.c_copy_result)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(text = stringResource(Res.string.c_copy_result))
            }

            Button(
                onClick = onUse,
                enabled = state.output.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = stringResource(Res.string.c_use)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text(text = stringResource(Res.string.c_use))
            }
        }
    }
}

@Composable
private fun getDimensionName(dimension: ConvertibleDimension): String {
    return when (dimension) {
        is UnitDimension -> stringResource(dimension.nameRes)
        is NumeralBaseDimension -> stringResource(Res.string.cpp_radix)
        else -> dimension.toString()
    }
}

@Composable
private fun getConvertibleName(convertible: Convertible): String {
    return when (convertible) {
        is ConverterUnit -> {
            val nameRes = convertible.nameRes
            val name = if (nameRes != null) stringResource(nameRes) else null
            if (name != null && name != convertible.symbol) "$name (${convertible.symbol})" else convertible.symbol
        }
        is NumeralBaseConvertible -> convertible.base.name
        else -> convertible.toString()
    }
}

@Composable
private fun ConversionRow(
    label: String,
    inputContent: @Composable () -> Unit,
    unitContent: @Composable () -> Unit,
    isResult: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1.2f)) {
                inputContent()
            }
            Box(modifier = Modifier.weight(0.8f)) {
                unitContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactUnitDropdown(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = options.getOrElse(selectedIndex) { "" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(CalculatorCornerRadius.Large),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        horizontal = CalculatorPadding.Medium,
                        vertical = CalculatorPadding.Medium
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = CalculatorElevation.Hero
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = index == selectedIndex
                DropdownMenuItem(
                    modifier = Modifier.heightIn(min = 50.dp),
                    text = { 
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded = false
                        onSelected(index)
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
