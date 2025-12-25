package jscl.math

import java.io.PrintStream

object Debug {
    @JvmStatic
    private var out: PrintStream? = null

    @JvmStatic
    private var indentation: Int = 0

    @JvmStatic
    fun println(x: Any?) {
        out?.let {
            repeat(indentation) {
                out?.print("   ")
            }
            out?.println(x)
        }
    }

    @JvmStatic
    fun getOutputStream(): PrintStream? = out

    @JvmStatic
    fun setOutputStream(out: PrintStream?) {
        Debug.out = out
    }

    @JvmStatic
    fun increment() {
        indentation++
    }

    @JvmStatic
    fun decrement() {
        indentation--
    }

    @JvmStatic
    fun reset() {
        indentation = 0
    }
}
