package org.solovyev.android.calculator.ui.formulas

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.formulas.*
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.tokens.CalculatorCornerRadius
import org.solovyev.android.calculator.ui.tokens.CalculatorElevation
import org.solovyev.android.calculator.ui.tokens.CalculatorPadding
import org.solovyev.android.calculator.ui.tokens.CalculatorSpacing

// =============================================================================
// REFINED FORMULA SCREEN - Beautiful formula cards with enhanced interactions
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaScreen(
    viewModel: FormulaViewModel, 
    onUseResult: (String) -> Unit, 
    onBack: () -> Unit
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFormula by remember { mutableStateOf<Formula?>(null) }
    var isSearchFocused by remember { mutableStateOf(false) }
    val formulas by viewModel.formulas.collectAsState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val filtered = remember(formulas, searchQuery) {
        if (searchQuery.isBlank()) formulas
        else formulas.filter { 
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.displayName.contains(searchQuery, ignoreCase = true)
        }
    }
    
    val grouped = filtered.groupBy { it.category }
    
    // Copy feedback
    var copyFeedbackVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.cpp_formula_library),
                onBack = onBack,
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(Res.string.cpp_a11y_clear_search)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Beautiful search field
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = if (isSearchFocused) CalculatorElevation.Elevated else CalculatorElevation.Pressed
                ) {
                    Column(
                        modifier = Modifier.padding(CalculatorPadding.Standard)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            placeholder = { 
                                Text(
                                    stringResource(Res.string.cpp_formula_search),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = searchQuery.isNotEmpty(),
                                    enter = if (reduceMotion) fadeIn(tween(80)) else fadeIn() + scaleIn(),
                                    exit = if (reduceMotion) fadeOut(tween(80)) else fadeOut() + scaleOut()
                                ) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = stringResource(Res.string.cpp_a11y_clear_search)
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused },
                            singleLine = true,
                            shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { focusManager.clearFocus() }
                            )
                        )

                        // Results count chip
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = if (reduceMotion) fadeIn(tween(80)) else fadeIn() + slideInVertically { it / 2 },
                            exit = if (reduceMotion) fadeOut(tween(80)) else fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            Row(
                                modifier = Modifier.padding(top = CalculatorPadding.Medium),
                                horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(CalculatorCornerRadius.Medium),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = stringResource(Res.string.cpp_formula_results_count, filtered.size),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = CalculatorPadding.Medium,
                                            vertical = CalculatorPadding.Small
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Divider with subtle gradient effect
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    thickness = 1.dp
                )

                // Formula list
                if (filtered.isEmpty()) {
                    EmptyFormulaState(searchQuery)
                } else {
                    FormulaList(
                        grouped = grouped,
                        onFormulaClick = { formula ->
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            focusManager.clearFocus()
                            selectedFormula = formula
                        }
                    )
                }
            }

            // Copy feedback
            AnimatedVisibility(
                visible = copyFeedbackVisible,
                enter = if (reduceMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn() + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                },
                exit = if (reduceMotion) fadeOut(tween(80)) else fadeOut() + scaleOut(targetScale = 0.9f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.inverseSurface,
                    tonalElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                        Text(
                            text = stringResource(Res.string.cpp_formula_ready),
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    // Formula input sheet
    selectedFormula?.let { formula ->
        FormulaInputSheet(
            formula = formula,
            viewModel = viewModel,
            onDismiss = { selectedFormula = null },
            onUseResult = { result ->
                copyFeedbackVisible = true
                scope.launch {
                    delay(if (reduceMotion) 900 else 1500)
                    copyFeedbackVisible = false
                }
                onUseResult(result)
            }
        )
    }
}

// =============================================================================
// FORMULA LIST - Staggered animated list with categories
// =============================================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FormulaList(
    grouped: Map<FormulaCategory, List<Formula>>,
    onFormulaClick: (Formula) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = CalculatorPadding.Medium)
    ) {
        grouped.entries.forEachIndexed { groupIndex, (category, items) ->
            // Category header
            item(key = "header_${category.name}") {
                CategoryHeader(
                    category = category
                )
            }

            // Formula cards
            itemsIndexed(
                items = items,
                key = { _, formula -> formula.id }
            ) { index, formula ->
                FormulaCard(
                    formula = formula,
                    index = index,
                    onClick = { onFormulaClick(formula) }
                )
            }
        }
    }
}

