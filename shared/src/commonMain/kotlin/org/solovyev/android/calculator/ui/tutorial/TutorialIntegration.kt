package org.solovyev.android.calculator.ui.tutorial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


/**
 * Data class representing user interaction metrics for hint triggering
 */
data class UserInteractionMetrics(
    val totalButtonPresses: Int = 0,
    val totalCalculations: Int = 0,
    val buttonTapCounts: Map<String, Int> = emptyMap(),
    val lastInteractionTime: Long = 0L,
    val firstLaunchDate: Long = 0L
)

/**
 * Sealed class representing different types of contextual hints
 */
sealed class ContextualHint(
    val id: String,
    val priority: Int,
    val cooldownMs: Long
) {
    data object SwipeUp : ContextualHint("swipe_up", 1, 24 * 60 * 60 * 1000L) // 24 hours
    data object SwipeDown : ContextualHint("swipe_down", 1, 24 * 60 * 60 * 1000L)
    data object SwipeLeft : ContextualHint("swipe_left", 2, 12 * 60 * 60 * 1000L) // 12 hours
    data object SwipeRight : ContextualHint("swipe_right", 2, 12 * 60 * 60 * 1000L)
    data object LongPress : ContextualHint("long_press", 3, 6 * 60 * 60 * 1000L) // 6 hours
    data object DoubleTapEquals : ContextualHint("double_tap_equals", 4, 48 * 60 * 60 * 1000L) // 48 hours
    data object ScientificFunctions : ContextualHint("scientific_functions", 5, 72 * 60 * 60 * 1000L) // 72 hours
    
    companion object {
        fun all(): List<ContextualHint> = listOf(
            SwipeUp, SwipeDown, SwipeLeft, SwipeRight,
            LongPress, DoubleTapEquals, ScientificFunctions
        )
    }
}

/**
 * Settings for tutorial and hint behavior
 */
data class TutorialSettings(
    val hintsEnabled: Boolean = true,
    val hintsDisabledPermanently: Boolean = false,
    val doNotDisturbStartHour: Int = 22, // 10 PM
    val doNotDisturbEndHour: Int = 8,    // 8 AM
    val idleTimeoutMs: Long = 3000L,      // 3 seconds
    val firstWeekRemindersEnabled: Boolean = true
)

/**
 * Manager for tutorial-related DataStore preferences
 */
class TutorialPreferences(private val dataStore: DataStore<Preferences>) {
    
    private val HINTS_ENABLED_KEY = booleanPreferencesKey("tutorial_hints_enabled")
    private val HINTS_DISABLED_PERM_KEY = booleanPreferencesKey("tutorial_hints_disabled_permanent")
    private val BUTTON_PRESS_COUNT_KEY = intPreferencesKey("tutorial_button_press_count")
    private val CALCULATION_COUNT_KEY = intPreferencesKey("tutorial_calculation_count")
    private val FIRST_LAUNCH_DATE_KEY = longPreferencesKey("tutorial_first_launch_date")
    private val LAST_HINT_SHOWN_KEY = longPreferencesKey("tutorial_last_hint_shown")
    private val HINT_COOLDOWNS_KEY_PREFIX = "tutorial_hint_cooldown_"
    
    val hintsEnabled: Flow<Boolean> = dataStore.data.map { 
        it[HINTS_ENABLED_KEY] != false 
    }
    
    val hintsDisabledPermanently: Flow<Boolean> = dataStore.data.map { 
        it[HINTS_DISABLED_PERM_KEY] == true 
    }
    
    val metrics: Flow<UserInteractionMetrics> = dataStore.data.map { prefs ->
        UserInteractionMetrics(
            totalButtonPresses = prefs[BUTTON_PRESS_COUNT_KEY] ?: 0,
            totalCalculations = prefs[CALCULATION_COUNT_KEY] ?: 0,
            buttonTapCounts = emptyMap(), // Simplified for now
            lastInteractionTime = prefs[LAST_HINT_SHOWN_KEY] ?: 0L,
            firstLaunchDate = prefs[FIRST_LAUNCH_DATE_KEY] ?: System.currentTimeMillis()
        )
    }
    
    suspend fun setHintsEnabled(enabled: Boolean) {
        dataStore.edit { it[HINTS_ENABLED_KEY] = enabled }
    }
    
    suspend fun setHintsDisabledPermanently(disabled: Boolean) {
        dataStore.edit { it[HINTS_DISABLED_PERM_KEY] = disabled }
    }
    
