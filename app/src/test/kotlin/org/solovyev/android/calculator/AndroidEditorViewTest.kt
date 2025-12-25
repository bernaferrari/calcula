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
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.solovyev.common.text.Strings
import java.util.Date
import java.util.Random
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(RobolectricTestRunner::class)
class AndroidEditorViewTest {

    @Test
    fun testCreation() {
        EditorView(RuntimeEnvironment.application)
    }

    @Test
    fun testAsyncWork() {
        val threadNum = 10
        val count = 10
        val maxTextLength = 100

        val editor = Editor(RuntimeEnvironment.getApplication(), mock(SharedPreferences::class.java), Tests.makeEngine())
        val random = Random(Date().time)
        val startLatchLatch = CountDownLatch(threadNum)
        val finishLatch = CountDownLatch(threadNum * count)
        val error = AtomicBoolean(false)

        for (i in 0 until threadNum) {
            Thread {
                try {
                    startLatchLatch.await()
                } catch (e: InterruptedException) {
                    println(e)
                    error.set(true)
                    for (j in 0 until count) {
                        finishLatch.countDown()
                    }
                    return@Thread
                }

                for (j in 0 until count) {
                    try {
                        val textLength = random.nextInt(maxTextLength)
                        editor.insert(Strings.generateRandomString(textLength), textLength)
                    } catch (e: Throwable) {
                        println(e)
                        error.set(true)
                    } finally {
                        finishLatch.countDown()
                    }
                }
            }.start()
            startLatchLatch.countDown()
        }

        if (finishLatch.await(60, TimeUnit.SECONDS)) {
            Assert.assertFalse(error.get())
        } else {
            Assert.fail("Too long execution!")
        }
    }
}
