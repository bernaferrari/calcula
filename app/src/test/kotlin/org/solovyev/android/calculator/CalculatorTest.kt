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

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.solovyev.android.calculator.calculations.CalculationFinishedEvent

@RunWith(RobolectricTestRunner::class)
class CalculatorTest : BaseCalculatorTest() {

    @Before
    override fun setUp() {
        super.setUp()
        doAnswer(object : Answer<Any?> {
            override fun answer(invocationOnMock: InvocationOnMock): Any? {
                val args = invocationOnMock.arguments
                val e = args[0] as CalculationFinishedEvent
                calculator.updateAnsVariable(e.stringResult)
                return null
            }
        }).`when`(bus).post(anyFinishedEvent())
    }

    @Test
    fun testAnsVariable() {
        assertEval("2", "2")
        assertEval("2", "2")
        assertEval("2", "ans")
        assertEval("4", "ans^2")
        assertEval("16", "ans^2")
        assertEval("0", "0")
        assertEval("0", "ans")
        assertEval("3", "3")
        assertEval("9", "ans*ans")
        assertError("ans*an")
        assertEval("81", "ans*ans")
    }

}
