package org.solovyev.android.calculator.ui.nb

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.ui.CalculatorFontFamily
import org.solovyev.android.calculator.ui.LocalCalculatorHapticsEnabled
import kotlin.math.roundToInt

/**
 * Not Boring Display - Result is the absolute hero.
 * 
 * Philosophy:
 * - Result is MASSIVE and centered
 * - Expression is secondary, small and light
 * - No chrome - swipe gestures reveal history/settings
 * - Subtle animations that feel satisfying, not distracting
 */
@Composable
fun NotBoringDisplay(
    state: DisplayState,
    editorState: EditorState,
    previewResult: String?,
    unitHint: String?,
    onEditorTextChange: (String, Int) -> Unit,
    onEditorSelectionChange: (Int) -> Unit,
    onSwipeDown: () -> Unit, // Reveal history
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val hapticsEnabled = org.solovyev.android.calculator.ui.LocalCalculatorHapticsEnabled.current
    
    var offsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "offset"
    )
    
    // The result scale - pops in when result appears
    val hasResult = state.text.isNotEmpty()
    val resultScale by animateFloatAsState(
        targetValue = if (hasResult) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "resultScale"
    )
    
    val resultAlpha by animateFloatAsState(
        targetValue = if (hasResult) 1f else 0f,
        animationSpec = tween(200),
        label = "resultAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startY = down.position.y
                    
                    var totalDrag = 0f
                    
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        
                        if (change == null || change.changedToUpIgnoreConsumed()) {
                            // Check if we swiped down enough
                            if (totalDrag > 100.dp.toPx()) {
                                if (hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onSwipeDown()
                            }
                            // Animate back
                            offsetY = 0f
                            break
                        } else {
                            val dragAmount = change.position.y - startY - totalDrag
                            totalDrag += dragAmount
                            
                            // Elastic resistance
                            val resistance = if (totalDrag > 0) 0.4f else 1f
                            offsetY = totalDrag * resistance
                            
                            change.consume()
                        }
                    }
                }
            }
            .offset { IntOffset(0, animatedOffset.roundToInt()) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Expression - secondary, smaller, lighter
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Show expression or preview result
                val showPreview = previewResult != null && !hasResult
                val displayText = if (showPreview) {
                    "= $previewResult"
                } else {
                    editorState.text
                }
                
                AnimatedContent(
                    targetState = displayText,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "expression"
                ) { text ->
                    Text(
                        text = text,
                        style = TextStyle(
                            fontFamily = CalculatorFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            color = if (showPreview) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Result - THE HERO - massive, bold, centered-ish
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = hasResult,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(tween(200)),
                    exit = fadeOut(tween(150))
                ) {
                    Text(
                        text = state.text,
                        modifier = Modifier.graphicsLayer {
                            scaleX = resultScale
                            scaleY = resultScale
                            alpha = resultAlpha
                        },
                        style = TextStyle(
                            fontFamily = CalculatorFontFamily,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-1.5).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Unit hint - subtle tertiary info
            AnimatedVisibility(
                visible = unitHint != null,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {
                Text(
                    text = unitHint ?: "",
                    style = TextStyle(
                        fontFamily = CalculatorFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                    )
                )
            }
            
            // Subtle pull indicator
            if (!hasResult && editorState.text.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "↓ pull for history",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}
