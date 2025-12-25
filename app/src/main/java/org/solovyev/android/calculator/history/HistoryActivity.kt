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

package org.solovyev.android.calculator.history

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.FragmentTab
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.view.Tabs
import javax.inject.Inject

@AndroidEntryPoint
open class HistoryActivity : BaseActivity(R.string.c_history) {

    @Inject
    lateinit var history: History

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        withFab(R.drawable.ic_delete_white_36dp) { v ->
            val fragment = tabs.currentFragment
            showClearHistoryDialog(fragment is RecentHistoryFragment)
        }
    }

    override fun populateTabs(tabs: Tabs) {
        super.populateTabs(tabs)
        tabs.addTab(FragmentTab.history)
        tabs.addTab(FragmentTab.saved_history)
    }

    private fun showClearHistoryDialog(recentHistory: Boolean) {
        AlertDialog.Builder(this, App.getTheme().alertDialogTheme)
            .setTitle(R.string.cpp_clear_history_title)
            .setMessage(R.string.cpp_clear_history_message)
            .setPositiveButton(R.string.cpp_clear_history) { dialog, which ->
                if (recentHistory) {
                    history.clearRecent()
                } else {
                    history.clearSaved()
                }
            }
            .setNegativeButton(R.string.cpp_cancel, null)
            .create()
            .show()
    }

    class Dialog : HistoryActivity()

    companion object {
        fun getClass(context: Context): Class<out HistoryActivity> {
            return if (App.isTablet(context)) Dialog::class.java else HistoryActivity::class.java
        }
    }
}
