package org.solovyev.android.calculator.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
// Material icons not available in commonMain - using text alternatives
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Data class representing the state of the tutorial
 */
data class TutorialState(
    val hasCompletedTutorial: Boolean = false,
    val shownHints: Set<String> = emptySet()
)

/**
 * Minimal tutorial manager - tracks tutorial completion and shown hints
 */
open class TutorialManager(private val dataStore: DataStore<Preferences>) {
    private val HINT_DISMISSED_KEY = booleanPreferencesKey("gesture_hint_dismissed")
    private val TUTORIAL_COMPLETED_KEY = booleanPreferencesKey("tutorial_completed")
    private val SHOWN_HINTS_KEY = stringPreferencesKey("shown_hints")

    val hintDismissed: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HINT_DISMISSED_KEY] ?: false
    }

    open val tutorialState: Flow<TutorialState> = dataStore.data.map { prefs ->
        val completed = prefs[TUTORIAL_COMPLETED_KEY] ?: false
        val hintsString = prefs[SHOWN_HINTS_KEY] ?: ""
        val hints = if (hintsString.isEmpty()) emptySet() else hintsString.split(",").toSet()
        TutorialState(
            hasCompletedTutorial = completed,
            shownHints = hints
        )
    }

    suspend fun dismissHint() {
        dataStore.edit { prefs ->
            prefs[HINT_DISMISSED_KEY] = true
        }
    }

    suspend fun resetHint() {
        dataStore.edit { prefs ->
            prefs[HINT_DISMISSED_KEY] = false
        }
    }

    open suspend fun markTutorialCompleted() {
        dataStore.edit { prefs ->
            prefs[TUTORIAL_COMPLETED_KEY] = true
        }
    }

    open suspend fun resetTutorial() {
        dataStore.edit { prefs ->
            prefs[TUTORIAL_COMPLETED_KEY] = false
            prefs[SHOWN_HINTS_KEY] = ""
        }
    }

    open suspend fun markHintShown(hintId: String) {
        dataStore.edit { prefs ->
            val currentHints = prefs[SHOWN_HINTS_KEY] ?: ""
            val hintsSet = if (currentHints.isEmpty()) mutableSetOf() else currentHints.split(",").toMutableSet()
            hintsSet.add(hintId)
            prefs[SHOWN_HINTS_KEY] = hintsSet.joinToString(",")
        }
    }
}

/**
 * OBSOLETE: Gesture hint banner - removed in favor of visual discovery.
 * 
 * The ModernKeyboard now uses pure visual gesture discovery:
 * - Ambient hints: faint secondary functions fade in/out
 * - Touch preview: functions appear when finger touches button
 * - Success glow: confirms successful swipe
 * 
 * This composable is kept for backward compatibility but renders nothing.
 */
@Composable
@Deprecated("Use ModernKeyboard visual discovery instead", ReplaceWith(""))
fun GestureHintBanner(
    tutorialManager: TutorialManager,
    modifier: Modifier = Modifier
) {
    // Visual discovery in ModernKeyboard replaces this
    // No text, no banners, no emojis - pure visual communication
}
