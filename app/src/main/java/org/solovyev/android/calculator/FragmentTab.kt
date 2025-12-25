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

package org.solovyev.android.calculator

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import org.solovyev.android.calculator.about.AboutFragment
import org.solovyev.android.calculator.about.ReleaseNotesFragment
import org.solovyev.android.calculator.functions.FunctionsFragment
import org.solovyev.android.calculator.history.RecentHistoryFragment
import org.solovyev.android.calculator.history.SavedHistoryFragment
import org.solovyev.android.calculator.operators.OperatorsFragment
import org.solovyev.android.calculator.variables.VariablesFragment
import kotlin.reflect.KClass

enum class FragmentTab(
    val type: Class<out Fragment>,
    @StringRes val title: Int
) {
    history(RecentHistoryFragment::class.java, R.string.cpp_history_tab_recent),
    saved_history(SavedHistoryFragment::class.java, R.string.cpp_history_tab_saved),
    variables(VariablesFragment::class.java, R.string.cpp_vars_and_constants),
    functions(FunctionsFragment::class.java, R.string.c_functions),
    operators(OperatorsFragment::class.java, R.string.c_operators),
    about(AboutFragment::class.java, R.string.cpp_about),
    release_notes(ReleaseNotesFragment::class.java, R.string.c_release_notes);

    val tag: String = name
}
