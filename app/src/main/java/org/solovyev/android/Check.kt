/*
 * Copyright 2014 serso aka se.solovyev
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

package org.solovyev.android

import android.os.Looper

object Check {

    private val junit: Boolean = isJunit()

    private fun isJunit(): Boolean {
        val stackTrace = Thread.currentThread().stackTrace
        for (element in stackTrace) {
            val className = element.className
            if (className.startsWith("org.junit.") || className.startsWith("org.robolectric.")) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun isMainThread() {
        if (!junit && Looper.getMainLooper() != Looper.myLooper()) {
            throw AssertionException("Should be called on the main thread")
        }
    }

    @JvmStatic
    fun isNotMainThread() {
        if (!junit && Looper.getMainLooper() == Looper.myLooper()) {
            throw AssertionException("Should not be called on the main thread")
        }
    }

    @JvmStatic
    fun isNotNull(o: Any?) {
        isNotNull(o, "Object should not be null")
    }

    @JvmStatic
    fun isNotNull(o: Any?, message: String) {
        if (o == null) {
            throw AssertionException(message)
        }
    }

    @JvmStatic
    fun notEquals(expected: Int, actual: Int) {
        if (expected == actual) {
            throw AssertionException("Should not be equal")
        }
    }

    @JvmStatic
    fun equals(expected: Int, actual: Int) {
        if (expected != actual) {
            throw AssertionException("Should be equal")
        }
    }

    @JvmStatic
    fun equals(expected: Any?, actual: Any?) {
        equals(expected, actual, "Should be equal")
    }

    @JvmStatic
    fun equals(expected: Any?, actual: Any?, message: String) {
        if (expected === actual) {
            // both nulls or same
            return
        }

        if (expected != null && actual != null && expected == actual) {
            // equals
            return
        }

        throw AssertionException(message)
    }

    @JvmStatic
    fun isTrue(expression: Boolean) {
        if (!expression) {
            throw AssertionException("")
        }
    }

    @JvmStatic
    fun isTrue(expression: Boolean, message: String) {
        if (!expression) {
            throw AssertionException(message)
        }
    }

    @JvmStatic
    fun isFalse(expression: Boolean, message: String) {
        if (expression) {
            throw AssertionException(message)
        }
    }

    @JvmStatic
    fun isNull(o: Any?) {
        isNull(o, "Object should be null")
    }

    @JvmStatic
    fun isNull(o: Any?, message: String) {
        if (o != null) {
            throw AssertionException(message)
        }
    }

    @JvmStatic
    fun isNotEmpty(s: String?) {
        if (s == null || s.isEmpty()) {
            throw AssertionException("String should not be empty")
        }
    }

    @JvmStatic
    fun isNotEmpty(array: Array<String>?) {
        if (array == null || array.isEmpty()) {
            throw AssertionException("Array should not be empty")
        }
    }

    @JvmStatic
    fun isNotEmpty(c: Collection<*>?) {
        if (c == null || c.isEmpty()) {
            throw AssertionException("Collection should not be empty")
        }
    }

    @JvmStatic
    fun isNotEmpty(c: Map<*, *>?) {
        if (c == null || c.isEmpty()) {
            throw AssertionException("Map should not be empty")
        }
    }

    @JvmStatic
    fun same(expected: Any?, actual: Any?) {
        if (expected !== actual) {
            throw AssertionException("Objects should be the same")
        }
    }

    @JvmStatic
    fun isEmpty(c: Collection<*>?) {
        if (c != null && !c.isEmpty()) {
            throw AssertionException("Collection should be empty")
        }
    }

    @JvmStatic
    fun shouldNotHappen(): Nothing {
        throw AssertionException("Should not happen")
    }

    private class AssertionException(message: String) : RuntimeException(message)
}
