package com.bernaferrari.calculator.domain.input

import com.bernaferrari.calculator.domain.math.CalcError
import com.bernaferrari.calculator.domain.math.Decimal
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.math.UserFunction
import com.bernaferrari.calculator.domain.math.UserFunctions
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.Notation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.Serializable

/** The native caller supplies its dispatcher. The web entry point installs a dedicated worker. */
internal object CalculatorWork {
    var remote: (suspend (Request) -> Reply?)? = null

    suspend fun preview(editor: Editor, angle: AngleUnit, decimals: Int, notation: Notation): NumericDisplay? {
        val worker = remote
        if (worker != null) return worker(Request(Kind.PREVIEW, editor.text, editor.saveAnswer(), angle, decimals, notation,
            functions = UserFunctions.all, savedExpression = editor.savedExpression))?.display
        val context = currentCoroutineContext()
        return Calculator.previewDisplay(editor, angle, decimals, notation, context::ensureActive)
    }

    suspend fun moreDigits(editor: Editor, digits: Int, afterDigits: Int): NumericDisplay? {
        val worker = remote
        if (worker != null) return worker(Request(Kind.DIGITS, editor.text, editor.saveAnswer(),
            digits = digits, afterDigits = afterDigits))?.display
        val context = currentCoroutineContext()
        return Calculator.moreDigits(editor, digits, afterDigits, context::ensureActive)
    }

    suspend fun equals(state: CalcState): Pair<CalcState, Outcome> {
        val worker = remote
        if (worker == null) {
            val context = currentCoroutineContext()
            return Calculator.press(state, Key.EQUALS, context::ensureActive)
        }
        val reply = worker(Request(Kind.EQUALS, state.editor.text, state.editor.saveAnswer(), state.angleUnit,
            state.decimalPlaces, state.notation, state.resultShown, state.repeatTail, functions = UserFunctions.all,
            savedExpression = state.editor.savedExpression))
        val error = if (reply == null) CalcError.TIME_LIMIT else reply.error
        if (error != null) return state.copy(error = error, selection = null) to Outcome.Failed(error)
        val expression = reply?.expression ?: return state.copy(selection = null) to Outcome.None
        val editor = Editor(reply.text).restoreAnswer(reply.answer)
        return state.copy(editor = editor, resultShown = true, repeatTail = reply.repeatTail, fraction = reply.fraction,
            error = null, selection = null) to Outcome.Evaluated(expression, reply.text, reply.answerOperand, reply.approximate)
    }

    suspend fun rpn(state: RpnState, operation: RpnOperation): Pair<RpnState, RpnOutcome> {
        val worker = remote
        if (worker == null) {
            val context = currentCoroutineContext()
            return operation.run(state, context::ensureActive)
        }
        val reply = worker(Request(Kind.RPN, "", null, state.angleUnit, state.decimalPlaces, state.notation,
            functions = UserFunctions.all, rpn = RpnInput.from(state, operation)))
        if (reply == null) return state.copy(error = CalcError.TIME_LIMIT) to RpnOutcome.Failed(CalcError.TIME_LIMIT)
        val result = reply.rpn ?: return state to RpnOutcome.None
        val next = state.copy(stack = result.stack.map(::Decimal), entry = result.entry, lift = result.lift, error = reply.error)
        val outcome = when {
            reply.error != null -> RpnOutcome.Failed(reply.error)
            reply.expression != null -> RpnOutcome.Evaluated(reply.expression, reply.text, reply.approximate)
            else -> RpnOutcome.None
        }
        return next to outcome
    }

    @Serializable
    sealed interface RpnOperation {
        @Serializable data class Press(val key: Key) : RpnOperation
        @Serializable data class Function(val name: String) : RpnOperation
        @Serializable data class Insert(val number: String) : RpnOperation

        fun run(state: RpnState, checkpoint: () -> Unit): Pair<RpnState, RpnOutcome> = when (this) {
            is Press -> Rpn.press(state, key, checkpoint)
            is Function -> Rpn.applyFunction(state, name, checkpoint)
            is Insert -> Rpn.pushNumber(state, number, checkpoint) to RpnOutcome.None
        }
    }

    @Serializable
    data class RpnInput(val stack: List<String>, val entry: String?, val lift: Boolean, val operation: RpnOperation) {
        companion object {
            fun from(state: RpnState, operation: RpnOperation) =
                RpnInput(state.stack.map { NumberFormatter.exact(it).replace('−', '-') }, state.entry, state.lift, operation)
        }
    }

    @Serializable
    enum class Kind { PREVIEW, EQUALS, DIGITS, RPN }

    @Serializable
    data class Request(
        val kind: Kind,
        val text: String,
        val answer: String?,
        val angle: AngleUnit = AngleUnit.DEGREES,
        val decimals: Int = -1,
        val notation: Notation = Notation.NORMAL,
        val resultShown: Boolean = false,
        val repeatTail: String? = null,
        val digits: Int = 15,
        val functions: List<UserFunction> = emptyList(),
        val rpn: RpnInput? = null,
        val savedExpression: String? = null,
        val afterDigits: Int = 15,
    ) {
        fun run(): Reply {
            UserFunctions.all = functions
            val editor = Editor(text).restoreAnswer(answer).copy(savedExpression = savedExpression)
            return when (kind) {
                Kind.PREVIEW -> Reply(display = Calculator.previewDisplay(editor, angle, decimals, notation))
                Kind.DIGITS -> Reply(display = Calculator.moreDigits(editor, digits, afterDigits))
                Kind.RPN -> {
                    val input = rpn ?: return Reply()
                    val state = RpnState(input.stack.map(::Decimal), input.entry, input.lift,
                        angleUnit = angle, decimalPlaces = decimals, notation = notation)
                    val (next, outcome) = input.operation.run(state) {}
                    val saved = RpnInput.from(next, input.operation)
                    when (outcome) {
                        is RpnOutcome.Evaluated -> Reply(text = outcome.result, expression = outcome.expression,
                            approximate = outcome.approximate, rpn = saved)
                        is RpnOutcome.Failed -> Reply(error = outcome.error, rpn = saved)
                        else -> Reply(rpn = saved)
                    }
                }
                Kind.EQUALS -> {
                    val state = CalcState(editor = editor, angleUnit = angle, decimalPlaces = decimals, notation = notation,
                        resultShown = resultShown, repeatTail = repeatTail)
                    val (next, outcome) = Calculator.press(state, Key.EQUALS)
                    when (outcome) {
                        is Outcome.Evaluated -> Reply(text = outcome.result, answer = next.editor.saveAnswer(),
                            expression = outcome.expression, answerOperand = outcome.answerOperand, approximate = outcome.approximate,
                            repeatTail = next.repeatTail, fraction = next.fraction)
                        is Outcome.Failed -> Reply(error = outcome.error)
                        else -> Reply()
                    }
                }
            }
        }
    }

    @Serializable
    data class Reply(
        val display: NumericDisplay? = null,
        val text: String = "",
        val answer: String? = null,
        val expression: String? = null,
        val answerOperand: String? = null,
        val approximate: Boolean = false,
        val repeatTail: String? = null,
        val fraction: String? = null,
        val error: CalcError? = null,
        val rpn: RpnInput? = null,
    )
}
