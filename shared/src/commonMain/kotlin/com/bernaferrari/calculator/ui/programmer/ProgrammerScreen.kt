package com.bernaferrari.calculator.ui.programmer

import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.snap
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.rounded.Check
import com.bernaferrari.calculator.ui.calculator.Hint
import com.bernaferrari.calculator.domain.programmer.ProgEntry
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.zIndex
import com.bernaferrari.calculator.platform.systemConfirmsCopy
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarHost
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.ExpandLess
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
import androidx.compose.runtime.key
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

    /** Recent calculations, oldest first, each in the base it was typed in; kept between visits. */
    private val _tape = MutableStateFlow<List<ProgEntry>>(emptyList())
    val tape: StateFlow<List<ProgEntry>> = _tape

    init {
        // Opens as it was left: base, word size, signedness and the tape.
        viewModelScope.launch {
            val prefs = preferences.getUserPreferences().first()
            _state.value = _state.value.copy(base = prefs.programmerBase, word = prefs.programmerWord, signed = prefs.programmerSigned)
            _tape.value = prefs.programmerTape
        }
    }

    fun onKey(key: ProgKey) {
        val before = _state.value
        val next = Programmer.press(before, key)
        _state.value = next
        val expression = next.expression
        if (next.resultShown && !before.resultShown && expression != null) {
            val value = Programmer.value(next) ?: return
            val kept = (_tape.value + ProgEntry(expression, next.base, next.word, value)).takeLast(50)
            _tape.value = kept
            viewModelScope.launch { preferences.update { it.copy(programmerTape = kept) } }
        }
    }

    fun onReuse(entry: ProgEntry) { _state.value = Programmer.withValue(_state.value, entry.value) }

    fun onBase(base: Base) {
        _state.value = Programmer.withBase(_state.value, base)
        viewModelScope.launch { preferences.update { it.copy(programmerBase = base) } }
    }

    fun onFlipBit(bit: Int) { _state.value = Programmer.flipBit(_state.value, bit) }

    fun onSigned(signed: Boolean) {
        _state.value = Programmer.withSigned(_state.value, signed)
        viewModelScope.launch { preferences.update { it.copy(programmerSigned = signed) } }
    }

    fun onShowBits(show: Boolean) {
        viewModelScope.launch { preferences.update { it.copy(programmerShowBits = show) } }
    }

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
    val tape by viewModel.tape.collectAsStateWithLifecycle()
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val value = Programmer.value(state)
    @Suppress("DEPRECATION") val clipboard = LocalClipboardManager.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMessage = stringResource(Res.string.copied)
    // Plain digits, no grouping spaces, so they paste cleanly into code.
    val copy: (String) -> Unit = { text ->
        haptics.confirm()
        clipboard.setText(AnnotatedString(text.replace('−', '-')))
        if (!systemConfirmsCopy) scope.launch { snackbar.currentSnackbarData?.dismiss(); snackbar.showSnackbar(copiedMessage) }
    }

    // A hardware keyboard types too (tablets, the web): digits, a–f, operators, Enter, Backspace, Esc.
    val keyFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { keyFocus.requestFocus() } }
    val keyboard = Modifier
        .focusRequester(keyFocus)
        .focusable()
        .onPreviewKeyEvent { event ->
            if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
            val key = keyboardKey(event)
            if (key != null) { haptics.tap(); viewModel.onKey(key) }
            key != null
        }
    // Opened from the overflow menu, so it grows out of the ⋮ button, as Settings does.
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize().then(keyboard)) {
        BoxWithConstraints(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)),
        ) {
            SnackbarHost(snackbar, Modifier.align(Alignment.Center).zIndex(1f))
            val landscape = maxWidth > maxHeight && maxHeight < 600.dp
            // The readout needs room for the answer, the bits and the bases; the keys get the rest.
            val keypadHeight = (maxHeight * 0.52f).coerceIn(340.dp, 500.dp)
            val header: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp).fillMaxWidth()) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    // The bases live up here beside the word size, so the readout below keeps its room
                    // for the number and its bits.
                    BaseSelector(state.base, Modifier.weight(1f)) { haptics.tap(); viewModel.onBase(it) }
                    WordPicker(state.word, state.signed, onSigned = { haptics.tap(); viewModel.onSigned(it) }) { haptics.tap(); viewModel.onWord(it) }
                }
            }
            val workingScroll = rememberLazyListState()
            // Typing or reusing an answer returns to the current value. Browsing history alone
            // leaves the scroll position untouched.
            LaunchedEffect(state, tape.size, prefs.programmerShowBits) { workingScroll.scrollToItem(0) }
            val readout: @Composable (Modifier) -> Unit = { modifier ->
                val muted = MaterialTheme.colorScheme.onSurfaceVariant
                val mono = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp, textAlign = TextAlign.End)
                val earlier = if (state.resultShown && state.expression != null) tape.dropLast(1) else tape
                // One working area: history, the current value, alternate base, and bits all scroll
                // together. Only the header and keypad stay pinned.
                LazyColumn(
                    state = workingScroll,
                    reverseLayout = true,
                    horizontalAlignment = Alignment.End,
                    contentPadding = PaddingValues(bottom = 8.dp),
                    modifier = modifier
                        .padding(horizontal = 12.dp)
                        .clipToBounds()
                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                        .drawWithContent {
                            drawContent()
                            // Fade only a clipped history row, never the first line of a long value.
                            val info = workingScroll.layoutInfo
                            val top = info.viewportStartOffset
                            val clippedHistory = info.visibleItemsInfo.any {
                                it.key != "readout" && it.offset <= top && it.offset + it.size > top
                            }
                            val currentOffset = info.visibleItemsInfo.firstOrNull { it.key == "readout" }?.offset
                            val fadeHeight = currentOffset?.let { (it - top).toFloat().coerceIn(0f, 24.dp.toPx()) } ?: 24.dp.toPx()
                            if (clippedHistory && fadeHeight > 0f) drawRect(
                                Brush.verticalGradient(0f to Color.Transparent, fadeHeight / size.height.coerceAtLeast(1f) to Color.Black),
                                blendMode = BlendMode.DstIn,
                            )
                        },
                ) {
                    item(key = "readout") {
                        Column(Modifier.fillMaxWidth()) {
                            val current = state.expression
                            if (state.resultShown && current != null) {
                                Text(
                                    Programmer.display(current, state.base) + " =",
                                    style = mono.copy(fontSize = 24.sp),
                                    color = muted,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
                                )
                            }
                            val error = state.error
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    // Hold the number to copy it, as typed in this base.
                                    .combinedClickable(
                                        interactionSource = null,
                                        indication = null,
                                        onClick = {},
                                        onLongClick = { value?.let { copy(Programmer.format(it, state.base, state.word, state.signed)) } },
                                    )
                                    .padding(horizontal = 12.dp),
                            ) {
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
                                        wrapAtMinimum = true,
                                    )
                                }
                                }
                            }
                            // Under the number, right-aligned like the calculator: the answer so far while typing
                            // an operation, then the same value in one other base, tagged as the number is.
                            val preview = if (!state.resultShown && error == null && Programmer.hasOperation(state)) value else null
                            val other = if (state.base == Base.DEC) Base.HEX else Base.DEC
                            if (preview != null) FitText(
                                "= " + Programmer.group(Programmer.format(preview, state.base, state.word, state.signed), state.base),
                                style = mono.copy(color = muted),
                                maxSize = 24.sp,
                                minSize = 18.sp,
                                wrapAtMinimum = true,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            )
                            // Share one utility row: disclosure on the left, alternate base on the right.
                            // The shorter label saves a whole button row without shrinking either tap target.
                            val bitsAction = stringResource(if (prefs.programmerShowBits) Res.string.programmer_hide_bits else Res.string.programmer_show_bits)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            ) {
                                TextButton(
                                    onClick = { haptics.tap(); viewModel.onShowBits(!prefs.programmerShowBits) },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    modifier = Modifier.heightIn(min = 48.dp).semantics { contentDescription = bitsAction },
                                ) {
                                    Text(stringResource(Res.string.programmer_bit_view))
                                    Icon(
                                        if (prefs.programmerShowBits) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        // Tap to work in that base instead; hold to copy it.
                                        .combinedClickable(
                                            enabled = value != null,
                                            onClick = { haptics.tap(); viewModel.onBase(other) },
                                            onLongClick = { value?.let { copy(Programmer.format(it, other, state.word, state.signed)) } },
                                        )
                                        .heightIn(min = 48.dp)
                                        .padding(horizontal = 12.dp, vertical = 4.dp),
                                ) {
                                    if (value != null && error == null) {
                                        Text(other.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                                        FitText(
                                            Programmer.group(Programmer.format(value, other, state.word, state.signed), other),
                                            style = TextStyle(fontFamily = GoogleSansFlex, textAlign = TextAlign.End, color = muted),
                                            maxSize = 20.sp,
                                            minSize = 16.sp,
                                            wrapAtMinimum = true,
                                            modifier = Modifier.weight(1f, fill = false),
                                        )
                                    }
                                }
                            }
                            if (prefs.programmerShowBits) {
                                BitGrid(value ?: 0L, state.word, onCollapse = { haptics.heavy(); viewModel.onShowBits(false) }) {
                                    haptics.tick(); viewModel.onFlipBit(it)
                                }
                            }
                        }
                    }
                    items(earlier.asReversed()) { entry ->
                        val shown = Programmer.inBase(entry.tokens, entry.base, state.base, entry.word, state.signed)
                        val answer = Programmer.group(Programmer.format(entry.value, state.base, entry.word, state.signed), state.base)
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
            }
            val keys: @Composable (Modifier) -> Unit = { modifier ->
                ProgrammerKeypad(state.base, haptics, viewModel::onKey, modifier, prefs.reduceMotion)
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
private fun BitGrid(value: Long, word: WordSize, modifier: Modifier = Modifier, onCollapse: () -> Unit, onFlip: (Int) -> Unit) {
    val collapseDescription = stringResource(Res.string.programmer_hide_bits)
    val colors = MaterialTheme.colorScheme
    val rows = (word.bits + 15) / 16
    // Rows above the highest set bit step back, so the bits in use stand out; the lowest row never does.
    val mask = if (word.bits == 64) -1L else (1L shl word.bits) - 1
    val topBit = 63 - (value and mask).countLeadingZeroBits()
    // Rows as close as the cells in a group: one board, not four strips.
    val gap = 2.dp
    Column(
        modifier
            .fillMaxWidth()
            // Each row: 16dp of bits and, under every group of four, the hex digit they spell.
            .height(26.dp * rows + gap * (rows - 1))
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        for (r in 0 until rows) {
            val high = word.bits - 1 - r * 16
            val count = minOf(16, word.bits - r * 16)
            val quiet = r < rows - 1 && high - count + 1 > topBit
            val rowAlpha by animateFloatAsState(if (quiet) 0.45f else 1f, label = "row")
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().weight(1f).graphicsLayer { alpha = rowAlpha }) {
                Text("$high", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 10.sp), color = colors.outline, modifier = Modifier.width(20.dp).padding(bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f).fillMaxHeight()) {
                    for (g in 0 until 4) {
                        if (g * 4 >= count) { Spacer(Modifier.weight(1f)); continue }
                        val nibble = ((value ushr (high - g * 4 - 3)) and 0xF).toInt()
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                            for (i in 0 until 4) {
                                val index = g * 4 + i
                                if (index >= count) { Spacer(Modifier.weight(1f)); continue }
                                val bit = high - index
                                val on = (value ushr bit) and 1L == 1L
                                val description = stringResource(Res.string.programmer_bit_description, bit, if (on) 1 else 0)
                                val fill by animateColorAsState(if (on) colors.primary else colors.surfaceContainerHigh, label = "bit")
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(fill)
                                        .semantics { contentDescription = description }
                                        .combinedClickable(
                                            onClick = { onFlip(bit) },
                                            onLongClickLabel = collapseDescription,
                                            onLongClick = onCollapse,
                                        ),
                                ) {
                                    Text(if (on) "1" else "0", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 11.sp), color = if (on) colors.onPrimary else colors.onSurfaceVariant)
                                }
                            }
                        }
                        // The group's hex digit, so the board reads straight across to the hex number.
                        Text(
                            nibble.toString(16).uppercase(),
                            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 9.sp, lineHeight = 9.sp, textAlign = TextAlign.Center),
                            color = if (nibble != 0) colors.primary else colors.outline,
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                        )
                        }
                    }
                }
                Text("${high - count + 1}", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 10.sp, textAlign = TextAlign.End), color = colors.outline, modifier = Modifier.width(20.dp).padding(bottom = 10.dp))
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
private fun WordPicker(word: WordSize, signed: Boolean, onSigned: (Boolean) -> Unit, onPick: (WordSize) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(Res.string.programmer_bits, word.bits))
                Text(
                    stringResource(if (signed) Res.string.programmer_signed else Res.string.programmer_unsigned),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = stringResource(Res.string.programmer_word_size), modifier = Modifier.size(20.dp))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            WordSize.entries.forEach {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.programmer_bits, it.bits)) },
                    trailingIcon = { if (it == word) Icon(Icons.Rounded.Check, contentDescription = null) },
                    onClick = { open = false; onPick(it) },
                )
            }
            HorizontalDivider()
            // How decimal reads the top bit: FFFF FFFF as −1, or as 4 294 967 295.
            listOf(true, false).forEach { s ->
                DropdownMenuItem(
                    text = { Text(stringResource(if (s) Res.string.programmer_signed else Res.string.programmer_unsigned)) },
                    trailingIcon = { if (s == signed) Icon(Icons.Rounded.Check, contentDescription = null) },
                    onClick = { open = false; if (s != signed) onSigned(s) },
                )
            }
        }
    }
}

