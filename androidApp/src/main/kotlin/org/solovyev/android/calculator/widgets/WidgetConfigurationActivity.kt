package org.solovyev.android.calculator.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.solovyev.android.calculator.AppPreferences
import org.solovyev.android.calculator.ui.theme.CalculatorTheme

/**
 * Configuration activity for calculator widgets.
 * Allows users to customize widget appearance before adding to home screen.
 */
class WidgetConfigurationActivity : ComponentActivity() {

    private val appPreferences: AppPreferences by inject()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the widget ID from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        // If the widget ID is invalid, finish
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setResult(RESULT_CANCELED)

        setContent {
            CalculatorTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Widget Settings") }
                        )
                    }
                ) { padding ->
                    WidgetConfigurationContent(
                        modifier = Modifier.padding(padding),
                        appPreferences = appPreferences,
                        onSave = { saveConfiguration() },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun saveConfiguration() {
        // Update the widget
        WidgetUpdateWorker.triggerImmediateUpdate(this)

        // Return the widget ID
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }
}

@Composable
private fun WidgetConfigurationContent(
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    var useDynamicColors by remember { mutableStateOf(true) }
    var isLightTheme by remember { mutableStateOf(false) }
    var backgroundOpacity by remember { mutableFloatStateOf(1.0f) }
    var enableHaptics by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Colors Toggle
        SettingRow(
            title = "Use Dynamic Colors",
            description = "Match your wallpaper colors (Android 12+)"
        ) {
            Switch(
                checked = useDynamicColors,
                onCheckedChange = { useDynamicColors = it }
            )
        }

        // Light/Dark Theme Toggle
        SettingRow(
            title = "Light Theme",
            description = "Use light colors for widget"
        ) {
            Switch(
                checked = isLightTheme,
                onCheckedChange = { isLightTheme = it }
            )
        }

        // Background Opacity
        Column {
            Text(
                text = "Background Opacity",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${(backgroundOpacity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Slider(
                value = backgroundOpacity,
                onValueChange = { backgroundOpacity = it },
                valueRange = 0.3f..1.0f
            )
        }

        // Haptics Toggle
        SettingRow(
            title = "Haptic Feedback",
            description = "Vibrate on button press"
        ) {
            Switch(
                checked = enableHaptics,
                onCheckedChange = { enableHaptics = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    scope.launch {
                        // Save preferences
                        appPreferences.widgets.setCalculatorOpacity(backgroundOpacity)
                        appPreferences.theme.setUseDynamicColors(useDynamicColors)
                        appPreferences.theme.setLightTheme(isLightTheme)
                        appPreferences.haptics.setEnabled(enableHaptics)
                        onSave()
                    }
                }
            ) {
                Text("Add Widget")
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    description: String,
    control: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        control()
    }
}
