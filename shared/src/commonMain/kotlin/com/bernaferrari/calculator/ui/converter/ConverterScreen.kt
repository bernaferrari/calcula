package com.bernaferrari.calculator.ui.converter

import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.createSavedStateHandle
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.domain.convert.Category
import com.bernaferrari.calculator.domain.convert.MeasureUnit
import com.bernaferrari.calculator.domain.convert.Units
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.Haptics
import com.bernaferrari.calculator.ui.calculator.KeyCell
import com.bernaferrari.calculator.ui.calculator.FitText
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex

/**
 * Unit conversion in the calculator's own language: two rows (tap one to type into it), a swap
 * button between them, and the same keys as the main keypad.
 */
@Composable
fun ConverterScreen(
    seed: String?,
    onNavigateBack: () -> Unit,
    onUse: (String) -> Unit,
    viewModel: ConverterViewModel = viewModel { ConverterViewModel(createSavedStateHandle()) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(seed) { if (seed != null) viewModel.seed(seed) }
    @Suppress("DEPRECATION") val clipboard = LocalClipboardManager.current
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val formatLocale = prefs.decimalSeparator.separators()
    val formatter = remember(formatLocale) { DisplayFormatter(formatLocale) }
    var picking by remember { mutableStateOf<Boolean?>(null) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("converter", isScreen = true).fillMaxSize()) {
    BoxWithConstraints(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal))) {
        // Side by side on phones held sideways, and on any wide window (tablets, unfolded foldables).
        val landscape = maxWidth > maxHeight && (maxHeight < 600.dp || maxWidth >= 840.dp)
        val header: @Composable () -> Unit = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                    Text(stringResource(Res.string.menu_unit_converter), style = MaterialTheme.typography.titleLarge)
                }
        }
        val categories: @Composable () -> Unit = {
                val chips = rememberLazyListState()
                LaunchedEffect(state.category) { chips.animateScrollToItem(Category.entries.indexOf(state.category), scrollOffset = -120) }
                LazyRow(
                    state = chips,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(Category.entries) { category ->
                        FilterChip(
                            selected = category == state.category,
                            onClick = { haptics.tap(); viewModel.onCategory(category) },
                            label = { Text(stringResource(category.title())) },
                        )
                    }
                }
        }
        val card: @Composable (Modifier) -> Unit = { modifier ->
                // Both rows live in one card, the swap button riding the seam between them.
                var turns by remember { mutableFloatStateOf(0f) }
                val rotation by animateFloatAsState(turns * 180f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow), label = "swap")
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize().padding(6.dp)) {
                            listOf(true, false).forEach { top ->
                                UnitRow(
                                    unit = if (top) state.top else state.bottom,
                                    text = if (top) state.topText else state.bottomText,
                                    active = state.topActive == top,
                                    formatter = formatter,
                                    picking = picking == top,
                                    dimmed = picking != null && picking != top,
                                    onActivate = { haptics.tap(); picking = null; viewModel.onActivate(top = top) },
                                    onCopy = { value ->
                                        haptics.confirm()
                                        clipboard.setText(AnnotatedString(value.replace('.', formatter.decimal).replace('−', '-')))
                                    },
                                    onPickUnit = { haptics.tap(); picking = if (picking == top) null else top },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = { haptics.tap(); turns += 1; viewModel.onSwap() },
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 20.dp).size(52.dp),
                        ) {
                            Icon(
                                Icons.Rounded.SwapVert,
                                contentDescription = stringResource(Res.string.converter_swap),
                                modifier = Modifier.size(26.dp).rotate(rotation),
                            )
                        }
                    }
                }
        }
        val keys: @Composable (Modifier) -> Unit = { modifier ->
                AnimatedContent(
                    targetState = picking,
                    transitionSpec = {
                        (fadeIn(tween(160, delayMillis = 60)) + scaleIn(spring(dampingRatio = 0.75f, stiffness = 500f), initialScale = 0.94f)) togetherWith
                            (fadeOut(tween(100)) + scaleOut(tween(140), targetScale = 0.96f))
                    },
                    label = "keypad",
                    modifier = modifier,
                ) { target ->
                    if (target == null) {
                        ConverterKeypad(
                            decimal = formatter.decimal,
                            haptics = haptics,
                            onDigit = viewModel::onDigit,
                            onDecimal = viewModel::onDecimal,
                            onDelete = viewModel::onDelete,
                            onClear = viewModel::onClear,
                            onNegate = viewModel::onNegate,
                            canUse = state.output.isNotEmpty(),
                            onUse = { onUse(state.output) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        UnitGrid(
                            units = Units.of(state.category),
                            selected = if (target) state.top else state.bottom,
                            haptics = haptics,
                            onPick = { viewModel.onUnit(target, it); picking = null },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
        }

        // Choosing a unit morphs the keypad into the unit tiles, in place: no sheet over the numbers.
        @OptIn(ExperimentalComposeUiApi::class)
        BackHandler(enabled = picking != null) { picking = null }
        if (landscape) {
            // Side by side: values on the left, keys on the right, both at full height.
            Row(Modifier.fillMaxSize()) {
                Column(Modifier.weight(0.55f).fillMaxHeight()) {
                    header()
                    categories()
                    card(Modifier.weight(1f).fillMaxWidth().padding(bottom = bottomInset))
                }
                keys(Modifier.weight(0.45f).fillMaxHeight().statusBarsPadding().padding(top = 8.dp, bottom = bottomInset + 8.dp))
            }
        } else {
            // A tall tablet keeps the converter at a phone's width, centred, rather than stretched.
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(Modifier.widthIn(max = 560.dp).fillMaxSize()) {
                    header()
                    categories()
                    card(Modifier.weight(1f).fillMaxWidth())
                    keys(Modifier.fillMaxWidth().height(300.dp + bottomInset).padding(bottom = bottomInset))
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UnitRow(
    unit: MeasureUnit,
    text: String,
    active: Boolean,
    picking: Boolean,
    dimmed: Boolean,
    formatter: DisplayFormatter,
    onActivate: () -> Unit,
    onCopy: (String) -> Unit,
    onPickUnit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    // The row you type into reads as a field; the other one is the answer, in the accent color.
    val field by animateColorAsState(if (active || picking) colors.surfaceContainerHigh else Color.Transparent, label = "field")
    val numberColor by animateColorAsState(if (active) colors.onSurface else colors.primary, label = "number")
    // While choosing a unit, the row being changed lights its pill and the other row steps back.
    val pillColor by animateColorAsState(if (picking) colors.primary else colors.surfaceContainerHighest, label = "pill")
    val pillContent by animateColorAsState(if (picking) colors.onPrimary else colors.onSurface, label = "pillContent")
    val alpha by animateFloatAsState(if (dimmed) 0.38f else 1f, label = "dim")
    val symbolColor = colors.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = alpha }
            .clip(RoundedCornerShape(22.dp))
            .background(field)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onActivate,
                onLongClick = { if (text.isNotEmpty()) onCopy(text) },
            )
            .padding(start = 96.dp, end = 20.dp),
    ) {
        // The unit is a pill: tap it and the keypad turns into that category's units.
        Surface(onClick = onPickUnit, shape = CircleShape, color = pillColor) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)) {
                Text(unit.name, style = MaterialTheme.typography.labelLarge, color = pillContent, maxLines = 1)
                Icon(
                    if (picking) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                    contentDescription = null,
                    tint = pillContent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            FitText(
                text = if (text.isEmpty()) "0" else formatter.number(text),
                style = TextStyle(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Normal,
                    
                    textAlign = TextAlign.End,
                    color = if (text.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.4f) else numberColor,
                ),
                maxSize = 52.sp,
                minSize = 22.sp,
                modifier = Modifier.weight(1f).alignByBaseline(),
            )
            Text(
                unit.symbol,
                style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp),
                color = symbolColor,
                modifier = Modifier.padding(start = 8.dp).alignByBaseline(),
            )
        }
    }
}

/** Every unit of the category as a tile: big symbol, small name, the current one filled. */
@Composable
private fun UnitGrid(units: List<MeasureUnit>, selected: MeasureUnit, haptics: Haptics, onPick: (MeasureUnit) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val gap = 6.dp
    val rows = units.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = modifier.padding(horizontal = gap * 2, vertical = gap)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
                row.forEach { unit ->
                    val isSelected = unit == selected
                    val content = if (isSelected) colors.onPrimary else colors.onSurface
                    KeyCell(
                        container = if (isSelected) colors.primary else colors.surfaceContainerHigh,
                        content = content,
                        description = unit.name,
                        haptics = haptics,
                        onClick = { onPick(unit) },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(unit.symbol, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 22.sp, fontWeight = FontWeight.Medium), color = content, maxLines = 1)
                            Text(unit.name, style = MaterialTheme.typography.labelSmall, color = content.copy(alpha = 0.75f), maxLines = 1)
                        }
                    }
                }
                repeat(3 - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

private val KeyStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 30.sp)

@Composable
private fun ConverterKeypad(
    decimal: Char,
    haptics: Haptics,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onNegate: () -> Unit,
    canUse: Boolean,
    onUse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val gap = 6.dp
    val rows = listOf("789", "456", "123")
    Column(verticalArrangement = Arrangement.spacedBy(gap), modifier = modifier.padding(horizontal = gap * 2, vertical = gap)) {
        rows.forEachIndexed { index, digits ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
                digits.forEach { d -> TextKey(d.toString(), colors.surfaceContainerHigh, colors.onSurface, haptics, { onDigit(d) }) }
                when (index) {
                    // The same tones as the calculator: its side column tinted, C marked by its ink.
                    0 -> IconKey(colors.secondaryContainer, colors.onSecondaryContainer, haptics, onDelete, onLongClick = onClear)
                    1 -> TextKey("C", colors.surfaceContainerHighest, colors.tertiary, haptics, onClear)
                    else -> TextKey("±", colors.secondaryContainer, colors.onSecondaryContainer, haptics, onNegate)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
            TextKey("0", colors.surfaceContainerHigh, colors.onSurface, haptics, { onDigit('0') }, weight = 2f)
            TextKey(decimal.toString(), colors.surfaceContainerHigh, colors.onSurface, haptics, onDecimal)
            UseKey(enabled = canUse, haptics = haptics, onUse = onUse)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TextKey(
    label: String,
    container: Color,
    content: Color,
    haptics: Haptics,
    onClick: () -> Unit,
    weight: Float = 1f,
) {
    KeyCell(
        container = container,
        content = content,
        description = label,
        haptics = haptics,
        onClick = onClick,
        modifier = Modifier.weight(weight).fillMaxHeight(),
    ) { Text(label, style = KeyStyle, color = content) }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.IconKey(
    container: Color,
    content: Color,
    haptics: Haptics,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val description = stringResource(Res.string.converter_backspace)
    KeyCell(
        container = container,
        content = content,
        description = description,
        haptics = haptics,
        onClick = onClick,
        onLongClick = { haptics.heavy(); onLongClick() },
        modifier = Modifier.weight(1f).fillMaxHeight().semantics { contentDescription = description },
    ) { Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = null, tint = content, modifier = Modifier.size(26.dp)) }
}

/** The accent key: takes the converted value back into the calculator. */
@Composable
private fun androidx.compose.foundation.layout.RowScope.UseKey(enabled: Boolean, haptics: Haptics, onUse: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val description = stringResource(Res.string.converter_use_description)
    val container by animateColorAsState(if (enabled) colors.primary else colors.surfaceContainerHighest, label = "use")
    val content = if (enabled) colors.onPrimary else colors.onSurfaceVariant.copy(alpha = 0.5f)
    KeyCell(
        container = container,
        content = content,
        description = description,
        haptics = haptics,
        onClick = { if (enabled) { haptics.confirm(); onUse() } },
        modifier = Modifier.weight(1f).fillMaxHeight().semantics { contentDescription = description },
    ) { Text(stringResource(Res.string.converter_use), style = KeyStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Medium), color = content) }
}

private fun Category.title(): StringResource = when (this) {
    Category.LENGTH -> Res.string.unit_length
    Category.MASS -> Res.string.unit_mass
    Category.TEMPERATURE -> Res.string.unit_temperature
    Category.AREA -> Res.string.unit_area
    Category.VOLUME -> Res.string.unit_volume
    Category.SPEED -> Res.string.unit_speed
    Category.TIME -> Res.string.unit_time
    Category.DATA -> Res.string.unit_data
    Category.ENERGY -> Res.string.unit_energy
    Category.PRESSURE -> Res.string.unit_pressure
}
