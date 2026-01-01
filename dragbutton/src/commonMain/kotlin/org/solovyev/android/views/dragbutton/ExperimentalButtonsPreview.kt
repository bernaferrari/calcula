package org.solovyev.android.views.dragbutton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Preview removed due to compilation issues in commonMain


@Composable
private fun AnimatedDragButtonSection() {
    var lastAction by remember { mutableStateOf("Drag or tap a button") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎯 AnimatedDragButton",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Drag in any direction. Watch the edge glow, text animations, and feel the haptic pulse when crossing the threshold.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button "1" with sin/asin/A
                ButtonContainer(modifier = Modifier.weight(1f)) {
                    AnimatedDragButton(
                        text = "1",
                        onClick = { lastAction = "Tap: 1" },
                        onDrag = { direction ->
                            lastAction = when (direction) {
                                DragDirection.up -> "↑ sin"
                                DragDirection.down -> "↓ asin"
                                DragDirection.left -> "← A"
                                DragDirection.right -> "→ 1"
                            }
                            true
                        },
                        directionTexts = mapOf(
                            DragDirection.up to DirectionTextConfig("sin"),
                            DragDirection.down to DirectionTextConfig("asin"),
                            DragDirection.left to DirectionTextConfig("A")
                        ),
                        textStyle = TextStyle(fontSize = 28.sp),
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                }
                
                // Button "7" with i/!/0b
                ButtonContainer(modifier = Modifier.weight(1f)) {
                    AnimatedDragButton(
                        text = "7",
                        onClick = { lastAction = "Tap: 7" },
                        onDrag = { direction ->
                            lastAction = when (direction) {
                                DragDirection.up -> "↑ i"
                                DragDirection.down -> "↓ !"
                                DragDirection.left -> "← 0b:"
                                DragDirection.right -> "→ 7"
                            }
                            true
                        },
                        directionTexts = mapOf(
                            DragDirection.up to DirectionTextConfig("i"),
                            DragDirection.down to DirectionTextConfig("!"),
                            DragDirection.left to DirectionTextConfig("0b:")
                        ),
                        textStyle = TextStyle(fontSize = 28.sp),
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                }
                
                // Button "×" with ^/^2
                ButtonContainer(modifier = Modifier.weight(1f)) {
                    AnimatedDragButton(
                        text = "×",
                        onClick = { lastAction = "Tap: ×" },
                        onDrag = { direction ->
                            lastAction = when (direction) {
                                DragDirection.up -> "↑ ^"
                                DragDirection.down -> "↓ ^2"
                                else -> "→ ×"
                            }
                            true
                        },
                        directionTexts = mapOf(
                            DragDirection.up to DirectionTextConfig("^"),
                            DragDirection.down to DirectionTextConfig("^2")
                        ),
                        textStyle = TextStyle(fontSize = 28.sp),
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
            }
            
            ActionIndicator(lastAction)
        }
    }
}

@Composable
private fun PopoverDragButtonSection() {
    var lastAction by remember { mutableStateOf("Long-press a button") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🎨 PopoverDragButton",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Long-press to show the popover. Move your finger to select, feel haptics on each change. Pull down far to cancel.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button "1" with sin/asin
                ButtonContainer(modifier = Modifier.weight(1f)) {
                    PopoverDragButton(
                        text = "1",
                        onClick = { lastAction = "Tap: 1" },
                        onDirectionSelected = { direction ->
                            lastAction = when (direction) {
                                DragDirection.up -> "Selected: sin"
                                DragDirection.down -> "Selected: asin"
                                DragDirection.left -> "Selected: A"
                                DragDirection.right -> "Selected: →"
                            }
                        },
                        directionTexts = mapOf(
                            DragDirection.up to DirectionTextConfig("sin"),
                            DragDirection.down to DirectionTextConfig("asin"),
                            DragDirection.left to DirectionTextConfig("A")
                        ),
                        textStyle = TextStyle(fontSize = 28.sp),
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                }
                
                // Button "×" with ^/^2
                ButtonContainer(modifier = Modifier.weight(1f)) {
                    PopoverDragButton(
                        text = "×",
                        onClick = { lastAction = "Tap: ×" },
                        onDirectionSelected = { direction ->
                            lastAction = when (direction) {
                                DragDirection.up -> "Selected: ^"
                                DragDirection.down -> "Selected: ^2"
                                else -> "Selected: $direction"
                            }
                        },
                        directionTexts = mapOf(
                            DragDirection.up to DirectionTextConfig("^"),
                            DragDirection.down to DirectionTextConfig("^2")
                        ),
                        textStyle = TextStyle(fontSize = 28.sp),
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }
                
                // Button "Vars" with π/e
                ButtonContainer(modifier = Modifier.weight(1f)) {
                    PopoverDragButton(
                        text = "Vars",
                        onClick = { lastAction = "Tap: Vars" },
                        onDirectionSelected = { direction ->
                            lastAction = when (direction) {
                                DragDirection.up -> "Selected: π"
                                DragDirection.down -> "Selected: e"
                                else -> "Selected: $direction"
                            }
                        },
                        directionTexts = mapOf(
                            DragDirection.up to DirectionTextConfig("π"),
                            DragDirection.down to DirectionTextConfig("e")
                        ),
                        textStyle = TextStyle(fontSize = 16.sp),
                        modifier = Modifier.fillMaxSize(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    )
                }
            }
            
            ActionIndicator(lastAction)
        }
    }
}

@Composable
private fun ComparisonSection() {
    var lastAction by remember { mutableStateOf("Try both side by side") }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "⚖️ Side-by-Side Comparison",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Same button, different interaction patterns:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Animated\n(drag)",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                    ButtonContainer(modifier = Modifier.height(72.dp)) {
                        AnimatedDragButton(
                            text = "5",
                            onClick = { lastAction = "Animated: Tap" },
                            onDrag = { direction ->
                                lastAction = "Animated: $direction"
                                true
                            },
                            directionTexts = mapOf(
                                DragDirection.up to DirectionTextConfig("t"),
                                DragDirection.down to DirectionTextConfig("j"),
                                DragDirection.left to DirectionTextConfig("E")
                            ),
                            textStyle = TextStyle(fontSize = 24.sp),
                            modifier = Modifier.fillMaxSize(),
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Popover\n(long-press)",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                    ButtonContainer(modifier = Modifier.height(72.dp)) {
                        PopoverDragButton(
                            text = "5",
                            onClick = { lastAction = "Popover: Tap" },
                            onDirectionSelected = { direction ->
                                lastAction = "Popover: $direction"
                            },
                            directionTexts = mapOf(
                                DragDirection.up to DirectionTextConfig("t"),
                                DragDirection.down to DirectionTextConfig("j"),
                                DragDirection.left to DirectionTextConfig("E")
                            ),
                            textStyle = TextStyle(fontSize = 24.sp),
                            modifier = Modifier.fillMaxSize(),
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    }
                }
            }
            
            ActionIndicator(lastAction)
        }
    }
}

