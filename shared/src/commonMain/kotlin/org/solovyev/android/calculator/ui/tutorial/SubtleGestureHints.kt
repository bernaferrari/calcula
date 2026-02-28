package org.solovyev.android.calculator.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Philosophy:
 * - Never interrupt the user
 * - Show once, dismiss forever
 * - Subtle visual indicators only
 * - No overlays, no forced tutorials, no popups
 * - Gently guiding, never pushing
 */

/**
 * Data class representing the state of gesture hints
 */
data class GestureHintState(
    val hintBarDismissed: Boolean = false,
    val appLaunchCount: Int = 0,
    val buttonIndicatorsShown: Boolean = false,
    val hintsEnabled: Boolean = true
)

/**
 * Manager for subtle gesture hints.
 * Handles persistence and logic for when to show hints.
 */
class SubtleHintManager(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val KEY_HINT_BAR_DISMISSED = booleanPreferencesKey("subtle_hint_bar_dismissed")
        private val KEY_APP_LAUNCH_COUNT = intPreferencesKey("subtle_hint_launch_count")
        private val KEY_BUTTON_INDICATORS_SHOWN = booleanPreferencesKey("subtle_hint_indicators_shown")
        private val KEY_HINTS_ENABLED = booleanPreferencesKey("subtle_hints_enabled")
        private const val MAX_LAUNCHES_FOR_HINT_BAR = 3
    }

    val hintState: Flow<GestureHintState> = dataStore.data.map { prefs ->
        GestureHintState(
            hintBarDismissed = prefs[KEY_HINT_BAR_DISMISSED] ?: false,
            appLaunchCount = prefs[KEY_APP_LAUNCH_COUNT] ?: 0,
            buttonIndicatorsShown = prefs[KEY_BUTTON_INDICATORS_SHOWN] ?: false,
            hintsEnabled = prefs[KEY_HINTS_ENABLED] ?: true
        )
    }

    /**
     * Should the hint bar be shown?
     * Shows for first 3 launches if not dismissed and hints are enabled
     */
    suspend fun shouldShowHintBar(): Boolean {
        val state = hintState.first()
        return state.hintsEnabled &&
               !state.hintBarDismissed &&
               state.appLaunchCount < MAX_LAUNCHES_FOR_HINT_BAR
    }

    /**
     * Should button indicators be shown?
     * Only on first launch, if hints are enabled and not already shown
     */
    suspend fun shouldShowButtonIndicators(): Boolean {
        val state = hintState.first()
        return state.hintsEnabled &&
               !state.buttonIndicatorsShown &&
               state.appLaunchCount == 0
    }

    suspend fun incrementLaunchCount() {
        dataStore.edit { prefs ->
            val current = prefs[KEY_APP_LAUNCH_COUNT] ?: 0
            prefs[KEY_APP_LAUNCH_COUNT] = current + 1
        }
    }

    suspend fun dismissHintBar() {
        dataStore.edit { prefs ->
            prefs[KEY_HINT_BAR_DISMISSED] = true
        }
    }

    suspend fun markButtonIndicatorsShown() {
        dataStore.edit { prefs ->
            prefs[KEY_BUTTON_INDICATORS_SHOWN] = true
        }
    }

    suspend fun setHintsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HINTS_ENABLED] = enabled
        }
    }

    suspend fun resetAllHints() {
        dataStore.edit { prefs ->
            prefs[KEY_HINT_BAR_DISMISSED] = false
            prefs[KEY_BUTTON_INDICATORS_SHOWN] = false
            prefs[KEY_APP_LAUNCH_COUNT] = 0
        }
    }
}

/**
 * Static hint bar displayed below the display area.
 * Shows for first 3 app launches, user can dismiss with X.
 * Never shows again after dismiss.
 *
 * @param hintText The text to display (default: "Swipe buttons for more")
 * @param modifier Modifier for styling
 * @param onDismiss Callback when user dismisses the hint
 */
@Composable
fun StaticHintBar(
    modifier: Modifier = Modifier,
    hintText: String = "Swipe buttons for more",
    onDismiss: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300) // Slight delay for smooth slide-in after display appears
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(400),
            expandFrom = Alignment.Top
        ) + fadeIn(animationSpec = tween(400)),
        exit = shrinkVertically(
            animationSpec = tween(300),
            shrinkTowards = Alignment.Top
        ) + fadeOut(animationSpec = tween(300))
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = hintText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        visible = false
                        onDismiss()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss hint",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Subtle arrow indicators for buttons with swipe actions.
 * Shows tiny "▲" and "▼" on buttons, 30% opacity.
 * Only on first launch, fades out after 10 seconds.
 *
 * @param hasSwipeUp Whether this button has a swipe-up action
 * @param hasSwipeDown Whether this button has a swipe-down action
 * @param modifier Modifier for positioning
 */
