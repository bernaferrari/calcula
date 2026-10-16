package com.bernaferrari.calculator.ui.graph

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bernaferrari.calculator.domain.calculator.ExpressionEvaluator
import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: GraphingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top bar
        TopAppBar(
            title = { 
                Text(
                    "Function Grapher",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Medium
                ) 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { viewModel.resetZoom() }
                ) {
                    Icon(Icons.Default.CenterFocusStrong, "Reset Zoom")
                }
            }
        )
        
        // Function input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                OutlinedTextField(
                    value = state.currentFunction,
                    onValueChange = viewModel::updateFunction,
                    label = { Text("Function (e.g., sin(x), x^2, log(x))") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addFunction() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Plot")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.clearFunctions() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }
        }
        
        // Quick function buttons
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickFunctions) { function ->
                FilterChip(
                    onClick = { viewModel.updateFunction(function.expression) },
                    label = { Text(function.name) },
                    selected = false
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Graph canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            InteractiveGraph(
                functions = state.functions,
                viewport = state.viewport,
                onViewportChange = viewModel::updateViewport,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Function list
        if (state.functions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Active Functions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    state.functions.forEachIndexed { index, function ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        Color(function.color),
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                function.expression,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            IconButton(
                                onClick = { viewModel.removeFunction(index) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    "Remove",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun InteractiveGraph(
    functions: List<GraphFunction>,
    viewport: Viewport,
    onViewportChange: (Viewport) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    var canvasSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures(
                    onGesture = { _, pan, zoom, _ ->
                        val newViewport = viewport.copy(
                            xMin = viewport.xMin - pan.x / (canvasSize.width / (viewport.xMax - viewport.xMin)),
                            xMax = viewport.xMax - pan.x / (canvasSize.width / (viewport.xMax - viewport.xMin)),
                            yMin = viewport.yMin + pan.y / (canvasSize.height / (viewport.yMax - viewport.yMin)),
                            yMax = viewport.yMax + pan.y / (canvasSize.height / (viewport.yMax - viewport.yMin))
                        )
                        
                        val centerX = (newViewport.xMin + newViewport.xMax) / 2
                        val centerY = (newViewport.yMin + newViewport.yMax) / 2
                        val rangeX = (newViewport.xMax - newViewport.xMin) / zoom
                        val rangeY = (newViewport.yMax - newViewport.yMin) / zoom
                        
                        onViewportChange(
                            Viewport(
                                xMin = centerX - rangeX / 2,
                                xMax = centerX + rangeX / 2,
                                yMin = centerY - rangeY / 2,
                                yMax = centerY + rangeY / 2
                            )
                        )
                    }
                )
            }
            .onGloballyPositioned { coordinates ->
                canvasSize = coordinates.size
            }
    ) {
        drawGrid(viewport)
        drawAxes(viewport)
        
        functions.forEach { function ->
            drawFunction(function, viewport, canvasSize)
        }
        
        drawLabels(viewport)
        drawCrosshair()
    }
}

private fun DrawScope.drawGrid(viewport: Viewport) {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val strokeWidth = 1.dp.toPx()
    
    // Vertical grid lines
    val xStep = calculateStep(viewport.xMax - viewport.xMin)
    var x = (viewport.xMin / xStep).toInt() * xStep
    while (x <= viewport.xMax) {
        if (abs(x) > 1e-10) { // Don't draw line at x=0 (that's the axis)
            val pixelX = (((x - viewport.xMin) / (viewport.xMax - viewport.xMin)) * size.width).toFloat()
            drawLine(
                color = gridColor,
                start = Offset(pixelX, 0f),
                end = Offset(pixelX, size.height),
                strokeWidth = strokeWidth
            )
        }
        x += xStep
    }
    
    // Horizontal grid lines
    val yStep = calculateStep(viewport.yMax - viewport.yMin)
    var y = (viewport.yMin / yStep).toInt() * yStep
    while (y <= viewport.yMax) {
        if (abs(y) > 1e-10) { // Don't draw line at y=0 (that's the axis)
            val pixelY = (size.height - ((y - viewport.yMin) / (viewport.yMax - viewport.yMin)) * size.height).toFloat()
            drawLine(
                color = gridColor,
                start = Offset(0f, pixelY),
                end = Offset(size.width, pixelY),
                strokeWidth = strokeWidth
            )
        }
        y += yStep
    }
}

private fun DrawScope.drawAxes(viewport: Viewport) {
    val axisColor = Color.Black.copy(alpha = 0.8f)
    val strokeWidth = 2.dp.toPx()
    
    // X-axis (y = 0)
    if (viewport.yMin <= 0 && viewport.yMax >= 0) {
        val pixelY = (size.height - ((0 - viewport.yMin) / (viewport.yMax - viewport.yMin)) * size.height).toFloat()
        drawLine(
            color = axisColor,
            start = Offset(0f, pixelY),
            end = Offset(size.width, pixelY),
            strokeWidth = strokeWidth
        )
    }
    
    // Y-axis (x = 0)
    if (viewport.xMin <= 0 && viewport.xMax >= 0) {
        val pixelX = (((0 - viewport.xMin) / (viewport.xMax - viewport.xMin)) * size.width).toFloat()
        drawLine(
            color = axisColor,
            start = Offset(pixelX, 0f),
            end = Offset(pixelX, size.height),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawFunction(
    function: GraphFunction,
    viewport: Viewport,
    canvasSize: androidx.compose.ui.unit.IntSize
) {
    val color = Color(function.color)
    val strokeWidth = 2.5.dp.toPx()
    val evaluator = ExpressionEvaluator(AngleUnit.RADIANS, 15)
    
    val points = mutableListOf<Offset>()
    val stepCount = (canvasSize.width / 2).coerceAtLeast(100)
    val step = (viewport.xMax - viewport.xMin) / stepCount
    
    for (i in 0..stepCount) {
        val x = viewport.xMin + i * step
        
        try {
            evaluator.setVariable("x", BigDecimal(x.toString()))
            val result = evaluator.evaluate(function.expression)
            
            if (result.isSuccess) {
                val y = result.getOrThrow().toDouble()
                
                // Skip infinite or NaN values
                if (y.isFinite() && y >= viewport.yMin && y <= viewport.yMax) {
                    val pixelX = (((x - viewport.xMin) / (viewport.xMax - viewport.xMin)) * size.width).toFloat()
                    val pixelY = (size.height - ((y - viewport.yMin) / (viewport.yMax - viewport.yMin)) * size.height).toFloat()
                    
                    points.add(Offset(pixelX, pixelY))
                }
            }
        } catch (e: Exception) {
            // Skip invalid points
        }
    }
    
    // Draw connected line segments
    if (points.size > 1) {
        for (i in 0 until points.size - 1) {
            drawLine(
                color = color,
                start = points[i],
                end = points[i + 1],
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.drawLabels(viewport: Viewport) {
    // Placeholder for axes labels/ticks would go here (Compose text in Canvas is limited)
}

private fun DrawScope.drawCrosshair() {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val crossColor = Color.Black.copy(alpha = 0.15f)
    drawLine(crossColor, start = Offset(centerX - 10, centerY), end = Offset(centerX + 10, centerY), strokeWidth = 1f)
    drawLine(crossColor, start = Offset(centerX, centerY - 10), end = Offset(centerX, centerY + 10), strokeWidth = 1f)
}

private fun calculateStep(range: Double): Double {
    val magnitude = 10.0.pow(floor(log10(range)))
    val normalized = range / magnitude
    
    return when {
        normalized <= 1.0 -> magnitude * 0.2
        normalized <= 2.0 -> magnitude * 0.5
        normalized <= 5.0 -> magnitude
        else -> magnitude * 2.0
    }
}

data class GraphFunction(
    val expression: String,
    val color: Long,
    val isVisible: Boolean = true
)

data class Viewport(
    val xMin: Double = -10.0,
    val xMax: Double = 10.0,
    val yMin: Double = -10.0,
    val yMax: Double = 10.0
)

data class QuickFunction(
    val name: String,
    val expression: String
)

private val quickFunctions = listOf(
    QuickFunction("sin(x)", "sin(x)"),
    QuickFunction("cos(x)", "cos(x)"),
    QuickFunction("tan(x)", "tan(x)"),
    QuickFunction("x²", "x^2"),
    QuickFunction("x³", "x^3"),
    QuickFunction("√x", "sqrt(x)"),
    QuickFunction("ln(x)", "ln(x)"),
    QuickFunction("log(x)", "log(x)"),
    QuickFunction("e^x", "e^x"),
    QuickFunction("1/x", "1/x")
)

private val functionColors = listOf(
    0xFF2196F3, // Blue
    0xFFF44336, // Red  
    0xFF4CAF50, // Green
    0xFFFF9800, // Orange
    0xFF9C27B0, // Purple
    0xFF607D8B, // Blue Grey
    0xFFE91E63, // Pink
    0xFF795548  // Brown
)