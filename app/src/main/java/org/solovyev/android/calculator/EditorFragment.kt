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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.databinding.CppAppEditorBinding
import javax.inject.Inject

@AndroidEntryPoint
class EditorFragment : BaseFragment(R.layout.cpp_app_editor) {

    @Inject
    lateinit var editor: Editor

    private var _binding: CppAppEditorBinding? = null
    private val binding get() = _binding!!

    private lateinit var editorView: EditorView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        _binding = CppAppEditorBinding.bind(view!!)
        editorView = binding.calculatorEditor
        editor.setView(editorView)
        return view
    }

    override fun onResume() {
        super.onResume()
        editorView.requestFocus()
    }

    override fun onDestroyView() {
        editor.clearView(editorView)
        _binding = null
        super.onDestroyView()
    }
}
