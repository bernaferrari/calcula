@file:Suppress("UNCHECKED_CAST")

package jscl.math.operator

import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NotIntegerException

/**
 * User: serso
 * Date: 12/23/11
 * Time: 4:47 PM
 */
class Gcd : Operator {

    constructor(first: Generic, second: Generic) : this(arrayOf(first, second))

    constructor() : this(arrayOfNulls<Generic>(2) as Array<Generic>)

    private constructor(parameters: Array<Generic>) : super("gcd", parameters)

    override fun newInstance(parameters: Array<Generic>): Operator {
        return Gcd(parameters)
    }

    override fun getMinParameters(): Int = 2

    override fun selfExpand(): Generic {
        return expressionValue()
    }

    override fun numeric(): Generic {
        val first = parameters!![0]
        val second = parameters!![1]

        try {
            val firstInt = first.integerValue()
            val secondInt = second.integerValue()

            return firstInt.gcd(secondInt)
        } catch (e: NotIntegerException) {
            // ok => continue
        }

        return first.gcd(second)
    }

    override fun newInstance(): Gcd {
        return Gcd()
    }
}
