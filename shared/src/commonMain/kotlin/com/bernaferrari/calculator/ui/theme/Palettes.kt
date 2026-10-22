package com.bernaferrari.calculator.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.ColorStyle
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

/**
 * The full Material 3 scheme for [palette] in [style], generated from its seed. The wallpaper's
 * own scheme is used as is in the default style; the other styles restyle its main color.
 * Graphite stays truly neutral whatever the style.
 */
@Composable
fun paletteColorScheme(palette: AppPalette, dark: Boolean, style: ColorStyle = ColorStyle.TONAL_SPOT): ColorScheme {
    val seed = if (palette == AppPalette.WALLPAPER) {
        val wallpaper = wallpaperColorScheme(dark)
        if (wallpaper != null && style == ColorStyle.TONAL_SPOT) return wallpaper
        // The light scheme's primary is the closest stand-in for the wallpaper's seed.
        wallpaperColorScheme(false)?.primary ?: Color(palette.seed)
    } else {
        Color(palette.seed)
    }
    val kolorStyle = if (palette == AppPalette.GRAPHITE) PaletteStyle.Neutral else style.toKolor()
    return remember(seed, dark, kolorStyle) {
        dynamicColorScheme(seedColor = seed, isDark = dark, style = kolorStyle, specVersion = ColorSpec.SpecVersion.SPEC_2025)
    }
}

/** Whether the style choice changes anything for [palette] (graphite is always neutral). */
fun AppPalette.hasStyles(): Boolean = this != AppPalette.GRAPHITE

private fun ColorStyle.toKolor(): PaletteStyle = when (this) {
    // Tonal spot keeps secondary and tertiary in the seed's own family: one accent, not three.
    ColorStyle.TONAL_SPOT -> PaletteStyle.TonalSpot
    ColorStyle.NEUTRAL -> PaletteStyle.Neutral
    ColorStyle.VIBRANT -> PaletteStyle.Vibrant
    ColorStyle.EXPRESSIVE -> PaletteStyle.Expressive
    ColorStyle.RAINBOW -> PaletteStyle.Rainbow
}
