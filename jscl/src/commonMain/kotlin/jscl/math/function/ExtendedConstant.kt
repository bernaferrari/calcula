package jscl.math.function

import org.solovyev.common.math.MathEntity

/**
 * User: serso
 * Date: 11/7/11
 * Time: 12:06 PM
 */
open class ExtendedConstant : IConstant, Comparable<ExtendedConstant> {

    private var constant: Constant
    private var value: String?
    private var javaString: String?
    private var description: String?

    internal constructor() {
        constant = Constant("") // dummy value
        value = null
        javaString = null
        description = null
    }

    internal constructor(
        constant: Constant,
        value: String?,
        javaString: String?
    ) {
        this.constant = constant
        this.value = value
        this.javaString = javaString
        this.description = null
    }

    internal constructor(
        constant: Constant,
        value: Double?,
        javaString: String?
    ) {
        this.constant = constant
        this.value = value?.toString()
        this.javaString = javaString
        this.description = null
    }

    override val name: String
        get() = constant.name

    override fun isSystem(): Boolean {
        return constant.isSystem()
    }

    override fun getId(): Int {
        return constant.getId()
    }

    override fun setId(id: Int) {
        constant.setId(id)
    }

    override fun isIdDefined(): Boolean {
        return constant.isIdDefined()
    }

    override fun copy(that: MathEntity) {
        constant.copy(that)

        if (that is IConstant) {
            description = that.getDescription()
            value = that.getValue()
        }

        if (that is ExtendedConstant) {
            javaString = that.javaString
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ExtendedConstant) return false

        return constant == other.constant
    }

    override fun hashCode(): Int {
        return constant.hashCode()
    }

    override fun getConstant(): Constant {
        return constant
    }

    override fun getDescription(): String? {
        return description
    }

    override fun isDefined(): Boolean {
        return value != null
    }

    override fun getValue(): String? {
        return value
    }

    override fun getDoubleValue(): Double? {
        var result: Double? = null

        if (value != null) {
            try {
                result = value!!.toDouble()
            } catch (e: NumberFormatException) {
                // do nothing - string is not a double
            }
        }

        return result
    }

    override fun toJava(): String {
        return when {
            javaString != null -> javaString!!
            value != null -> value.toString()
            else -> constant.name
        }
    }

    override fun toString(): String {
        return toString(this)
    }

    override fun compareTo(other: ExtendedConstant): Int {
        return constant.compareTo(other.getConstant())
    }

    class Builder {
        private var constant: Constant
        private var value: String?
        private var javaString: String? = null
        private var description: String? = null

        constructor(constant: Constant, value: Double?) {
            this.constant = constant
            this.value = value?.toString()
        }

        constructor(constant: Constant, value: String?) {
            this.constant = constant
            this.value = value
        }

        fun setJavaString(javaString: String?): Builder {
            this.javaString = javaString
            return this
        }

        fun setDescription(description: String?): Builder {
            this.description = description
            return this
        }

        fun create(): ExtendedConstant {
            val result = ExtendedConstant()
            result.constant = constant
            result.value = value
            result.javaString = javaString
            result.description = description
            return result
        }
    }

    companion object {
        fun toString(constant: IConstant): String {
            val doubleValue = constant.getDoubleValue()
            return if (doubleValue == null) {
                val stringValue = constant.getValue()
                if (stringValue != null && stringValue.isNotEmpty()) {
                    constant.name + " = " + stringValue
                } else {
                    constant.name
                }
            } else {
                constant.name + " = " + doubleValue
            }
        }
    }
}
