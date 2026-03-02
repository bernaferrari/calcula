package org.solovyev.android.calculator

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.solovyev.android.calculator.language.Languages
import org.solovyev.android.calculator.ui.CalculatorApp
import org.solovyev.android.calculator.ui.settings.Language

/**
 * Main calculator activity with deep link support and intent handling.
 * 
 * Features:
 * - Deep linking to open calculator with pre-filled expressions
 * - Intent handling for expression sharing
 * - Edge-to-edge display support
 * - Hardware key handling (back button as undo)
 */
class CalculatorActivity : BaseActivity(R.string.cpp_app_name) {

    private val editor: Editor by inject()
    private val languages: Languages by inject()

    private var useBackAsPrevious = false
    private var pendingExpression: String? = null
    private var openConverter = false
    private var openHistory = false
    private var openSettings = false
    private var openVariables = false
    private var openFunctions = false

    @Composable
    override fun Content() {
        val languageOptions = languages.getList().map { language ->
            Language(
                code = if (language.code == Languages.SYSTEM_LANGUAGE_CODE) "system" else language.code,
                displayName = language.name
            )
        }
        CalculatorApp(
            initialExpression = pendingExpression,
            openConverter = openConverter,
            openHistory = openHistory,
            openSettings = openSettings,
            openVariables = openVariables,
            openFunctions = openFunctions,
            onInitialExpressionConsumed = {
                pendingExpression = null
            },
            onOpenConverterConsumed = {
                openConverter = false
            },
            onOpenHistoryConsumed = {
                openHistory = false
            },
            onOpenSettingsConsumed = {
                openSettings = false
            },
            onOpenVariablesConsumed = {
                openVariables = false
            },
            onOpenFunctionsConsumed = {
                openFunctions = false
            },
            availableLanguages = languageOptions
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        
        // Handle intent extras
        handleIntent(intent)
        
        lifecycleScope.launch {
            appPreferences.gui.useBackAsPrevious.collect { enabled ->
                useBackAsPrevious = enabled
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handle incoming intents including deep links and extras
     */
    private fun handleIntent(intent: Intent) {
        pendingExpression = null
        openConverter = false
        openHistory = false
        openSettings = false
        openVariables = false
        openFunctions = false

        // Handle deep link
        if (intent.action == Intent.ACTION_VIEW) {
            val data: Uri? = intent.data
            data?.let { uri ->
                when (uri.scheme) {
                    "calculatorpp" -> handleDeepLink(uri)
                    "https" -> handleHttpsLink(uri)
                }
            }
        }

        // Handle extras
        if (intent.hasExtra(EXTRA_EXPRESSION)) {
            pendingExpression = intent.getStringExtra(EXTRA_EXPRESSION)
        }
        if (intent.hasExtra(EXTRA_OPEN_CONVERTER)) {
            openConverter = intent.getBooleanExtra(EXTRA_OPEN_CONVERTER, false)
        }
        if (intent.hasExtra(EXTRA_OPEN_HISTORY)) {
            openHistory = intent.getBooleanExtra(EXTRA_OPEN_HISTORY, false)
        }
        if (intent.hasExtra(EXTRA_OPEN_SETTINGS)) {
            openSettings = intent.getBooleanExtra(EXTRA_OPEN_SETTINGS, false)
        }
        if (intent.hasExtra(EXTRA_OPEN_VARIABLES)) {
            openVariables = intent.getBooleanExtra(EXTRA_OPEN_VARIABLES, false)
        }
        if (intent.hasExtra(EXTRA_OPEN_FUNCTIONS)) {
            openFunctions = intent.getBooleanExtra(EXTRA_OPEN_FUNCTIONS, false)
        }
    }

    /**
     * Handle calculatorpp:// deep links
     */
    private fun handleDeepLink(uri: Uri) {
        when (uri.host) {
            "calculate" -> {
                pendingExpression = uri.getQueryParameter("expression")
            }
            "converter" -> {
                openConverter = true
            }
            "history" -> {
                openHistory = true
            }
            "settings" -> {
                openSettings = true
            }
            "variables" -> {
                openVariables = true
            }
            "functions" -> {
                openFunctions = true
            }
        }
    }

    /**
     * Handle HTTPS links for web-based sharing
     */
    private fun handleHttpsLink(uri: Uri) {
        if (uri.host == "calculatorpp.app" || uri.host == "www.calculatorpp.app") {
            when (uri.path) {
                "/calculate" -> {
                    pendingExpression = uri.getQueryParameter("q")
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle back button as undo when enabled
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0 && useBackAsPrevious) {
            if (editor.undo()) {
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
    }

    companion object {
        const val EXTRA_EXPRESSION = "expression"
        const val EXTRA_OPEN_CONVERTER = "open_converter"
        const val EXTRA_OPEN_HISTORY = "open_history"
        const val EXTRA_OPEN_SETTINGS = "open_settings"
        const val EXTRA_OPEN_VARIABLES = "open_variables"
        const val EXTRA_OPEN_FUNCTIONS = "open_functions"

        /**
         * Create a basic intent to open the calculator
         */
        fun newIntent(context: android.content.Context): Intent {
            return Intent(context, CalculatorActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
        }

        /**
         * Create an intent with a pre-filled expression
         */
        fun newIntentWithExpression(context: android.content.Context, expression: String): Intent {
            return newIntent(context).apply {
                putExtra(EXTRA_EXPRESSION, expression)
            }
        }

        /**
         * Create an intent to open the converter
         */
        fun newIntentForConverter(context: android.content.Context): Intent {
            return newIntent(context).apply {
                putExtra(EXTRA_OPEN_CONVERTER, true)
            }
        }

        /**
         * Create an intent to open the history
         */
        fun newIntentForHistory(context: android.content.Context): Intent {
            return newIntent(context).apply {
                putExtra(EXTRA_OPEN_HISTORY, true)
            }
        }

        fun newIntentForSettings(context: android.content.Context): Intent {
            return newIntent(context).apply {
                putExtra(EXTRA_OPEN_SETTINGS, true)
            }
        }

        fun newIntentForVariables(context: android.content.Context): Intent {
            return newIntent(context).apply {
                putExtra(EXTRA_OPEN_VARIABLES, true)
            }
        }

        fun newIntentForFunctions(context: android.content.Context): Intent {
            return newIntent(context).apply {
                putExtra(EXTRA_OPEN_FUNCTIONS, true)
            }
        }

        /**
         * Create a deep link URI for sharing calculations
         */
        fun createDeepLinkUri(expression: String): Uri {
            return Uri.parse("calculatorpp://calculate")
                .buildUpon()
                .appendQueryParameter("expression", expression)
                .build()
        }
    }
}
