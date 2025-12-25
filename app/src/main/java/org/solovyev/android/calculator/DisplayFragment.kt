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

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import dagger.hilt.android.AndroidEntryPoint
import jscl.NumeralBase
import jscl.math.Generic
import jscl.math.NotDoubleException
import org.solovyev.android.calculator.converter.ConverterFragment
import org.solovyev.android.calculator.jscl.JsclOperation
import javax.inject.Inject

@AndroidEntryPoint
class DisplayFragment : BaseFragment(R.layout.cpp_app_display),
    View.OnClickListener,
    MenuItem.OnMenuItemClickListener {

    private enum class ConversionMenuItem(
        val toNumeralBase: NumeralBase,
        @StringRes val title: Int
    ) {
        TO_BIN(NumeralBase.bin, R.string.convert_to_bin),
        TO_DEC(NumeralBase.dec, R.string.convert_to_dec),
        TO_HEX(NumeralBase.hex, R.string.convert_to_hex);

        companion object {
            fun getByTitle(title: Int): ConversionMenuItem? {
                return when (title) {
                    R.string.convert_to_bin -> TO_BIN
                    R.string.convert_to_dec -> TO_DEC
                    R.string.convert_to_hex -> TO_HEX
                    else -> null
                }
            }
        }
    }

    @Inject
    lateinit var preferences: SharedPreferences

    @Inject
    lateinit var errorReporter: ErrorReporter

    @Inject
    lateinit var display: Display

    @Inject
    lateinit var launcher: ActivityLauncher

    @Inject
    lateinit var calculator: Calculator

    @Inject
    lateinit var engine: Engine

    private lateinit var displayView: DisplayView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        displayView = view!!.findViewById(R.id.calculator_display)
        display.setView(displayView)
        displayView.setOnClickListener(this)
        return view
    }

    override fun onDestroyView() {
        display.clearView(displayView)
        super.onDestroyView()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        val state = display.getState()
        if (!state.valid) {
            return
        }
        addMenu(menu, R.string.cpp_copy, this)

        val result = state.result
        val operation = state.operation
        if (result != null) {
            if (operation == JsclOperation.numeric && result.constants.isEmpty()) {
                for (item in ConversionMenuItem.values()) {
                    if (isMenuItemVisible(item, result)) {
                        addMenu(menu, item.title, this)
                    }
                }
                try {
                    result.doubleValue()
                    addMenu(menu, R.string.c_convert, this)
                } catch (ignored: NotDoubleException) {
                }
            }
            if (launcher.canPlot(result)) {
                addMenu(menu, R.string.c_plot, this)
            }
        }
    }

    private fun isMenuItemVisible(
        menuItem: ConversionMenuItem,
        generic: Generic
    ): Boolean {
        val fromNumeralBase = engine.getMathEngine().getNumeralBase()
        if (fromNumeralBase != menuItem.toNumeralBase) {
            return calculator.canConvert(generic, fromNumeralBase, menuItem.toNumeralBase)
        }
        return false
    }

    override fun onClick(v: View) {
        val state = display.getState()
        if (state.valid) {
            v.setOnCreateContextMenuListener(this)
            v.showContextMenu()
            v.setOnCreateContextMenuListener(null)
        } else {
            showEvaluationError(v.context, state.text)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val state = display.getState()
        val result = state.result
        return when (item.itemId) {
            R.string.cpp_copy -> {
                display.copy()
                true
            }
            R.string.convert_to_bin, R.string.convert_to_dec, R.string.convert_to_hex -> {
                val menuItem = ConversionMenuItem.getByTitle(item.itemId) ?: return false
                if (result != null) {
                    calculator.convert(state, menuItem.toNumeralBase)
                }
                true
            }
            R.string.c_convert -> {
                ConverterFragment.show(requireActivity(), getValue(result))
                true
            }
            R.string.c_plot -> {
                launcher.plot(result)
                true
            }
            else -> false
        }
    }

    companion object {
        private fun getValue(result: Generic?): Double {
            if (result == null) {
                return 1.0
            }
            return try {
                result.doubleValue()
            } catch (ignored: NotDoubleException) {
                1.0
            }
        }

        @JvmStatic
        fun showEvaluationError(context: Context, errorMessage: String) {
            AlertDialog.Builder(context, App.getTheme().alertDialogTheme)
                .setPositiveButton(R.string.cpp_cancel, null)
                .setMessage(errorMessage)
                .create()
                .show()
        }
    }
}
