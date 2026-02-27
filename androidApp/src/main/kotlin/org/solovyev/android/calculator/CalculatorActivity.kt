package org.solovyev.android.calculator

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.solovyev.android.calculator.ui.CalculatorApp

class CalculatorActivity : BaseActivity(R.string.cpp_app_name) {

    private val editor: Editor by inject()

    private var useBackAsPrevious = false

    @Composable
    override fun Content() {
        CalculatorApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Mad Activity part 2
        lifecycleScope.launch {
            appPreferences.gui.useBackAsPrevious.collect { enabled ->
                useBackAsPrevious = enabled
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
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
}
