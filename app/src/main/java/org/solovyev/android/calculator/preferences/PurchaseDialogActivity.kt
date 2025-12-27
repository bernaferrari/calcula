package org.solovyev.android.calculator.preferences

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.billing.BillingManager
import javax.inject.Inject

@AndroidEntryPoint
class PurchaseDialogActivity : ComponentActivity() {

    @Inject
    lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                val adFreePurchased by billingManager.adFreePurchased.collectAsStateWithLifecycle()
                LaunchedEffect(adFreePurchased) {
                    if (adFreePurchased) {
                        finish()
                    }
                }
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text(text = getString(R.string.cpp_purchase_title)) },
                    text = { Text(text = getString(R.string.cpp_purchase_text)) },
                    confirmButton = {
                        TextButton(onClick = { purchase() }) {
                            Text(text = getString(R.string.cpp_continue))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text(text = getString(R.string.cpp_cancel))
                        }
                    }
                )
            }
        }
    }

    private fun purchase() {
        lifecycleScope.launch {
            billingManager.launchAdFreePurchase(this@PurchaseDialogActivity)
        }
    }
}
