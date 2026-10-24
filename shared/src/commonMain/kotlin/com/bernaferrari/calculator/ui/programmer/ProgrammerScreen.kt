package com.bernaferrari.calculator.ui.programmer

import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.AppGraph
import com.bernaferrari.calculator.domain.programmer.Base
import com.bernaferrari.calculator.domain.programmer.ProgError
import com.bernaferrari.calculator.domain.programmer.ProgKey
import com.bernaferrari.calculator.domain.programmer.ProgState
import com.bernaferrari.calculator.domain.programmer.Programmer
import com.bernaferrari.calculator.domain.programmer.WordSize
import com.bernaferrari.calculator.domain.repository.PreferencesRepository
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.FitText
import com.bernaferrari.calculator.ui.calculator.Haptics
import com.bernaferrari.calculator.ui.calculator.KeyCell
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

class ProgrammerViewModel(private val preferences: PreferencesRepository = AppGraph.preferences) : ViewModel() {
    private val _state = MutableStateFlow(ProgState())
    val state: StateFlow<ProgState> = _state

    init {
        // Opens in the base and word size last used.
        viewModelScope.launch {
            val prefs = preferences.getUserPreferences().first()
            _state.value = _state.value.copy(base = prefs.programmerBase, word = prefs.programmerWord)
        }
    }

    fun onKey(key: ProgKey) { _state.value = Programmer.press(_state.value, key) }

    fun onBase(base: Base) {
        _state.value = Programmer.withBase(_state.value, base)
        viewModelScope.launch { preferences.update { it.copy(programmerBase = base) } }
    }

    fun onFlipBit(bit: Int) { _state.value = Programmer.flipBit(_state.value, bit) }

    fun onWord(word: WordSize) {
        _state.value = Programmer.withWord(_state.value, word)
        viewModelScope.launch { preferences.update { it.copy(programmerWord = word) } }
    }
}

/**
 * Integers for people who think in bits: the value in all four bases at once (tap one to type in
 * it), a word size, and keys for the bitwise operators. A screen of its own, so the calculator
 * stays as simple as it is.
 */
@Composable
fun ProgrammerScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgrammerViewModel = viewModel { ProgrammerViewModel() },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val value = Programmer.value(state)

    // Opened from the overflow menu, so it grows out of the ⋮ button, as Settings does.
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
        BoxWithConstraints(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)),
        ) {
            val landscape = maxWidth > maxHeight && maxHeight < 600.dp
            val keypadHeight = (maxHeight * 0.58f).coerceIn(360.dp, 520.dp)
            val header: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp).fillMaxWidth()) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    Text(stringResource(Res.string.menu_programmer), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    WordPicker(state.word) { haptics.tap(); viewModel.onWord(it) }
                }
            }
            val readout: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.Bottom) {
                    Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp), contentAlignment = Alignment.BottomEnd) {
                        val error = state.error
                        if (error != null) {
                            Text(
                                stringResource(if (error == ProgError.DIVIDE_BY_ZERO) Res.string.error_divide_by_zero else Res.string.error_syntax),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        } else {
                            val text = Programmer.display(state)
                            FitText(
                                text.ifEmpty { "0" },
                                style = TextStyle(
                                    fontFamily = GoogleSansFlex,
                                    textAlign = TextAlign.End,
                                    color = if (text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface,
                                ),
                                maxSize = 52.sp,
                                minSize = 20.sp,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    // One other reading of the number, quietly: decimal, or hex while typing in decimal.
                    val other = if (state.base == Base.DEC) Base.HEX else Base.DEC
                    Text(
                        value?.let { "${other.name}  " + Programmer.group(Programmer.format(it, other, state.word), other) }.orEmpty(),
                        style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp, textAlign = TextAlign.End),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(32.dp),
                    )
                    BitGrid(value ?: 0L, state.word) { haptics.tick(); viewModel.onFlipBit(it) }
                    Spacer(Modifier.height(8.dp))
                    BaseSelector(state.base) { haptics.tap(); viewModel.onBase(it) }
                }
            }
            val keys: @Composable (Modifier) -> Unit = { modifier ->
                ProgrammerKeypad(state.base, haptics, viewModel::onKey, modifier)
            }
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(0.5f).fillMaxHeight().padding(bottom = bottomInset)) {
                        header()
                        readout(Modifier.weight(1f).fillMaxWidth())
                    }
                    keys(Modifier.weight(0.5f).fillMaxHeight().statusBarsPadding().padding(top = 8.dp, bottom = bottomInset + 8.dp))
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
                        header()
                        readout(Modifier.weight(1f).fillMaxWidth())
                        keys(Modifier.fillMaxWidth().height(keypadHeight + bottomInset).padding(top = 8.dp, bottom = bottomInset))
                    }
                }
            }
        }
    }
}