    suspend fun incrementButtonPresses() {
        dataStore.edit { prefs ->
            val current = prefs[BUTTON_PRESS_COUNT_KEY] ?: 0
            prefs[BUTTON_PRESS_COUNT_KEY] = current + 1
            prefs[LAST_HINT_SHOWN_KEY] = System.currentTimeMillis()
        }
    }
    
    suspend fun incrementCalculations() {
        dataStore.edit { prefs ->
            val current = prefs[CALCULATION_COUNT_KEY] ?: 0
            prefs[CALCULATION_COUNT_KEY] = current + 1
        }
    }
    
    suspend fun recordButtonTap(buttonId: String) {
        dataStore.edit { prefs ->
            val key = intPreferencesKey("tutorial_button_tap_$buttonId")
            val current = prefs[key] ?: 0
            prefs[key] = current + 1
        }
    }
    
    suspend fun getButtonTapCount(buttonId: String): Int {
        return dataStore.data.first()[intPreferencesKey("tutorial_button_tap_$buttonId")] ?: 0
    }
    
    suspend fun markHintShown(hintId: String) {
        dataStore.edit { prefs ->
            prefs[longPreferencesKey(HINT_COOLDOWNS_KEY_PREFIX + hintId)] = System.currentTimeMillis()
            prefs[LAST_HINT_SHOWN_KEY] = System.currentTimeMillis()
        }
    }
    
    suspend fun getLastHintShownTime(hintId: String): Long {
        return dataStore.data.first()[longPreferencesKey(HINT_COOLDOWNS_KEY_PREFIX + hintId)] ?: 0L
    }
    
    suspend fun isInCooldown(hint: ContextualHint): Boolean {
        val lastShown = getLastHintShownTime(hint.id)
        return System.currentTimeMillis() - lastShown < hint.cooldownMs
    }
    
    suspend fun initializeFirstLaunch() {
        dataStore.edit { prefs ->
            if (prefs[FIRST_LAUNCH_DATE_KEY] == null) {
                prefs[FIRST_LAUNCH_DATE_KEY] = System.currentTimeMillis()
            }
        }
    }
    
    suspend fun resetAllHints() {
        dataStore.edit { prefs ->
            ContextualHint.all().forEach { hint ->
                prefs.remove(longPreferencesKey(HINT_COOLDOWNS_KEY_PREFIX + hint.id))
            }
            prefs[BUTTON_PRESS_COUNT_KEY] = 0
            prefs[CALCULATION_COUNT_KEY] = 0
            prefs[HINTS_DISABLED_PERM_KEY] = false
            prefs[HINTS_ENABLED_KEY] = true
        }
    }
}

/**
 * ViewModel for managing tutorial state and contextual hints
 */
