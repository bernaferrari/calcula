package org.solovyev.android.calculator.errors

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.IntentCompat
import androidx.core.os.BundleCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.UiPreferences
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme
import org.solovyev.common.msg.Message
import javax.inject.Inject

@AndroidEntryPoint
class FixableErrorsActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var uiPreferences: UiPreferences

    private var errorsState: ArrayList<FixableError>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialErrors = savedInstanceState?.let {
            BundleCompat.getParcelableArrayList(it, STATE_ERRORS, FixableError::class.java)
        } ?: intent?.let {
            IntentCompat.getParcelableArrayListExtra(it, EXTRA_ERRORS, FixableError::class.java)
        }

        if (initialErrors == null || initialErrors.isEmpty()) {
            finish()
            return
        }
        errorsState = ArrayList(initialErrors)

        setContent {
            CalculatorTheme {
                var errors by remember { mutableStateOf(initialErrors.toList()) }
                val current = errors.firstOrNull()

                if (current == null || !uiPreferences.showFixableErrorDialog) {
                    finish()
                } else {
                    AlertDialog(
                        onDismissRequest = {
                            errors = errors.drop(1)
                            errorsState = ArrayList(errors)
                            if (errors.isEmpty()) finish()
                        },
                        title = { Text(text = getString(R.string.cpp_fixable_error_title)) },
                        text = { Text(text = current.message) },
                        confirmButton = {
                            val error = current.error
                            if (error != null) {
                                TextButton(
                                    onClick = {
                                        lifecycleScope.launch {
                                            error.fix(appPreferences.settings)
                                        }
                                        errors = errors.drop(1)
                                        errorsState = ArrayList(errors)
                                        if (errors.isEmpty()) finish()
                                    }
                                ) {
                                    Text(text = getString(R.string.fix))
                                }
                            }
                        },
                        dismissButton = {
                            androidx.compose.foundation.layout.Row {
                                TextButton(
                                    onClick = {
                                        uiPreferences.setShowFixableErrorDialog(false)
                                        finish()
                                    }
                                ) {
                                    Text(text = getString(R.string.cpp_dont_show_again))
                                }
                                TextButton(
                                    onClick = {
                                        errors = errors.drop(1)
                                        errorsState = ArrayList(errors)
                                        if (errors.isEmpty()) finish()
                                    }
                                ) {
                                    Text(text = getString(R.string.close))
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        errorsState?.let { outState.putParcelableArrayList(STATE_ERRORS, it) }
    }

    companion object {
        const val EXTRA_ERRORS = "errors"
        const val STATE_ERRORS = "errors"

        fun show(context: Context, messages: List<Message>) {
            val errors = ArrayList(messages.map { FixableError(it) })
            show(context, errors)
        }

        fun show(context: Context, errors: ArrayList<FixableError>) {
            val intent = Intent(context, FixableErrorsActivity::class.java).apply {
                putExtra(EXTRA_ERRORS, errors)
                App.addIntentFlags(this, false, context)
            }
            context.startActivity(intent)
        }
    }
}
