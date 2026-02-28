package org.solovyev.android.calculator.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Swipe
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
 * Subtle gesture hint banner shown below display
 * Dismissible, remembers dismissal
 */
@Composable
fun GestureHintBanner(
    tutorialManager: TutorialManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var dismissed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dismissed = tutorialManager.hintDismissed.first()
    }

    AnimatedVisibility(
        visible = !dismissed,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Swipe,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(
                    text = "Swipe buttons for more functions",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        scope.launch {
                            tutorialManager.dismissHint()
                            dismissed = true
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss hint",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
