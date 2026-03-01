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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
import org.solovyev.android.calculator.ui.Res
import org.solovyev.android.calculator.ui.StandardTopAppBar

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
    var searchQuery by remember { mutableStateOf("") }
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
                title = "Formulas",
                onBack = onBack,
                actions = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text(
                                "✕",
                                style = MaterialTheme.typography.titleMedium
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
                    shadowElevation = if (isSearchFocused) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { 
                                Text(
                                    "🔍 Search formulas...",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = searchQuery.isNotEmpty(),
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text(
                                            "✕",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isSearchFocused = it.isFocused },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
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
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "${filtered.size} result${if (filtered.size != 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                enter = fadeIn() + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ),
                exit = fadeOut() + scaleOut(targetScale = 0.9f),
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
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Formula ready",
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
                    delay(1500)
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
        contentPadding = PaddingValues(vertical = 12.dp)
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
    val categoryEmoji = getCategoryEmoji(category)
    val categoryColor = getCategoryColor(category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category icon
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = categoryColor.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = categoryEmoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

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
    val categoryColor = getCategoryColor(formula.category)
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    // Entrance animation
    LaunchedEffect(Unit) {
        delay(index * 40L)
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
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .graphicsLayer {
                this.scaleX = scale * pressScale
                this.scaleY = scale * pressScale
                this.alpha = alpha
            }
            .shadow(
                elevation = if (isPressed) 2.dp else 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = categoryColor.copy(alpha = 0.1f),
                spotColor = categoryColor.copy(alpha = 0.15f)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    isPressed = true
                    onClick()
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category icon with gradient background
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
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

                Spacer(modifier = Modifier.width(16.dp))

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

                    Spacer(modifier = Modifier.height(2.dp))

                    // Description
                    Text(
                        text = formula.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Formula expression chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = formula.expression,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Arrow indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Reset press state
    LaunchedEffect(isPressed) {
        if (isPressed) {
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Surface(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (searchQuery.isEmpty()) "📐" else "🔍",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = if (searchQuery.isEmpty()) "No formulas available" else "No formulas found",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isEmpty()) {
                "Formulas will appear here when available"
            } else {
                "Try adjusting your search terms"
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
                        Text(
                            text = getCategoryEmoji(formula.category),
                            style = MaterialTheme.typography.titleLarge
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
                        "Formula",
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
                Text(
                    text = "✏️",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Input Values",
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
                Text(
                    text = "🔢",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Calculate",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // Result section
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = fadeOut() + shrinkVertically()
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
                                    Text(
                                        text = "✓",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Result",
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
                                Text("Close")
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
                                Text("📋")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Use Result")
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
            Text("${variable.symbol} — ${variable.name}")
        },
        supportingText = if (isError) {
            { 
                Text(
                    "Please enter a valid number", 
                    color = MaterialTheme.colorScheme.error
                ) 
            }
        } else {
            {
                Text(
                    "Default: ${variable.defaultValue}",
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
// CATEGORY HELPERS - Emojis and colors for formula categories
// =============================================================================

private fun getCategoryEmoji(category: FormulaCategory): String {
    return when (category) {
        FormulaCategory.PHYSICS -> "⚛️"
        FormulaCategory.MATHEMATICS -> "📐"
        FormulaCategory.FINANCE -> "💰"
        FormulaCategory.ENGINEERING -> "🔧"
        FormulaCategory.HEALTH -> "❤️"
        FormulaCategory.EVERYDAY -> "💡"
        FormulaCategory.CUSTOM -> "✏️"
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

private fun getCategoryDescription(category: FormulaCategory): String {
    return when (category) {
        FormulaCategory.PHYSICS -> "Physical laws and equations"
        FormulaCategory.MATHEMATICS -> "Algebra, geometry, and calculus"
        FormulaCategory.FINANCE -> "Loans, interest, and investments"
        FormulaCategory.ENGINEERING -> "Circuit analysis and structural"
        FormulaCategory.HEALTH -> "BMI, BMR, and fitness"
        FormulaCategory.EVERYDAY -> "Conversions and daily utilities"
        FormulaCategory.CUSTOM -> "Your saved custom formulas"
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
    "Error" 
}
