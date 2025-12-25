package org.solovyev.android.calculator.memory

import android.text.TextUtils
import android.util.Log
import com.squareup.otto.Bus
import dagger.Lazy
import jscl.math.Expression
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.text.ParseException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.AppModule
import org.solovyev.android.calculator.Notifier
import org.solovyev.android.calculator.ToJsclTextProcessor
import org.solovyev.android.io.FileSystem
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Modern Kotlin version of Memory using StateFlow and Coroutines
 * This is an enhanced version with modern patterns - the original Memory.kt should be replaced with this
 */
@Singleton
class MemoryModern @Inject constructor(
    @Named(AppModule.THREAD_INIT) private val initDispatcher: CoroutineDispatcher,
    private val fileSystem: FileSystem,
    @Named(AppModule.DIR_FILES) private val filesDir: Lazy<File>,
    private val notifier: Notifier,
    private val jsclProcessor: ToJsclTextProcessor,
    private val bus: Bus
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // StateFlow for reactive memory value
    private val _valueState = MutableStateFlow(EMPTY)
    val valueState: StateFlow<Generic> = _valueState.asStateFlow()

    // StateFlow for loaded state
    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    // SharedFlow for value ready events
    private val _valueReadyEvents = MutableSharedFlow<String>(replay = 0)
    val valueReadyEvents: SharedFlow<String> = _valueReadyEvents.asSharedFlow()

    // Debounced write flow
    private val writeChannel = MutableSharedFlow<Generic>(
        replay = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    init {
        // Initialize asynchronously
        scope.launch(initDispatcher) {
            val loadedValue = loadValue()
            withContext(Dispatchers.Main.immediate) {
                _valueState.value = loadedValue
                _isLoaded.value = true
            }
        }

        // Setup debounced write
        scope.launch {
            writeChannel
                .debounce(3000L)
                .collect { value ->
                    writeValue(value)
                }
        }
    }

    private suspend fun loadValue(): Generic = withContext(Dispatchers.IO) {
        try {
            val value = fileSystem.read(getFile())
            if (TextUtils.isEmpty(value)) {
                EMPTY
            } else {
                numeric(Expression.valueOf(value.toString()))
            }
        } catch (e: IOException) {
            Log.e(App.TAG, e.message, e)
            EMPTY
        } catch (e: ParseException) {
            Log.e(App.TAG, e.message, e)
            EMPTY
        }
    }

    fun add(that: Generic) {
        scope.launch {
            if (!_isLoaded.first { it }) return@launch

            try {
                val newValue = _valueState.value.add(that)
                setValue(newValue)
            } catch (e: RuntimeException) {
                notifier.showMessage(e)
            }
        }
    }

    fun subtract(that: Generic) {
        scope.launch {
            if (!_isLoaded.first { it }) return@launch

            try {
                val newValue = _valueState.value.subtract(that)
                setValue(newValue)
            } catch (e: RuntimeException) {
                notifier.showMessage(e)
            }
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

    fun clear() {
        scope.launch {
            if (!_isLoaded.first { it }) return@launch
            setValue(EMPTY)
        }
    }

    private fun getFile(): File = File(filesDir.get(), "memory.txt")

    fun requestValue() {
        scope.launch {
            if (!_isLoaded.first { it }) return@launch

            val value = getValue()
            // Emit to both SharedFlow and Bus for backward compatibility
            _valueReadyEvents.emit(value)
            bus.post(ValueReadyEvent(value))
        }
    }

    fun requestShow() {
        scope.launch {
            if (!_isLoaded.first { it }) return@launch
            show()
        }
    }

    private suspend fun writeValue(value: Generic) = withContext(Dispatchers.IO) {
        val stringValue = try {
            value.toString()
        } catch (e: RuntimeException) {
            Log.w(App.TAG, e.message, e)
            return@withContext
        }

        val preparedExpression = prepareExpression(stringValue)
        fileSystem.writeSilently(getFile(), preparedExpression)
    }

    private fun prepareExpression(value: String): String {
        return try {
            jsclProcessor.process(value).value
        } catch (ignored: org.solovyev.android.calculator.ParseException) {
            value
        }
    }

    fun cleanup() {
        scope.cancel()
    }

    data class ValueReadyEvent(val value: String)

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
