package com.bernaferrari.calculator.web

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.ComposeViewport
import com.bernaferrari.calculator.App
import com.bernaferrari.calculator.resources.Res
import kotlinx.browser.document

/**
 * A browser canvas has no system fonts to fall back on, so the symbols Google Sans Flex lacks
 * (π, √, ∛, ƒ, superscripts) come from two tiny bundled subsets, preloaded before the app shows.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        val resolver = LocalFontFamilyResolver.current
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            runCatching {
                resolver.preload(FontFamily(Font("CalculatorMath", Res.readBytes("files/math_fallback.ttf"))))
                resolver.preload(FontFamily(Font("CalculatorRoots", Res.readBytes("files/math_roots.otf"))))
            }.onFailure { println("Fallback fonts unavailable: $it") }
            ready = true
            document.getElementById("loading")?.remove()
        }
        if (ready) App()
    }
}
