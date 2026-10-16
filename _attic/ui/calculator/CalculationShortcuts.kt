package com.bernaferrari.calculator.ui.calculator

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CalculationShortcut(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val action: ShortcutAction
)

sealed class ShortcutAction {
    data class Expression(val expr: String) : ShortcutAction()
    data class Function(val name: String, val needsInput: Boolean = true) : ShortcutAction()
    object OpenConverter : ShortcutAction()
    object OpenProgrammer : ShortcutAction()
    object OpenStatistics : ShortcutAction()
    object OpenGraph : ShortcutAction()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationShortcutsPanel(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onShortcutClick: (CalculationShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                // Shortcuts grid
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(getShortcuts()) { shortcut ->
                        ShortcutCard(
                            shortcut = shortcut,
                            onClick = { onShortcutClick(shortcut) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    shortcut: CalculationShortcut,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = shortcut.color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with animated background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                shortcut.color.copy(alpha = 0.3f),
                                shortcut.color.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = shortcut.icon,
                    contentDescription = null,
                    tint = shortcut.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = shortcut.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = shortcut.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Arrow indicator
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun FloatingShortcutButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRotating by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isRotating) 180f else 0f,
        animationSpec = tween(300),
        finishedListener = { isRotating = false }
    )
    
    FloatingActionButton(
        onClick = {
            isRotating = true
            onClick()
        },
        modifier = modifier
            .size(56.dp)
            .rotate(rotation),
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = CircleShape
    ) {
        Icon(
            Icons.Default.Apps,
            contentDescription = "Quick Actions",
            modifier = Modifier.size(24.dp)
        )
    }
}

private fun getShortcuts(): List<CalculationShortcut> = listOf(
    CalculationShortcut(
        id = "percentage",
        title = "Percentage Calculator",
        subtitle = "Quick percentage calculations",
        icon = Icons.Default.Percent,
        color = Color(0xFF4CAF50),
        action = ShortcutAction.Function("percent", true)
    ),
    CalculationShortcut(
        id = "tip",
        title = "Tip Calculator",
        subtitle = "Calculate tips and split bills",
        icon = Icons.Default.Receipt,
        color = Color(0xFF2196F3),
        action = ShortcutAction.Function("tip", true)
    ),
    CalculationShortcut(
        id = "converter",
        title = "Unit Converter",
        subtitle = "Convert between units",
        icon = Icons.Default.SwapHoriz,
        color = Color(0xFFFF9800),
        action = ShortcutAction.OpenConverter
    ),
    CalculationShortcut(
        id = "programmer",
        title = "Programmer Mode",
        subtitle = "Binary, hex, and more",
        icon = Icons.Default.Code,
        color = Color(0xFF9C27B0),
        action = ShortcutAction.OpenProgrammer
    ),
    CalculationShortcut(
        id = "statistics",
        title = "Statistics",
        subtitle = "Mean, median, std dev",
        icon = Icons.Default.Functions,
        color = Color(0xFFE91E63),
        action = ShortcutAction.OpenStatistics
    ),
    CalculationShortcut(
        id = "graph",
        title = "Graphing",
        subtitle = "Plot functions",
        icon = Icons.Default.ShowChart,
        color = Color(0xFF00BCD4),
        action = ShortcutAction.OpenGraph
    ),
    CalculationShortcut(
        id = "constants",
        title = "Constants",
        subtitle = "π, e, golden ratio",
        icon = Icons.Default.Science,
        color = Color(0xFF795548),
        action = ShortcutAction.Expression("constants")
    ),
    CalculationShortcut(
        id = "date_calc",
        title = "Date Calculator",
        subtitle = "Days between dates",
        icon = Icons.Default.DateRange,
        color = Color(0xFF607D8B),
        action = ShortcutAction.Function("date", false)
    )
)