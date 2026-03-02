package org.solovyev.android.calculator.ui.about

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.jetbrains.compose.resources.stringResource
import org.solovyev.android.calculator.ui.*
import org.solovyev.android.calculator.ui.tokens.CalculatorCornerRadius
import org.solovyev.android.calculator.ui.tokens.CalculatorElevation
import org.solovyev.android.calculator.ui.tokens.CalculatorPadding
import org.solovyev.android.calculator.ui.tokens.CalculatorSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

/**
 * About screen data for customization
 */
data class AboutScreenData(
    val versionName: String = "",
    val releaseNotesContent: String = "",
    val isLightTheme: Boolean = false
)

private data class AboutQuickAction(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit
)

/**
 * Actions that can be performed from the About screen.
 * The actual implementation is platform-specific (Android Intent, iOS URL handling).
 */
interface AboutActions {
    fun openPlayStore()
    fun openSourceCode()
    fun openFacebook()
    fun openDeveloperWebsite()
    fun sendEmail()
    fun openWebsite()
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AboutScreen(
    viewModel: AboutViewModel = koinViewModel(),
    actions: AboutActions = koinInject(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            StandardTopAppBar(
                title = stringResource(Res.string.cpp_about),
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = {
                        Text(
                            text = stringResource(Res.string.cpp_about),
                            fontWeight = if (pagerState.currentPage == 0) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = {
                        Text(
                            text = stringResource(Res.string.cpp_release_notes),
                            fontWeight = if (pagerState.currentPage == 1) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }

                HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> AboutTab(data = state, actions = actions)
                    1 -> ReleaseNotesTab(releaseNotesContent = state.releaseNotesContent)
                }
            }
        }
    }
}

@Composable
private fun AboutTab(
    data: AboutScreenData,
    actions: AboutActions
) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    val currentYear = remember {
        Instant
            .fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .year
    }
    val translators = stringResource(Res.string.cpp_translators_list)
    val showTranslators = translators.isNotBlank()

    var headerVisible by remember { mutableStateOf(false) }
    var cardsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!reduceMotion) delay(100)
        headerVisible = true
        if (!reduceMotion) delay(150)
        cardsVisible = true
    }

    val headerScale by animateFloatAsState(
        targetValue = if (headerVisible) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "headerScale"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = CalculatorPadding.Standard,
            vertical = CalculatorPadding.Large
        ),
        verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.Large + 4.dp)
    ) {
        // Hero Header Card with improved design
        item {
            AnimatedVisibility(
                visible = headerVisible,
                enter = if (reduceMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn() + scaleIn(initialScale = 0.95f, animationSpec = spring(stiffness = Spring.StiffnessLow))
                }
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(headerScale),
                    shape = RoundedCornerShape(CalculatorCornerRadius.Display),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = CalculatorElevation.Elevated)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // App icon with animated shadow
                            Surface(
                                modifier = Modifier.size(96.dp),
                                shape = RoundedCornerShape(CalculatorCornerRadius.Display),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = CalculatorElevation.Hero,
                                tonalElevation = CalculatorElevation.Standard
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(CalculatorSpacing.Large + 4.dp))

                            Text(
                                text = stringResource(Res.string.cpp_app_name),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(CalculatorSpacing.Small))

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(Res.string.cpp_version_format, data.versionName),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(CalculatorSpacing.Medium))

                            Text(
                                text = stringResource(Res.string.cpp_about_tagline),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions with improved cards
        item {
            AnimatedVisibility(
                visible = cardsVisible,
                enter = if (reduceMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn(animationSpec = tween(400, delayMillis = 100)) +
                        slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(400, delayMillis = 100))
                }
            ) {
                val quickActions = listOf(
                    AboutQuickAction(
                        id = "rate",
                        icon = Icons.Filled.Star,
                        title = stringResource(Res.string.cpp_about_rate),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = { actions.openPlayStore() }
                    ),
                    AboutQuickAction(
                        id = "source",
                        icon = Icons.Filled.Code,
                        title = stringResource(Res.string.cpp_about_source),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = { actions.openSourceCode() }
                    ),
                    AboutQuickAction(
                        id = "support",
                        icon = Icons.Filled.Info,
                        title = stringResource(Res.string.cpp_about_support),
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        onClick = { actions.openFacebook() }
                    )
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium)
                ) {
                    items(quickActions, key = { it.id }) { action ->
                        QuickActionCard(
                            modifier = Modifier.width(136.dp),
                            icon = action.icon,
                            title = action.title,
                            containerColor = action.containerColor,
                            contentColor = action.contentColor,
                            onClick = action.onClick
                        )
                    }
                }
            }
        }

        // Developer Section
        item {
            AnimatedVisibility(
                visible = cardsVisible,
                enter = if (reduceMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn(animationSpec = tween(400, delayMillis = 200)) +
                        slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(400, delayMillis = 200))
                }
            ) {
                AboutSection(title = stringResource(Res.string.cpp_about_developer)) {
                    AboutListItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(Res.string.cpp_about_created_by),
                        subtitle = stringResource(Res.string.cpp_about_developer_identity),
                        onClick = { actions.openDeveloperWebsite() }
                    )
                    AboutListItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(Res.string.cpp_about_contact),
                        subtitle = stringResource(Res.string.cpp_about_developer_email),
                        onClick = { actions.sendEmail() }
                    )
                    AboutListItem(
                        icon = Icons.Filled.Language,
                        title = stringResource(Res.string.cpp_about_website),
                        subtitle = stringResource(Res.string.cpp_about_developer_site),
                        onClick = { actions.openWebsite() }
                    )
                }
            }
        }

        // Legal Section
        item {
            AnimatedVisibility(
                visible = cardsVisible,
                enter = if (reduceMotion) {
                    fadeIn(tween(80))
                } else {
                    fadeIn(animationSpec = tween(400, delayMillis = 300)) +
                        slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(400, delayMillis = 300))
                }
            ) {
                AboutSection(title = stringResource(Res.string.cpp_about_legal_open_source)) {
                    AboutListItem(
                        icon = Icons.Filled.Info,
                        title = stringResource(Res.string.cpp_about_license),
                        subtitle = stringResource(Res.string.cpp_about_license_value)
                    )
                    AboutListItem(
                        icon = Icons.Filled.Settings,
                        title = stringResource(Res.string.cpp_about_libraries),
                        subtitle = stringResource(Res.string.cpp_about_libraries_value)
                    )
                }
            }
        }

        // Translators Section
        if (showTranslators) {
            item {
                AnimatedVisibility(
                    visible = cardsVisible,
                    enter = if (reduceMotion) {
                        fadeIn(tween(80))
                    } else {
                        fadeIn(animationSpec = tween(400, delayMillis = 400)) +
                            slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(400, delayMillis = 400))
                    }
                ) {
                    AboutSection(title = stringResource(Res.string.cpp_translators_text)) {
                        AboutListItem(
                            icon = Icons.Filled.Language,
                            title = stringResource(Res.string.cpp_about_contributors),
                            subtitle = translators
                        )
                    }
                }
            }
        }

        // Copyright
        item {
            AnimatedVisibility(
                visible = cardsVisible,
                enter = if (reduceMotion) fadeIn(tween(80)) else fadeIn(animationSpec = tween(400, delayMillis = 500))
            ) {
                Text(
                    text = stringResource(Res.string.cpp_copyright_format, currentYear),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = CalculatorPadding.Large),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = CalculatorElevation.Standard),
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CalculatorPadding.Standard),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(CalculatorSpacing.Small + 2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AboutSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = CalculatorPadding.XSmall, bottom = CalculatorPadding.Medium)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(CalculatorCornerRadius.ExtraLarge),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = CalculatorElevation.Subtle)
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun AboutListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        onClick = {
            if (onClick != null) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = CalculatorPadding.Standard,
                    vertical = CalculatorPadding.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(CalculatorCornerRadius.Standard),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(CalculatorSpacing.Large))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(CalculatorSpacing.XSmall))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReleaseNotesTab(releaseNotesContent: String) {
    val reduceMotion = LocalCalculatorReduceMotion.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(CalculatorPadding.Standard),
        verticalArrangement = Arrangement.spacedBy(CalculatorSpacing.Medium)
    ) {
        item {
            AnimatedVisibility(
                visible = visible,
                enter = if (reduceMotion) fadeIn(tween(80)) else fadeIn() + slideInVertically { it / 3 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(CalculatorPadding.Large),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(CalculatorSpacing.Large))
                        Column {
                            Text(
                                text = stringResource(Res.string.cpp_release_notes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(Res.string.cpp_about_whats_new),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        item {
            AnimatedVisibility(
                visible = visible,
                enter = if (reduceMotion) fadeIn(tween(80)) else fadeIn() + slideInVertically { it / 2 }
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Text(
                        text = if (releaseNotesContent.isNotEmpty()) {
                            org.solovyev.android.calculator.ui.rememberHtml(releaseNotesContent)
                        } else {
                            androidx.compose.ui.text.AnnotatedString(stringResource(Res.string.cpp_about_no_release_notes))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(CalculatorPadding.Large)
                    )
                }
            }
        }
    }
}
