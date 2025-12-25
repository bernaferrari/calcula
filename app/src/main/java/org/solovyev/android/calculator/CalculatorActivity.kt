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

import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.GravityCompat
import dagger.hilt.android.AndroidEntryPoint
import jscl.AngleUnit
import jscl.NumeralBase
import org.solovyev.android.calculator.converter.ConverterFragment
import org.solovyev.android.calculator.databinding.ActivityMainBinding
import org.solovyev.android.calculator.history.History
import org.solovyev.android.calculator.keyboard.PartialKeyboardUi
import org.solovyev.android.widget.menu.CustomPopupMenu
import javax.inject.Inject

@AndroidEntryPoint
class CalculatorActivity : BaseActivity(R.layout.activity_main, R.string.cpp_app_name), View.OnClickListener {

    @Inject
    lateinit var keyboard: Keyboard

    @Inject
    lateinit var partialKeyboardUi: PartialKeyboardUi

    @Inject
    lateinit var history: History

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var startupHelper: StartupHelper

    private val mainMenu = MainMenu()
    private var partialKeyboard: View? = null
    private lateinit var editorContainer: FrameLayout
    private lateinit var mainMenuButton: View
    private var useBackAsPrevious = false

    override fun bindViews(contentView: View) {
        val binding = ActivityMainBinding.bind(contentView.findViewById(R.id.main))
        partialKeyboard = binding.partialKeyboard
        editorContainer = binding.editorContainer.editor
        mainMenuButton = binding.editorContainer.mainMenu
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            val fm = supportFragmentManager
            val t = fm.beginTransaction()
            t.add(R.id.editor, EditorFragment(), "editor")
            t.add(R.id.display, DisplayFragment(), "display")
            t.add(R.id.keyboard, KeyboardFragment(), "keyboard")
            t.commit()
        }

        partialKeyboard?.let {
            partialKeyboardUi.onCreateView(this, it)
        }

        mainMenuButton.setOnClickListener(this)

        useBackAsPrevious = Preferences.Gui.useBackAsPrevious.getPreference(preferences) ?: false
        if (savedInstanceState == null) {
            startupHelper.onMainActivityOpened(this)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && event?.repeatCount == 0 && useBackAsPrevious) {
            history.undo()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        launcher.setActivity(this)
        restartIfModeChanged()
    }

    override fun onPause() {
        launcher.clearActivity(this)
        super.onPause()
    }

    override fun onDestroy() {
        if (partialKeyboard != null) {
            partialKeyboardUi.onDestroyView()
        }
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(preferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(preferences, key)
        key?.let {
            if (Preferences.Gui.useBackAsPrevious.isSameKey(it)) {
                useBackAsPrevious = Preferences.Gui.useBackAsPrevious.getPreference(this.preferences) ?: false
            }
        }
        mainMenu.onSharedPreferenceChanged(key)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.main_menu) {
            mainMenu.toggle()
        }
    }

    override fun toggleMenu(): Boolean {
        if (!super.toggleMenu()) {
            mainMenu.toggle()
        }
        return true
    }

    inner class MainMenu : PopupMenu.OnMenuItemClickListener {
        private var popup: CustomPopupMenu? = null

        fun toggle() {
            if (popup == null) {
                popup = CustomPopupMenu(
                    this@CalculatorActivity,
                    mainMenuButton,
                    GravityCompat.END,
                    android.R.attr.actionOverflowMenuStyle,
                    0
                ).apply {
                    inflate(R.menu.main)
                    setOnMenuItemClickListener(this@MainMenu)
                    setKeepOnSubMenu(true)
                    setForceShowIcon(true)
                }
            }

            popup?.let {
                if (it.isShowing()) {
                    it.dismiss()
                } else {
                    updateMode()
                    updateAngleUnits()
                    updateNumeralBase()
                    it.show()
                }
            }
        }

        private fun updateMode() {
            popup?.menu?.let { menu ->
                val menuItem = menu.findItem(R.id.menu_mode)
                menuItem.title = makeTitle(R.string.cpp_mode, activityMode.nameRes)
            }
        }

        private fun makeTitle(@StringRes prefix: Int, @StringRes suffix: Int): CharSequence {
            val p = getString(prefix)
            val s = getString(suffix)
            val title = SpannableString("$p: $s")
            title.setSpan(StyleSpan(Typeface.BOLD), 0, p.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
            return title
        }

        private fun updateAngleUnits() {
            popup?.menu?.let { menu ->
                val menuItem = menu.findItem(R.id.menu_angle_units)
                val angles = Engine.Preferences.angleUnit.getPreference(preferences) ?: AngleUnit.deg
                menuItem.title = makeTitle(R.string.cpp_angles, Engine.Preferences.angleUnitName(angles))
            }
        }

        private fun updateNumeralBase() {
            popup?.menu?.let { menu ->
                val menuItem = menu.findItem(R.id.menu_numeral_base)
                val numeralBase = Engine.Preferences.numeralBase.getPreference(preferences) ?: NumeralBase.dec
                menuItem.title = makeTitle(R.string.cpp_radix, Engine.Preferences.numeralBaseName(numeralBase))
            }
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.menu_settings -> {
                    launcher.showSettings()
                    true
                }
                R.id.menu_history -> {
                    launcher.showHistory()
                    true
                }
                R.id.menu_plotter -> {
                    launcher.showPlotter()
                    true
                }
                R.id.menu_conversion_tool -> {
                    ConverterFragment.show(this@CalculatorActivity)
                    true
                }
                R.id.menu_about -> {
                    launcher.showAbout()
                    true
                }
                R.id.menu_mode_engineer -> {
                    Preferences.Gui.mode.putPreference(preferences, Preferences.Gui.Mode.engineer)
                    restartIfModeChanged()
                    true
                }
                R.id.menu_mode_simple -> {
                    Preferences.Gui.mode.putPreference(preferences, Preferences.Gui.Mode.simple)
                    restartIfModeChanged()
                    true
                }
                R.id.menu_au_deg -> {
                    Engine.Preferences.angleUnit.putPreference(preferences, AngleUnit.deg)
                    true
                }
                R.id.menu_au_rad -> {
                    Engine.Preferences.angleUnit.putPreference(preferences, AngleUnit.rad)
                    true
                }
                R.id.menu_nb_bin -> {
                    Engine.Preferences.numeralBase.putPreference(preferences, NumeralBase.bin)
                    true
                }
                R.id.menu_nb_dec -> {
                    Engine.Preferences.numeralBase.putPreference(preferences, NumeralBase.dec)
                    true
                }
                R.id.menu_nb_hex -> {
                    Engine.Preferences.numeralBase.putPreference(preferences, NumeralBase.hex)
                    true
                }
                else -> false
            }
        }

        fun onSharedPreferenceChanged(key: String?) {
            key?.let {
                when {
                    Preferences.Gui.mode.isSameKey(it) -> updateMode()
                    Engine.Preferences.angleUnit.isSameKey(it) -> updateAngleUnits()
                    Engine.Preferences.numeralBase.isSameKey(it) -> updateNumeralBase()
                }
            }
        }
    }
}
