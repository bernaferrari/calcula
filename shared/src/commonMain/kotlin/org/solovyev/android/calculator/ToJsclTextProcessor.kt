package org.solovyev.android.calculator

import jscl.math.function.IConstant
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.text.TextProcessor
import jscl.common.msg.MessageType

class ToJsclTextProcessor(private val engine: Engine) : TextProcessor<PreparedExpression, String> {

    override fun process(from: String): PreparedExpression {
        return processWithDepth(from, 0, mutableListOf(), engine)
    }

    companion object {
        private const val MAX_DEPTH = 20

        private fun processWithDepth(
            s: String,
            depth: Int,
            undefinedVars: MutableList<IConstant>,
            engine: Engine
        ): PreparedExpression {
            return replaceVariables(
                processExpression(removeWhitespaces(s), engine).toString(),
                depth,
                undefinedVars,
                engine
            )
        }

        private fun removeWhitespaces(s: String): String {
            val res = StringBuilder(s.length)
            for (c in s) {
                if (c.isWhitespace()) continue
                res.append(c)
            }
            return res.toString()
        }

        private fun processExpression(s: String, engine: Engine): StringBuilder {
            val result = StringBuilder()
            val results = MathType.Results()

            var mathTypeResult: MathType.Result? = null
            var mathTypeBefore: MathType.Result? = null

            val nb = LiteNumberBuilder(engine)
            var i = 0
            while (i < s.length) {
                if (s[i] == ' ') {
                    i++
                    continue
                }

                results.release(mathTypeBefore)
                mathTypeBefore = mathTypeResult
                mathTypeResult = MathType.getType(s, i, nb.isHexMode(), engine)

                nb.process(mathTypeResult)

                if (mathTypeBefore != null) {
                    val current = mathTypeResult.type
                    if (current.isNeedMultiplicationSignBefore(mathTypeBefore.type)) {
                        result.append("*")
                    }
                }

                if (mathTypeBefore != null &&
                    (mathTypeBefore.type == MathType.function || mathTypeBefore.type == MathType.operator) &&
                    App.find(MathType.groupSymbols, s, i) != null
                ) {
                    val functionName = mathTypeBefore.match
                    val function = engine.functionsRegistry.get(functionName)
                    if (function == null || function.getMinParameters() > 0) {
                        throw ParseException(
                            i, s,
                            CalculatorMessage(CalculatorMessages.msg_005, MessageType.error, mathTypeBefore.match)
                        )
                    }
                }

                i = mathTypeResult.processToJscl(result, i)
                i++
            }
            return result
        }

        private fun replaceVariables(
            s: String,
            depth: Int,
            undefinedVars: MutableList<IConstant>,
            engine: Engine
        ): PreparedExpression {
            var currentDepth = depth
            if (currentDepth >= MAX_DEPTH) {
                throw ParseException(s, CalculatorMessage(CalculatorMessages.msg_006, MessageType.error))
            } else {
                currentDepth++
            }

            val result = StringBuilder()
            var i = 0
            while (i < s.length) {
                var offset = 0
                val functionName = App.find(MathType.function.getTokens(engine), s, i)
                if (functionName == null) {
                    val operatorName = App.find(MathType.operator.getTokens(engine), s, i)
                    if (operatorName == null) {
                        val varName = App.find(engine.variablesRegistry.getNames(), s, i)
                        if (varName != null) {
                            val variable = engine.variablesRegistry.get(varName)
                            if (variable != null) {
                                if (!variable.isDefined()) {
                                    undefinedVars.add(variable)
                                    result.append(varName)
                                    offset = varName.length
                                } else {
                                    val value = variable.getValue()
                                        ?: throw AssertionError()

                                    if (variable.getDoubleValue() != null) {
                                        result.append(varName)
                                    } else {
                                        result.append("(")
                                            .append(processWithDepth(value, currentDepth, undefinedVars, engine))
                                            .append(")")
                                    }
                                    offset = varName.length
                                }
                            }
                        }
                    } else {
                        result.append(operatorName)
                        offset = operatorName.length
                    }
                } else {
                    result.append(functionName)
                    offset = functionName.length
                }

                if (offset == 0) {
                    result.append(s[i])
                } else {
                    i += offset - 1
                }
                i++
            }

            return PreparedExpression(result.toString(), undefinedVars)
        }
    }
}
