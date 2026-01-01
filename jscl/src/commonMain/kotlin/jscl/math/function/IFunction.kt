package jscl.math.function

import jscl.common.math.MathEntity

interface IFunction : MathEntity {
    fun getContent(): String
    fun getDescription(): String?
    fun toJava(): String
    fun getParameterNames(): List<String>
}