@Composable
private fun ButtonContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .height(80.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
private fun ActionIndicator(action: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Text(
            text = action,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Interactive demo that can be embedded in the app for testing.
 */
@Composable
fun ExperimentalButtonsDemo(
    onAction: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Experimental Buttons",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ButtonContainer(modifier = Modifier.weight(1f)) {
                AnimatedDragButton(
                    text = "Drag",
                    onClick = { onAction("Animated: Click") },
                    onDrag = { direction ->
                        onAction("Animated: $direction")
                        true
                    },
                    directionTexts = mapOf(
                        DragDirection.up to DirectionTextConfig("↑"),
                        DragDirection.down to DirectionTextConfig("↓"),
                        DragDirection.left to DirectionTextConfig("←"),
                        DragDirection.right to DirectionTextConfig("→")
                    ),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
            
            ButtonContainer(modifier = Modifier.weight(1f)) {
                PopoverDragButton(
                    text = "Hold",
                    onClick = { onAction("Popover: Click") },
                    onDirectionSelected = { direction ->
                        onAction("Popover: $direction")
                    },
                    directionTexts = mapOf(
                        DragDirection.up to DirectionTextConfig("↑"),
                        DragDirection.down to DirectionTextConfig("↓"),
                        DragDirection.left to DirectionTextConfig("←"),
                        DragDirection.right to DirectionTextConfig("→")
                    ),
                    textStyle = TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxSize(),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}
