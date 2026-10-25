package com.bernaferrari.calculator.ui.programmer

import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
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

    /** This visit's calculations, oldest first, each in the base it was typed in. */
    private val _tape = MutableStateFlow<List<ProgEntry>>(emptyList())
    val tape: StateFlow<List<ProgEntry>> = _tape

    fun onKey(key: ProgKey) {
        val before = _state.value
        val next = Programmer.press(before, key)
        _state.value = next
        val expression = next.expression
        if (next.resultShown && !before.resultShown && expression != null) {
            val value = Programmer.value(next) ?: return
            _tape.value = (_tape.value + ProgEntry(expression, next.base, next.word, value)).takeLast(50)
        }
    }

    fun onReuse(entry: ProgEntry) { _state.value = Programmer.withValue(_state.value, entry.value) }

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

/** One line of the tape: `FF + 1 = 100`. */
data class ProgEntry(val tokens: List<String>, val base: Base, val word: WordSize, val value: Long)

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
    val tape by viewModel.tape.collectAsStateWithLifecycle()
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
            // The readout needs room for the answer, the bits and the bases; the keys get the rest.
            val keypadHeight = (maxHeight * 0.54f).coerceIn(340.dp, 500.dp)
            val header: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp).fillMaxWidth()) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    // The bases live up here beside the word size, so the readout below keeps its room
                    // for the number and its bits.
                    BaseSelector(state.base, Modifier.weight(1f)) { haptics.tap(); viewModel.onBase(it) }
                    WordPicker(state.word) { haptics.tap(); viewModel.onWord(it) }
                }
            }
            val readout: @Composable (Modifier) -> Unit = { modifier ->
                Column(modifier.padding(horizontal = 12.dp), verticalArrangement = Arrangement.Bottom) {
                    // The number and what's above it; the board below keeps a fixed size, pinned over
                    // the keys, so nothing jumps as lines come and go. The tape gives way first.
                    Column(Modifier.weight(1f).fillMaxWidth().clipToBounds(), verticalArrangement = Arrangement.Bottom) {
                    val muted = MaterialTheme.colorScheme.onSurfaceVariant
                    val mono = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp, textAlign = TextAlign.End)
                    // Earlier calculations, newest at the bottom, in today's base; tap one to go on from its answer.
                    val earlier = if (state.resultShown && state.expression != null) tape.dropLast(1) else tape
                    LazyColumn(
                        reverseLayout = true,
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        items(earlier.asReversed()) { entry ->
                            val shown = Programmer.inBase(entry.tokens, entry.base, state.base, entry.word)
                            val answer = Programmer.group(Programmer.format(entry.value, state.base, entry.word), state.base)
                            Text(
                                buildAnnotatedString {
                                    append(Programmer.display(shown, state.base))
                                    append(" = ")
                                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append(answer) }
                                },
                                style = mono.copy(fontSize = 18.sp),
                                color = muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { haptics.tap(); viewModel.onReuse(entry) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            )
                        }
                    }
                    // The number and its lines, anchored to the bottom: if they ever need more room
                    // than there is, they run up into the tape's space, never down behind the board.
                    Column(Modifier.fillMaxWidth().wrapContentHeight(Alignment.Bottom, unbounded = true)) {
                    val error = state.error
                    val expression = state.expression
                    // What gave the answer, kept above it, as the calculator does.
                    if (state.resultShown && expression != null) {
                        Text(
                            Programmer.display(expression, state.base) + " =",
                            style = mono.copy(fontSize = 24.sp),
                            color = muted,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        )
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        // Which base the big number is in, right beside it: 10 in BIN is not ten.
                        Text(
                            state.base.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp, end = 8.dp),
                        )
                        Box(Modifier.weight(1f, fill = false), contentAlignment = Alignment.BottomEnd) {
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
                                    color = if (text.isEmpty()) muted.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface,
                                ),
                                maxSize = 52.sp,
                                minSize = 20.sp,
                            )
                        }
                        }
                    }
                    // Under the number, right-aligned like the calculator: the answer so far while typing
                    // an operation, then the same value in one other base, tagged as the number is.
                    val preview = if (!state.resultShown && error == null && Programmer.hasOperation(state)) value else null
                    val other = if (state.base == Base.DEC) Base.HEX else Base.DEC
                    // Its room is kept even when empty, so the number doesn't hop up at the first operator.
                    Text(
                        preview?.let { "= " + Programmer.group(Programmer.format(it, state.base, state.word), state.base) }.orEmpty(),
                        style = mono.copy(fontSize = 24.sp),
                        color = muted,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(28.dp),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).height(28.dp),
                    ) {
                        if (value != null && error == null) {
                            Text(other.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                            Text(
                                Programmer.group(Programmer.format(value, other, state.word), other),
                                style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp),
                                color = muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    }
                    }
                    Spacer(Modifier.height(4.dp))
                    BitGrid(value ?: 0L, state.word) { haptics.tick(); viewModel.onFlipBit(it) }
                    Spacer(Modifier.height(8.dp))
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
private fun BitGrid(value: Long, word: WordSize, modifier: Modifier = Modifier, onFlip: (Int) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val rows = (word.bits + 15) / 16
    // Rows as close as the cells in a group: one board, not four strips.
    val gap = 2.dp
    Column(
        modifier
            .fillMaxWidth()
            .height(20.dp * rows + gap * (rows - 1))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        for (r in 0 until rows) {
            val high = word.bits - 1 - r * 16
            val count = minOf(16, word.bits - r * 16)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().weight(1f)) {
                Text("$high", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 10.sp), color = colors.outline, modifier = Modifier.width(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f).fillMaxHeight()) {
                    for (g in 0 until 4) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f).fillMaxHeight()) {
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
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
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
private fun BaseSelector(selected: Base, modifier: Modifier = Modifier, onSelect: (Base) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween), modifier = modifier.padding(horizontal = 4.dp)) {
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
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.weight(1f).height(40.dp),
            ) { Text(base.name, maxLines = 1, style = MaterialTheme.typography.labelLarge) }
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
