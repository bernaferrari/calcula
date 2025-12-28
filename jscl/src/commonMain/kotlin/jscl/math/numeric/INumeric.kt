package jscl.math.numeric

interface INumeric<T : INumeric<T>> {

    fun pow(exponent: Int): T

    fun abs(): T

    fun negate(): T

    fun signum(): Int

    fun sgn(): T

    fun ln(): T

    fun lg(): T

    fun exp(): T

    fun inverse(): T

    fun sqrt(): T

    fun nThRoot(n: Int): T

    /*
     * ******************************************************************************************
     *
     * TRIGONOMETRIC FUNCTIONS
     *
     * *******************************************************************************************/

    fun sin(): T

    fun cos(): T

    fun tan(): T

    fun cot(): T

    /*
      * ******************************************************************************************
      *
      * INVERSE TRIGONOMETRIC FUNCTIONS
      *
      * *******************************************************************************************/

    fun asin(): T

    fun acos(): T

    fun atan(): T

    fun acot(): T

    /*
      * ******************************************************************************************
      *
      * HYPERBOLIC TRIGONOMETRIC FUNCTIONS
      *
      * *******************************************************************************************/

    fun sinh(): T

    fun cosh(): T

    fun tanh(): T

    fun coth(): T

    /*
      * ******************************************************************************************
      *
      * INVERSE HYPERBOLIC TRIGONOMETRIC FUNCTIONS
      *
      * *******************************************************************************************/

    fun asinh(): T

    fun acosh(): T

    fun atanh(): T

    fun acoth(): T
}
