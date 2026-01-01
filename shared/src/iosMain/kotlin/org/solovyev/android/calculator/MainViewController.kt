package org.solovyev.android.calculator

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.context.startKoin
import org.solovyev.android.calculator.di.commonModule
import org.solovyev.android.calculator.di.platformModule
import org.solovyev.android.calculator.ui.CalculatorApp

private var koinInitialized = false

fun initializeApp() {
    if (!koinInitialized) {
        startKoin {
            modules(commonModule, platformModule)
        }
        koinInitialized = true
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = {
        initializeApp()
    }
) {
    CalculatorApp()
}