/**
 * The value's bits, most significant first, sixteen to a row in groups of four, numbered at each
 * row's ends. Tap one to flip it.
 */
@Composable
private fun BitGrid(value: Long, word: WordSize, onFlip: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val rows = (word.bits + 15) / 16
    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until rows) {
            val high = word.bits - 1 - r * 16
            val count = minOf(16, word.bits - r * 16)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("$high", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 10.sp), color = colors.outline, modifier = Modifier.width(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    for (g in 0 until 4) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                            for (i in 0 until 4) {
                                val index = g * 4 + i
                                if (index >= count) { Spacer(Modifier.weight(1f)); continue }
                                val bit = high - index
                                val on = (value ushr bit) and 1L == 1L
                                val fill by animateColorAsState(if (on) colors.primary else colors.surfaceContainerHigh, label = "bit")
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(fill)
                                        .clickable { onFlip(bit) },
                                ) {
                                    Text(if (on) "1" else "0", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 11.sp), color = if (on) colors.onPrimary else colors.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Text("${high - count + 1}", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 10.sp, textAlign = TextAlign.End), color = colors.outline, modifier = Modifier.width(20.dp))
            }
        }
    }
}

/** The base you type in: one row, like the settings' choices. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BaseSelector(selected: Base, onSelect: (Base) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween), modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Base.entries.forEachIndexed { index, base ->
            ToggleButton(
                checked = base == selected,
                onCheckedChange = { if (it) onSelect(base) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    Base.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.weight(1f).height(44.dp),
            ) { Text(base.name, maxLines = 1) }
        }
    }
}

@Composable
private fun WordPicker(word: WordSize, onPick: (WordSize) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Text(stringResource(Res.string.programmer_bits, word.bits))
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = stringResource(Res.string.programmer_word_size), modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            WordSize.entries.forEach {
                DropdownMenuItem(text = { Text(stringResource(Res.string.programmer_bits, it.bits)) }, onClick = { open = false; onPick(it) })
            }
        }
    }
}

private enum class Tone { Digit, Hex, Function, Operator, Equals, Clear }

private data class PKey(val key: ProgKey, val tone: Tone, val label: String = key.text, val weight: Float = 1f)

/**
 * Five columns: the hex digits down the left, the decimal pad in the middle, operators on the
 * right like the calculator's, and the bitwise operators along the top. Digits the base doesn't
 * have step back.
 */
