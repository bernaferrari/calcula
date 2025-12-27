package org.solovyev.android.calculator.wizard

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.release.ChooseThemeReleaseNoteStep
import org.solovyev.android.calculator.release.ReleaseNoteStep
import org.solovyev.android.calculator.release.ReleaseNotes
import org.solovyev.android.wizard.ListWizardFlow
import org.solovyev.android.wizard.WizardStep
import kotlin.math.abs

@Composable
fun WizardScreen(
    flow: ListWizardFlow,
    step: WizardStep,
    wizardName: String,
    settingsState: WizardSettingsState,
    onModeChange: (Preferences.Gui.Mode) -> Unit,
    onThemeChange: (Preferences.Gui.Theme) -> Unit,
    onShowAppIconChange: (Boolean) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onFinish: () -> Unit,
    onFinishAbruptly: () -> Unit
) {
    val canGoNext = flow.getNextStep(step) != null
    val canGoPrev = flow.getPrevStep(step) != null
    val firstTimeWizard = wizardName == CalculatorWizards.FIRST_TIME_WIZARD
    val nextLabel = when {
        canGoNext && (canGoPrev || !firstTimeWizard) -> R.string.cpp_wizard_next
        canGoNext -> R.string.cpp_wizard_start
        else -> R.string.cpp_wizard_finish
    }
    val prevLabel = if (canGoPrev) R.string.cpp_wizard_back else R.string.cpp_wizard_skip

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WizardStepHeader(step = step)
        WizardStepContent(
            step = step,
            settingsState = settingsState,
            onModeChange = onModeChange,
            onThemeChange = onThemeChange,
            onShowAppIconChange = onShowAppIconChange
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (canGoPrev || firstTimeWizard) {
                OutlinedButton(
                    onClick = {
                        if (canGoPrev) onPrev() else onFinishAbruptly()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(prevLabel))
                }
            }
            Button(
                onClick = { if (canGoNext) onNext() else onFinish() },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(nextLabel))
            }
        }
    }
}

