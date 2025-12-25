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

package org.solovyev.android.wizard

class ListWizardFlow(private val steps: List<WizardStep>) : WizardFlow {

    override fun getStepByName(name: String): WizardStep? =
        steps.find { it.name == name }

    override fun getNextStep(step: WizardStep): WizardStep? {
        val index = steps.indexOf(step)
        return if (index >= 0 && index + 1 < steps.size) {
            steps[index + 1]
        } else {
            null
        }
    }

    override fun getPrevStep(step: WizardStep): WizardStep? {
        val index = steps.indexOf(step)
        return if (index >= 1) {
            steps[index - 1]
        } else {
            null
        }
    }

    override val firstStep: WizardStep
        get() = steps[0]

    fun getStepAt(position: Int): WizardStep = steps[position]

    fun getPositionFor(step: WizardStep): Int =
        steps.indexOfFirst { it == step }

    val count: Int
        get() = steps.size
}
