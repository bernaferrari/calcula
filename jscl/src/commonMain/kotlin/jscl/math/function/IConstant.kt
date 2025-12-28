package jscl.math.function

import org.solovyev.common.math.MathEntity

/**
 * User: serso
 * Date: 11/10/11
 * Time: 6:01 PM
 */
interface IConstant : MathEntity {
    fun getConstant(): Constant
    fun getDescription(): String?
    fun isDefined(): Boolean
    fun getValue(): String?
    fun getDoubleValue(): Double?
    fun toJava(): String
}
