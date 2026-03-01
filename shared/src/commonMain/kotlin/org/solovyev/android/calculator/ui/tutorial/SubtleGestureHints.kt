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
 * OBSOLETE: Static hint bar - removed in favor of visual discovery in ModernKeyboard.
 *
 * Visual discovery provides:
 * - Ambient hints: faint secondary functions fade in/out
 * - Touch preview: functions appear when finger touches button
 * - Success glow: confirms successful swipe
 *
 * This composable is kept for backward compatibility but renders nothing.
 */
@Composable
@Deprecated("Use ModernKeyboard visual discovery instead", ReplaceWith(""))
fun StaticHintBar(
    modifier: Modifier = Modifier,
    hintText: String = "",
    onDismiss: () -> Unit = {}
) {
    // Visual discovery in ModernKeyboard replaces text-based hints
}

/**
 * OBSOLETE: Subtle arrow indicators - replaced by visual discovery in ModernKeyboard.
 *
 * ModernKeyboard now shows the actual function labels (sin, cos, etc.) rather
 * than generic arrows. This provides immediate value and clearer communication.
 *
 * Kept for backward compatibility.
 */
@Composable
@Deprecated("Use ModernKeyboard visual discovery instead", ReplaceWith(""))
fun ButtonSubtleIndicators(
    hasSwipeUp: Boolean,
    hasSwipeDown: Boolean,
    modifier: Modifier = Modifier,
    autoFadeDelay: Long = 10000L
) {
    // ModernKeyboard shows actual function names as hints, not generic arrows
}

/**
 * OBSOLETE: Persistent button indicators - replaced by visual discovery.
 *
 * ModernKeyboard shows actual function labels that are more informative
 * than generic arrows.
 *
 * Kept for backward compatibility.
 */
@Composable
@Deprecated("Use ModernKeyboard visual discovery instead", ReplaceWith(""))
fun PersistentButtonIndicators(
    hasSwipeUp: Boolean,
    hasSwipeDown: Boolean,
    isPressed: Boolean,
    modifier: Modifier = Modifier
) {
    // ModernKeyboard shows actual function names as hints
}

/**
 * OBSOLETE: Help button indicator - replaced by visual discovery.
 *
 * ModernKeyboard's touch preview shows available functions directly
 * when the user touches a button. No need for help tooltips.
 *
 * Kept for backward compatibility.
 */
@Composable
@Deprecated("Use ModernKeyboard visual discovery instead", ReplaceWith(""))
fun ButtonHelpIndicator(
    helpText: String,
    modifier: Modifier = Modifier
) {
    // Touch preview in ModernKeyboard provides this information visually
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
 * OBSOLETE: Container for hint bar - replaced by visual discovery.
 *
 * ModernKeyboard manages its own discovery hints internally through
 * GestureDiscoveryState. No external container needed.
 *
 * Kept for backward compatibility.
 */
@Composable
@Deprecated("ModernKeyboard handles discovery internally", ReplaceWith("content()"))
fun HintBarContainer(
    hintManager: SubtleHintManager,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    content()
}

/**
 * OBSOLETE: Button wrapper for hints - replaced by visual discovery.
 *
 * ModernKeyboard's ModernButton composable handles hint display internally
 * through GestureDiscoveryState and shows actual function labels.
 *
 * Kept for backward compatibility.
 */
@Composable
@Deprecated("ModernKeyboard handles discovery internally", ReplaceWith("content()"))
fun SubtleHintButtonWrapper(
    hintManager: SubtleHintManager,
    hasSwipeUp: Boolean,
    hasSwipeDown: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    content()
}

/**
 * OBSOLETE: Demo helper - no longer needed.
 *
 * ModernKeyboard's visual discovery is self-demonstrating through
 * ambient hints and touch previews.
 */
@Composable
@Deprecated("Visual discovery is self-demonstrating", ReplaceWith(""))
fun SubtleGestureHintsDemo(
    hintManager: SubtleHintManager
) {
    // ModernKeyboard demonstrates gestures through actual use
}