class TutorialViewModel(
    private val tutorialManager: TutorialManager,
    private val tutorialPreferences: TutorialPreferences
) : ViewModel() {
    
    private val _tutorialState = MutableStateFlow(TutorialState())
    val tutorialState: StateFlow<TutorialState> = _tutorialState.asStateFlow()
    
    private val _showTutorial = MutableStateFlow(false)
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()
    
    private val _currentHint = MutableStateFlow<ContextualHint?>(null)
    val currentHint: StateFlow<ContextualHint?> = _currentHint.asStateFlow()
    
    private val _hintSettings = MutableStateFlow(TutorialSettings())
    val hintSettings: StateFlow<TutorialSettings> = _hintSettings.asStateFlow()
    
    private val _isCalculating = MutableStateFlow(false)
    private val _userIdle = MutableStateFlow(false)
    
    private val buttonPresses = mutableStateMapOf<String, Int>()
    private var calculationCount = 0
    
    init {
        viewModelScope.launch {
            // Load initial state
            tutorialPreferences.initializeFirstLaunch()
            _tutorialState.value = tutorialManager.tutorialState.first()
            
            // Check if tutorial should be shown on first launch
            val state = _tutorialState.value
            if (!state.hasCompletedTutorial && !state.shownHints.contains("first_launch_skipped")) {
                _showTutorial.value = true
            }
        }
    }
    
    /**
     * Check if tutorial should be shown for first-time users
     */
    fun shouldShowTutorialOnLaunch(): Boolean {
        return !_tutorialState.value.hasCompletedTutorial &&
               _tutorialState.value.shownHints.isEmpty()
    }
    
    /**
     * Start the tutorial flow
     */
    fun startTutorial() {
        _showTutorial.value = true
    }
    
    /**
     * Complete the tutorial
     */
    fun completeTutorial() {
        viewModelScope.launch {
            tutorialManager.markTutorialCompleted()
            _tutorialState.value = _tutorialState.value.copy(hasCompletedTutorial = true)
            _showTutorial.value = false
        }
    }
    
    /**
     * Skip the tutorial (mark as seen but not completed)
     */
    fun skipTutorial() {
        viewModelScope.launch {
            _tutorialState.value = _tutorialState.value.copy(
                shownHints = _tutorialState.value.shownHints + "first_launch_skipped"
            )
            _showTutorial.value = false
        }
    }
    
    /**
     * Dismiss the tutorial dialog
     */
    fun dismissTutorial() {
        _showTutorial.value = false
    }
    
    /**
     * Record a button press for hint triggering logic
     */
    fun onButtonPressed(buttonId: String) {
        viewModelScope.launch {
            tutorialPreferences.incrementButtonPresses()
            tutorialPreferences.recordButtonTap(buttonId)
            
            val currentCount = buttonPresses[buttonId] ?: 0
            buttonPresses[buttonId] = currentCount + 1
            
            // Check for swipe hint trigger (after 3 rapid taps on same button)
            if (currentCount >= 2 && shouldShowHint(ContextualHint.SwipeUp)) {
                triggerHint(ContextualHint.SwipeUp)
            }
            
            // Check for long press hint trigger (after 5 total button presses)
            val totalPresses = buttonPresses.values.sum()
            if (totalPresses == 5 && shouldShowHint(ContextualHint.LongPress)) {
                triggerHint(ContextualHint.LongPress)
            }
        }
    }
    
    /**
     * Record a calculation for hint triggering logic
     */
    fun onCalculationPerformed() {
        viewModelScope.launch {
            tutorialPreferences.incrementCalculations()
            calculationCount++
            
            // Check for equals double-tap hint trigger (after first calculation)
            if (calculationCount == 1 && shouldShowHint(ContextualHint.DoubleTapEquals)) {
                triggerHint(ContextualHint.DoubleTapEquals)
            }
            
            // Check for scientific functions hint trigger (after 10 calculations)
            if (calculationCount == 10 && shouldShowHint(ContextualHint.ScientificFunctions)) {
                triggerHint(ContextualHint.ScientificFunctions)
            }
        }
    }
    
    /**
     * Set whether a calculation is currently in progress
     */
    fun setCalculating(calculating: Boolean) {
        _isCalculating.value = calculating
    }
    
    /**
     * Set whether the user is idle (no recent interactions)
     */
    fun setUserIdle(idle: Boolean) {
        _userIdle.value = idle
    }
    
    /**
     * Check if a specific hint should be shown
     */
    private suspend fun shouldShowHint(hint: ContextualHint): Boolean {
        val settings = hintSettings.value
        
        // Check if hints are disabled
        if (!settings.hintsEnabled || settings.hintsDisabledPermanently) return false
        
        // Check if in cooldown
        if (tutorialPreferences.isInCooldown(hint)) return false
        
        // Check if user is calculating (don't interrupt)
        if (_isCalculating.value) return false
        
        // Check Do Not Disturb hours
        if (isInDoNotDisturb(settings)) return false
        
        // Check if hint was already shown in tutorial state
        val state = _tutorialState.value
        if (state.shownHints.contains(hint.id)) return false
        
        return true
    }
    
    /**
     * Check if current time is in Do Not Disturb period
     */
    private fun isInDoNotDisturb(settings: TutorialSettings): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return if (settings.doNotDisturbStartHour > settings.doNotDisturbEndHour) {
            // Crosses midnight (e.g., 22:00 to 08:00)
            hour >= settings.doNotDisturbStartHour || hour < settings.doNotDisturbEndHour
        } else {
            hour >= settings.doNotDisturbStartHour && hour < settings.doNotDisturbEndHour
        }
    }
    
    /**
     * Trigger a hint to be shown
     */
    private fun triggerHint(hint: ContextualHint) {
        viewModelScope.launch {
            // Only show if no higher priority hint is showing
            val current = _currentHint.value
            if (current == null || hint.priority < current.priority) {
                _currentHint.value = hint
                tutorialPreferences.markHintShown(hint.id)
                
                // Auto-dismiss after 5 seconds
                delay(5000)
                if (_currentHint.value == hint) {
                    _currentHint.value = null
                }
            }
        }
    }
    
    /**
     * Dismiss the current hint
     */
    fun dismissCurrentHint() {
        _currentHint.value = null
    }
    
    /**
     * Mark a gesture as performed (to hide related hints)
     */
    fun onGesturePerformed(gestureId: String) {
        viewModelScope.launch {
            tutorialManager.markHintShown(gestureId)
            
            // Also hide any related contextual hint
            when (gestureId) {
                "swipe_up" -> if (_currentHint.value == ContextualHint.SwipeUp) _currentHint.value = null
                "swipe_down" -> if (_currentHint.value == ContextualHint.SwipeDown) _currentHint.value = null
                "swipe_left" -> if (_currentHint.value == ContextualHint.SwipeLeft) _currentHint.value = null
                "swipe_right" -> if (_currentHint.value == ContextualHint.SwipeRight) _currentHint.value = null
                "long_press" -> if (_currentHint.value == ContextualHint.LongPress) _currentHint.value = null
                "double_tap" -> if (_currentHint.value == ContextualHint.DoubleTapEquals) _currentHint.value = null
            }
        }
    }
    
    /**
     * Reset all hints (show them again)
     */
    fun resetAllHints() {
        viewModelScope.launch {
            tutorialPreferences.resetAllHints()
            tutorialManager.resetTutorial()
            _tutorialState.value = TutorialState()
        }
    }
    
    /**
     * Disable hints permanently
     */
    fun disableHintsPermanently() {
        viewModelScope.launch {
            tutorialPreferences.setHintsDisabledPermanently(true)
            _hintSettings.value = _hintSettings.value.copy(hintsDisabledPermanently = true)
        }
    }
    
    /**
     * Enable/disable hints
     */
    fun setHintsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            tutorialPreferences.setHintsEnabled(enabled)
            _hintSettings.value = _hintSettings.value.copy(hintsEnabled = enabled)
        }
    }
}

