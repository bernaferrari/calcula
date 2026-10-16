@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.calculator.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.bernaferrari.calculator.resources.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.ui.theme.availablePalettes
import com.bernaferrari.calculator.ui.theme.paletteColorScheme
import org.jetbrains.compose.resources.stringResource

/** Where a row sits in its group: outer corners are generous, inner ones tight (Android 16 style). */
enum class SegmentPosition { Only, First, Middle, Last }

fun segmentShape(position: SegmentPosition): Shape = when (position) {
    SegmentPosition.Only -> RoundedCornerShape(24.dp)
    SegmentPosition.First -> RoundedCornerShape(24.dp, 24.dp, 6.dp, 6.dp)
    SegmentPosition.Middle -> RoundedCornerShape(6.dp)
    SegmentPosition.Last -> RoundedCornerShape(6.dp, 6.dp, 24.dp, 24.dp)
}

/** A titled group of rows; each row gets its [SegmentPosition] so the group reads as one card. */
@Composable
fun SettingsSection(title: String, rows: List<@Composable (SegmentPosition) -> Unit>) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 36.dp, end = 20.dp, top = 24.dp, bottom = 8.dp),
    )
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        rows.forEachIndexed { index, row ->
            val position = when {
                rows.size == 1 -> SegmentPosition.Only
                index == 0 -> SegmentPosition.First
                index == rows.lastIndex -> SegmentPosition.Last
                else -> SegmentPosition.Middle
            }
            row(position)
        }
    }
}

@Composable
private fun RowSurface(position: SegmentPosition, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = segmentShape(position),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier.fillMaxWidth(),
    ) { content() }
}

@Composable
private fun RowIcon(icon: ImageVector, tint: Color = MaterialTheme.colorScheme.primary) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(alpha = 0.12f)),
    ) { Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp)) }
}

@Composable
private fun RowText(title: String, subtitle: String?, modifier: Modifier = Modifier, titleColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = titleColor)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun SwitchRow(
    position: SegmentPosition,
    icon: ImageVector,
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    RowSurface(position, Modifier.toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 12.dp)) {
            RowIcon(icon)
            Spacer(Modifier.width(16.dp))
            RowText(title, subtitle, Modifier.weight(1f))
            Spacer(Modifier.width(12.dp))
            Switch(checked = checked, onCheckedChange = null)
        }
    }
}

@Composable
fun ActionRow(
    position: SegmentPosition,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    destructive: Boolean = false,
    showChevron: Boolean = true,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    RowSurface(position, modifier.then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 12.dp)) {
            RowIcon(icon, tint)
            Spacer(Modifier.width(16.dp))
            RowText(title, subtitle, Modifier.weight(1f), titleColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            if (onClick != null && showChevron) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** A setting with a few exclusive values, shown as a connected button group under its title. */
@Composable
fun <T> ChoiceRow(
    position: SegmentPosition,
    icon: ImageVector,
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    subtitle: String? = null,
) {
    RowSurface(position) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RowIcon(icon)
                Spacer(Modifier.width(16.dp))
                RowText(title, subtitle)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                modifier = Modifier.fillMaxWidth().selectableGroup(),
            ) {
                val labels = options.map { label(it) }
                options.forEachIndexed { index, option ->
                    // Wider labels ("Auto", "1,234.5") get proportionally wider buttons.
                    val width = 1f + 0.12f * (labels[index].length - 1).coerceAtLeast(0)
                    ToggleButton(
                        checked = option == selected,
                        onCheckedChange = { if (it) onSelect(option) },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        colors = ToggleButtonDefaults.colors(
                            checkedContainerColor = MaterialTheme.colorScheme.primary,
                            checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier.weight(width).heightIn(min = 48.dp),
                    ) { Text(labels[index], maxLines = 1, softWrap = false) }
                }
            }
        }
    }
}

enum class PreviewKind { System, Light, Dark }