@Composable
private fun ProgrammerKeypad(base: Base, haptics: Haptics, onKey: (ProgKey) -> Unit, modifier: Modifier) {
    val rows = listOf(
        listOf(PKey(ProgKey.AND, Tone.Function), PKey(ProgKey.OR, Tone.Function), PKey(ProgKey.XOR, Tone.Function), PKey(ProgKey.NOT, Tone.Function), PKey(ProgKey.MOD, Tone.Function)),
        listOf(PKey(ProgKey.A, Tone.Hex), PKey(ProgKey.SHL, Tone.Function), PKey(ProgKey.SHR, Tone.Function), PKey(ProgKey.CLEAR, Tone.Clear, "AC"), PKey(ProgKey.DELETE, Tone.Function)),
        listOf(PKey(ProgKey.B, Tone.Hex), PKey(ProgKey.LPAREN, Tone.Function), PKey(ProgKey.RPAREN, Tone.Function), PKey(ProgKey.NEGATE, Tone.Function), PKey(ProgKey.DIVIDE, Tone.Operator)),
        listOf(PKey(ProgKey.C, Tone.Hex), PKey(ProgKey.D7, Tone.Digit), PKey(ProgKey.D8, Tone.Digit), PKey(ProgKey.D9, Tone.Digit), PKey(ProgKey.TIMES, Tone.Operator)),
        listOf(PKey(ProgKey.D, Tone.Hex), PKey(ProgKey.D4, Tone.Digit), PKey(ProgKey.D5, Tone.Digit), PKey(ProgKey.D6, Tone.Digit), PKey(ProgKey.MINUS, Tone.Operator)),
        listOf(PKey(ProgKey.E, Tone.Hex), PKey(ProgKey.D1, Tone.Digit), PKey(ProgKey.D2, Tone.Digit), PKey(ProgKey.D3, Tone.Digit), PKey(ProgKey.PLUS, Tone.Operator)),
        listOf(PKey(ProgKey.F, Tone.Hex), PKey(ProgKey.D0, Tone.Digit, weight = 2f), PKey(ProgKey.EQUALS, Tone.Equals, weight = 2f)),
    )
    val gap = 6.dp
    Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = modifier.padding(horizontal = gap * 2, vertical = gap)) {
        rows.forEachIndexed { r, row ->
            // The bitwise row is a little shorter: words, not digits.
            Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(if (r == 0) 0.75f else 1f)) {
                row.forEach { spec ->
                    val digit = spec.key.digit
                    val enabled = digit == null || digit < base.radix
                    ProgKeyCell(spec, enabled, haptics, onKey)
                }
            }
        }
    }
}

@Composable
private fun RowScope.ProgKeyCell(spec: PKey, enabled: Boolean, haptics: Haptics, onKey: (ProgKey) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val container = when (spec.tone) {
        Tone.Digit -> colors.surfaceContainerHigh
        Tone.Hex -> colors.surfaceContainer
        Tone.Operator -> colors.secondaryContainer
        Tone.Equals -> colors.primary
        Tone.Function, Tone.Clear -> colors.surfaceContainerHighest
    }
    val content = when (spec.tone) {
        Tone.Digit, Tone.Hex -> colors.onSurface
        Tone.Operator -> colors.onSecondaryContainer
        Tone.Equals -> colors.onPrimary
        Tone.Clear -> colors.tertiary
        Tone.Function -> colors.onSurfaceVariant
    }
    val alpha by animateFloatAsState(if (enabled) 1f else 0.3f, label = "enabled")
    // Words (AND, mod, AC) are set smaller than digits and symbols.
    val words = spec.label.count { it.isLetter() } >= 2
    KeyCell(
        container = container,
        content = content,
        description = spec.label,
        haptics = haptics,
        onClick = { if (enabled) onKey(spec.key) },
        onLongClick = if (spec.key == ProgKey.DELETE) ({ haptics.heavy(); onKey(ProgKey.CLEAR) }) else null,
        modifier = Modifier.weight(spec.weight).fillMaxHeight().graphicsLayer { this.alpha = alpha },
    ) {
        if (spec.key == ProgKey.DELETE) {
            Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
        } else {
            Text(
                spec.label,
                style = TextStyle(fontFamily = GoogleSansFlex, fontSize = if (words) 16.sp else 26.sp, fontWeight = if (words) FontWeight.Medium else FontWeight.Normal),
                color = content,
            )
        }
    }
}
