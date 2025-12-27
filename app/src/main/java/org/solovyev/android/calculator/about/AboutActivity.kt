package org.solovyev.android.calculator.about

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.about.AboutScreen
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

@AndroidEntryPoint
open class AboutActivity : BaseActivity(R.string.cpp_about) {

    @Composable
    override fun Content() {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        CalculatorTheme(theme = themePreference) {
            AboutScreen(onBack = { finish() })
        }

    }

    @AndroidEntryPoint
    class Dialog : AboutActivity()

    companion object {
        @JvmStatic
        fun getClass(context: Context): Class<out AboutActivity> {
            return if (App.isTablet(context)) Dialog::class.java else AboutActivity::class.java
        }
    }
}