// =============================================================================
// CATEGORY HEADER - Beautiful category section header
// =============================================================================

@Composable
private fun CategoryHeader(
    category: FormulaCategory,
    modifier: Modifier = Modifier
) {
    val categoryIcon = getCategoryIcon(category)
    val categoryColor = getCategoryColor(category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = CalculatorPadding.Large,
                vertical = CalculatorPadding.Medium
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = categoryColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor
                )
            }
        }

        Spacer(modifier = Modifier.width(CalculatorSpacing.Medium))

        Column {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = getCategoryDescription(category),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// =============================================================================
// FORMULA CARD - Beautiful tactile card design
// =============================================================================

@Composable
private fun FormulaCard(
    formula: Formula,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    val categoryColor = getCategoryColor(formula.category)
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // Entrance animation
    LaunchedEffect(Unit) {
        if (!reduceMotion) delay(index * 40L)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (reduceMotion) 1f else if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = CalculatorPadding.Standard,
                vertical = CalculatorPadding.Small
            )
            .graphicsLayer {
                this.scaleX = scale * pressScale
                this.scaleY = scale * pressScale
                this.alpha = alpha
            }
            .shadow(
                elevation = if (isPressed) CalculatorElevation.Standard else CalculatorElevation.Display,
                shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
                ambientColor = categoryColor.copy(alpha = 0.1f),
                spotColor = categoryColor.copy(alpha = 0.15f)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    if (!reduceMotion) isPressed = true
                    onClick()
                },
            shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CalculatorPadding.Standard),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon with gradient background
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                    color = categoryColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = formula.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = categoryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(CalculatorSpacing.Large))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Formula name
                    Text(
                        text = formula.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(CalculatorSpacing.XSmall))

                    // Description
                    Text(
                        text = formula.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(CalculatorSpacing.Small))

                    // Formula expression chip
                    Surface(
                        shape = RoundedCornerShape(CalculatorCornerRadius.Medium),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = formula.expression,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = CalculatorPadding.Small + 2.dp,
                                vertical = CalculatorPadding.XSmall + 1.dp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(CalculatorSpacing.Medium))

                // Arrow indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed && !reduceMotion) {
            delay(150)
            isPressed = false
        }
    }
}

// =============================================================================
// EMPTY FORMULA STATE - Beautiful illustration
// =============================================================================

