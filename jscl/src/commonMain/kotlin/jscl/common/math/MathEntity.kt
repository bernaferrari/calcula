package jscl.common.math

interface MathEntity {

    val name: String

    fun isSystem(): Boolean

    fun getId(): Int

    fun setId(id: Int)

    fun isIdDefined(): Boolean

    fun copy(that: MathEntity)
}
