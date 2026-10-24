@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.bernaferrari.calculator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.FlickCatalog
import com.bernaferrari.calculator.ui.calculator.Spec
import com.bernaferrari.calculator.ui.calculator.Tone
import com.bernaferrari.calculator.ui.calculator.customizable
import com.bernaferrari.calculator.ui.calculator.keypadRows
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.calculator.slot
import com.bernaferrari.calculator.ui.calculator.withFlicks
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import org.jetbrains.compose.resources.stringResource

/**
 * The keypad, drawn with every flick as a little tag above or below its key. Tap a tag to choose
 * what that flick does: any built-in function, a constant, one of your functions, or nothing.
 */
@Composable
fun CustomizeKeysScreen(onNavigateBack: () -> Unit, viewModel: SettingsViewModel = viewModel { SettingsViewModel() }) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val decimal = prefs.decimalSeparator.separators().decimal
    val defaults = remember(decimal, prefs.rpn) { keypadRows(decimal, rpn = prefs.rpn) }
    val rows = defaults.withFlicks(FlickCatalog.resolve(prefs.flicks, prefs.functions))
    var picking by remember { mutableStateOf<Pair<Spec, Boolean>?>(null) }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp).fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                Text(stringResource(Res.string.settings_customize_keys), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { haptics.heavy(); viewModel.resetFlicks() }, enabled = prefs.flicks.isNotEmpty()) {
                    Text(stringResource(Res.string.customize_reset))
                }
            }
            Text(
                stringResource(Res.string.customize_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.widthIn(max = 520.dp).fillMaxSize().padding(12.dp),
                ) {
                    rows.forEachIndexed { r, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f).fillMaxWidth()) {
                            row.forEachIndexed { c, spec ->
                                val original = defaults[r][c]
                                KeyTile(
                                    spec = spec,
                                    changedUp = spec.slot(true) in prefs.flicks,
                                    changedDown = spec.slot(false) in prefs.flicks,
                                    onPick = { up -> haptics.tap(); picking = original to up },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    picking?.let { (spec, up) ->
        val slot = spec.slot(up)
        val default = if (up) spec.up else spec.down
        FlickPicker(
            title = stringResource(if (up) Res.string.customize_flick_up else Res.string.customize_flick_down, spec.label),
            defaultLabel = default?.label,
            current = prefs.flicks[slot],
            functions = FlickCatalog.functions(prefs.functions),
            onPick = { id -> haptics.tap(); viewModel.setFlick(slot, id); picking = null },
            onDismiss = { picking = null },
        )
    }
}

@Composable
private fun KeyTile(spec: Spec, changedUp: Boolean, changedDown: Boolean, onPick: (up: Boolean) -> Unit, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val container = when (spec.tone) {
        Tone.Digit -> colors.surfaceContainerHigh
        Tone.Operator -> colors.secondaryContainer
        Tone.Equals -> colors.primary
        Tone.Function -> colors.surfaceContainerHighest
    }
    val content = when (spec.tone) {
        Tone.Operator -> colors.onSecondaryContainer
        Tone.Equals -> colors.onPrimary
        else -> colors.onSurface
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = container,
        contentColor = content,
        modifier = modifier.alpha(if (spec.customizable) 1f else 0.4f),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(4.dp)) {
            if (spec.customizable) FlickTag(spec.up?.label, changedUp) { onPick(true) } else Box(Modifier.height(24.dp))
            Text(spec.label, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 24.sp))
            if (spec.customizable) FlickTag(spec.down?.label, changedDown) { onPick(false) } else Box(Modifier.height(24.dp))
        }
    }
}

/** One flick on the diagram: its label, or a dash when it does nothing. Chosen ones carry the accent. */
@Composable
private fun FlickTag(label: String?, changed: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(if (changed) colors.primary else colors.surface.copy(alpha = 0.7f), label = "tag")
    val ink = if (changed) colors.onPrimary else colors.onSurfaceVariant
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(24.dp)
            .widthIn(min = 36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
    ) {
        Text(label ?: "—", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 12.sp, fontWeight = FontWeight.Medium), color = ink, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
private fun FlickPicker(
    title: String,
    defaultLabel: String?,
    current: String?,
    functions: List<Pair<String, com.bernaferrari.calculator.ui.calculator.CustomFlick>>,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = current == null,
                    onClick = { onPick(null) },
                    label = { Text(stringResource(Res.string.customize_default, defaultLabel ?: "—")) },
                )
                FilterChip(selected = current == FlickCatalog.NONE, onClick = { onPick(FlickCatalog.NONE) }, label = { Text(stringResource(Res.string.customize_none)) })
            }
            Section(stringResource(Res.string.customize_builtin), FlickCatalog.builtIn, current, onPick)
            Section(stringResource(Res.string.library_constants), FlickCatalog.constants(), current, onPick)
            if (functions.isNotEmpty()) Section(stringResource(Res.string.library_functions), functions, current, onPick)
        }
    }
}

@Composable
private fun Section(title: String, items: List<Pair<String, com.bernaferrari.calculator.ui.calculator.CustomFlick>>, current: String?, onPick: (String) -> Unit) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (id, flick) ->
            FilterChip(selected = id == current, onClick = { onPick(id) }, label = { Text(flick.label, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 15.sp)) })
        }
    }
}