/**
 * Composable that shows a contextual hint overlay
 */
@Composable
fun ContextualHintOverlay(
    hint: ContextualHint,
    anchorPosition: Offset? = null,
    onDismiss: () -> Unit
) {
    val hintData = when (hint) {
        ContextualHint.SwipeUp -> GestureHintData(
            direction = GestureDirection.UP,
            actionLabel = "Swipe Up",
            actionDescription = "Swipe up on buttons to access secondary functions",
            exampleValue = "Try swiping up on sin → asin"
        )
        ContextualHint.SwipeDown -> GestureHintData(
            direction = GestureDirection.DOWN,
            actionLabel = "Swipe Down",
            actionDescription = "Swipe down for alternate functions and constants",
            exampleValue = "Try swiping down on π → e"
        )
        ContextualHint.SwipeLeft -> GestureHintData(
            direction = GestureDirection.LEFT,
            actionLabel = "Swipe Left",
            actionDescription = "Swipe left to quickly clear entries",
            exampleValue = "Swipe left on any number to clear"
        )
        ContextualHint.SwipeRight -> GestureHintData(
            direction = GestureDirection.RIGHT,
            actionLabel = "Swipe Right",
            actionDescription = "Swipe right for additional actions",
            exampleValue = "Try swiping right on numbers"
        )
        ContextualHint.LongPress -> GestureHintData(
            direction = GestureDirection.UP,
            actionLabel = "Long Press",
            actionDescription = "Hold a button to see all available options",
            exampleValue = "Long press any function button"
        )
        ContextualHint.DoubleTapEquals -> GestureHintData(
            direction = GestureDirection.UP,
            actionLabel = "Double Tap =",
            actionDescription = "Double tap the equals button to plot functions",
            exampleValue = "Enter a function, then double tap ="
        )
        ContextualHint.ScientificFunctions -> GestureHintData(
            direction = GestureDirection.UP,
            actionLabel = "Scientific Functions",
            actionDescription = "Swipe up anywhere on the keyboard to reveal scientific functions",
            exampleValue = "Try it now!"
        )
    }
    
    GestureHintOverlay(
        hintData = hintData,
        isVisible = true,
        onDismiss = onDismiss,
        onHintShown = {},
        anchorPosition = anchorPosition
    )
}

/**
 * Composable that shows a small hint indicator on a button
 */
@Composable
fun ButtonHintIndicator(
    hintType: GestureDirection,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = "Gesture hint",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Extension function to check if we're in the first week of use
 */
fun TutorialPreferences.isFirstWeekOfUse(): Flow<Boolean> {
    return metrics.map { metrics ->
        val daysSinceFirstLaunch = (System.currentTimeMillis() - metrics.firstLaunchDate) / (24 * 60 * 60 * 1000)
        daysSinceFirstLaunch < 7
    }
}

/**
 * Extension function to get remaining cooldown time for a hint
 */
suspend fun TutorialPreferences.getCooldownRemaining(hint: ContextualHint): Long {
    val lastShown = getLastHintShownTime(hint.id)
    val elapsed = System.currentTimeMillis() - lastShown
    return (hint.cooldownMs - elapsed).coerceAtLeast(0)
}