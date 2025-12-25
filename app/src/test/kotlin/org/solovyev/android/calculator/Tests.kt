package org.solovyev.android.calculator

import android.content.SharedPreferences
import android.os.Handler
import dagger.Lazy
import jscl.JsclMathEngine
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.solovyev.android.calculator.entities.BaseEntitiesRegistry
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.operators.OperatorsRegistry
import org.solovyev.android.calculator.operators.PostfixFunctionsRegistry
import org.solovyev.android.io.FileSystem
import java.io.File
import java.util.concurrent.Executor

object Tests {

    fun sameThreadExecutor(): Executor {
        return Executor { command -> command.run() }
    }

    fun makeEngine(): Engine {
        val mathEngine = JsclMathEngine.getInstance()
        mathEngine.groupingSeparator = ' '
        val engine = Engine(mathEngine)
        engine.postfixFunctionsRegistry = init(PostfixFunctionsRegistry(mathEngine))
        engine.functionsRegistry = init(FunctionsRegistry(mathEngine))
        engine.variablesRegistry = init(VariablesRegistry(mathEngine))
        engine.operatorsRegistry = init(OperatorsRegistry(mathEngine))
        engine.errorReporter = object : ErrorReporter {
            override fun onException(e: Throwable) {
                throw AssertionError(e)
            }

            override fun onError(message: String) {
                throw AssertionError(message)
            }
        }
        engine.initAsync()
        return engine
    }

    private fun <T : BaseEntitiesRegistry<*>> init(registry: T): T {
        registry.preferences = Mockito.mock(SharedPreferences::class.java)
        registry.filesDir = Lazy<File> {
            RuntimeEnvironment.application.filesDir
        }
        registry.fileSystem = Mockito.mock(FileSystem::class.java)
        registry.handler = Handler()
        return registry
    }
}
