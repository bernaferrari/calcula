package com.bernaferrari.calculator.ui.graphing

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bernaferrari.calculator.domain.model.GraphingFunction
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphingScreen(
    onNavigateBack: () -> Unit,
    viewModel: GraphingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Function Grapher",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.resetView() }) {
                        Icon(
                            imageVector = Icons.Default.CenterFocusStrong,
                            contentDescription = "Reset view"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.addFunction() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add function"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Graph display
            GraphCanvas(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(16.dp)
            )
            
            // Function list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.functions) { function ->
                    FunctionItem(
                        function = function,
                        onUpdateFunction = { newExpression ->
                            viewModel.updateFunction(function, newExpression)
                        },
                        onToggleVisibility = {
                            viewModel.toggleFunctionVisibility(function)
                        },
                        onDeleteFunction = {
                            viewModel.deleteFunction(function)
                        },
                        onChangeColor = { color ->
                            viewModel.changeFunctionColor(function, color)
                        }
                    )
                }
                
                if (state.functions.isEmpty()) {
                    item {
                        EmptyFunctionsMessage()
                    }
                }
            }
        }
    }
}

@Composable
private fun GraphCanvas(
    state: GraphingState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            drawGraph(state)
        }
    }
}

private fun DrawScope.drawGraph(state: GraphingState) {
    val width = size.width
    val height = size.height
    
    // Draw grid
    if (state.gridEnabled) {
        drawGrid(state, width, height)
    }
    
    // Draw axes
    drawAxes(state, width, height)
    
    // Draw functions
    state.functions.filter { it.isVisible }.forEach { function ->
        drawFunction(function, state, width, height)
    }
}

private fun DrawScope.drawGrid(state: GraphingState, width: Float, height: Float) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val xRange = state.xMax - state.xMin
    val yRange = state.yMax - state.yMin
    
    // Vertical grid lines
    val xStep = calculateGridStep(xRange)
    var x = ceil(state.xMin / xStep) * xStep
    while (x <= state.xMax) {
        val screenX = ((x - state.xMin) / xRange * width).toFloat()
        drawLine(
            color = gridColor,
            start = Offset(screenX, 0f),
            end = Offset(screenX, height),
            strokeWidth = 1.dp.toPx()
        )
        x += xStep
    }
    
    // Horizontal grid lines
    val yStep = calculateGridStep(yRange)
    var y = ceil(state.yMin / yStep) * yStep
    while (y <= state.yMax) {
        val screenY = (height - (y - state.yMin) / yRange * height).toFloat()
        drawLine(
            color = gridColor,
            start = Offset(0f, screenY),
            end = Offset(width, screenY),
            strokeWidth = 1.dp.toPx()
        )
        y += yStep
    }
}

private fun DrawScope.drawAxes(state: GraphingState, width: Float, height: Float) {
    val axisColor = Color.Black.copy(alpha = 0.8f)
    val xRange = state.xMax - state.xMin
    val yRange = state.yMax - state.yMin
    
    // X-axis (y = 0)
    if (state.yMin <= 0.0 && state.yMax >= 0.0) {
        val screenY = (height - (0.0 - state.yMin) / yRange * height).toFloat()
        drawLine(
            color = axisColor,
            start = Offset(0f, screenY),
            end = Offset(width, screenY),
            strokeWidth = 2.dp.toPx()
        )
    }
    
    // Y-axis (x = 0)
    if (state.xMin <= 0.0 && state.xMax >= 0.0) {
        val screenX = ((0.0 - state.xMin) / xRange * width).toFloat()
        drawLine(
            color = axisColor,
            start = Offset(screenX, 0f),
            end = Offset(screenX, height),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawFunction(
    function: GraphingFunction,
    state: GraphingState,
    width: Float,
    height: Float
) {
    val points = mutableListOf<Offset>()
    val xRange = state.xMax - state.xMin
    val yRange = state.yMax - state.yMin
    val stepSize = xRange / width * 2 // Sample more frequently for smoother curves
    
    for (i in 0..width.toInt() step 2) {
        val x = state.xMin + (i / width) * xRange
        try {
            val y = evaluateFunction(function.expression, x)
            if (y.isFinite() && y >= state.yMin && y <= state.yMax) {
                val screenX = ((x - state.xMin) / xRange * width).toFloat()
                val screenY = (height - (y - state.yMin) / yRange * height).toFloat()
                points.add(Offset(screenX, screenY))
            }
        } catch (e: Exception) {
            // Skip invalid points
        }
    }
    
    // Draw the function curve
    if (points.size > 1) {
        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        
        drawPath(
            path = path,
            color = Color(function.color),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

private fun evaluateFunction(expression: String, x: Double): Double {
    // Simple function evaluator for basic functions
    // This is a simplified version - you'd want to use the ExpressionEvaluator here
    val expr = expression.replace("x", x.toString())
    return when {
        expr.startsWith("sin(") -> sin(x)
        expr.startsWith("cos(") -> cos(x)
        expr.startsWith("tan(") -> tan(x)
        expr.startsWith("ln(") -> ln(x)
        expr.startsWith("log(") -> log10(x)
        expr == "x" -> x
        expr == "x^2" -> x * x
        expr == "x^3" -> x * x * x
        expr == "sqrt(x)" -> sqrt(x)
        else -> {
            try {
                expr.toDouble()
            } catch (e: Exception) {
                x // Default to linear function
            }
        }
    }
}

private fun calculateGridStep(range: Double): Double {
    val magnitude = 10.0.pow(floor(log10(range)))
    val normalized = range / magnitude
    
    return when {
        normalized <= 1.0 -> magnitude * 0.1
        normalized <= 2.0 -> magnitude * 0.2
        normalized <= 5.0 -> magnitude * 0.5
        else -> magnitude
    }
}

@Composable
private fun FunctionItem(
    function: GraphingFunction,
    onUpdateFunction: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onDeleteFunction: () -> Unit,
    onChangeColor: (Long) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(function.expression) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Color indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(function.color))
                        .clickable {
                            // Cycle through predefined colors
                            val colors = listOf(
                                0xFF2196F3, 0xFFF44336, 0xFF4CAF50, 0xFFFF9800,
                                0xFF9C27B0, 0xFF607D8B, 0xFFE91E63, 0xFF795548
                            )
                            val currentIndex = colors.indexOf(function.color)
                            val nextIndex = (currentIndex + 1) % colors.size
                            onChangeColor(colors[nextIndex])
                        }
                )
                
                // Function expression
                if (isEditing) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                onUpdateFunction(editText)
                                isEditing = false
                                keyboardController?.hide()
                            }
                        ),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = "f(x) = ${function.expression}",
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                            .clickable { 
                                isEditing = true
                                editText = function.expression
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (function.isVisible) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }
                    )
                }
                
                // Action buttons
                Row {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (function.isVisible) {
                                Icons.Default.Visibility
                            } else {
                                Icons.Default.VisibilityOff
                            },
                            contentDescription = if (function.isVisible) "Hide" else "Show",
                            tint = if (function.isVisible) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                    }
                    
                    IconButton(onClick = onDeleteFunction) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFunctionsMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Functions,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No functions to graph",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Tap the + button to add a function",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}