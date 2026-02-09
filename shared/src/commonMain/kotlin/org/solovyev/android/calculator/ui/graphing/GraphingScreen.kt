package org.solovyev.android.calculator.ui.graphing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

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
    val points by viewModel.points.collectAsState()
    val expression by viewModel.expression.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var inputExpression by remember { mutableStateOf(initialExpression) }
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(initialExpression) {
        if (initialExpression.isNotBlank()) {
            viewModel.setExpression(initialExpression)
            inputExpression = initialExpression
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Graph") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetView() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset View"
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
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "y =",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    OutlinedTextField(
                        value = inputExpression,
                        onValueChange = { inputExpression = it },
                        placeholder = { Text("sin(x)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            viewModel.setExpression(inputExpression)
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalButton(
                        onClick = {
                            viewModel.setExpression(inputExpression)
                            focusManager.clearFocus()
                        }
                    ) {
                        Text("Plot")
                    }
                }
            }
            
            // Error message
            error?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Graph canvas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                GraphCanvas(
                    state = graphState,
                    points = points,
                    onPan = { dx, dy -> viewModel.pan(dx, dy) },
                    onZoom = { scale, fx, fy -> viewModel.zoom(scale, fx, fy) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun GraphCanvas(
    state: GraphState,
    points: List<GraphPoint>,
    onPan: (Float, Float) -> Unit,
    onZoom: (Float, Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
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
        val gridStep = calculateGridStep(xRange)
        var x = (state.xMin / gridStep).toLong() * gridStep
        while (x <= state.xMax) {
            val screenX = toScreenX(x)
            drawLine(
                color = gridColor,
                start = Offset(screenX, 0f),
                end = Offset(screenX, height),
                strokeWidth = 1f
            )
            x += gridStep
        }
        
        var y = (state.yMin / gridStep).toLong() * gridStep
        while (y <= state.yMax) {
            val screenY = toScreenY(y)
            drawLine(
                color = gridColor,
                start = Offset(0f, screenY),
                end = Offset(width, screenY),
                strokeWidth = 1f
            )
            y += gridStep
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
        
        // Draw the function curve
        if (points.size >= 2) {
            val path = Path()
            var started = false
            
            points.forEach { point ->
                val screenX = toScreenX(point.x.toDouble())
                val screenY = toScreenY(point.y.toDouble())
                
                // Only draw visible points
                if (screenX in 0f..width && screenY in -height..height * 2) {
                    if (!started) {
                        path.moveTo(screenX, screenY)
                        started = true
                    } else {
                        path.lineTo(screenX, screenY)
                    }
                } else if (started) {
                    // Break the path at discontinuities
                    started = false
                }
            }
            
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

private fun calculateGridStep(range: Double): Double {
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
