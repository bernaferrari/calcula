package org.solovyev.android.calculator.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.tokens.CalculatorCornerRadius
import org.solovyev.android.calculator.ui.tokens.CalculatorElevation
import org.solovyev.android.calculator.ui.tokens.CalculatorPadding
import org.solovyev.android.calculator.ui.tokens.CalculatorSpacing
import org.solovyev.android.calculator.GuiTheme
import org.solovyev.android.calculator.GuiMode

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onThemeSelected: (GuiTheme) -> Unit,
    onModeSelected: (GuiMode) -> Unit, // Keep for API compat, but ignored
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedTheme by remember { mutableStateOf(GuiTheme.material_theme) }
    val haptic = LocalHapticFeedback.current
    val reduceMotion = LocalCalculatorReduceMotion.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (reduceMotion) {
                    fadeIn(animationSpec = tween(90)) togetherWith fadeOut(animationSpec = tween(70))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                    ) togetherWith slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                0 -> WelcomeStep(
                    reduceMotion = reduceMotion,
                    onContinue = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentStep = 1 
                    }
                )
                1 -> ThemeStep(
                    selectedTheme = selectedTheme,
                    reduceMotion = reduceMotion,
                    onThemeSelected = { 
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTheme = it
                    },
                    onContinue = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentStep = 2 
                    }
                )
                2 -> TipsStep(
                    reduceMotion = reduceMotion,
                    onComplete = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        // Persist theme only when finishing onboarding to avoid activity recreation
                        // while user is still choosing appearance.
                        onThemeSelected(selectedTheme)
                        onModeSelected(GuiMode.modern) // Always set Modern mode
                        onComplete()
                    }
                )
            }
        }

        // Progress indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = CalculatorPadding.XLarge),
            horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Small)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == currentStep) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= currentStep)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(
    reduceMotion: Boolean,
    onContinue: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(if (reduceMotion) 0 else 150)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CalculatorSpacing.XXLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        // Animated App Icon with pulsing shadow
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                initialScale = 0.6f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(animationSpec = tween(500))
        ) {
            val pulseScale = if (reduceMotion) {
                1f
            } else {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                ).value
            }

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        shadowElevation = 20.dp.toPx()
                    }
                    .clip(RoundedCornerShape(CalculatorCornerRadius.Display))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        Spacer(Modifier.height(CalculatorSpacing.XXLarge))

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 4 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 200)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.cpp_onboarding_title),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(CalculatorSpacing.Medium))

                Text(
                    text = stringResource(Res.string.cpp_onboarding_subtitle),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = CalculatorElevation.Elevated)
            ) {
                Text(
                    text = stringResource(Res.string.cpp_onboarding_continue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(CalculatorPadding.XLarge))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 600))
        ) {
            Text(
                text = stringResource(Res.string.cpp_onboarding_free_open_source),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(Modifier.height(CalculatorPadding.XLarge))
    }
}

@Composable
private fun ThemeStep(
    selectedTheme: GuiTheme,
    reduceMotion: Boolean,
    onThemeSelected: (GuiTheme) -> Unit,
    onContinue: () -> Unit
) {
    val themes = listOf(
        Triple(
            GuiTheme.material_theme,
            stringResource(Res.string.cpp_theme_system),
            stringResource(Res.string.cpp_theme_system_summary)
        ),
        Triple(
            GuiTheme.material_light,
            stringResource(Res.string.cpp_theme_light),
            stringResource(Res.string.cpp_theme_light_summary)
        ),
        Triple(
            GuiTheme.material_dark,
            stringResource(Res.string.cpp_theme_dark),
            stringResource(Res.string.cpp_theme_dark_summary)
        )
    )

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(if (reduceMotion) 0 else 100)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CalculatorPadding.XLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(CalculatorSpacing.XXLarge + CalculatorSpacing.XLarge))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -20 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.cpp_onboarding_choose_appearance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(CalculatorSpacing.Small))

                Text(
                    text = stringResource(Res.string.cpp_onboarding_appearance_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(CalculatorSpacing.XXLarge))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            themes.forEachIndexed { index, (theme, name, summary) ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = index * 100)) +
                            slideInHorizontally(initialOffsetX = { it / 3 }, animationSpec = tween(400, delayMillis = index * 100))
                ) {
                    ThemeOption(
                        theme = theme,
                        name = name,
                        summary = summary,
                        isSelected = selectedTheme == theme,
                        onClick = { onThemeSelected(theme) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 400))
        ) {
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = CalculatorElevation.Elevated)
            ) {
                Text(
                    text = stringResource(Res.string.cpp_onboarding_continue),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(CalculatorPadding.XLarge))
    }
}

