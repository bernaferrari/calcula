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

package org.solovyev.android.calculator.errors

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.UiPreferences
import org.solovyev.common.msg.Message
import javax.inject.Inject

@AndroidEntryPoint
class FixableErrorsActivity : AppCompatActivity() {

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var uiPreferences: UiPreferences

    private var errors: ArrayList<FixableError>? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)

        errors = state?.getParcelableArrayList(STATE_ERRORS)
            ?: intent?.getParcelableArrayListExtra(EXTRA_ERRORS)

        if (errors == null) {
            finish()
            return
        }

        if (state == null) {
            showNextError()
        }
    }

    override fun onSaveInstanceState(out: Bundle) {
        super.onSaveInstanceState(out)
        out.putParcelableArrayList(STATE_ERRORS, errors)
    }

    fun showNextError() {
        val errors = this.errors
        if (errors == null || errors.isEmpty()) {
            finish()
            return
        }

        if (!uiPreferences.showFixableErrorDialog) {
            finish()
            return
        }

        val fixableError = errors.removeAt(0)
        FixableErrorFragment.show(fixableError, supportFragmentManager)
    }

    fun onDialogClosed() {
        val fragment = supportFragmentManager.findFragmentByTag(FixableErrorFragment.FRAGMENT_TAG)
        if (fragment == null) {
            // activity is closing
            return
        }
        showNextError()
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
