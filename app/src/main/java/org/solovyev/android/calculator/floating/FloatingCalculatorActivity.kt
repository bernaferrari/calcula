package org.solovyev.android.calculator.floating

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ui.compose.theme.CalculatorTheme

@AndroidEntryPoint
class FloatingCalculatorActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        launchFloatingCalculator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CalculatorTheme {
                FloatingPermissionFlow(
                    onRequestOverlay = {
                        App.showSystemPermissionSettings(
                            this,
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                        )
                    },
                    onRequestNotifications = {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onContinueWithoutNotifications = { launchFloatingCalculator(this) },
                    onLaunch = { launchFloatingCalculator(this) },
                    onFinish = { finish() }
                )
            }
        }
    }

    private fun isPostNotificationsPermissionGranted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Composable
    private fun FloatingPermissionFlow(
        onRequestOverlay: () -> Unit,
        onRequestNotifications: () -> Unit,
        onContinueWithoutNotifications: () -> Unit,
        onLaunch: () -> Unit,
        onFinish: () -> Unit
    ) {
        val context = LocalContext.current
        val hasOverlayPermission = FloatingCalculatorView.isOverlayPermissionGranted(context)
        val hasPostNotificationsPermission = isPostNotificationsPermissionGranted()

        if (hasOverlayPermission && hasPostNotificationsPermission) {
            LaunchedEffect(Unit) { onLaunch() }
            return
        }

        if (!hasOverlayPermission) {
            AlertDialog(
                onDismissRequest = onFinish,
                title = { Text(text = getString(R.string.cpp_missing_permission_title)) },
                text = {
                    Text(
                        text = getString(
                            R.string.cpp_missing_permission_message,
                            getString(R.string.cpp_permission_overlay)
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = onRequestOverlay) {
                        Text(text = getString(android.R.string.ok))
                    }
                }
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPostNotificationsPermission) {
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (shouldShowRationale) {
                AlertDialog(
                    onDismissRequest = onFinish,
                    title = { Text(text = getString(R.string.cpp_missing_permission_title)) },
                    text = {
                        Text(
                            text = getString(
                                R.string.cpp_missing_permission_message,
                                getString(R.string.cpp_permission_post_notifications)
                            )
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = onRequestNotifications) {
                            Text(text = getString(android.R.string.ok))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = onContinueWithoutNotifications) {
                            Text(text = getString(R.string.cpp_continue_without_permissions))
                        }
                    }
                )
            } else {
                LaunchedEffect(Unit) { onRequestNotifications() }
            }
        }
    }

    companion object {
        private fun launchFloatingCalculator(activity: Activity) {
            FloatingCalculatorService.show(activity)
            activity.finish()
        }
    }
}
