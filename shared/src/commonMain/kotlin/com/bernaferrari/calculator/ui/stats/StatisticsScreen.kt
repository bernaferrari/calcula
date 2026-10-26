@file:OptIn(ExperimentalLayoutApi::class)

package com.bernaferrari.calculator.ui.stats

import androidx.compose.foundation.layout.Spacer
import com.bernaferrari.calculator.ui.topBarInset
import com.bernaferrari.calculator.ui.TopBar
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.math.toDecimalOrNull
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import com.bernaferrari.calculator.domain.stats.Statistics
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.Haptics
import com.bernaferrari.calculator.ui.calculator.KeyCell
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class StatsState(val values: List<String> = emptyList(), val entry: String = "")

/** The list lives in settings, so it's still there next time. */
class StatisticsViewModel(private val preferences: PreferencesRepository = AppGraph.preferences) : ViewModel() {
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state

    init {
        viewModelScope.launch { _state.value = StatsState(values = preferences.getUserPreferences().first().statsData) }
    }

    fun onDigit(d: Char) = edit { if (it.length >= 20) it else if (it == "0") d.toString() else it + d }
    fun onDot() = edit { if ('.' in it) it else if (it.isEmpty() || it == "−") "${it}0." else "$it." }
    fun onNegate() = edit { if (it.startsWith("−")) it.drop(1) else "−$it" }
    fun onDelete() = edit { it.dropLast(1) }

    fun onAdd(): Boolean {
        val entry = _state.value.entry
        val value = entry.replace('−', '-').removeSuffix(".").toDecimalOrNull() ?: return false
        save(_state.value.values + NumberFormatter.format(value, 20))
        _state.value = _state.value.copy(entry = "")
        return true
    }

    fun onRemove(index: Int) = save(_state.value.values.filterIndexed { i, _ -> i != index })
    fun onClear() { save(emptyList()); _state.value = StatsState() }

    private fun edit(change: (String) -> String) { _state.value = _state.value.copy(entry = change(_state.value.entry)) }

    private fun save(values: List<String>) {
        _state.value = _state.value.copy(values = values)
        viewModelScope.launch { preferences.update { it.copy(statsData = values) } }
    }
}