@Composable
private fun ThemeOption(
    theme: GuiTheme,
    name: String,
    summary: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        animationSpec = tween(200),
        label = "themeOptionContainer"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) CalculatorElevation.Elevated else CalculatorElevation.Subtle,
        label = "elevation"
    )

    Surface(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(CalculatorCornerRadius.ExtraLarge))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
        color = containerColor,
        tonalElevation = elevation,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CalculatorPadding.Large,
                    vertical = CalculatorPadding.Standard
                ),
            horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme preview with depth
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                shadowElevation = CalculatorElevation.Standard
            ) {
                Box(
                    modifier = Modifier.background(
                        brush = when (theme) {
                            GuiTheme.material_dark -> Brush.verticalGradient(
                                colors = listOf(Color(0xFF2A2A2E), Color(0xFF1A1A1E))
                            )
                            GuiTheme.material_light -> Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFFFF), Color(0xFFF5F5F7))
                            )
                            GuiTheme.material_theme -> Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    when (theme) {
                        GuiTheme.material_dark -> {
                            Icon(
                                imageVector = Icons.Filled.DarkMode,
                                contentDescription = null,
                                tint = Color(0xFFE7EAF0)
                            )
                        }
                        GuiTheme.material_light -> {
                            Icon(
                                imageVector = Icons.Filled.LightMode,
                                contentDescription = null,
                                tint = Color(0xFF5A5A5E)
                            )
                        }
                        else -> {
                            Text(
                                text = "A",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
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
private fun TipsStep(
    reduceMotion: Boolean,
    onComplete: () -> Unit
) {
    val tips = listOf(
        Triple("Tap", stringResource(Res.string.cpp_onboarding_tip1_title), stringResource(Res.string.cpp_onboarding_tip1_subtitle)),
        Triple("f(x)", stringResource(Res.string.cpp_onboarding_tip2_title), stringResource(Res.string.cpp_onboarding_tip2_subtitle)),
        Triple("H", stringResource(Res.string.cpp_onboarding_tip3_title), stringResource(Res.string.cpp_onboarding_tip3_subtitle))
    )

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(if (reduceMotion) 0 else 100)
        visible = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = CalculatorPadding.XLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(CalculatorSpacing.XXLarge + CalculatorSpacing.XLarge))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -20 })
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Success icon
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(CalculatorPadding.XLarge))

                Text(
                    text = stringResource(Res.string.cpp_onboarding_complete_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(CalculatorSpacing.Small))

                Text(
                    text = stringResource(Res.string.cpp_onboarding_complete_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(CalculatorSpacing.XXLarge))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            tips.forEachIndexed { index, (icon, title, subtitle) ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(400, delayMillis = index * 150)) +
                            slideInHorizontally(initialOffsetX = { it / 4 }, animationSpec = tween(400, delayMillis = index * 150))
                ) {
                    TipCard(
                        icon = icon,
                        title = title,
                        subtitle = subtitle
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(400, delayMillis = 500))
        ) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = CalculatorElevation.Elevated)
            ) {
                Text(
                    text = stringResource(Res.string.cpp_start_calculating),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(CalculatorPadding.XLarge))
    }
}

@Composable
private fun TipCard(
    icon: String,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = CalculatorElevation.Subtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CalculatorPadding.Large,
                    vertical = CalculatorPadding.Large - 2.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Large),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(CalculatorCornerRadius.Large),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(CalculatorSpacing.XSmall))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) 
        MaterialTheme.colorScheme.primaryContainer 
    else 
        MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f) 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
