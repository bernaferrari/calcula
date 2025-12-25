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

package org.solovyev.android.calculator.wizard

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.Spinner
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseActivity
import org.solovyev.android.calculator.Preferences
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.keyboard.BaseKeyboardUi

@AndroidEntryPoint
open class ChooseThemeWizardStep : WizardFragment(), AdapterView.OnItemSelectedListener {

    private val themes = mutableListOf<ThemeUi>()
    private lateinit var preview: FrameLayout
    private lateinit var adapter: WizardArrayAdapter<ThemeUi>

    override fun getViewResId(): Int = R.layout.cpp_wizard_step_choose_theme

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        val theme = Preferences.Gui.getTheme(preferences)
        val spinner = root.findViewById<Spinner>(R.id.wizard_theme_spinner)

        themes.clear()
        themes.add(ThemeUi(Preferences.Gui.Theme.material_theme))
        themes.add(ThemeUi(Preferences.Gui.Theme.material_black_theme))
        themes.add(ThemeUi(Preferences.Gui.Theme.material_light_theme))
        themes.add(ThemeUi(Preferences.Gui.Theme.metro_blue_theme))
        themes.add(ThemeUi(Preferences.Gui.Theme.metro_green_theme))
        themes.add(ThemeUi(Preferences.Gui.Theme.metro_purple_theme))

        adapter = WizardArrayAdapter(requireActivity(), themes)
        spinner.adapter = adapter
        spinner.setSelection(findPosition(theme))
        spinner.onItemSelectedListener = this

        preview = root.findViewById(R.id.wizard_theme_preview)
        updatePreview(theme)
    }

    private fun findPosition(theme: Preferences.Gui.Theme): Int {
        return themes.indexOfFirst { it.theme == theme }.takeIf { it >= 0 } ?: 0
    }

    private fun updatePreview(theme: Preferences.Gui.Theme) {
        preview.removeAllViews()
        val context = ContextThemeWrapper(requireActivity(), theme.theme)
        LayoutInflater.from(context).inflate(R.layout.cpp_wizard_step_choose_theme_preview, preview)

        App.processViews(preview, object : App.ViewProcessor<View> {
            override fun process(view: View) {
                BaseKeyboardUi.adjustButton(view)
                BaseActivity.setFont(view, typeface)
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val theme = adapter.getItem(position) ?: return
        Preferences.Gui.theme.putPreference(preferences, theme.theme)
        updatePreview(theme.theme)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // No action needed
    }

    private inner class ThemeUi(val theme: Preferences.Gui.Theme) {
        val name: String = theme.getName(requireActivity())

        override fun toString(): String = name
    }
}
