package org.solovyev.android.calculator.memory

import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.squareup.otto.Bus
import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.text.ParseException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Notifier
import org.solovyev.android.calculator.ToJsclTextProcessor
import org.solovyev.android.calculator.di.AppCoroutineScope
import org.solovyev.android.calculator.di.AppDirectories
import org.solovyev.android.calculator.di.AppDispatchers
import org.solovyev.android.io.FileSystem
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Memory @Inject constructor(
    private val fileSystem: FileSystem,
    private val directories: AppDirectories,
    private val dispatchers: AppDispatchers,
    private val appScope: AppCoroutineScope,
    private val handler: Handler
) {
    @Inject
    lateinit var notifier: Notifier

    @Inject
    lateinit var jsclProcessor: ToJsclTextProcessor

    @Inject
    lateinit var bus: Bus

    private var value: Generic = EMPTY
    private var loaded = false
    private val whenLoadedRunnables = mutableListOf<() -> Unit>()
    private val writeTask = WriteTask()

    init {
        // Initialize asynchronously using coroutines
        appScope.launchIO {
            initAsync()
        }
    }

    private suspend fun initAsync() {
        Check.isNotMainThread()
        val value = loadValue()
        withContext(dispatchers.main) {
            onLoaded(value)
        }
    }

    private fun onLoaded(value: Generic) {
        this.value = value
        this.loaded = true
        whenLoadedRunnables.forEach { it() }
        whenLoadedRunnables.clear()
    }

    private suspend fun loadValue(): Generic {
        Check.isNotMainThread()
        return try {
            val value = fileSystem.read(getFile())
            if (TextUtils.isEmpty(value)) EMPTY else numeric(Expression.valueOf(value.toString()))
        } catch (e: IOException) {
            Log.e(App.TAG, e.message, e)
            EMPTY
        } catch (e: ParseException) {
            Log.e(App.TAG, e.message, e)
            EMPTY
        }
    }

    fun add(that: Generic) {
        Check.isMainThread()
        if (!loaded) {
            postAdd(that)
            return
        }
        try {
            setValue(value.add(that))
        } catch (e: RuntimeException) {
            notifier.showMessage(e)
        }
    }

    private fun postAdd(that: Generic) {
        whenLoadedRunnables.add {
            add(that)
        }
    }

    fun subtract(that: Generic) {
        Check.isMainThread()
        if (!loaded) {
            postSubtract(that)
            return
        }
        try {
            setValue(value.subtract(that))
        } catch (e: RuntimeException) {
            notifier.showMessage(e)
        }
    }

    private fun postSubtract(that: Generic) {
        whenLoadedRunnables.add {
            subtract(that)
        }
    }

    private fun getValue(): String {
        Check.isTrue(loaded)
        return try {
            value.toString()
        } catch (e: RuntimeException) {
            Log.w(App.TAG, e.message, e)
            ""
        }
    }

    private fun setValue(newValue: Generic) {
        Check.isTrue(loaded)
        value = numeric(newValue)
        handler.removeCallbacks(writeTask)
        handler.postDelayed(writeTask, 3000L)
        show()
    }

    private fun show() {
        notifier.showMessage(getValue())
    }

    fun clear() {
        Check.isMainThread()
        if (!loaded) {
            postClear()
            return
        }
        setValue(EMPTY)
    }

    private fun postClear() {
        whenLoadedRunnables.add {
            clear()
        }
    }

    private fun getFile() = directories.getFile("memory.txt")

    fun requestValue() {
        if (!loaded) {
            postValue()
            return
        }
        bus.post(ValueReadyEvent(getValue()))
    }

    private fun postValue() {
        whenLoadedRunnables.add {
            requestValue()
        }
    }

    fun requestShow() {
        if (!loaded) {
            postShow()
            return
        }
        show()
    }

    private fun postShow() {
        whenLoadedRunnables.add {
            requestShow()
        }
    }

    data class ValueReadyEvent(val value: String)

    private inner class WriteTask : Runnable {
        override fun run() {
            Check.isMainThread()
            if (!loaded) {
                return
            }
            val value = getValue()
            appScope.launchIO {
                fileSystem.writeSilently(getFile(), prepareExpression(value))
            }
        }

        private fun prepareExpression(value: String): String {
            return try {
                jsclProcessor.process(value).value
            } catch (ignored: org.solovyev.android.calculator.ParseException) {
                value
            }
        }
    }

    companion object {
        private val EMPTY: Generic = numeric(Expression.valueOf(JsclInteger.ZERO))

        private fun numeric(generic: Generic): Generic {
            return try {
                generic.numeric()
            } catch (e: RuntimeException) {
                generic
            }
        }
    }
}
