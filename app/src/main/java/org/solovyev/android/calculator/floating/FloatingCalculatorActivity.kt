/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.floating

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.R

@AndroidEntryPoint
class FloatingCalculatorActivity : AppCompatActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        launchFloatingCalculator(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hasOverlayPermission = FloatingCalculatorView.isOverlayPermissionGranted(this)
        val hasPostNotificationsPermission = isPostNotificationsPermissionGranted()
        if (hasOverlayPermission && hasPostNotificationsPermission) {
            launchFloatingCalculator(this)
            return
        }

        Check.isTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        if (savedInstanceState == null) {
            when {
                !hasOverlayPermission -> {
                    App.showDialog(
                        OverlayPermissionDialog(),
                        "no-overlay-permission-dialog",
                        supportFragmentManager
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    ) {
                        App.showDialog(
                            PostNotificationsPermissionDialog(),
                            "post-notifications-permission-dialog",
                            supportFragmentManager
                        )
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                else -> {
                    Check.shouldNotHappen()
                    finish()
                }
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

    @TargetApi(Build.VERSION_CODES.M)
    class OverlayPermissionDialog : BaseDialogFragment() {

        override fun onPrepareDialog(builder: AlertDialog.Builder) {
            val permission = getString(R.string.cpp_permission_overlay)
            builder.setMessage(getString(R.string.cpp_missing_permission_message, permission))
            builder.setTitle(R.string.cpp_missing_permission_title)
            builder.setPositiveButton(android.R.string.ok, null)
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    val activity = requireActivity()
                    App.showSystemPermissionSettings(
                        activity,
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    )
                    dismiss()
                }
                else -> super.onClick(dialog, which)
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            activity?.finish()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    class PostNotificationsPermissionDialog : BaseDialogFragment() {

        override fun onPrepareDialog(builder: AlertDialog.Builder) {
            val permission = getString(R.string.cpp_permission_post_notifications)
            builder.setMessage(getString(R.string.cpp_missing_permission_message, permission))
            builder.setTitle(R.string.cpp_missing_permission_title)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setNegativeButton(R.string.cpp_continue_without_permissions, null)
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            val activity = requireActivity()
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    App.showSystemPermissionSettings(
                        activity,
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    )
                    dismiss()
                }
                else -> {
                    launchFloatingCalculator(activity)
                    super.onClick(dialog, which)
                }
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            activity?.finish()
        }
    }

    companion object {
        private fun launchFloatingCalculator(activity: Activity) {
            FloatingCalculatorService.show(activity)
            activity.finish()
        }
    }
}
