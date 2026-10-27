package com.bernaferrari.calculator

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import com.bernaferrari.calculator.domain.convert.Category
import com.bernaferrari.calculator.domain.model.AppPalette
import com.bernaferrari.calculator.domain.model.ColorStyle
import com.bernaferrari.calculator.domain.programmer.Base
import com.bernaferrari.calculator.domain.programmer.ProgKey
import com.bernaferrari.calculator.ui.calculator.CalculatorScreen
import com.bernaferrari.calculator.ui.converter.ConverterScreen
import com.bernaferrari.calculator.ui.converter.ConverterViewModel
import com.bernaferrari.calculator.ui.graph.GraphScreen
import com.bernaferrari.calculator.ui.programmer.ProgrammerScreen
import com.bernaferrari.calculator.ui.programmer.ProgrammerViewModel
import com.bernaferrari.calculator.ui.settings.CustomizeKeysScreen
import com.bernaferrari.calculator.ui.settings.SettingsScreen
import com.bernaferrari.calculator.ui.stats.StatisticsScreen
import com.bernaferrari.calculator.ui.theme.CalculatorTheme

/** Only in the isolated .storecapture APK. Every scene is the production Android UI. */
class StoreCaptureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val transparent = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = transparent, navigationBarStyle = transparent)
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        val scene = intent.getStringExtra("scene") ?: "calculator"
        val dark = intent.getStringExtra("theme") == "dark"
        val reduceMotion = intent.getBooleanExtra("reduceMotion", true)
        val palette = AppPalette.entries.firstOrNull { it.name == intent.getStringExtra("palette") } ?: AppPalette.INDIGO
        val style = ColorStyle.entries.firstOrNull { it.name == intent.getStringExtra("style") } ?: ColorStyle.TONAL_SPOT
        val phoneCalculator = scene == "calculator" && resources.configuration.screenWidthDp < 600
        val historyEntries = listOf(
            """{"id":5,"expression":"250×12","result":"3000","timestamp":1792881600000,"note":"Annual budget"}""",
            """{"id":4,"expression":"1299×1.08","result":"1402.92","timestamp":1792881540000,"note":"Laptop with tax"}""",
            """{"id":3,"expression":"84.5÷4","result":"21.125","timestamp":1792881480000,"note":"Split four ways"}""",
            """{"id":2,"expression":"0.1+0.2","result":"0.3","timestamp":1792881420000}""",
            """{"id":1,"expression":"48÷0.75","result":"64","timestamp":1792881360000}""",
        )
        // The package id is separate from ordinary debug and release installs; no real user data is touched.
        getSharedPreferences("calculator", MODE_PRIVATE).edit()
            .putString("preferences", """{"themeMode":"${if (dark) "DARK" else "LIGHT"}","palette":"${palette.name}","colorStyle":"${style.name}","flickHints":"LABELS","keypadScale":${if (phoneCalculator) 1.12f else 1f},"reduceMotion":$reduceMotion,"swipeHintSeen":true,"decimalSeparator":"DOT","programmerBase":"HEX","statsData":["72","84","91","78","88","95","82","90"],"programmerTape":[{"tokens":["FF","+","1"],"base":"HEX","word":"QWORD","value":256},{"tokens":["FF","AND","F"],"base":"HEX","word":"QWORD","value":15},{"tokens":["80","<<","1"],"base":"HEX","word":"QWORD","value":256}]}""")
            .putString("history", historyEntries.take(if (phoneCalculator) 1 else 5).joinToString(",", "[", "]"))
            .putString("graph", """{"functions":["sin(x)","a*sin(b*x)","x²+y²=4"],"active":1,"centerX":0.0,"centerY":0.0,"span":12.0,"parameters":{"a":{"value":2.0,"min":-4.0,"max":4.0},"b":{"value":1.0,"min":0.25,"max":3.0}}}""")
            .commit()
        setContent {
            CalculatorTheme(darkTheme = dark, palette = palette, colorStyle = style) {
                Surface(Modifier.fillMaxSize()) {
                    when (scene) {
                        "graph" -> GraphScreen(onNavigateBack = {})
                        "converter" -> {
                            val vm = remember { ConverterViewModel(SavedStateHandle()).apply { onCategory(Category.TEMPERATURE) } }
                            ConverterScreen(seed = "22", onNavigateBack = {}, onUse = {}, viewModel = vm)
                        }
                        "customize" -> CustomizeKeysScreen(onNavigateBack = {})
                        "appearance" -> SettingsScreen(onNavigateBack = {}, onOpenAbout = {})
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
        }
    }
}
