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

package org.solovyev.android.calculator.about

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.FragmentTab
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.view.Tabs

@AndroidEntryPoint
open class AboutActivity : BaseActivity(R.string.cpp_about) {

    @AndroidEntryPoint
    class Dialog : AboutActivity()

    override fun populateTabs(tabs: Tabs) {
        super.populateTabs(tabs)
        tabs.addTab(FragmentTab.about)
        tabs.addTab(FragmentTab.release_notes)
    }

    companion object {
        @JvmStatic
        fun getClass(context: Context): Class<out AboutActivity> {
            return if (App.isTablet(context)) Dialog::class.java else AboutActivity::class.java
        }
    }
}
