package com.bernaferrari.calculator.ui.converter

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.R
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.domain.convert.Category
import com.bernaferrari.calculator.domain.convert.MeasureUnit
import com.bernaferrari.calculator.domain.convert.Units
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.Haptics
import com.bernaferrari.calculator.ui.calculator.KeyCell
import com.bernaferrari.calculator.ui.calculator.NumberFeatures
import com.bernaferrari.calculator.ui.calculator.locale
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex

/**
 * Unit conversion in the calculator's own language: two rows (tap one to type into it), a swap
 * button between them, and the same keys as the main keypad.
 */
@Composable
fun ConverterScreen(onNavigateBack: () -> Unit, viewModel: ConverterViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings: SettingsViewModel = hiltViewModel()
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val locale = LocalConfiguration.current.locales[0]
    val formatLocale = prefs.decimalSeparator.locale(locale)
    val formatter = remember(formatLocale) { DisplayFormatter(formatLocale) }
    var picking by remember { mutableStateOf<Boolean?>(null) }
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("converter", isScreen = true).fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
            Text(stringResource(R.string.menu_unit_converter), style = MaterialTheme.typography.titleLarge)
        }
        LazyRow(
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

        Box(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxSize().padding(vertical = 8.dp)) {
                UnitRow(
                    unit = state.top,
                    text = state.topText,
                    active = state.topActive,
                    formatter = formatter,
                    onActivate = { haptics.tap(); viewModel.onActivate(top = true) },
                    onPickUnit = { picking = true },
                    modifier = Modifier.weight(1f),
                )
                UnitRow(
                    unit = state.bottom,
                    text = state.bottomText,
                    active = !state.topActive,
                    formatter = formatter,
                    onActivate = { haptics.tap(); viewModel.onActivate(top = false) },
                    onPickUnit = { picking = false },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Choosing a unit morphs the keypad into the unit tiles, in place: no sheet over the numbers.
        BackHandler(enabled = picking != null) { picking = null }
        AnimatedContent(
            targetState = picking,
            transitionSpec = {
                (fadeIn(tween(160, delayMillis = 60)) + scaleIn(spring(dampingRatio = 0.75f, stiffness = 500f), initialScale = 0.94f)) togetherWith
                    (fadeOut(tween(100)) + scaleOut(tween(140), targetScale = 0.96f))
            },
            label = "keypad",
            modifier = Modifier.fillMaxWidth().height(300.dp + bottomInset).padding(bottom = bottomInset),
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
                    onSwap = viewModel::onSwap,
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
    }
}

@Composable
private fun UnitRow(
    unit: MeasureUnit,
    text: String,
    active: Boolean,
    formatter: DisplayFormatter,
    onActivate: () -> Unit,
    onPickUnit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val numberColor by animateColorAsState(if (active) colors.onSurface else colors.onSurfaceVariant, label = "number")
    val accent by animateColorAsState(if (active) colors.primary else Color.Transparent, label = "accent")
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onActivate)
            .padding(horizontal = 24.dp),
    ) {
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(1f)) {
            // The unit is a chip: tap it to choose a different one.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onPickUnit)
                    .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            ) {
                Text(unit.name, style = MaterialTheme.typography.titleMedium, color = colors.onSurfaceVariant)
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = colors.onSurfaceVariant)
            }
            Row {
                BasicText(
                    text = if (text.isEmpty()) "0" else formatter.number(text),
                    style = TextStyle(
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Normal,
                        fontFeatureSettings = NumberFeatures,
                        textAlign = TextAlign.End,
                        color = if (text.isEmpty()) colors.onSurfaceVariant.copy(alpha = 0.4f) else numberColor,
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 24.sp, maxFontSize = 56.sp, stepSize = 2.sp),
                    modifier = Modifier.weight(1f, fill = false).alignByBaseline(),
                )
                Text(
                    unit.symbol,
                    style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp),
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp).alignByBaseline(),
                )
            }
            // A short accent bar marks the row you're typing into.
            Box(Modifier.padding(top = 4.dp).size(width = 32.dp, height = 3.dp).background(accent, CircleShape))
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

private val KeyStyle = TextStyle(fontFamily = GoogleSansFlex, fontSize = 30.sp, fontFeatureSettings = NumberFeatures)

@Composable
private fun ConverterKeypad(
    decimal: Char,
    haptics: Haptics,
    onDigit: (Char) -> Unit,
    onDecimal: () -> Unit,
    onDelete: () -> Unit,
    onClear: () -> Unit,
    onNegate: () -> Unit,
    onSwap: () -> Unit,
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
                    0 -> IconKey(colors.surfaceContainerHighest, colors.onSurfaceVariant, haptics, onDelete, onLongClick = onClear)
                    1 -> TextKey("AC", colors.surfaceContainerHighest, colors.tertiary, haptics, onClear)
                    else -> TextKey("±", colors.surfaceContainerHighest, colors.onSurfaceVariant, haptics, onNegate)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(gap), modifier = Modifier.weight(1f)) {
            TextKey("0", colors.surfaceContainerHigh, colors.onSurface, haptics, { onDigit('0') }, weight = 2f)
            TextKey(decimal.toString(), colors.surfaceContainerHigh, colors.onSurface, haptics, onDecimal)
            SwapKey(haptics, onSwap)
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
    val description = stringResource(R.string.converter_backspace)
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

/** The accent key: swaps the two units, the arrows turning half a circle each time. */
@Composable
private fun androidx.compose.foundation.layout.RowScope.SwapKey(haptics: Haptics, onSwap: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var turns by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(turns * 180f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow), label = "swap")
    KeyCell(
        container = colors.primary,
        content = colors.onPrimary,
        description = stringResource(R.string.converter_swap),
        haptics = haptics,
        onClick = { turns += 1; onSwap() },
        modifier = Modifier.weight(1f).fillMaxHeight(),
    ) { Icon(Icons.Rounded.SwapVert, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(30.dp).rotate(rotation)) }
}

private fun Category.title(): Int = when (this) {
    Category.LENGTH -> R.string.unit_length
    Category.MASS -> R.string.unit_mass
    Category.TEMPERATURE -> R.string.unit_temperature
    Category.AREA -> R.string.unit_area
    Category.VOLUME -> R.string.unit_volume
    Category.SPEED -> R.string.unit_speed
    Category.TIME -> R.string.unit_time
    Category.DATA -> R.string.unit_data
    Category.ENERGY -> R.string.unit_energy
    Category.PRESSURE -> R.string.unit_pressure
}
