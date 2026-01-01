package org.solovyev.android.calculator

import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.ui.CalculatorApp

class CalculatorActivity : BaseActivity(R.string.cpp_app_name) {

    private val history: History by inject()

    private var useBackAsPrevious = false

    @Composable
    override fun Content() {
        CalculatorApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        useBackAsPrevious = runBlocking { appPreferences.gui.useBackAsPrevious.first() }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0 && useBackAsPrevious) {
            lifecycleScope.launch {
                history.undo()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        restartIfModeChanged()
    }
}
