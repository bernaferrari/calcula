package org.solovyev.android.calculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.solovyev.android.calculator.ui.CalculatorApp

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

    private var useBackAsPrevious = false
    private var pendingExpression: String? = null
    private var openConverter = false
    private var openHistory = false

    @Composable
    override fun Content() {
        CalculatorApp(
            initialExpression = pendingExpression,
            openHistory = openHistory,
            onInitialExpressionConsumed = {
                pendingExpression = null
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
        pendingExpression = intent.getStringExtra(EXTRA_EXPRESSION)
        openConverter = intent.getBooleanExtra(EXTRA_OPEN_CONVERTER, false)
        openHistory = intent.getBooleanExtra(EXTRA_OPEN_HISTORY, false)
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