@Composable
private fun WizardStepHeader(step: WizardStep) {
    val title = when (step) {
        is CalculatorWizardStep -> step.titleResId.takeIf { it != 0 }?.let { stringResource(it) }
        else -> null
    }
    title?.let {
        Text(text = it, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun WizardStepContent(
    step: WizardStep,
    settingsState: WizardSettingsState,
    onModeChange: (Preferences.Gui.Mode) -> Unit,
    onThemeChange: (Preferences.Gui.Theme) -> Unit,
    onShowAppIconChange: (Boolean) -> Unit
) {
    when (step) {
        is CalculatorWizardStep -> when (step) {
            CalculatorWizardStep.WELCOME -> WelcomeStep()
            CalculatorWizardStep.CHOOSE_MODE -> ChooseModeStep(settingsState.mode, onModeChange)
            CalculatorWizardStep.CHOOSE_THEME -> ChooseThemeStep(settingsState.theme, onThemeChange)
            CalculatorWizardStep.ON_SCREEN_CALCULATOR -> OnscreenStep(settingsState.showAppIcon, onShowAppIconChange)
            CalculatorWizardStep.DRAG_BUTTON -> DragButtonStep()
            CalculatorWizardStep.LAST -> FinalStep()
        }
        is ChooseThemeReleaseNoteStep -> ChooseThemeStep(
            theme = settingsState.theme,
            onThemeChange = onThemeChange,
            introText = stringResource(R.string.cpp_release_notes_choose_theme)
        )
        is ReleaseNoteStep -> ReleaseNoteStepContent(step)
        else -> {}
    }
}

@Composable
private fun WelcomeStep() {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.foundation.Image(
            painter = painterResource(if (isLight) R.drawable.logo_wizard_light else R.drawable.logo_wizard),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.c_first_start_text), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ChooseModeStep(
    mode: Preferences.Gui.Mode,
    onModeChange: (Preferences.Gui.Mode) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Modern mode card - full width, featured as recommended
        ModeOptionFull(
            title = stringResource(R.string.cpp_mode_modern),
            selected = mode == Preferences.Gui.Mode.modern,
            description = stringResource(R.string.cpp_wizard_mode_modern_description),
            onClick = { onModeChange(Preferences.Gui.Mode.modern) },
            recommended = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ModeOption(
                title = stringResource(R.string.cpp_mode_simple),
                selected = mode == Preferences.Gui.Mode.simple,
                description = stringResource(R.string.cpp_wizard_mode_simple_description),
                onClick = { onModeChange(Preferences.Gui.Mode.simple) }
            )
            ModeOption(
                title = stringResource(R.string.cpp_mode_engineer),
                selected = mode == Preferences.Gui.Mode.engineer,
                description = stringResource(R.string.cpp_wizard_mode_engineer_description),
                onClick = { onModeChange(Preferences.Gui.Mode.engineer) }
            )
        }
    }
}

@Composable
private fun ModeOptionFull(
    title: String,
    selected: Boolean,
    description: String,
    onClick: () -> Unit,
    recommended: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                if (recommended) {
                    Text(
                        text = "Recommended",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RowScope.ModeOption(
    title: String,
    selected: Boolean,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ChooseThemeStep(
    theme: Preferences.Gui.Theme,
    onThemeChange: (Preferences.Gui.Theme) -> Unit,
    introText: String? = null
) {
    val options = listOf(
        Preferences.Gui.Theme.material_theme,
        Preferences.Gui.Theme.material_black_theme,
        Preferences.Gui.Theme.material_light_theme,
        Preferences.Gui.Theme.metro_blue_theme,
        Preferences.Gui.Theme.metro_green_theme,
        Preferences.Gui.Theme.metro_purple_theme
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        introText?.let { Text(text = it) }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                ThemeOption(
                    title = option.getName(LocalContext.current),
                    selected = option == theme,
                    onClick = { onThemeChange(option) }
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(title: String, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer
            else MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primary)
                    .border(1.dp, MaterialTheme.colorScheme.onPrimary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = title)
        }
    }
}

@Composable
private fun OnscreenStep(
    showAppIcon: Boolean,
    onShowAppIconChange: (Boolean) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.background.luminance() > 0.5f
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        androidx.compose.foundation.Image(
            painter = painterResource(if (isLight) R.drawable.logo_wizard_window_light else R.drawable.logo_wizard_window),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )
        Text(text = stringResource(R.string.cpp_wizard_onscreen_description))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Switch(checked = showAppIcon, onCheckedChange = onShowAppIconChange)
            Text(text = stringResource(R.string.cpp_wizard_onscreen_checkbox))
        }
    }
}

@Composable
private fun DragButtonStep() {
    var action by remember { mutableStateOf(DragAction.Center) }
    val instructions = when (action) {
        DragAction.Center -> stringResource(R.string.cpp_wizard_dragbutton_action_center)
        DragAction.Up -> stringResource(R.string.cpp_wizard_dragbutton_action_up)
        DragAction.Down -> stringResource(R.string.cpp_wizard_dragbutton_action_down)
        DragAction.End -> stringResource(R.string.cpp_wizard_dragbutton_action_end)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.cpp_wizard_dragbutton_description), textAlign = TextAlign.Center)
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.extraLarge)
                .border(2.dp, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraLarge)
                .clickable {
                    if (action == DragAction.Center || action == DragAction.End) {
                        action = action.next()
                    }
                }
                .pointerInput(action) {
                    detectDragGestures { _, dragAmount ->
                        if (abs(dragAmount.y) > abs(dragAmount.x)) {
                            val direction = if (dragAmount.y < 0) DragDirection.Up else DragDirection.Down
                            if (action.expectedDirection == direction) {
                                action = action.next()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "9", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Text(text = instructions, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinalStep() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = stringResource(R.string.cpp_wizard_final_done), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReleaseNoteStepContent(step: ReleaseNoteStep) {
    val context = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val linkColor = MaterialTheme.colorScheme.primary
    val version = step.name.removePrefix("release-note-").toIntOrNull() ?: 0
    val title = stringResource(R.string.cpp_new_in_version, ReleaseNotes.getReleaseNoteVersion(version))
    val description = ReleaseNotes.getReleaseNoteDescription(context, version)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        AndroidView(
            factory = { ctx ->
                TextView(ctx).apply {
                    text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    setTextColor(textColor.toArgb())
                    setLinkTextColor(linkColor.toArgb())
                }
            },
            update = { view ->
                view.setTextColor(textColor.toArgb())
                view.setLinkTextColor(linkColor.toArgb())
            }
        )
    }
}

private enum class DragAction {
    Center,
    Up,
    Down,
    End;

    val expectedDirection: DragDirection?
        get() = when (this) {
            Center -> null
            Up -> DragDirection.Up
            Down -> DragDirection.Down
            End -> null
        }

    fun next(): DragAction {
        return when (this) {
            Center -> Up
            Up -> Down
            Down -> End
            End -> Center
        }
    }
}

private enum class DragDirection {
    Up,
    Down
}
