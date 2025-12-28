package org.solovyev.android.calculator

import android.app.Application
import android.os.Handler
import android.os.Looper
import jscl.JsclMathEngine
import kotlinx.coroutines.runBlocking
import org.robolectric.RuntimeEnvironment
import org.solovyev.android.calculator.di.AppCoroutineScope
import org.solovyev.android.calculator.di.AppDirectories
import org.solovyev.android.calculator.di.AppDispatchers
import org.solovyev.android.calculator.di.AppPreferences
import org.solovyev.android.calculator.functions.FunctionsRegistry
import org.solovyev.android.calculator.memory.Memory
import org.solovyev.android.calculator.operators.OperatorsRegistry
import org.solovyev.android.calculator.operators.PostfixFunctionsRegistry
import org.solovyev.android.io.FileSystem
import java.util.concurrent.Executor

object Tests {

    data class CalculatorEnvironment(
        val application: Application,
        val appPreferences: AppPreferences,
        val engine: Engine,
        val calculator: Calculator,
        val editor: Editor,
        val preprocessor: ToJsclTextProcessor
    )

    fun sameThreadExecutor(): Executor = Executor { command -> command.run() }

    fun makeEngine(): Engine {
        val deps = createCoreDependencies()
        val engine = createEngine(deps)
        runBlocking { engine.initAsync() }
        return engine
    }

    fun createCalculatorEnvironment(): CalculatorEnvironment {
        val deps = createCoreDependencies()
        val engine = createEngine(deps)

        val preprocessor = ToJsclTextProcessor().apply {
            this.engine = engine
        }
        val notifier = Notifier().apply {
            application = deps.application
            handler = deps.handler
        }
        val memory = Memory(
            deps.fileSystem,
            deps.directories,
            deps.dispatchers,
            notifier,
            preprocessor
        )
        val editor = Editor(
            deps.application,
            deps.appPreferences,
            engine,
            memory
        )
        val calculator = Calculator(
            deps.appPreferences,
            engine,
            preprocessor,
            editor,
            deps.functionsRegistry,
            deps.variablesRegistry
        ).apply {
            setSynchronous()
        }

        runBlocking { calculator.initAsync() }

        return CalculatorEnvironment(
            application = deps.application,
            appPreferences = deps.appPreferences,
            engine = engine,
            calculator = calculator,
            editor = editor,
            preprocessor = preprocessor
        )
    }

    private fun createEngine(deps: CoreDependencies): Engine {
        val mathEngine = JsclMathEngine.getInstance()
        mathEngine.setGroupingSeparator(' ')
        val engine = Engine(
            mathEngine = mathEngine,
            context = deps.application,
            appPreferences = deps.appPreferences,
            errorReporter = deps.errorReporter,
            functionsRegistry = deps.functionsRegistry,
            variablesRegistry = deps.variablesRegistry,
            operatorsRegistry = deps.operatorsRegistry,
            postfixFunctionsRegistry = deps.postfixFunctionsRegistry
        )
        return engine
    }

    private data class CoreDependencies(
        val application: Application,
        val appPreferences: AppPreferences,
        val errorReporter: ErrorReporter,
        val dispatchers: AppDispatchers,
        val appScope: AppCoroutineScope,
        val directories: AppDirectories,
        val fileSystem: FileSystem,
        val handler: Handler,
        val functionsRegistry: FunctionsRegistry,
        val variablesRegistry: VariablesRegistry,
        val operatorsRegistry: OperatorsRegistry,
        val postfixFunctionsRegistry: PostfixFunctionsRegistry
    )

    private fun createCoreDependencies(): CoreDependencies {
        val application = RuntimeEnvironment.application
        val handler = Handler(Looper.getMainLooper())
        val errorReporter = object : ErrorReporter {
            override fun onException(e: Throwable) {
                throw AssertionError(e)
            }

            override fun onError(message: String) {
                throw AssertionError(message)
            }
        }

        val dispatchers = AppDispatchers()
        val appScope = AppCoroutineScope(dispatchers)
        val directories = AppDirectories(application, dispatchers, appScope)
        val fileSystem = FileSystem().apply {
            this.errorReporter = errorReporter
        }
        val appPreferences = AppPreferences(application)

        val mathEngine = JsclMathEngine.getInstance()
        val functionsRegistry = FunctionsRegistry(mathEngine)
        val variablesRegistry = VariablesRegistry(mathEngine)
        val operatorsRegistry = OperatorsRegistry(mathEngine)
        val postfixFunctionsRegistry = PostfixFunctionsRegistry(mathEngine)

        val registries = listOf(
            functionsRegistry,
            variablesRegistry,
            operatorsRegistry,
            postfixFunctionsRegistry
        )
        registries.forEach { registry ->
            registry.handler = handler
            registry.application = application
            registry.errorReporter = errorReporter
            registry.fileSystem = fileSystem
            registry.directories = directories
            registry.appScope = appScope
        }

        return CoreDependencies(
            application = application,
            appPreferences = appPreferences,
            errorReporter = errorReporter,
            dispatchers = dispatchers,
            appScope = appScope,
            directories = directories,
            fileSystem = fileSystem,
            handler = handler,
            functionsRegistry = functionsRegistry,
            variablesRegistry = variablesRegistry,
            operatorsRegistry = operatorsRegistry,
            postfixFunctionsRegistry = postfixFunctionsRegistry
        )
    }
}
