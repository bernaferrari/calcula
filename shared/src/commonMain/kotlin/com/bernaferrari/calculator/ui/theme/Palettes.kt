package com.bernaferrari.calculator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.platform.supportsDynamicColor
import com.bernaferrari.calculator.platform.wallpaperColorScheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.material3.dynamicColorScheme

/** The palettes this platform can show, wallpaper first where it exists. */
val availablePalettes: List<AppPalette>
    get() = AppPalette.entries.filter { it != AppPalette.WALLPAPER || supportsDynamicColor }

/** What a person who never chose gets: their wallpaper where possible, indigo elsewhere. */
fun AppPalette?.orDefault(): AppPalette = when {
    this == null -> if (supportsDynamicColor) AppPalette.WALLPAPER else AppPalette.INDIGO
    this == AppPalette.WALLPAPER && !supportsDynamicColor -> AppPalette.INDIGO
    else -> this
}

/** The full Material 3 scheme for [palette], generated from its seed (or taken from the wallpaper). */
@Composable
fun paletteColorScheme(palette: AppPalette, dark: Boolean): ColorScheme {
    if (palette == AppPalette.WALLPAPER) wallpaperColorScheme(dark)?.let { return it }
    return remember(palette, dark) {
        dynamicColorScheme(
            seedColor = Color(palette.seed),
            isDark = dark,
            // Tonal spot keeps secondary and tertiary in the seed's own family: one accent, not three.
            // Graphite stays truly neutral.
            style = if (palette == AppPalette.GRAPHITE) PaletteStyle.Neutral else PaletteStyle.TonalSpot,
            specVersion = ColorSpec.SpecVersion.SPEC_2025,
        )
    }
}
