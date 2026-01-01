package org.solovyev.android.calculator.math

import jscl.JsclMathEngine
import jscl.NumeralBase
import jscl.math.function.Constants
import org.solovyev.android.Check
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.ParseException

enum class MathType(
    val priority: Int,
    private val needMultiplicationSignBefore: Boolean,
    private val needMultiplicationSignAfter: Boolean,
    val groupType: MathGroupType,
    private val _tokens: List<String>
) {

    numeral_base(
        50, true, false, MathGroupType.number,
        NumeralBase.values().map { it.getJsclPrefix() }
    ),

    dot(200, true, true, MathGroupType.number, listOf(".")) {
        override fun isNeedMultiplicationSignBefore(mathTypeBefore: MathType): Boolean {
            return super.isNeedMultiplicationSignBefore(mathTypeBefore) && mathTypeBefore != digit
        }
    },

    grouping_separator(250, false, false, MathGroupType.number, listOf("'", " ")) {
        override fun processToJscl(result: StringBuilder, i: Int, match: String): Int {
            return i
        }
    },

    power_10(300, false, false, MathGroupType.number, listOf("E")),

    postfix_function(400, false, true, MathGroupType.function, emptyList()) {
        override fun getTokens(engine: Engine): List<String> {
            return engine.postfixFunctionsRegistry.getNames()
        }
    },

    unary_operation(500, false, false, MathGroupType.operation, listOf("−", "-", "=")) {
        override fun getSubstituteToJscl(match: String): String? {
            return if (match == "−") "-" else null
        }

        override fun getSubstituteFromJscl(match: String): String? {
            return if (match == "-") "−" else null
        }
    },

    binary_operation(600, false, false, MathGroupType.operation, listOf("−", "-", "+", "*", "×", "∙", "/", "^")) {
        override fun getSubstituteFromJscl(match: String): String? {
            return if (match == "-") "−" else null
        }

        override fun getSubstituteToJscl(match: String): String? {
            return when (match) {
                "×", "∙" -> "*"
                "−" -> "-"
                else -> null
            }
        }
    },

    open_group_symbol(800, true, false, MathGroupType.other, listOf("(", "[", "{")) {
        override fun isNeedMultiplicationSignBefore(mathTypeBefore: MathType): Boolean {
            return super.isNeedMultiplicationSignBefore(mathTypeBefore) &&
                   mathTypeBefore != function && mathTypeBefore != `operator`
        }

        override fun getSubstituteToJscl(match: String): String {
            return "("
        }
    },

    close_group_symbol(900, false, true, MathGroupType.other, listOf(")", "]", "}")) {
        override fun isNeedMultiplicationSignBefore(mathTypeBefore: MathType): Boolean {
            return false
        }

        override fun getSubstituteToJscl(match: String): String {
            return ")"
        }
    },

    function(1000, true, true, MathGroupType.function, emptyList()) {
        override fun getTokens(engine: Engine): List<String> {
            return engine.functionsRegistry.getNames()
        }

        override val tokens: List<String>
            get() {
                Check.shouldNotHappen()
                return super.tokens
            }
    },

    `operator`(1050, true, true, MathGroupType.function, emptyList()) {
        override fun getTokens(engine: Engine): List<String> {
            return engine.operatorsRegistry.getNames()
        }

        override val tokens: List<String>
            get() {
                Check.shouldNotHappen()
                return super.tokens
            }
    },

    constant(1100, true, true, MathGroupType.other, emptyList()) {
        override fun getTokens(engine: Engine): List<String> {
            return engine.variablesRegistry.getNames()
        }

        override val tokens: List<String>
            get() {
                Check.shouldNotHappen()
                return super.tokens
            }

        override fun getSubstituteFromJscl(match: String): String? {
            return if (Constants.INF_2.name == match) INFINITY else super.getSubstituteFromJscl(match)
        }
    },

    digit(1125, true, true, MathGroupType.number,
        NumeralBase.hex.getAcceptableCharacters().map { it.toString() }
    ) {
        override fun isNeedMultiplicationSignBefore(mathTypeBefore: MathType): Boolean {
            return super.isNeedMultiplicationSignBefore(mathTypeBefore) &&
                   mathTypeBefore != digit && mathTypeBefore != dot
        }
    },

    comma(1150, false, false, MathGroupType.other, listOf(",")),

    text(1200, false, false, MathGroupType.other, emptyList()) {
        override fun processToJscl(result: StringBuilder, i: Int, match: String): Int {
            if (match.isNotEmpty()) {
                result.append(match[0])
            }
            return i
        }
    };
    


    constructor(
        priority: Int,
        needMultiplicationSignBefore: Boolean,
        needMultiplicationSignAfter: Boolean,
        groupType: MathGroupType,
        vararg tokens: String
    ) : this(priority, needMultiplicationSignBefore, needMultiplicationSignAfter, groupType, tokens.toList())

    open fun getTokens(engine: Engine): List<String> {
        return tokens
    }

    open val tokens: List<String>
        get() = _tokens

    private fun isNeedMultiplicationSignAfter(): Boolean {
        return needMultiplicationSignAfter
    }

    open fun isNeedMultiplicationSignBefore(mathTypeBefore: MathType): Boolean {
        return needMultiplicationSignBefore && mathTypeBefore.isNeedMultiplicationSignAfter()
    }

    open fun processToJscl(result: StringBuilder, i: Int, match: String): Int {
        val substitute = getSubstituteToJscl(match)
        result.append(substitute ?: match)
        return returnI(i, match)
    }

    protected fun returnI(i: Int, match: String): Int {
        return if (match.length > 1) {
            i + match.length - 1
        } else {
            i
        }
    }

    protected open fun getSubstituteFromJscl(match: String): String? {
        return null
    }

    protected open fun getSubstituteToJscl(match: String): String? {
        return null
    }

    enum class MathGroupType {
        function,
        number,
        operation,
        other
    }

    data class Result(
        var type: MathType = text,
        var match: String = ""
    ) {
        fun set(type: MathType, match: String): Result {
            this.type = type
            this.match = match
            return this
        }

        fun processToJscl(result: StringBuilder, i: Int): Int {
            return type.processToJscl(result, i, match)
        }
    }

    class Results {
        private val list = mutableListOf<Result>()

        fun obtain(): Result {
            return if (list.isEmpty()) {
                Result()
            } else {
                list.removeAt(list.size - 1)
            }
        }

        fun release(result: Result?) {
            result?.let { list.add(it) }
        }
    }

    companion object {
        val groupSymbols = listOf("()", "[]", "{}")
        const val EXPONENT = 'E'
        const val E = "e"
        const val C = "c"
        const val NAN = "NaN"
        const val INFINITY = "∞"
        const val INFINITY_JSCL = "Infinity"

        private var mathTypesByPriority: List<MathType>? = null

        /**
         * Method determines mathematical entity type for text substring starting from ith index
         *
         * @param text    analyzed text
         * @param i       index which points to start of substring
         * @param hexMode true if current mode if HEX
         * @param engine math engine
         * @return math entity type of substring starting from ith index of specified text
         */
        fun getType(text: String, i: Int, hexMode: Boolean, engine: Engine): Result {
            return getType(text, i, hexMode, Result(), engine)
        }

        fun getType(text: String, i: Int, hexMode: Boolean, result: Result, engine: Engine): Result {
            when {
                i < 0 -> throw IllegalArgumentException("I must be more or equals to 0.")
                i >= text.length && i != 0 -> throw IllegalArgumentException("I must be less than size of text.")
                i == 0 && text.isEmpty() -> return result.set(MathType.text, text)
            }

            val mathTypes = getMathTypesByPriority()
            for (mathType in mathTypes) {
                val s = App.find(mathType.getTokens(engine), text, i) ?: continue

                if (s.length > 1) {
                    if (mathType == function) {
                        val nextToken = i + s.length
                        if (nextToken < text.length) {
                            // function must have an open group symbol after its name
                            if (open_group_symbol.tokens.contains(text.substring(nextToken, nextToken + 1))) {
                                return result.set(function, s)
                            }
                        } else if (nextToken == text.length) {
                            // or its name should finish the expression
                            return result.set(function, s)
                        }
                        continue
                    }
                    return result.set(mathType, s)
                }

                if (hexMode || JsclMathEngine.getInstance().getNumeralBase() == NumeralBase.hex) {
                    val ch = s[0]
                    if (NumeralBase.hex.getAcceptableCharacters().contains(ch)) {
                        return result.set(digit, s)
                    }
                }

                if (mathType == grouping_separator) {
                    if (i + 1 < text.length &&
                        digit.tokens.contains(text.substring(i + 1, i + 2)) &&
                        i - 1 >= 0 && digit.tokens.contains(text.substring(i - 1, i))
                    ) {
                        return result.set(mathType, s)
                    }
                    continue
                }

                return result.set(mathType, s)
            }

            return result.set(MathType.text, text.substring(i))
        }

        private fun getMathTypesByPriority(): List<MathType> {
            if (mathTypesByPriority == null) {
                mathTypesByPriority = values().sortedBy { it.priority }
            }
            return mathTypesByPriority!!
        }

        fun isOpenGroupSymbol(c: Char): Boolean {
            return c == '(' || c == '[' || c == '{'
        }

        fun isCloseGroupSymbol(c: Char): Boolean {
            return c == ')' || c == ']' || c == '}'
        }
    }
}
