@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class, ExperimentalWasmJsInterop::class)

package com.bernaferrari.calculator.store

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.ComposeViewport
import androidx.lifecycle.SavedStateHandle
import com.bernaferrari.calculator.domain.convert.Category
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.ColorStyle
import com.bernaferrari.calculator.resources.Res
import com.bernaferrari.calculator.ui.calculator.CalculatorScreen
import com.bernaferrari.calculator.ui.converter.ConverterScreen
import com.bernaferrari.calculator.ui.converter.ConverterViewModel
import com.bernaferrari.calculator.ui.graph.GraphScreen
import com.bernaferrari.calculator.ui.programmer.ProgrammerScreen
import com.bernaferrari.calculator.ui.programmer.ProgrammerViewModel
import com.bernaferrari.calculator.domain.programmer.Base
import com.bernaferrari.calculator.domain.programmer.ProgKey
import com.bernaferrari.calculator.ui.settings.CustomizeKeysScreen
import com.bernaferrari.calculator.ui.settings.SettingsScreen
import com.bernaferrari.calculator.ui.stats.StatisticsScreen
import com.bernaferrari.calculator.ui.theme.CalculatorTheme
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.coroutines.delay
private fun installCaptureExport(): Unit = js("""{
 const button=document.createElement('button');button.id='export-capture';button.textContent='Export capture PNG';
 button.style.cssText='position:fixed;z-index:99999;right:8px;top:8px;font:12px sans-serif;padding:8px;opacity:.35';
 button.onclick=()=>{
  function find(root){for(const el of root.children||[]){if(el.tagName==='CANVAS')return el;if(el.shadowRoot){const c=find(el.shadowRoot);if(c)return c;}const c=find(el);if(c)return c;}return null;}
  const canvas=find(document.body);if(!canvas)throw new Error('App canvas not ready');
  const image=document.createElement('img');image.id='capture-export';image.alt='Exported Calcula capture';image.src=canvas.toDataURL('image/png');image.style.cssText='position:fixed;inset:0;width:100%;height:100%;object-fit:contain;z-index:99998';document.body.appendChild(image);
 };
 document.body.appendChild(button);
}""")

private fun query(name: String): String? = js("new URLSearchParams(window.location.search).get(name)")

/** Built only with -PstoreCapture; fixtures cannot enter ordinary app distributions. */
fun main() {
    val scene = query("scene") ?: "calculator"
    val dark = query("theme") == "dark"
    val palette = AppPalette.entries.firstOrNull { it.name == query("palette") } ?: AppPalette.INDIGO
    val style = ColorStyle.entries.firstOrNull { it.name == query("style") } ?: ColorStyle.TONAL_SPOT
    val density = query("density")?.toFloatOrNull()?.coerceIn(1f, 4f) ?: 2.5f
    // Dedicated localhost origin; only fixture-owned keys are replaced, never real user data.
    localStorage.setItem("preferences", """{"themeMode":"${if (dark) "DARK" else "LIGHT"}","palette":"${palette.name}","colorStyle":"${style.name}","flickHints":"LABELS","reduceMotion":true,"swipeHintSeen":true,"decimalSeparator":"DOT","programmerBase":"HEX","statsData":["72","84","91","78","88","95","82","90"]}""")
    localStorage.setItem("history", """[
      {"id":5,"expression":"250×12","result":"3000","timestamp":1792798800000,"note":"Annual budget"},
      {"id":4,"expression":"1299×1.08","result":"1402.92","timestamp":1792798740000,"note":"Laptop with tax"},
      {"id":3,"expression":"84.5÷4","result":"21.125","timestamp":1792798680000,"note":"Split four ways"},
      {"id":2,"expression":"0.1+0.2","result":"0.3","timestamp":1792798620000},
      {"id":1,"expression":"48÷0.75","result":"64","timestamp":1792798560000}
    ]""")
    localStorage.setItem("graph", """{"functions":["sin(x)","a*sin(b*x)","x²+y²=4"],"active":1,"centerX":0.0,"centerY":0.0,"span":12.0,"parameters":{"a":{"value":2.0,"min":-4.0,"max":4.0},"b":{"value":1.0,"min":0.25,"max":3.0}}}""")
    ComposeViewport {
        val resolver = LocalFontFamilyResolver.current
        var ready by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            resolver.preload(FontFamily(Font("CalculatorMath", Res.readBytes("files/math_fallback.ttf"))))
            resolver.preload(FontFamily(Font("CalculatorRoots", Res.readBytes("files/math_roots.otf"))))
            ready = true
            document.getElementById("loading")?.remove()
        }
        if (ready) CompositionLocalProvider(LocalDensity provides Density(density)) {
            CalculatorTheme(darkTheme = dark, palette = palette, colorStyle = style) {
                Surface(Modifier.fillMaxSize()) {
                    when (scene) {
                        "graph" -> GraphScreen(onNavigateBack = {})
                        "converter" -> {
                            val vm = remember { ConverterViewModel(SavedStateHandle()).apply { onCategory(Category.TEMPERATURE) } }
                            ConverterScreen(seed = "22", onNavigateBack = {}, onUse = {}, viewModel = vm)
                        }
                        "customize" -> CustomizeKeysScreen(onNavigateBack = {})
                        "appearance" -> SettingsScreen(onNavigateBack = {}, onOpenAbout = {}, onOpenCustomizeKeys = {})
                        "programmer" -> {
                            val vm = remember { ProgrammerViewModel().apply {
                                onBase(Base.HEX)
                                listOf(ProgKey.C, ProgKey.D0, ProgKey.F, ProgKey.F, ProgKey.E, ProgKey.E).forEach(::onKey)
                            } }
                            ProgrammerScreen(onNavigateBack = {}, viewModel = vm)
                        }
                        "statistics" -> StatisticsScreen(onNavigateBack = {}, onUse = {})
                        else -> CalculatorScreen(initialExpression = "1450×1.15", onOpenSettings = {}, onOpenConverter = {})
                    }
                }
            }
            LaunchedEffect(scene) {
                delay(800)
                installCaptureExport()
                document.title = "Capture ready: $scene"
            }
        }
    }
}
