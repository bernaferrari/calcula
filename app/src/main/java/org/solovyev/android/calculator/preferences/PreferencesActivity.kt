package org.solovyev.android.calculator.preferences

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.ActivityLauncher
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.android.calculator.FeatureFlags
import org.solovyev.android.calculator.feedback.FeedbackReporter
import org.solovyev.android.calculator.billing.BillingManager
import org.solovyev.android.wizard.Wizards
import javax.inject.Inject

@AndroidEntryPoint
open class PreferencesActivity : BaseActivity(R.string.cpp_settings) {

    @Inject
    lateinit var billingManager: BillingManager

    @Inject
    lateinit var feedbackReporter: FeedbackReporter

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var wizards: Wizards

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @Composable
    override fun Content() {
        val themePreference by appPreferences.settings.theme.collectAsStateWithLifecycle(
            initialValue = appPreferences.settings.getThemeBlocking()
        )
        val destination = destinationFromIntent(intent)
        CalculatorTheme(
            theme = themePreference
        ) {
            PreferencesContent(
                destination = destination,
                onBack = { finish() }
            )
        }
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun PreferencesContent(
        destination: SettingsDestination,
        onBack: () -> Unit
    ) {
        var activeDestination by rememberSaveable(destination) {
            mutableStateOf(destination)
        }
        var backStack by rememberSaveable { mutableStateOf(listOf<SettingsDestination>()) }
        val billingEnabled = FeatureFlags.ENABLE_BILLING
        val adFreePurchased by if (billingEnabled) {
            billingManager.adFreePurchased.collectAsStateWithLifecycle()
        } else {
            remember { mutableStateOf(true) }
        }

        fun navigateTo(next: SettingsDestination) {
            backStack = backStack + activeDestination
            activeDestination = next
        }

        fun handleBack() {
            val previous = backStack.lastOrNull()
            if (previous != null) {
                activeDestination = previous
                backStack = backStack.dropLast(1)
            } else {
                onBack()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(titleForDestinationRes(activeDestination))) },
                    navigationIcon = {
                        IconButton(onClick = ::handleBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = getString(R.string.cpp_back)
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                SettingsScreen(
                    destination = activeDestination,
                    adFreePurchased = adFreePurchased,
                    onNavigate = ::navigateTo,
                    onStartWizard = { /* TODO: Trigger wizard via Navigation 3 when this is a screen */ },
                    onReportBug = { feedbackReporter.report() },
                    onOpenAbout = { launcher.showAbout() },
                    onSupportProject = {
                        if (billingEnabled) {
                            startActivity(Intent(this@PreferencesActivity, PurchaseDialogActivity::class.java))
                        }
                    }
                )
            }
        }
    }

    class Dialog : PreferencesActivity()

    companion object {
        const val EXTRA_DESTINATION = "destination"

        private fun titleForDestinationRes(destination: SettingsDestination): Int {
            return when (destination) {
                SettingsDestination.MAIN -> R.string.cpp_settings
                SettingsDestination.NUMBER_FORMAT -> R.string.cpp_number_format
                SettingsDestination.APPEARANCE -> R.string.cpp_appearance
                SettingsDestination.ONSCREEN -> R.string.cpp_floating_calculator
                SettingsDestination.WIDGET -> R.string.cpp_widget
                SettingsDestination.OTHER -> R.string.cpp_other
            }
        }

        fun getClass(context: Context): Class<out PreferencesActivity> {
            return if (App.isTablet(context)) Dialog::class.java else PreferencesActivity::class.java
        }

        fun makeIntent(
            context: Context,
            destination: SettingsDestination
        ): Intent {
            val intent = Intent(context, getClass(context))
            intent.putExtra(EXTRA_DESTINATION, destination.name)
            return intent
        }

        private fun destinationFromIntent(intent: Intent): SettingsDestination {
            val name = intent.getStringExtra(EXTRA_DESTINATION)
            return if (name.isNullOrBlank()) {
                SettingsDestination.MAIN
            } else {
                runCatching { SettingsDestination.valueOf(name) }
                    .getOrDefault(SettingsDestination.MAIN)
            }
        }
    }
}