@Composable
fun ButtonSubtleIndicators(
    hasSwipeUp: Boolean,
    hasSwipeDown: Boolean,
    modifier: Modifier = Modifier,
    autoFadeDelay: Long = 10000L // 10 seconds
) {
    var visible by remember { mutableStateOf(true) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 0.3f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "indicator_alpha"
    )

    LaunchedEffect(Unit) {
        delay(autoFadeDelay)
        visible = false
    }

    if (!hasSwipeUp && !hasSwipeDown) return

    Box(
        modifier = modifier.alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasSwipeUp) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp)
                )
            }
            if (hasSwipeDown) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

/**
 * Alternative version of button indicators that persists until user interacts.
 * Shows on long-press preview or first launch with longer duration.
 */
@Composable
fun PersistentButtonIndicators(
    hasSwipeUp: Boolean,
    hasSwipeDown: Boolean,
    isPressed: Boolean,
    modifier: Modifier = Modifier
) {
    val targetAlpha = when {
        isPressed -> 0.5f
        else -> 0.3f
    }
    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis = 200),
        label = "persistent_indicator_alpha"
    )

    if (!hasSwipeUp && !hasSwipeDown) return

    Box(
        modifier = modifier.alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasSwipeUp) {
                Text(
                    text = "▲",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        lineHeight = 8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (hasSwipeDown) {
                Text(
                    text = "▼",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        lineHeight = 8.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Small help button that shows in the corner of scientific buttons.
 * Tap shows: "Swipe up for sin, down for asin"
 * Non-intrusive, only appears on buttons with swipe actions.
 *
 * @param helpText The help text to show
 * @param modifier Modifier for positioning
 */
@Composable
fun ButtonHelpIndicator(
    helpText: String,
    modifier: Modifier = Modifier
) {
    var showTooltip by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        showTooltip = !showTooltip
                    }
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = showTooltip,
            enter = fadeIn(animationSpec = tween(150)) +
                    expandVertically(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)) +
                    shrinkVertically(animationSpec = tween(150))
        ) {
            Surface(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .clickable { showTooltip = false },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                tonalElevation = 4.dp
            ) {
                Text(
                    text = helpText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Auto-hide tooltip after 3 seconds
        LaunchedEffect(showTooltip) {
            if (showTooltip) {
                delay(3000)
                showTooltip = false
            }
        }
    }
}

/**
 * Settings item for enabling/disabling gesture hints.
 * Shows in Settings > Help or Settings > Accessibility.
 *
 * @param enabled Current enabled state
 * @param onEnabledChange Callback when user toggles the setting
 * @param modifier Modifier for styling
 */
@Composable
fun GestureHintsSettingsItem(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Show gesture hints",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Subtle indicators for button swipes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChange
        )
    }
}

/**
 * Container that manages the hint bar visibility based on app state.
 * Use this in the main calculator screen.
 *
 * @param hintManager The SubtleHintManager instance
 * @param modifier Modifier for the container
 * @param content Content below the hint bar (typically the keyboard)
 */
@Composable
fun HintBarContainer(
    hintManager: SubtleHintManager,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showHintBar by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showHintBar = hintManager.shouldShowHintBar()
    }

    Column(modifier = modifier) {
        if (showHintBar) {
            StaticHintBar(
                onDismiss = {
                    scope.launch {
                        hintManager.dismissHintBar()
                    }
                    showHintBar = false
                }
            )
        }
        content()
    }
}

/**
 * Integration component that wraps a calculator button with subtle indicators.
 * Automatically manages indicator visibility based on hint state.
 *
 * @param hintManager The SubtleHintManager instance
 * @param hasSwipeUp Whether this button has swipe-up action
 * @param hasSwipeDown Whether this button has swipe-down action
 * @param modifier Modifier for positioning
 * @param content The button content to wrap
 */
@Composable
fun SubtleHintButtonWrapper(
    hintManager: SubtleHintManager,
    hasSwipeUp: Boolean,
    hasSwipeDown: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showIndicators by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showIndicators = hintManager.shouldShowButtonIndicators()
        if (showIndicators) {
            // Mark as shown so they don't appear again
            hintManager.markButtonIndicatorsShown()
        }
    }

    Box(modifier = modifier) {
        content()

        if (showIndicators) {
            ButtonSubtleIndicators(
                hasSwipeUp = hasSwipeUp,
                hasSwipeDown = hasSwipeDown,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
            )
        }
    }
}

/**
 * Preview/Development helper to demonstrate the hint system.
 * Not intended for production use.
 */
@Composable
fun SubtleGestureHintsDemo(
    hintManager: SubtleHintManager
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Demo hint bar
        StaticHintBar(
            onDismiss = {}
        )

        // Demo button with indicators
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("sin", style = MaterialTheme.typography.titleMedium)

            ButtonSubtleIndicators(
                hasSwipeUp = true,
                hasSwipeDown = true,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }

        // Demo help indicator
        ButtonHelpIndicator(
            helpText = "Swipe up for sin, down for asin",
            modifier = Modifier.padding(8.dp)
        )
    }
}