private enum class Tone { Digit, Hex, Function, Operator, Equals, Clear }

private data class PKey(val key: ProgKey, val tone: Tone, val label: String = key.text, val weight: Float = 1f, val up: ProgKey? = null)

/**
 * Five columns: the hex digits down the left, the decimal pad in the middle, operators on the
 * right like the calculator's, and the bitwise operators along the top. Digits the base doesn't
 * have step back.
 */
@Composable
private fun ProgrammerKeypad(base: Base, haptics: Haptics, onKey: (ProgKey) -> Unit, modifier: Modifier, reduceMotion: Boolean) {
    val rows = listOf(
        listOf(PKey(ProgKey.AND, Tone.Function), PKey(ProgKey.OR, Tone.Function), PKey(ProgKey.XOR, Tone.Function), PKey(ProgKey.NOT, Tone.Function), PKey(ProgKey.MOD, Tone.Function)),
        listOf(PKey(ProgKey.A, Tone.Hex), PKey(ProgKey.SHL, Tone.Function, up = ProgKey.ROL), PKey(ProgKey.SHR, Tone.Function, up = ProgKey.ROR), PKey(ProgKey.CLEAR, Tone.Clear, "AC"), PKey(ProgKey.DELETE, Tone.Function)),
        listOf(PKey(ProgKey.B, Tone.Hex), PKey(ProgKey.LPAREN, Tone.Function), PKey(ProgKey.RPAREN, Tone.Function), PKey(ProgKey.NEGATE, Tone.Function), PKey(ProgKey.DIVIDE, Tone.Operator)),
        listOf(PKey(ProgKey.C, Tone.Hex), PKey(ProgKey.D7, Tone.Digit), PKey(ProgKey.D8, Tone.Digit), PKey(ProgKey.D9, Tone.Digit), PKey(ProgKey.TIMES, Tone.Operator)),
        listOf(PKey(ProgKey.D, Tone.Hex), PKey(ProgKey.D4, Tone.Digit), PKey(ProgKey.D5, Tone.Digit), PKey(ProgKey.D6, Tone.Digit), PKey(ProgKey.MINUS, Tone.Operator)),
        listOf(PKey(ProgKey.E, Tone.Hex), PKey(ProgKey.D1, Tone.Digit), PKey(ProgKey.D2, Tone.Digit), PKey(ProgKey.D3, Tone.Digit), PKey(ProgKey.PLUS, Tone.Operator)),
        listOf(PKey(ProgKey.F, Tone.Hex), PKey(ProgKey.D0, Tone.Digit, weight = 2f), PKey(ProgKey.EQUALS, Tone.Equals, weight = 2f)),
    )
    val gap = 6.dp
    // A–F only mean something in hex: elsewhere their column folds away and the pad widens.
    val hex by animateFloatAsState(if (base == Base.HEX) 1f else 0f, if (reduceMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow), label = "hex")
    val rotateLeft = stringResource(Res.string.programmer_rotate_left)
    val rotateRight = stringResource(Res.string.programmer_rotate_right)
    Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = modifier.padding(horizontal = gap * 2, vertical = gap)) {
        rows.forEachIndexed { r, row ->
            // The bitwise row is a little shorter: words, not digits.
            Row(modifier = Modifier.weight(if (r == 0) 0.75f else 1f)) {
                row.forEachIndexed cell@ { index, spec ->
                    if (spec.tone == Tone.Hex && hex < 0.001f) return@cell
                    // Stable identity keeps digits from inheriting a fading hex key's state.
                    key(spec.key) {
                        if (index > 0) {
                            val gapScale = if (index == 1 && row.first().tone == Tone.Hex) hex.coerceIn(0f, 1f) else 1f
                            Spacer(Modifier.width(gap * gapScale))
                        }
                        val digit = spec.key.digit
                        val enabled = digit == null || digit < base.radix
                        val shown = if (spec.tone == Tone.Hex) spec.copy(weight = spec.weight * hex) else spec
                        // Shifts flick up to rotate: ROL on <<, ROR on >>.
                        val up = spec.up?.let { key -> Hint(key.text, if (key == ProgKey.ROL) rotateLeft else rotateRight) { onKey(key) } }
                        ProgKeyCell(shown, enabled, haptics, onKey, up, if (spec.tone == Tone.Hex) hex else 1f)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ProgKeyCell(spec: PKey, enabled: Boolean, haptics: Haptics, onKey: (ProgKey) -> Unit, up: Hint? = null, fade: Float = 1f) {
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
        up = up,
        labelFlicks = up != null,
        showDots = false,
        modifier = Modifier.weight(spec.weight).fillMaxHeight()
            .then(if (spec.tone == Tone.Hex) Modifier.clipToBounds() else Modifier)
            .graphicsLayer { this.alpha = alpha * fade },
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

/** What a key on a hardware keyboard means here, or null when it means nothing. */
private fun keyboardKey(event: KeyEvent): ProgKey? {
    when (event.key) {
        Key.Enter, Key.NumPadEnter -> return ProgKey.EQUALS
        Key.Backspace -> return ProgKey.DELETE
        Key.Escape, Key.Delete -> return ProgKey.CLEAR
        // By key as well as by character, for keyboards and browsers that don't send the character.
        Key.NumPadAdd -> return ProgKey.PLUS
        Key.NumPadSubtract -> return ProgKey.MINUS
        Key.NumPadMultiply -> return ProgKey.TIMES
        Key.NumPadDivide -> return ProgKey.DIVIDE
        Key.Equals -> return if (event.isShiftPressed) ProgKey.PLUS else ProgKey.EQUALS
        Key.Minus -> return ProgKey.MINUS
        Key.Slash -> return ProgKey.DIVIDE
        Key.Eight -> if (event.isShiftPressed) return ProgKey.TIMES
        Key.Nine -> if (event.isShiftPressed) return ProgKey.LPAREN
        Key.Zero -> if (event.isShiftPressed) return ProgKey.RPAREN
        Key.Five -> if (event.isShiftPressed) return ProgKey.MOD
    }
    val c = event.utf16CodePoint.takeIf { it > 0 }?.toChar() ?: return null
    c.digitToIntOrNull(16)?.let { d -> return ProgKey.entries.first { it.digit == d } }
    return when (c) {
        '+' -> ProgKey.PLUS
        '-' -> ProgKey.MINUS
        '*' -> ProgKey.TIMES
        '/' -> ProgKey.DIVIDE
        '%' -> ProgKey.MOD
        '(' -> ProgKey.LPAREN
        ')' -> ProgKey.RPAREN
        '&' -> ProgKey.AND
        '|' -> ProgKey.OR
        '^' -> ProgKey.XOR
        '~' -> ProgKey.NOT
        '<' -> ProgKey.SHL
        '>' -> ProgKey.SHR
        '=' -> ProgKey.EQUALS
        else -> null
    }
}