/** Three little calculators to pick the theme by sight rather than by name. */
@Composable
fun ThemePicker(position: SegmentPosition, selected: PreviewKind, palette: AppPalette, label: @Composable (PreviewKind) -> String, onSelect: (PreviewKind) -> Unit) {
    // The previews wear the chosen palette in both modes, so the choice is visible before it's made.
    val light = paletteColorScheme(palette, dark = false)
    val dark = paletteColorScheme(palette, dark = true)
    RowSurface(position) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth().padding(16.dp).selectableGroup(),
        ) {
            PreviewKind.entries.forEach { kind ->
                val isSelected = kind == selected
                val ring by animateDpAsState(if (isSelected) 3.dp else 0.dp, label = "ring")
                val labelColor by animateColorAsState(
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "label",
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    // A phone-sized preview, never a poster on wide screens.
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .widthIn(max = 128.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .selectable(isSelected, role = Role.RadioButton) { onSelect(kind) },
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.72f)
                            .border(ring, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                            .padding(5.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    ) {
                        MiniCalculator(kind, light, dark, Modifier.fillMaxSize())
                        if (isSelected) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(22.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            ) { Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp)) }
                        }
                    }
                    Text(label(kind), style = MaterialTheme.typography.labelLarge, color = labelColor, modifier = Modifier.padding(bottom = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniCalculator(kind: PreviewKind, light: ColorScheme, dark: ColorScheme, modifier: Modifier) {
    Canvas(modifier) {
        when (kind) {
            PreviewKind.Light -> drawMini(light)
            PreviewKind.Dark -> drawMini(dark)
            PreviewKind.System -> {
                // Half light, half dark, split on the diagonal.
                drawMini(light)
                val half = Path().apply {
                    moveTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                clipPath(half) { drawMini(dark) }
            }
        }
    }
}

/** A tiny calculator in [scheme]'s colors: a display line on top and a 3×4 keypad with an accent `=`. */
private fun DrawScope.drawMini(scheme: ColorScheme) {
    drawRect(scheme.surface)
    val pad = size.width * 0.1f
    val gap = size.width * 0.05f
    // Display: two lines of "text".
    drawRoundRect(scheme.onSurfaceVariant.copy(alpha = 0.5f), Offset(size.width * 0.45f, size.height * 0.12f), Size(size.width * 0.45f, size.height * 0.04f), CornerRadius(8f))
    drawRoundRect(scheme.onSurface, Offset(size.width * 0.3f, size.height * 0.2f), Size(size.width * 0.6f, size.height * 0.07f), CornerRadius(8f))
    val cols = 3
    val rows = 4
    val top = size.height * 0.38f
    val keyW = (size.width - pad * 2 - gap * (cols - 1)) / cols
    val keyH = (size.height - top - pad - gap * (rows - 1)) / rows
    for (r in 0 until rows) for (c in 0 until cols) {
        val color = when {
            r == rows - 1 && c == cols - 1 -> scheme.primary
            c == cols - 1 -> scheme.surfaceContainerHighest
            else -> scheme.surfaceContainerHigh
        }
        drawRoundRect(
            color,
            Offset(pad + c * (keyW + gap), top + r * (keyH + gap)),
            Size(keyW, keyH),
            CornerRadius(keyH * 0.3f),
        )
    }
}

/**
 * Every color family as a gradient swatch, wallpaper colors first where the platform has them: a disc turned 45° that springs upright into a rounded square with
 * a ring when chosen. The gradient counter-rotates, so the colors hold still while the shape turns.
 */
@Composable
fun PaletteRow(position: SegmentPosition, selected: AppPalette, label: @Composable (AppPalette) -> String, onSelect: (AppPalette) -> Unit) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    RowSurface(position) {
        Column(Modifier.fillMaxWidth().selectableGroup().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                availablePalettes.forEach { palette ->
                    val seed = Color(palette.seed)
                    val wallpaper = if (palette == AppPalette.WALLPAPER) paletteColorScheme(palette, dark) else null
                    PaletteSwatch(
                        // Wallpaper shows its own three colors; every other family its seed, lightening.
                        colors = wallpaper?.let { listOf(it.primary, it.secondary, it.tertiary) }
                            ?: listOf(seed, androidx.compose.ui.graphics.lerp(seed, Color.White, 0.45f)),
                        selected = palette == selected,
                        description = label(palette),
                        onClick = { onSelect(palette) },
                        icon = if (wallpaper != null) Icons.Outlined.Wallpaper else null,
                    )
                }
            }
        }
    }
}

/** ChangeDetection's swatch: the change of shape, not only color, shows which one is chosen. */
@Composable
private fun PaletteSwatch(colors: List<Color>, selected: Boolean, description: String, onClick: () -> Unit, icon: ImageVector? = null, size: Dp = 46.dp) {
    val haptics = LocalHapticFeedback.current
    val interactions = remember { MutableInteractionSource() }
    val bouncy = spring<Dp>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val corner by animateDpAsState(if (selected) size * 0.3f else size / 2, bouncy, label = "corner")
    val ring = animateDpAsState(if (selected) 3.dp else 0.dp, bouncy, label = "ring").value.coerceAtLeast(0.dp)
    val rotation by animateFloatAsState(
        if (selected) 0f else 45f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rotation",
    )
    val ringColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent, label = "ringColor")
    val stops = if (colors.size == 1) colors + colors else colors
    val angle = -rotation * kotlin.math.PI.toFloat() / 180f
    val brush = remember(stops, angle) {
        object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                // The diagonal, turned by the counter-rotation, centred on the swatch.
                val half = Offset(
                    size.width * kotlin.math.cos(angle) - size.height * kotlin.math.sin(angle),
                    size.width * kotlin.math.sin(angle) + size.height * kotlin.math.cos(angle),
                ) / 2f
                val center = Offset(size.width / 2, size.height / 2)
                return LinearGradientShader(from = center - half, to = center + half, colors = stops)
            }
        }
    }
    val outer = RoundedCornerShape(corner.coerceAtLeast(0.dp))
    val inner = RoundedCornerShape((corner - ring).coerceAtLeast(0.dp))
    val core = RoundedCornerShape((corner - ring - 2.dp).coerceAtLeast(0.dp))
    Box(
        Modifier
            .size(size)
            .semantics { contentDescription = description }
            .selectable(selected = selected, role = Role.RadioButton, interactionSource = interactions, indication = null) {
                if (!selected) {
                    haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    onClick()
                }
            }
            .rotate(rotation)
            .clip(outer)
            .background(brush)
            .padding(ring)
            .clip(inner)
            .background(ringColor)
            .padding(if (selected) 2.dp else 0.dp)
            .clip(core)
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        if (icon != null) Icon(icon, null, Modifier.size(size * 0.42f).rotate(-rotation), tint = Color.White)
    }
}
