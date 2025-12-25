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
import android.view.View
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.keyboard.BaseKeyboardUi
import org.solovyev.android.views.Adjuster
import org.solovyev.android.views.dragbutton.DirectionDragButton
import org.solovyev.android.views.dragbutton.DirectionDragListener
import org.solovyev.android.views.dragbutton.DragDirection
import org.solovyev.android.views.dragbutton.DragEvent

@AndroidEntryPoint
class DragButtonWizardStep : WizardFragment() {

    private var actionTextView: TextView? = null
    private var action = DragButtonAction.center

    override fun getViewResId(): Int = R.layout.cpp_wizard_step_drag_button

    override fun onViewCreated(root: View, savedInstanceState: Bundle?) {
        super.onViewCreated(root, savedInstanceState)

        val dragButton = root.findViewById<DirectionDragButton>(R.id.wizard_dragbutton)
        dragButton.setOnClickListener(this)
        dragButton.setOnDragListener(object : DirectionDragListener(requireActivity()) {
            override fun onDrag(view: View, event: DragEvent, direction: DragDirection): Boolean {
                if (action.dragDirection == direction) {
                    setNextAction()
                    return true
                }
                return false
            }
        })
        Adjuster.adjustText(dragButton, BaseKeyboardUi.getTextScale(requireActivity()))
        actionTextView = root.findViewById(R.id.wizard_dragbutton_action_textview)

        savedInstanceState?.let {
            setAction(it.getSerializable(ACTION) as? DragButtonAction ?: DragButtonAction.center)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ACTION, action)
    }

    private fun setNextAction() {
        setAction(action.getNextAction())
    }

    private fun setAction(action: DragButtonAction) {
        if (this.action != action) {
            this.action = action
            actionTextView?.setText(this.action.actionTextResId)
        }
    }

    private enum class DragButtonAction(
        val actionTextResId: Int,
        val dragDirection: DragDirection?
    ) {
        center(R.string.cpp_wizard_dragbutton_action_center, null),
        up(R.string.cpp_wizard_dragbutton_action_up, DragDirection.up),
        down(R.string.cpp_wizard_dragbutton_action_down, DragDirection.down),
        end(R.string.cpp_wizard_dragbutton_action_end, null);

        fun getNextAction(): DragButtonAction {
            val values = values()
            val position = values.indexOf(this)
            return if (position < values.size - 1) {
                values[position + 1]
            } else {
                values[0]
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.wizard_dragbutton -> {
                if (action == DragButtonAction.center || action == DragButtonAction.end) {
                    setNextAction()
                }
                return
            }
        }
        super.onClick(v)
    }

    companion object {
        private const val ACTION = "action"
    }
}
