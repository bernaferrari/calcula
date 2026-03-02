package org.solovyev.android.calculator.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface OnboardingPreferences {
    val onboardingCompleted: Flow<Boolean>
    val showTips: Flow<Boolean>
    suspend fun setOnboardingCompleted(value: Boolean)
    suspend fun setShowTips(value: Boolean)
}

@Composable
fun MinimalOnboarding(
    preferences: OnboardingPreferences,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }
    var selectedTheme by remember { mutableStateOf(ThemeOption.SYSTEM) }
    var dontShowTips by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeOut(animationSpec = tween(200))
            },
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> WelcomePage(
                    onContinue = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        currentPage = 1
                    },
                    onSkip = {
                        scope.launch {
                            preferences.setOnboardingCompleted(true)
                            onComplete()
                        }
                    }
                )
                1 -> GesturePage(
                    onContinue = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        currentPage = 2
                    },
                    onSkip = {
                        scope.launch {
                            preferences.setOnboardingCompleted(true)
                            onComplete()
                        }
                    }
                )
                2 -> ThemePage(
                    selectedTheme = selectedTheme,
                    onThemeSelected = { selectedTheme = it },
                    onContinue = {
                        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                        currentPage = 3
                    },
                    onSkip = {
                        scope.launch {
                            preferences.setOnboardingCompleted(true)
                            onComplete()
                        }
                    }
                )
                3 -> CompletePage(
                    dontShowTips = dontShowTips,
                    onDontShowTipsChange = { dontShowTips = it },
                    onComplete = {
                        scope.launch {
                            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                            preferences.setOnboardingCompleted(true)
                            preferences.setShowTips(!dontShowTips)
                            onComplete()
                        }
                    }
                )
            }
        }

        // Page indicator dots
        if (currentPage < 3) {
            PageIndicator(
                currentPage = currentPage,
                totalPages = 4,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp)
            )
        }
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalPages) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label = "indicator_width"
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .background(
                        color = if (isActive)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun WelcomePage(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Animated Pi logo with gradient and shadow
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(600))
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "breathing")
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breathing_scale"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = -3f,
                targetValue = 3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(4000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "gentle_rotation"
            )

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                    }
                    .shadow(
                        elevation = 24.dp,
                        shape = CircleShape,
                        spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .size(90.dp)
                        .graphicsLayer {
                            rotationZ = -rotation // Counter-rotate icon
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Title with staggered animation
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(500, delayMillis = 200))
        ) {
            Text(
                text = "Calculator++",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(500, delayMillis = 350))
        ) {
            Text(
                text = "Precision at your fingertips",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // CTA Button
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(500, delayMillis = 500))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Skip button
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(500, delayMillis = 600))
        ) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GesturePage(
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Visual gesture demo - no text, pure demonstration
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn()
        ) {
            GestureDemo()
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Subtle hint text (minimal)
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(400, delayMillis = 300))
        ) {
            Text(
                text = "Swipe to discover",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 400))
        ) {
            Text(
                text = "Swipe up, down, left, or right on any button",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(400, delayMillis = 500))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 600))
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun GestureDemo() {
    val infiniteTransition = rememberInfiniteTransition(label = "gesture_demo")

    // Animate swipe demo
    val swipeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "swipe_progress"
    )

    val fingerOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0
                -60f at 600 using EaseOutCubic
                -60f at 1400
                0f at 2000 using EaseInCubic
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "finger_offset"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Direction hints that appear based on swipe
        val hintAlpha = if (swipeProgress in 0.2f..0.6f) 1f else 0.3f

        // Top hint - asin
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
                .alpha(if (swipeProgress > 0.3f && swipeProgress < 0.7f) 1f else 0.3f)
                .scale(if (swipeProgress > 0.3f && swipeProgress < 0.7f) 1.1f else 1f),
            contentAlignment = Alignment.Center
        ) {
            DemoFunctionLabel("asin", MaterialTheme.colorScheme.primary)
        }

        // Left hint - sinh
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 20.dp)
                .alpha(0.3f),
            contentAlignment = Alignment.Center
        ) {
            DemoFunctionLabel("sinh", MaterialTheme.colorScheme.tertiary)
        }

        // Right hint - cos
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-20).dp)
                .alpha(0.3f),
            contentAlignment = Alignment.Center
        ) {
            DemoFunctionLabel("cos", MaterialTheme.colorScheme.secondary)
        }

        // Main button with animated finger
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            // Main button
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "sin",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Animated finger indicator
            Box(
                modifier = Modifier
                    .offset(y = fingerOffset.dp + 60.dp)
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tap",
                    fontSize = 18.sp
                )
            }

            // Swipe trail effect
            if (swipeProgress in 0.1f..0.6f) {
                Box(
                    modifier = Modifier
                        .offset(y = (fingerOffset * 0.5f).dp + 40.dp)
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * (1 - swipeProgress * 1.5f)),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun DemoFunctionLabel(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class ThemeOption {
    LIGHT, DARK, SYSTEM
}

@Composable
private fun ThemePage(
    selectedTheme: ThemeOption,
    onThemeSelected: (ThemeOption) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Title
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { -it / 3 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn()
        ) {
            Text(
                text = "Choose your style",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Theme previews
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.9f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(animationSpec = tween(400, delayMillis = 150))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ThemePreviewCard(
                    theme = ThemeOption.LIGHT,
                    isSelected = selectedTheme == ThemeOption.LIGHT,
                    onClick = { onThemeSelected(ThemeOption.LIGHT) },
                    title = "Light",
                    backgroundColor = Color(0xFFF8F9FA),
                    surfaceColor = Color(0xFFFFFFFF),
                    primaryColor = Color(0xFF0066CC),
                    textColor = Color(0xFF1A1C1E)
                )

                ThemePreviewCard(
                    theme = ThemeOption.DARK,
                    isSelected = selectedTheme == ThemeOption.DARK,
                    onClick = { onThemeSelected(ThemeOption.DARK) },
                    title = "Dark",
                    backgroundColor = Color(0xFF1A1C1E),
                    surfaceColor = Color(0xFF2D3135),
                    primaryColor = Color(0xFF4D9FFF),
                    textColor = Color(0xFFE3E2E6)
                )

                ThemePreviewCard(
                    theme = ThemeOption.SYSTEM,
                    isSelected = selectedTheme == ThemeOption.SYSTEM,
                    onClick = { onThemeSelected(ThemeOption.SYSTEM) },
                    title = "System",
                    backgroundColor = Color(0xFFF0F4F8),
                    surfaceColor = Color(0xFFE8ECEF),
                    primaryColor = MaterialTheme.colorScheme.primary,
                    textColor = Color(0xFF1A1C1E),
                    isSystem = true
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 2 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(400, delayMillis = 400))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 500))
        ) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ThemePreviewCard(
    theme: ThemeOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    title: String,
    backgroundColor: Color,
    surfaceColor: Color,
    primaryColor: Color,
    textColor: Color,
    isSystem: Boolean = false
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = tween(200),
        label = "border_color"
    )

    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mini calculator mockup
            Surface(
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Display area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(surfaceColor.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "42",
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 4.dp),
                            fontSize = 10.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    // Keypad row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .background(
                                        if (index == 2) primaryColor else surfaceColor,
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (index == 2) "=" else (index + 1).toString(),
                                    fontSize = 8.sp,
                                    color = if (index == 2) Color.White else textColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSystem) "Follows device settings" else "Always $title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Selection indicator
            AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn(initialScale = 0.5f) + fadeIn(),
                exit = scaleOut(targetScale = 0.5f) + fadeOut()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletePage(
    dontShowTips: Boolean,
    onDontShowTipsChange: (Boolean) -> Unit,
    onComplete: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(300)
        showConfetti = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Celebration particles
        if (showConfetti) {
            CelebrationParticles()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Success checkmark with animation
            AnimatedVisibility(
                visible = visible,
                enter = scaleIn(
                    initialScale = 0.3f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeIn(animationSpec = tween(400, delayMillis = 200))
            ) {
                Text(
                    text = "You're all set!",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 300))
            ) {
                Text(
                    text = "Ready to calculate",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // "Don't show tips" option
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 400))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onDontShowTipsChange(!dontShowTips) }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Checkbox(
                        checked = dontShowTips,
                        onCheckedChange = onDontShowTipsChange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Don't show gesture tips",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Get Started button
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeIn(animationSpec = tween(400, delayMillis = 500))
            ) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = "Start Calculating",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CelebrationParticles() {
    val particleCount = 24
    val particles = remember {
        List(particleCount) { index ->
            ParticleState(
                angle = (index * 360f / particleCount) + (-20..20).random(),
                distance = (80..180).random().toFloat(),
                size = (6..14).random().toFloat(),
                delay = (index * 30)
            )
        }
    }

    particles.forEach { particle ->
        AnimatedParticle(particle)
    }
}

private data class ParticleState(
    val angle: Float,
    val distance: Float,
    val size: Float,
    val delay: Int
)

@Composable
private fun AnimatedParticle(particle: ParticleState) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(particle.delay.toLong())
        visible = true
    }

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "particle_progress"
    )

    val fadeOut by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 400, easing = EaseInCubic),
        label = "particle_fade"
    )

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )
    val color = colors[particle.angle.toInt() % colors.size]

    val radian = particle.angle.toDouble() * (kotlin.math.PI / 180.0)
    val x = kotlin.math.cos(radian).toFloat() * particle.distance * progress
    val y = kotlin.math.sin(radian).toFloat() * particle.distance * progress

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset(x.dp, y.dp)
                .size(particle.size.dp * (1f - progress * 0.3f))
                .alpha(fadeOut)
                .background(color, CircleShape)
        )
    }
}
