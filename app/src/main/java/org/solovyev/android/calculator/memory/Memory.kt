package org.solovyev.android.calculator.memory

import android.text.TextUtils
import android.util.Log
import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.text.ParseException
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Notifier
import org.solovyev.android.calculator.ToJsclTextProcessor
import org.solovyev.android.calculator.di.AppDirectories
import org.solovyev.android.calculator.di.AppDispatchers
import org.solovyev.android.io.FileSystem
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(FlowPreview::class)
class Memory @Inject constructor(
    private val fileSystem: FileSystem,
    private val directories: AppDirectories,
    private val dispatchers: AppDispatchers,
    private val notifier: Notifier,
    private val jsclProcessor: ToJsclTextProcessor
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _valueState = MutableStateFlow(EMPTY)
    val valueState: StateFlow<Generic> = _valueState.asStateFlow()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    private val _valueReadyEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val valueReadyEvents: SharedFlow<String> = _valueReadyEvents.asSharedFlow()

    private val writeChannel = MutableSharedFlow<Generic>(replay = 1)

    init {
        scope.launch(dispatchers.io) {
            val loadedValue = loadValue()
            withContext(dispatchers.main) {
                _valueState.value = loadedValue
                _isLoaded.value = true
            }
        }

        scope.launch {
            writeChannel
                .debounce(3000L)
                .collect { value ->
                    writeValue(value)
                }
        }
    }

    fun add(that: Generic) {
        scope.launch {
            awaitLoaded()
            try {
                setValue(_valueState.value.add(that))
            } catch (e: RuntimeException) {
                notifier.showMessage(e)
            }
        }
    }

    fun subtract(that: Generic) {
        scope.launch {
            awaitLoaded()
            try {
                setValue(_valueState.value.subtract(that))
            } catch (e: RuntimeException) {
                notifier.showMessage(e)
            }
        }
    }

    fun clear() {
        scope.launch {
            awaitLoaded()
            setValue(EMPTY)
        }
    }

    fun requestValue() {
        scope.launch {
            awaitLoaded()
            _valueReadyEvents.emit(getValue())
        }
    }

    fun requestShow() {
        scope.launch {
            awaitLoaded()
            show()
        }
    }

    private suspend fun awaitLoaded() {
        if (!_isLoaded.value) {
            _isLoaded.first { it }
        }
    }

    private suspend fun loadValue(): Generic = withContext(dispatchers.io) {
        Check.isNotMainThread()
        try {
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

    private fun getValue(): String {
        return try {
            _valueState.value.toString()
        } catch (e: RuntimeException) {
            Log.w(App.TAG, e.message, e)
            ""
        }
    }

    private suspend fun setValue(newValue: Generic) {
        val numericValue = numeric(newValue)
        _valueState.value = numericValue
        writeChannel.emit(numericValue)
        show()
    }

    private fun show() {
        notifier.showMessage(getValue())
    }

    private fun getFile() = directories.getFile("memory.txt")

    private suspend fun writeValue(value: Generic) {
        val text = try {
            value.toString()
        } catch (e: RuntimeException) {
            Log.w(App.TAG, e.message, e)
            ""
        }
        fileSystem.writeSilently(getFile(), prepareExpression(text))
    }

    private fun prepareExpression(value: String): String {
        return try {
            jsclProcessor.process(value).value
        } catch (ignored: org.solovyev.android.calculator.ParseException) {
            value
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
