package org.solovyev.android.calculator.plot

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.android.plotter.Plotter
import javax.inject.Inject

@AndroidEntryPoint
class PlotActivity : BaseActivity(R.string.c_plot) {

    @Inject
    lateinit var plotter: Plotter

    @Composable
    override fun Content() {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        CalculatorTheme(theme = themePreference) {
            PlotScreen(
                plotter = plotter,
                calculator = calculator,
                onBack = { finish() }
            )
        }
    }

    companion object {
        const val POINTS_COUNT = 1000
    }
}