/**
 * A list of numbers and what can be said about it: type, Add, repeat. Every result is a tile;
 * tap one to take it back to the calculator.
 */
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    onUse: (String) -> Unit,
    viewModel: StatisticsViewModel = viewModel { StatisticsViewModel() },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val formatter = remember(prefs.decimalSeparator) { DisplayFormatter(prefs.decimalSeparator.separators()) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val values = remember(state.values) { state.values.mapNotNull { it.replace('−', '-').toDecimalOrNull() } }
    val summary = remember(values) { Statistics.summarize(values) }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            val keypadHeight = (maxHeight * 0.36f).coerceIn(260.dp, 340.dp)
            Column(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                TopBar {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    Text(stringResource(Res.string.menu_statistics), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    TextButton(onClick = { haptics.heavy(); viewModel.onClear() }, enabled = state.values.isNotEmpty() || state.entry.isNotEmpty()) {
                        Text(stringResource(Res.string.stats_clear))
                    }
                }
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(topBarInset()))
                    // The numbers so far, each a chip you can take out; the one being typed at the end.
                    // The same 8dp between rows as between chips: chips otherwise pad themselves out to a
                    // 48dp touch target, which opens the rows up twice as wide as the columns.
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.animateContentSize(com.bernaferrari.calculator.ui.motionSpec(androidx.compose.animation.core.spring()))) {
                        state.values.forEachIndexed { i, v ->
                            InputChip(
                                selected = false,
                                onClick = { haptics.tap(); viewModel.onRemove(i) },
                                label = { Text(formatter.number(v), style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 16.sp)) },
                                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(InputChipDefaults.IconSize)) },
                            )
                        }
                        InputChip(
                            selected = true,
                            onClick = {},
                            label = {
                                Text(
                                    (if (state.entry.isEmpty()) "" else formatter.number(state.entry)) + "|",
                                    style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 16.sp),
                                )
                            },
                        )
                    }
                    }
                    if (summary == null) {
                        Text(
                            stringResource(Res.string.stats_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    } else {
                        val tiles = listOf(
                            Res.string.stats_mean to summary.mean,
                            Res.string.stats_median to summary.median,
                            Res.string.stats_sum to summary.sum,
                            Res.string.stats_count to Decimal(summary.count),
                            Res.string.stats_sd_population to summary.populationSd,
                            Res.string.stats_sd_sample to summary.sampleSd,
                            Res.string.stats_min to summary.min,
                            Res.string.stats_max to summary.max,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 16.dp)) {
                            tiles.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    pair.forEachIndexed { i, (label, value) ->
                                        StatTile(label, value?.let { NumberFormatter.format(it, 10) }, formatter, emphasized = label == Res.string.stats_mean) {
                                            haptics.confirm(); onUse(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
                StatsKeypad(
                    haptics = haptics,
                    decimal = formatter.decimal,
                    canAdd = state.entry.replace("−", "").isNotEmpty(),
                    onDigit = viewModel::onDigit,
                    onDot = viewModel::onDot,
                    onNegate = viewModel::onNegate,
                    onDelete = viewModel::onDelete,
                    onAdd = { if (viewModel.onAdd()) haptics.confirm() else haptics.reject() },
                    modifier = Modifier.fillMaxWidth().height(keypadHeight + bottomInset).padding(bottom = bottomInset),
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatTile(label: StringResource, value: String?, formatter: DisplayFormatter, emphasized: Boolean, onUse: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (emphasized) colors.primaryContainer else colors.surfaceContainerHigh)
            .clickable(enabled = value != null) { value?.let(onUse) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = if (emphasized) colors.onPrimaryContainer else colors.onSurfaceVariant)
        Text(
            value?.let(formatter::number) ?: "—",
            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp, fontWeight = FontWeight.Medium),
            color = if (emphasized) colors.onPrimaryContainer else colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatsKeypad(
    haptics: Haptics,
    decimal: Char,
    canAdd: Boolean,
    onDigit: (Char) -> Unit,
    onDot: () -> Unit,
    onNegate: () -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val gap = 6.dp
    // Digits on the left; ⌫, ± and a tall Add on the right, like a number pad's Enter.
    Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = modifier.padding(horizontal = gap * 2, vertical = gap)) {
        Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(3f)) {
            listOf("789", "456", "123").forEach { digits ->
                Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
                    digits.forEach { d -> Key(d.toString(), colors.surfaceContainerHigh, colors.onSurface, haptics) { onDigit(d) } }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
                Key("0", colors.surfaceContainerHigh, colors.onSurface, haptics, weight = 2f) { onDigit('0') }
                Key(decimal.toString(), colors.surfaceContainerHigh, colors.onSurface, haptics, onClick = onDot)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
            Row(Modifier.weight(1f)) { Key("⌫", colors.secondaryContainer, colors.onSecondaryContainer, haptics, onClick = onDelete, icon = true) }
            Row(Modifier.weight(1f)) { Key("±", colors.secondaryContainer, colors.onSecondaryContainer, haptics, onClick = onNegate) }
            Row(Modifier.weight(2f)) {
                Key(
                    stringResource(Res.string.stats_add),
                    if (canAdd) colors.primary else colors.surfaceContainerHighest,
                    if (canAdd) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.5f),
                    haptics,
                    onClick = onAdd,
                    small = true,
                )
            }
        }
    }
}

@Composable
private fun RowScope.Key(
    label: String,
    container: Color,
    content: Color,
    haptics: Haptics,
    weight: Float = 1f,
    icon: Boolean = false,
    small: Boolean = false,
    onClick: () -> Unit,
) {
    KeyCell(container = container, content = content, description = label, haptics = haptics, onClick = onClick, modifier = Modifier.weight(weight).fillMaxHeight()) {
        if (icon) Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        else Text(label, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = if (small) 20.sp else 28.sp, fontWeight = if (small) FontWeight.Medium else FontWeight.Normal), color = content)
    }
}