@Composable
private fun EmptyFormulaState(searchQuery: String) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val pulseScale = if (reduceMotion) {
            1f
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            ).value
        }

        Surface(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (searchQuery.isEmpty()) {
                        Icons.Filled.Calculate
                    } else {
                        Icons.Filled.Search
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (searchQuery.isEmpty()) {
                stringResource(Res.string.cpp_formula_empty_title)
            } else {
                stringResource(Res.string.cpp_formula_no_results)
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isEmpty()) {
                stringResource(Res.string.cpp_formula_empty_subtitle)
            } else {
                stringResource(Res.string.cpp_formula_try_adjusting_search)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// =============================================================================
// FORMULA INPUT SHEET - Beautiful bottom sheet with form
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormulaInputSheet(
    formula: Formula,
    viewModel: FormulaViewModel,
    onDismiss: () -> Unit,
    onUseResult: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var values by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var result by remember { mutableStateOf<String?>(null) }
    val reduceMotion = LocalCalculatorReduceMotion.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { 
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        }
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = getCategoryColor(formula.category).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getCategoryIcon(formula.category),
                            contentDescription = null,
                            tint = getCategoryColor(formula.category)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        formula.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        formula.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Formula expression card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(Res.string.cpp_formula_expression_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        formula.expression,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Variables section header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.cpp_formula_input_values),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Variable input fields
            formula.variables.forEach { variable ->
                val hasError = values[variable.id]?.toDoubleOrNull() == null && 
                              values[variable.id]?.isNotEmpty() == true

                VariableInputField(
                    variable = variable,
                    value = values[variable.id] ?: variable.defaultValue,
                    onValueChange = { 
                        values = values + (variable.id to it)
                        result = null
                    },
                    isError = hasError,
                    isLast = formula.variables.last() == variable
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculate button
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    result = calculate(formula, values, viewModel)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = formula.variables.all { 
                    values[it.id]?.toDoubleOrNull() != null 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.cpp_formula_calculate),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Result section
            AnimatedVisibility(
                visible = result != null,
                enter = if (reduceMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn() + expandVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                },
                exit = if (reduceMotion) fadeOut(tween(80)) else fadeOut() + shrinkVertically()
            ) {
                result?.let { res ->
                    Column {
                        Spacer(modifier = Modifier.height(24.dp))

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        stringResource(Res.string.cpp_formula_result),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    res,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(stringResource(Res.string.cpp_a11y_close))
                            }
                            Button(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                    onUseResult(res)
                                    scope.launch {
                                        sheetState.hide()
                                        onDismiss()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TextFields,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(Res.string.c_use))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// =============================================================================
// VARIABLE INPUT FIELD - Beautiful input for formula variables
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VariableInputField(
    variable: FormulaVariable,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    isLast: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text("${variable.symbol} - ${variable.name}")
        },
        supportingText = if (isError) {
            { 
                Text(
                    stringResource(Res.string.c_value_is_not_a_number),
                    color = MaterialTheme.colorScheme.error
                ) 
            }
        } else {
            {
                Text(
                    stringResource(Res.string.cpp_default_value_format, variable.defaultValue),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (isLast) ImeAction.Done else ImeAction.Next
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        leadingIcon = {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        variable.symbol,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    )
}

// =============================================================================
// CATEGORY HELPERS - Icons and colors for formula categories
// =============================================================================

private fun getCategoryIcon(category: FormulaCategory): ImageVector {
    return when (category) {
        FormulaCategory.PHYSICS -> Icons.Filled.Speed
        FormulaCategory.MATHEMATICS -> Icons.Filled.Calculate
        FormulaCategory.FINANCE -> Icons.Filled.Star
        FormulaCategory.ENGINEERING -> Icons.Filled.Tune
        FormulaCategory.HEALTH -> Icons.Filled.Info
        FormulaCategory.EVERYDAY -> Icons.Filled.FlashOn
        FormulaCategory.CUSTOM -> Icons.Filled.Edit
    }
}

private fun getCategoryColor(category: FormulaCategory): Color {
    return when (category) {
        FormulaCategory.PHYSICS -> Color(0xFF2196F3)      // Blue
        FormulaCategory.MATHEMATICS -> Color(0xFF9C27B0)  // Purple
        FormulaCategory.FINANCE -> Color(0xFFFF9800)     // Orange
        FormulaCategory.ENGINEERING -> Color(0xFF607D8B) // Blue Gray
        FormulaCategory.HEALTH -> Color(0xFFF44336)      // Red
        FormulaCategory.EVERYDAY -> Color(0xFF00BCD4)    // Cyan
        FormulaCategory.CUSTOM -> Color(0xFF795548)      // Brown
    }
}

@Composable
private fun getCategoryDescription(category: FormulaCategory): String {
    return when (category) {
        FormulaCategory.PHYSICS -> stringResource(Res.string.cpp_formula_category_physics_desc)
        FormulaCategory.MATHEMATICS -> stringResource(Res.string.cpp_formula_category_math_desc)
        FormulaCategory.FINANCE -> stringResource(Res.string.cpp_formula_category_finance_desc)
        FormulaCategory.ENGINEERING -> stringResource(Res.string.cpp_formula_category_engineering_desc)
        FormulaCategory.HEALTH -> stringResource(Res.string.cpp_formula_category_health_desc)
        FormulaCategory.EVERYDAY -> stringResource(Res.string.cpp_formula_category_everyday_desc)
        FormulaCategory.CUSTOM -> stringResource(Res.string.cpp_formula_category_custom_desc)
    }
}

private fun calculate(
    formula: Formula, 
    values: Map<String, String>, 
    viewModel: FormulaViewModel
): String? = try {
    var expr = formula.expression
    formula.variables.forEach { 
        expr = expr.replace(it.id, values[it.id] ?: it.defaultValue) 
    }
    viewModel.evaluate(expr)
} catch (e: Exception) { 
    null
}
