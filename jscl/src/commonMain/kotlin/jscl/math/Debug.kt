package jscl.math

object Debug {
    private var out: ((String) -> Unit)? = null

    private var indentation: Int = 0

    fun println(x: Any?) {
        val output = out ?: return
        val prefix = "   ".repeat(indentation)
        output(prefix + x.toString())
    }

    fun getOutput(): ((String) -> Unit)? = out

    fun setOutput(output: ((String) -> Unit)?) {
        Debug.out = output
    }

    fun increment() {
        indentation++
    }

    fun decrement() {
        indentation--
    }

    fun reset() {
        indentation = 0
    }
}
