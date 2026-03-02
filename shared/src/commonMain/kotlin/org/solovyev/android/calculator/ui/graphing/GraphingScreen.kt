package org.solovyev.android.calculator.ui.graphing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.tokens.CalculatorCornerRadius
import org.solovyev.android.calculator.ui.tokens.CalculatorElevation
import org.solovyev.android.calculator.ui.tokens.CalculatorPadding
import org.solovyev.android.calculator.ui.tokens.CalculatorSpacing
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Interactive 2D Graphing Screen for Calculator++
 * 
 * Features:
 * - Function plotting (expressions with x)
 * - Pinch-to-zoom
 * - Pan/drag navigation
 * - Grid and axis rendering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphingScreen(
    initialExpression: String = "",
    onBack: () -> Unit,
    viewModel: GraphViewModel = koinViewModel()
) {
    val graphState by viewModel.graphState.collectAsState()
    val curves by viewModel.curves.collectAsState()
    val error by viewModel.error.collectAsState()
    val quickExpressions = remember {
        listOf("sin(x)", "cos(x)", "x^2", "x^3-x", "ln(x)")
    }

    var inputExpression by remember { mutableStateOf(initialExpression) }
    var editingCurveId by remember { mutableStateOf<Long?>(null) }
    var showRangeDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val onPlot: (String?) -> Unit = onPlot@{ overrideExpression ->
        val expressionToPlot = (overrideExpression ?: inputExpression).trim()
        if (expressionToPlot.isEmpty()) return@onPlot

        val targetCurveId = if (overrideExpression == null) editingCurveId else null
        val upsertedCurveId = viewModel.upsertFunction(
            expression = expressionToPlot,
            curveId = targetCurveId
        )
        if (upsertedCurveId != null) {
            if (targetCurveId == upsertedCurveId) {
                editingCurveId = null
            }
            inputExpression = ""
        }
        focusManager.clearFocus()
    }

    LaunchedEffect(initialExpression) {
        if (initialExpression.isNotBlank()) {
            viewModel.setExpression(initialExpression)
            inputExpression = ""
        }
    }

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.cpp_plotter),
                onBack = onBack,
                actions = {
                    FilledTonalIconButton(onClick = { showRangeDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = stringResource(Res.string.cpp_graph_set_range)
                        )
                    }
                    FilledTonalIconButton(onClick = { viewModel.resetView() }) {
                        Icon(
                            imageVector = Icons.Filled.ScreenRotation,
                            contentDescription = stringResource(Res.string.cpp_graph_reset_view)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Expression input
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CalculatorPadding.Standard),
                shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = CalculatorElevation.Standard
            ) {
                Column(
                    modifier = Modifier.padding(CalculatorPadding.Medium),
                    verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium)
                ) {
                    OutlinedTextField(
                        value = inputExpression,
                        onValueChange = { inputExpression = it },
                        placeholder = { Text(stringResource(Res.string.cpp_graph_input_placeholder)) },
                        prefix = {
                            Text(
                                text = stringResource(Res.string.cpp_graph_expression_prefix),
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onPlot(null) }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(CalculatorCornerRadius.Large)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium)
                    ) {
                        FilledTonalButton(
                            onClick = { onPlot(null) },
                            enabled = inputExpression.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                stringResource(
                                    if (editingCurveId == null) {
                                        Res.string.cpp_graph_add_function
                                    } else {
                                        Res.string.cpp_graph_update_function
                                    }
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.resetView() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(Res.string.cpp_graph_reset_view))
                        }
                    }

                    Text(
                        text = stringResource(Res.string.cpp_graph_quick_examples),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickExpressions) { example ->
                            AssistChip(
                                onClick = {
                                    onPlot(example)
                                },
                                label = { Text(example) }
                            )
                        }
                    }

                    Text(
                        text = stringResource(Res.string.cpp_graph_functions),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (curves.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(CalculatorCornerRadius.Standard),
                            color = MaterialTheme.colorScheme.surfaceContainerLowest
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = CalculatorPadding.Medium,
                                    vertical = CalculatorPadding.Small
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Functions,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(Res.string.cpp_graph_no_functions),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small)) {
                            curves.forEach { curve ->
                                Surface(
                                    shape = RoundedCornerShape(CalculatorCornerRadius.Standard),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Column(modifier = Modifier.padding(CalculatorPadding.Medium)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = Color(curve.color),
                                                modifier = Modifier.size(10.dp)
                                            ) {}
                                            Text(
                                                text = curve.expression,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = if (curve.enabled) {
                                                    MaterialTheme.colorScheme.onSurface
                                                } else {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                }
                                            )
                                            IconButton(
                                                onClick = {
                                                    editingCurveId = curve.id
                                                    inputExpression = curve.expression
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Edit,
                                                    contentDescription = stringResource(
                                                        Res.string.cpp_graph_edit_function_a11y,
                                                        curve.expression
                                                    )
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.toggleFunction(curve.id, !curve.enabled)
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (curve.enabled) {
                                                        Icons.Filled.Visibility
                                                    } else {
                                                        Icons.Filled.VisibilityOff
                                                    },
                                                    contentDescription = stringResource(
                                                        if (curve.enabled) {
                                                            Res.string.cpp_graph_hide_function_a11y
                                                        } else {
                                                            Res.string.cpp_graph_show_function_a11y
                                                        },
                                                        curve.expression
                                                    )
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.removeFunction(curve.id)
                                                    if (editingCurveId == curve.id) {
                                                        editingCurveId = null
                                                        inputExpression = ""
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.DeleteOutline,
                                                    contentDescription = stringResource(
                                                        Res.string.cpp_graph_delete_function_a11y,
                                                        curve.expression
                                                    )
                                                )
                                            }
                                        }
                                        curve.error?.let { curveError ->
                                            Text(
                                                text = curveError,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.clearFunctions()
                                editingCurveId = null
                                inputExpression = ""
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(Res.string.cpp_graph_clear_all))
                        }
                    }
                }
            }

            // Error message
            error?.let { errorMsg ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CalculatorPadding.Standard),
                    shape = RoundedCornerShape(CalculatorCornerRadius.Standard),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                ) {
                    Text(
                        text = errorMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(
                            horizontal = CalculatorPadding.Medium,
                            vertical = CalculatorPadding.Small
                        )
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = CalculatorPadding.Standard,
                        vertical = CalculatorPadding.Medium
                    ),
                horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small)
            ) {
                GraphMetricChip(
                    label = stringResource(Res.string.cpp_graph_x_range),
                    value = formatRange(graphState.xMin, graphState.xMax),
                    modifier = Modifier.weight(1f)
                )
                GraphMetricChip(
                    label = stringResource(Res.string.cpp_graph_y_range),
                    value = formatRange(graphState.yMin, graphState.yMax),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Graph canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = CalculatorPadding.Standard)
                    .padding(bottom = CalculatorPadding.Standard)
                    .clip(RoundedCornerShape(CalculatorCornerRadius.Large))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                GraphCanvas(
                    state = graphState,
                    curves = curves,
                    onPan = { dx, dy -> viewModel.pan(dx, dy) },
                    onZoom = { scale, fx, fy -> viewModel.zoom(scale, fx, fy) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    if (showRangeDialog) {
        GraphRangeDialog(
            state = graphState,
            onDismiss = { showRangeDialog = false },
            onApply = { xMin, xMax, yMin, yMax ->
                val updated = viewModel.setBounds(
                    xMin = xMin,
                    xMax = xMax,
                    yMin = yMin,
                    yMax = yMax
                )
                updated
            }
        )
    }
}

@Composable
private fun GraphCanvas(
    state: GraphState,
    curves: List<GraphCurve>,
    onPan: (Float, Float) -> Unit,
    onZoom: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (zoom != 1f) {
                        onZoom(zoom, centroid.x / size.width, centroid.y / size.height)
                    }
                    if (pan != Offset.Zero) {
                        onPan(pan.x / size.width, pan.y / size.height)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        
        val xRange = state.xMax - state.xMin
        val yRange = state.yMax - state.yMin
        
        fun toScreenX(x: Double): Float = ((x - state.xMin) / xRange * width).toFloat()
        fun toScreenY(y: Double): Float = ((state.yMax - y) / yRange * height).toFloat()
        
        // Draw grid
        val xGridStep = calculateGridStep(xRange)
        var x = kotlin.math.floor(state.xMin / xGridStep) * xGridStep
        while (x <= state.xMax) {
            val screenX = toScreenX(x)
            drawLine(
                color = gridColor,
                start = Offset(screenX, 0f),
                end = Offset(screenX, height),
                strokeWidth = 1f
            )
            x += xGridStep
        }
        
        val yGridStep = calculateGridStep(yRange)
        var y = kotlin.math.floor(state.yMin / yGridStep) * yGridStep
        while (y <= state.yMax) {
            val screenY = toScreenY(y)
            drawLine(
                color = gridColor,
                start = Offset(0f, screenY),
                end = Offset(width, screenY),
                strokeWidth = 1f
            )
            y += yGridStep
        }
        
        // Draw axes
        if (state.xMin <= 0 && state.xMax >= 0) {
            val axisX = toScreenX(0.0)
            drawLine(
                color = axisColor,
                start = Offset(axisX, 0f),
                end = Offset(axisX, height),
                strokeWidth = 2f
            )
        }
        if (state.yMin <= 0 && state.yMax >= 0) {
            val axisY = toScreenY(0.0)
            drawLine(
                color = axisColor,
                start = Offset(0f, axisY),
                end = Offset(width, axisY),
                strokeWidth = 2f
            )
        }
        
        curves.forEach { curve ->
            if (!curve.enabled) return@forEach

            curve.segments.forEach { segment ->
                if (segment.points.size < 2) return@forEach

                val path = Path()
                segment.points.forEachIndexed { index, point ->
                    val screenX = toScreenX(point.x.toDouble())
                    val screenY = toScreenY(point.y.toDouble())

                    if (index == 0) {
                        path.moveTo(screenX, screenY)
                    } else {
                        path.lineTo(screenX, screenY)
                    }
                }

                drawPath(
                    path = path,
                    color = Color(curve.color),
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
    }
}

private fun calculateGridStep(range: Double): Double {
    if (!range.isFinite() || range <= 0.0) {
        return 1.0
    }
    val roughStep = range / 10
    val magnitude = kotlin.math.floor(kotlin.math.log10(roughStep))
    val base = 10.0
    val power = magnitude.toInt()
    val powValue = if (power >= 0) {
        var result = 1.0
        repeat(power) { result *= base }
        result
    } else {
        var result = 1.0
        repeat(-power) { result *= base }
        1.0 / result
    }
    val normalizedStep = roughStep / powValue
    
    val step = when {
        normalizedStep < 1.5 -> 1.0
        normalizedStep < 3.0 -> 2.0
        normalizedStep < 7.0 -> 5.0
        else -> 10.0
    }
    
    return step * powValue
}

@Composable
private fun GraphMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(CalculatorCornerRadius.Standard),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = CalculatorPadding.Medium,
                vertical = CalculatorPadding.Small
            ),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatRange(min: Double, max: Double): String {
    val minAbs = abs(min)
    val maxAbs = abs(max)
    val decimals = when {
        maxAbs > 999 || minAbs > 999 -> 0
        maxAbs > 99 || minAbs > 99 -> 1
        else -> 2
    }
    return "${formatDecimal(min, decimals)}..${formatDecimal(max, decimals)}"
}

private fun formatDecimal(value: Double, decimals: Int): String {
    if (decimals <= 0) {
        return value.roundToInt().toString()
    }

    val scale = 10.0.pow(decimals)
    val rounded = (value * scale).roundToInt() / scale
    return rounded.toString()
}

@Composable
private fun GraphRangeDialog(
    state: GraphState,
    onDismiss: () -> Unit,
    onApply: (Double, Double, Double, Double) -> Boolean
) {
    var xMinText by remember(state) { mutableStateOf(state.xMin.toString()) }
    var xMaxText by remember(state) { mutableStateOf(state.xMax.toString()) }
    var yMinText by remember(state) { mutableStateOf(state.yMin.toString()) }
    var yMaxText by remember(state) { mutableStateOf(state.yMax.toString()) }
    var showValidationError by remember { mutableStateOf(false) }

    fun parse(value: String): Double? {
        return value.trim()
            .replace(',', '.')
            .replace('−', '-')
            .toDoubleOrNull()
    }

    fun tryApply() {
        val xMin = parse(xMinText)
        val xMax = parse(xMaxText)
        val yMin = parse(yMinText)
        val yMax = parse(yMaxText)

        if (xMin == null || xMax == null || yMin == null || yMax == null) {
            showValidationError = true
            return
        }

        val applied = onApply(xMin, xMax, yMin, yMax)
        if (applied) {
            onDismiss()
        } else {
            showValidationError = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.cpp_graph_set_range))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small)) {
                OutlinedTextField(
                    value = xMinText,
                    onValueChange = {
                        xMinText = it
                        showValidationError = false
                    },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.cpp_graph_x_min)) }
                )
                OutlinedTextField(
                    value = xMaxText,
                    onValueChange = {
                        xMaxText = it
                        showValidationError = false
                    },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.cpp_graph_x_max)) }
                )
                OutlinedTextField(
                    value = yMinText,
                    onValueChange = {
                        yMinText = it
                        showValidationError = false
                    },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.cpp_graph_y_min)) }
                )
                OutlinedTextField(
                    value = yMaxText,
                    onValueChange = {
                        yMaxText = it
                        showValidationError = false
                    },
                    singleLine = true,
                    label = { Text(stringResource(Res.string.cpp_graph_y_max)) }
                )

                if (showValidationError) {
                    Text(
                        text = stringResource(Res.string.cpp_graph_invalid_bounds),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { tryApply() }) {
                Text(stringResource(Res.string.cpp_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cpp_cancel))
            }
        }
    )
}
