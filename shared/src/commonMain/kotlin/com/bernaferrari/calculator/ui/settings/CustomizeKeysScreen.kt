@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.bernaferrari.calculator.ui.settings

import androidx.compose.foundation.background
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.foundation.BorderStroke
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
import com.bernaferrari.calculator.ui.calculator.tapSlot
import com.bernaferrari.calculator.platform.rememberTextSharer
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
    var picking by remember { mutableStateOf<Pair<Spec, Slot>?>(null) }
    var menu by remember { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    val share = rememberTextSharer()
    val slots = remember(defaults) { defaults.flatten().flatMap { listOf(it.tapSlot, it.slot(true), it.slot(false)) }.toSet() }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp).fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                Text(stringResource(Res.string.settings_customize_keys), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                Box {
                    IconButton(onClick = { haptics.tap(); menu = true }) { Icon(Icons.Rounded.MoreVert, contentDescription = null) }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.customize_export)) },
                            leadingIcon = { Icon(Icons.Rounded.IosShare, contentDescription = null) },
                            onClick = { menu = false; share(FlickCatalog.export(prefs.flicks)) },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.customize_import)) },
                            leadingIcon = { Icon(Icons.Rounded.Download, contentDescription = null) },
                            onClick = { menu = false; importing = true },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.customize_reset)) },
                            leadingIcon = { Icon(Icons.Rounded.RestartAlt, contentDescription = null) },
                            enabled = prefs.flicks.isNotEmpty(),
                            onClick = { menu = false; haptics.heavy(); viewModel.resetFlicks() },
                        )
                    }
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
                                    changedTap = spec.tapSlot in prefs.flicks,
                                    changedUp = spec.slot(true) in prefs.flicks,
                                    changedDown = spec.slot(false) in prefs.flicks,
                                    onPick = { slot -> haptics.tap(); picking = original to slot },
                                    modifier = Modifier.weight(1f).fillMaxHeight(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    picking?.let { (spec, which) ->
        val slot = when (which) { Slot.Tap -> spec.tapSlot; Slot.Up -> spec.slot(true); Slot.Down -> spec.slot(false) }
        val defaultLabel = when (which) { Slot.Tap -> spec.label; Slot.Up -> spec.up?.label; Slot.Down -> spec.down?.label }
        FlickPicker(
            title = stringResource(
                when (which) { Slot.Tap -> Res.string.customize_tap; Slot.Up -> Res.string.customize_flick_up; Slot.Down -> Res.string.customize_flick_down },
                spec.label,
            ),
            defaultLabel = defaultLabel,
            keysFirst = which == Slot.Tap,
            current = prefs.flicks[slot],
            functions = FlickCatalog.functions(prefs.functions),
            onPick = { id -> haptics.tap(); viewModel.setFlick(slot, id); picking = null },
            onDismiss = { picking = null },
        )
    }

    if (importing) {
        ImportDialog(
            read = { FlickCatalog.import(it, slots) },
            onImport = { haptics.confirm(); viewModel.setFlicks(it); importing = false },
            onDismiss = { importing = false },
        )
    }
}

internal enum class Slot { Tap, Up, Down }

/** A text field to paste an exported layout into; says so when the text isn't one. */
@Composable
private fun ImportDialog(read: (String) -> Map<String, String>?, onImport: (Map<String, String>) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var failed by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.customize_import_title)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it; failed = false },
                placeholder = { Text(stringResource(Res.string.customize_import_hint)) },
                isError = failed,
                supportingText = if (failed) ({ Text(stringResource(Res.string.customize_import_failed)) }) else null,
                minLines = 3,
                maxLines = 6,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { read(text)?.let(onImport) ?: run { failed = true } }, enabled = text.isNotBlank()) {
                Text(stringResource(Res.string.customize_import))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.function_cancel)) } },
    )
}

@Composable
private fun KeyTile(spec: Spec, changedTap: Boolean, changedUp: Boolean, changedDown: Boolean, onPick: (Slot) -> Unit, modifier: Modifier) {
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
        // A changed tap gets the accent as a ring, so the key keeps its own color.
        border = if (changedTap) BorderStroke(2.dp, colors.primary) else null,
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(4.dp)) {
            FlickTag(spec.up?.label, changedUp) { onPick(Slot.Up) }
            // The face itself: tap to choose what tapping the key does.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onPick(Slot.Tap) },
            ) {
                Text(
                    if (spec.blank) "—" else spec.label,
                    style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 24.sp),
                    maxLines = 1,
                    modifier = Modifier.alpha(if (spec.blank) 0.4f else 1f),
                )
            }
            FlickTag(spec.down?.label, changedDown) { onPick(Slot.Down) }
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
    keysFirst: Boolean,
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
            // A key's tap most likely wants another key; a flick, a function.
            if (keysFirst) Section(stringResource(Res.string.customize_basics), FlickCatalog.basics, current, onPick)
            Section(stringResource(Res.string.customize_builtin), FlickCatalog.builtIn, current, onPick)
            if (!keysFirst) Section(stringResource(Res.string.customize_basics), FlickCatalog.basics, current, onPick)
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
