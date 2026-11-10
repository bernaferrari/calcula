package com.bernaferrari.calculator.domain.math

import com.bernaferrari.calculator.domain.model.AngleUnit
import kotlin.math.abs
import kotlin.time.TimeSource
import kotlin.time.Duration.Companion.milliseconds
import com.bernaferrari.calculator.domain.math.Expression.Companion.fitsBudget

enum class CalcError { DIVIDE_BY_ZERO, DOMAIN, SYNTAX, OVERFLOW, TIME_LIMIT, PRECISION_LIMIT }

class CalcException(val error: CalcError) : Exception(error.name)

internal class DomainNeedsPrecision : Exception()

/**
 * Evaluates the calculator's expression language.
 *
 * Grammar (lowest to highest precedence):
 *   additive       := multiplicative (('+' | '−') multiplicative)*
 *   multiplicative := unary (('×' | '÷') unary | <implicit ×> power)*
 *   unary          := ('−' | '+') unary | power
 *   power          := postfix ('^' unary)?            right-associative, so 2^3^2 = 2^9
 *   postfix        := primary ('!' | '%' | '²' | '³' | '⁻¹')*
 *   primary        := number | π | e | '(' additive ')' | function '(' additive ')' | ('√' | '∛') postfix
 *
 * Missing closing parentheses at the end are closed automatically. Percent is "smart":
 * `a + b%` adds b percent of a, while `a × b%` multiplies by b/100.
 *
 * Evaluation starts at 40 digits and can retry the same tree at higher precision.
 */
object CalcEngine {
    internal val MC = MathContext(40, RoundingMode.HALF_EVEN)
    internal const val MAX_REFINEMENT_DIGITS = 4_096

    private val HUNDRED = Decimal(100)
    private val LIMIT = Decimal.ONE.scaleByPowerOfTen(10_000)

    fun evaluate(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): Decimal =
        evaluateValue(expression, angleUnit).displayValue

    // A bound answer is a single operand in the editor, with its own captured expression tree.
    internal fun evaluateValue(
        expression: String,
        angleUnit: AngleUnit,
        answer: Value? = null,
        checkCancelled: () -> Unit = {},
        displaySettled: ((Value) -> Boolean)? = null,
        timeBudgetMillis: Long = 600,
        requestedDigits: Int? = null,
        startingDigits: Int? = null,
    ): Value {
        val started = TimeSource.Monotonic.markNow()
        val checkpoint = {
            checkCancelled()
            if (started.elapsedNow() >= timeBudgetMillis.milliseconds) throw CalcException(CalcError.TIME_LIMIT)
        }
        checkpoint()
        val tokens = Tokenizer.tokenize(expression, answer?.let { Token.Num(it.v, it.error, it.symbolic, it.tree) })
        if (tokens.isEmpty()) throw CalcException(CalcError.SYNTAX)
        val parsed = Parser(tokens, angleUnit, checkpoint).parseAll()
        val retained = parsed.takeIf { it.fitsBudget() }
        val tree = if (retained != null || answer?.tree == null) parsed else
            Parser(Tokenizer.tokenize(expression, Token.Num(answer.v, answer.error, answer.symbolic)), angleUnit, checkpoint).parseAll()
        val constants = DecimalConstants(checkpoint)
        val passes = if (requestedDigits == null) {
            sequenceOf(MC.precision, 80, 160)
        } else {
            val target = requestedDigits.coerceIn(16, MAX_REFINEMENT_DIGITS) + 12
            val first = startingDigits?.let { (it + 12).coerceIn(28, target) } ?: target
            val limit = 2 * (MAX_REFINEMENT_DIGITS + 12)
            // Complete a smaller batch before attempting the target. If cancellation eats
            // the guard digits, continue beyond the target under the same deadline.
            generateSequence(first) { precision ->
                when {
                    precision < target -> minOf(precision * 2, target)
                    precision < limit -> minOf(precision * 2, limit)
                    else -> null
                }
            }
        }
        var last: Value? = null
        for (precision in passes) {
            val context = MathContext(precision, RoundingMode.HALF_EVEN, constants)
            val result = try {
                Evaluation(context, checkpoint).evaluate(tree)
            } catch (_: DomainNeedsPrecision) {
                last = null
                continue
            } catch (e: CalcException) {
                if (last == null || (e.error != CalcError.TIME_LIMIT && e.error != CalcError.PRECISION_LIMIT)) throw e
                break
            }
            if (result.error == null && result.symbolic == null) {
                last = null
                if (!result.retryable) break
                continue
            }
            last = result
            if (displaySettled == null || retained == null || result.exact || !result.retryable || displaySettled(result)) break
        }
        val result = last ?: throw CalcException(CalcError.PRECISION_LIMIT)
        if (result.v.abs() > LIMIT) throw CalcException(CalcError.OVERFLOW)
        return result.copy(tree = retained ?: tree.takeIf { it.fitsBudget() })
    }

    fun evaluateOrNull(expression: String, angleUnit: AngleUnit = AngleUnit.DEGREES): Decimal? =
        try {
            evaluate(expression, angleUnit)
        } catch (e: CalcException) {
            null
        } catch (e: ArithmeticException) {
            null
        }

    internal data class Value(
        val v: Decimal,
        val percent: Boolean = false,
        val error: Decimal? = Decimal.ZERO,
        val symbolic: SymbolicNumber? = if (error?.signum() == 0) SymbolicNumber.fromDecimal(v) else null,
        val defined: Boolean = true,
        val tree: Expression? = null,
        val retryable: Boolean = true,
    ) {
        val exact: Boolean get() = error?.signum() == 0
        val displayValue: Decimal get() =
            if (symbolic == null && error != null && v.abs() <= error) Decimal.ZERO else v

        val provenExact: Boolean get() = defined && (exact || symbolic != null)

        fun signIsKnown(): Boolean =
            v.signum() != 0 && (symbolic?.signum() == v.signum() || error?.let { v.abs() > it } == true)

        fun negate(): Value = copy(v = v.negate(), symbolic = symbolic?.negate())

        fun multiply(other: Value, context: MathContext): Value {
            if (symbolic != null && other.symbolic != null) symbolic.multiply(other.symbolic)?.let { return valueOf(it, context) }
            return roundedValue(v.multiply(other.v), DecimalError.multiply(v, error, other.v, other.error), context)
        }

        fun divide(other: Value, context: MathContext): Value {
            if (symbolic != null && other.symbolic != null) symbolic.divide(other.symbolic)?.let { return valueOf(it, context) }
            val radius = other.error
            if (radius != null && radius.signum() > 0 && other.v.abs() <= radius) {
                throw DomainNeedsPrecision()
            }
            val quotient = divide(v, other.v, context)
            return Value(quotient, error = DecimalError.divide(v, error, other.v, other.error, quotient))
        }

        fun add(other: Value, context: MathContext): Value {
            if (symbolic != null && other.symbolic != null) symbolic.add(other.symbolic)?.let { return valueOf(it, context) }
            if (v.signum() == 0 && exact) return other.copy(percent = false)
            if (other.v.signum() == 0 && other.exact) return copy(percent = false)
            val radius = DecimalError.add(error, other.error)
            v.dominantAddend(other.v, context)?.let { dominant ->
                val discarded = if (dominant === v) other.v else v
                return Value(dominant, error = DecimalError.add(radius, discarded.abs()))
            }
            return roundedValue(v.add(other.v), radius, context)
        }
    }

    private fun valueOf(number: SymbolicNumber, context: MathContext): Value =
        number.approximation(context)

    internal fun roundedValue(value: Decimal, error: Decimal?, context: MathContext): Value {
        val rounded = value.round(context)
        return Value(rounded, error = DecimalError.round(value, rounded, error))
    }

    private class Parser(
        private val tokens: List<Token>, private val angle: AngleUnit, private val checkCancelled: () -> Unit,
    ) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)

        private fun next(): Token {
            checkCancelled()
            return tokens.getOrNull(pos++) ?: throw CalcException(CalcError.SYNTAX)
        }

        fun parseAll(): Expression {
            val value = additive()
            if (pos != tokens.size) throw CalcException(CalcError.SYNTAX)
            return value
        }

        private fun additive(): Expression {
            var left = multiplicative()
            while (true) {
                val op = (peek() as? Token.Op)?.symbol
                if (op != '+' && op != '−') break
                pos++
                val right = multiplicative()
                left = Expression.Binary(op, left, right)
            }
            return left
        }

        private fun multiplicative(): Expression {
            var left = unary()
            while (true) {
                val token = peek()
                when {
                    token is Token.Op && token.symbol == '×' -> {
                        pos++
                        left = Expression.Binary('×', left, unary())
                    }
                    token is Token.Op && token.symbol == '÷' -> {
                        pos++
                        left = Expression.Binary('÷', left, unary())
                    }
                    token != null && token.startsOperand() -> {
                        left = Expression.Binary('×', left, power())
                    }
                    else -> return left
                }
            }
        }

        private fun unary(): Expression {
            val token = peek()
            if (token is Token.Op && token.symbol == '−') {
                pos++
                val inner = unary()
                return Expression.Unary("−", inner)
            }
            if (token is Token.Op && token.symbol == '+') {
                pos++
                return unary()
            }
            return power()
        }

        private fun power(): Expression {
            val base = postfix()
            val token = peek()
            if (token is Token.Op && token.symbol == '^') {
                pos++
                if (peek() == null) throw CalcException(CalcError.SYNTAX)
                val exponent = unary()
                return Expression.Binary('^', base, exponent)
            }
            return base
        }

        private fun postfix(): Expression {
            var value = primary()
            while (true) {
                val token = peek() as? Token.Postfix ?: return value
                pos++
                checkCancelled()
                value = Expression.Unary(token.symbol, value)
            }
        }

        private fun primary(): Expression {
            return when (val token = next()) {
                is Token.Num -> token.tree?.let { if (it.isPercent()) Expression.Unary("group", it) else it } ?: Expression.Number(
                    token.value.unscaled.toString(), token.value.scale(), Expression.encodeRadius(token.error), token.symbolic?.save(),
                )
                is Token.Const -> Expression.Constant(token.name)
                is Token.LParen -> group().let { if (it.isPercent()) Expression.Unary("group", it) else it }
                is Token.Func -> {
                    val arg = if (peek() is Token.LParen) {
                        pos++
                        group()
                    } else {
                        power()
                    }
                    if (token.name == "exp") Expression.Binary('^', Expression.Constant("e"), arg)
                    else Expression.Call(token.name, arg, angle)
                }
                is Token.Root -> Expression.Unary(if (token.degree == 2) "√" else "∛", postfix())
                else -> throw CalcException(CalcError.SYNTAX)
            }
        }

        /** Parses the inside of a parenthesis; the closing one is optional at the very end. */
        private fun group(): Expression {
            if (peek() == null) throw CalcException(CalcError.SYNTAX)
            val inner = additive()
            when (peek()) {
                is Token.RParen -> pos++
                null -> Unit
                else -> throw CalcException(CalcError.SYNTAX)
            }
            return inner
        }

    }

    private fun Expression.isPercent(): Boolean = this is Expression.Unary &&
        (op == "%" || (op == "−" && input.isPercent()))

    private class Evaluation(private val context: MathContext, private val checkCancelled: () -> Unit) {
        private val trig by lazy(LazyThreadSafetyMode.NONE) { DecimalTrig(context, checkCancelled) }

        fun evaluate(node: Expression, simplifyAdditions: Boolean = true): Value {
            checkCancelled()
            return when (node) {
                is Expression.Number -> node.value().let { it.symbolic?.let(::symbolicValue) ?: it }
                is Expression.Constant -> symbolicValue(if (node.name == "π") SymbolicNumber.PI else SymbolicNumber.E)
                is Expression.Unary -> unary(node)
                is Expression.Binary -> if (node.isAdditive()) {
                    if (simplifyAdditions) additive(node) else binary(node, simplifyAdditions = false)
                } else binary(node)
                is Expression.Call -> {
                    val power = node.input as? Expression.Binary
                    if (node.name == "ln" && power?.op == '^' && power.left == Expression.Constant("e")) {
                        return evaluate(power.right).copy(percent = false)
                    }
                    val arg = evaluate(node.input)
                    functionValue(node.name, arg, node.angle).let {
                        it.copy(defined = arg.defined && it.defined && domainIsKnown(node.name, arg),
                            retryable = arg.retryable && it.retryable)
                    }
                }
            }
        }

        private fun Expression.isAdditive(): Boolean = this is Expression.Binary &&
            (op == '+' || op == '−') && !right.isPercent()

        private fun additive(node: Expression.Binary): Value {
            val terms = mutableListOf<Pair<Expression, Boolean>>()
            fun collect(expression: Expression, positive: Boolean) {
                if (expression is Expression.Binary && expression.isAdditive()) {
                    collect(expression.left, positive)
                    collect(expression.right, if (expression.op == '+') positive else !positive)
                } else terms += expression to positive
            }
            collect(node, true)
            val cancelled = BooleanArray(terms.size)
            val values = mutableMapOf<Expression, Value>()
            val waiting = mutableMapOf<Pair<Expression, Boolean>, ArrayDeque<Int>>()
            for ((index, term) in terms.withIndex()) {
                val (expression, positive) = term
                val match = waiting[expression to !positive]?.removeLastOrNull()
                if (match == null) {
                    waiting.getOrPut(term) { ArrayDeque() }.addLast(index)
                } else {
                    val value = values.getOrPut(expression) { evaluate(expression) }
                    // A repeated invalid subtree still throws. An unproven domain can't justify a rewrite.
                    if (!value.defined) return binary(node, simplifyAdditions = false)
                    cancelled[index] = true
                    cancelled[match] = true
                }
            }
            if (cancelled.none { it }) return binary(node, simplifyAdditions = false)
            var result = Value(Decimal.ZERO)
            for ((index, term) in terms.withIndex()) {
                if (!cancelled[index]) {
                    val value = values.getOrPut(term.first) { evaluate(term.first) }
                    result = result.add(if (term.second) value else value.negate(), context)
                }
            }
            val remaining = terms.indices.filterNot { cancelled[it] }.map { values.getValue(terms[it].first) }
            return result.copy(defined = remaining.all { it.defined }, retryable = remaining.all { it.retryable })
        }

        private fun binary(node: Expression.Binary, simplifyAdditions: Boolean = true): Value {
            if (node.op == '^' && node.left == Expression.Constant("e")) {
                val logarithm = node.right as? Expression.Call
                if (logarithm?.name == "ln") {
                    val argument = evaluate(logarithm.input)
                    if (argument.defined && argument.v.signum() > 0 && argument.signIsKnown()) {
                        return argument.copy(percent = false)
                    }
                }
            }
            if (node.op == '+') {
                val a = squaredTrig(node.left)
                val b = squaredTrig(node.right)
                if (a != null && b != null && a.name != b.name && a.angle == b.angle && a.input == b.input) {
                    val argument = evaluate(a.input)
                    if (argument.defined) return Value(Decimal.ONE)
                }
            }
            val a = evaluate(node.left, simplifyAdditions)
            val b = evaluate(node.right, simplifyAdditions)
            val result = when (node.op) {
                '+', '−' -> {
                    val amount = if (b.percent) a.multiply(b, context) else b
                    a.add(if (node.op == '+') amount else amount.negate(), context)
                }
                '×' -> a.multiply(b, context)
                '÷' -> a.divide(b, context)
                '^' -> powerOf(a, b, context, checkCancelled)
                else -> throw CalcException(CalcError.SYNTAX)
            }
            val defined = a.defined && b.defined && when (node.op) {
                '÷' -> b.signIsKnown()
                '^' -> (a.v.signum() > 0 && a.signIsKnown()) ||
                    (b.exact && b.v.isInteger() && (b.v.signum() >= 0 || a.signIsKnown())) ||
                    (a.exact && a.v.signum() == 0 && b.v.signum() > 0 && b.signIsKnown()) ||
                    (a.v.signum() < 0 && a.signIsKnown() && oddDenominator(b) != null)
                else -> true
            }
            return result.copy(defined = defined, retryable = a.retryable && b.retryable && result.retryable)
        }

        private fun squaredTrig(node: Expression): Expression.Call? {
            val base = when (node) {
                is Expression.Unary -> if (node.op == "²") node.input else return null
                is Expression.Binary -> {
                    val exponent = node.right as? Expression.Number
                    if (node.op != '^' || exponent?.value()?.let { it.exact && it.v == Decimal(2) } != true) return null
                    node.left
                }
                else -> return null
            }
            return (base as? Expression.Call)?.takeIf { it.name == "sin" || it.name == "cos" }
        }

        private fun unary(node: Expression.Unary): Value {
            val value = evaluate(node.input)
            val result = when (node.op) {
                "−" -> value.negate()
                "group" -> value.copy(percent = false)
                "%" -> value.divide(Value(HUNDRED), context).copy(percent = true)
                "!" -> {
                    if (!value.exact) throw DomainNeedsPrecision()
                    factorial(value.v, checkCancelled, context)
                }
                "²" -> value.multiply(value, context)
                "³" -> powerOf(value, Value(Decimal(3)), context, checkCancelled)
                "⁻¹" -> Value(Decimal.ONE).divide(value, context).copy(defined = value.signIsKnown())
                "√", "∛" -> root(value, if (node.op == "√") 2 else 3)
                else -> throw CalcException(CalcError.SYNTAX)
            }
            return result.copy(defined = value.defined && result.defined, retryable = value.retryable && result.retryable)
        }

        private fun root(value: Value, degree: Int): Value {
            val exact = if (degree == 2) value.symbolic?.squareRoot() else value.symbolic?.cubeRoot()
            if (exact != null) return symbolicValue(exact)
            val center = if (degree == 2) sqrt(value.v, context) else cbrt(value.v, context)
            return Value(center, error = DecimalError.root(value.v, value.error, center, degree),
                defined = degree == 3 || value.exact || (value.v.signum() > 0 && value.signIsKnown()))
        }

        private fun symbolicValue(number: SymbolicNumber): Value = valueOf(number, context)

        private fun domainIsKnown(name: String, arg: Value): Boolean = when (name) {
            "ln", "log", "log₂" -> arg.v.signum() > 0 && arg.signIsKnown()
            "sin⁻¹", "cos⁻¹", "cosh⁻¹", "tanh⁻¹", "tan", "sec", "csc", "cot" -> arg.error != null
            else -> true
        }

        private fun functionValue(name: String, arg: Value, angle: AngleUnit): Value {
            if (name == "abs") return if (arg.v.signum() < 0) arg.negate().copy(percent = false) else arg.copy(percent = false)
            if (name == "sin" || name == "cos" || name == "tan") {
                arg.symbolic?.trig(name, angle)?.let { return symbolicValue(it) }
                return trig.circular(name, arg, angle)
            }
            if (name == "sin⁻¹" || name == "cos⁻¹" || name == "tan⁻¹") {
                arg.symbolic?.inverseTrig(name, angle)?.let { return symbolicValue(it) }
                return trig.inverse(name, arg, angle)
            }
            when (name) {
                "sec" -> return reciprocal(Value(Decimal.ONE), functionValue("cos", arg, angle))
                "csc" -> return reciprocal(Value(Decimal.ONE), functionValue("sin", arg, angle))
                "cot" -> return reciprocal(functionValue("cos", arg, angle), functionValue("sin", arg, angle))
                "sinh", "cosh", "tanh", "sinh⁻¹", "cosh⁻¹", "tanh⁻¹" -> return trig.hyperbolic(name, arg)
            }
            val exact = when (name) {
                "log" -> exactLog10(arg.v)
                "log₂" -> exactLog2(arg.v)
                else -> null
            }
            if (exact != null && arg.exact) return Value(exact)
            if (name == "ln") {
                arg.symbolic?.naturalLog()?.let { return symbolicValue(it) }
                return DecimalLogarithm.natural(arg, context, checkCancelled)
            }
            if (name == "log" || name == "log₂") {
                val work = context.withPrecision(context.precision + 8)
                val natural = DecimalLogarithm.natural(arg, work, checkCancelled)
                val constants = context.constants ?: DecimalConstants(checkCancelled)
                val base = if (name == "log") constants.lnTen(work) else constants.lnTwo(work)
                val result = natural.divide(base, work)
                return roundedValue(result.v, result.error, context).copy(retryable = natural.retryable)
            }
            return integerFunction(name, arg)
        }

        private fun integerFunction(name: String, arg: Value): Value {
            if (name == "sgn") {
                return when {
                    arg.exact || arg.signIsKnown() -> Value(Decimal(arg.v.signum()))
                    arg.error != null -> Value(Decimal.ZERO, error = Decimal.ONE)
                    else -> Value(Decimal(arg.v.signum()), error = null)
                }
            }
            fun rounded(value: Decimal): Decimal {
                if (value.scale() <= 0) return value
                return when (name) {
                    "floor" -> whole(value, up = false)
                    "ceil" -> whole(value, up = true)
                    "round" -> value.setScale(0, RoundingMode.HALF_UP)
                    else -> throw CalcException(CalcError.SYNTAX)
                }
            }
            val center = rounded(arg.v)
            if (arg.exact) return Value(center)
            val radius = arg.error ?: return Value(center, error = null)
            if (name == "round" && arg.v.scale() <= 0 && radius < Decimal("0.5")) return Value(center)
            // Endpoints can be extremely far apart in scale. Keep a bound without expanding
            // a thousands-of-digits integer merely to test whether the two endpoints agree.
            val centerDigits = arg.v.precision().toLong() - arg.v.scale()
            val radiusDigits = radius.precision().toLong() - radius.scale()
            if (abs(centerDigits - radiusDigits) <= context.precision.toLong() + 12) {
                val lower = rounded(arg.v.subtract(radius))
                val upper = rounded(arg.v.add(radius))
                if (lower == upper) return Value(lower)
            }
            // Rounding to an integer changes the input by at most one.
            return Value(center, error = DecimalError.add(radius, Decimal(2)))
        }

        private fun reciprocal(numerator: Value, denominator: Value): Value {
            val radius = denominator.error
            if (radius != null && radius.signum() > 0 && denominator.v.abs() <= radius) throw DomainNeedsPrecision()
            if (denominator.v.signum() == 0) throw CalcException(CalcError.DOMAIN)
            return numerator.divide(denominator, context)
        }

        /** The integer at or below [x] (or at or above, when [up]), without leaving decimals. */
        private fun whole(x: Decimal, up: Boolean): Decimal {
            val truncated = x.setScale(0, RoundingMode.DOWN)
            if (truncated.compareTo(x) == 0) return truncated
            return when {
                !up && x.signum() < 0 -> truncated.subtract(Decimal(1))
                up && x.signum() > 0 -> truncated.add(Decimal(1))
                else -> truncated
            }
        }

    }

    private fun Token.startsOperand(): Boolean =
        this is Token.Num || this is Token.Const || this is Token.Var || this is Token.LParen || this is Token.Func || this is Token.Root

    private fun divide(a: Decimal, b: Decimal, context: MathContext): Decimal {
        if (b.signum() == 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
        return a.divide(b, context)
    }

    private fun exactLog10(x: Decimal): Decimal? {
        val stripped = x.stripTrailingZeros()
        return if (stripped.unscaled == BigInteger.ONE) Decimal(-stripped.scale()) else null
    }

    private fun exactLog2(x: Decimal): Decimal? {
        if (x.signum() <= 0) return null
        val stripped = x.stripTrailingZeros()
        val coefficient = stripped.unscaled
        val scale = stripped.scale()
        if (scale < 0) return null
        if (scale == 0) {
            val exponent = coefficient.bitLength() - 1
            return Decimal(exponent).takeIf { coefficient == BigInteger.ONE.shl(exponent) }
        }
        // A reciprocal power of two has the decimal representation 5^scale × 10^−scale.
        val bits = coefficient.bitLength()
        // 2.321928 < log₂(5) < 2.321929. This filter only rejects impossible candidates.
        val low = scale.toLong() * 2_321_928 / 1_000_000 + 1
        val high = scale.toLong() * 2_321_929 / 1_000_000 + 1
        if (bits.toLong() !in low..high) return null
        return Decimal(-scale).takeIf { coefficient == BigInteger.fromInt(5).pow(scale) }
    }

    private fun oddDenominator(exponent: Value): Fraction? =
        exponent.symbolic?.fraction?.takeIf { !it.denominator.rem(BigInteger.TWO).isZero() }

    private fun powerOf(base: Value, exponent: Value, context: MathContext, checkCancelled: () -> Unit): Value {
        if (base.symbolic != null && exponent.symbolic != null) {
            base.symbolic.pow(exponent.symbolic)?.let { return valueOf(it, context) }
        }
        if (exponent.exact && exponent.v.signum() == 0) return Value(Decimal.ONE)
        val integer = exponent.exact && exponent.v.isInteger()
        if ((!integer || exponent.v.signum() < 0) && base.error?.let {
                it.signum() > 0 && base.v.abs() <= it
            } == true
        ) throw DomainNeedsPrecision()
        if (base.v.signum() == 0 && base.exact) {
            if (!exponent.signIsKnown()) throw DomainNeedsPrecision()
            if (exponent.v.signum() < 0) throw CalcException(CalcError.DIVIDE_BY_ZERO)
            return Value(Decimal.ZERO)
        }
        if (base.symbolic == SymbolicNumber.E) return exponential(exponent, context, checkCancelled)
        if (integer) {
            return integerPower(base, exponent.v, context, checkCancelled)
        }

        val negative = base.v.signum() < 0
        val fraction = if (negative) oddDenominator(exponent) ?: throw CalcException(CalcError.DOMAIN) else null
        val work = context.withPrecision(context.precision + 8)
        val magnitude = if (negative) base.negate() else base
        val logarithm = DecimalLogarithm.natural(magnitude, work, checkCancelled)
        val argument = logarithm.multiply(exponent, work).copy(retryable = logarithm.retryable && exponent.retryable)
        val result = exponential(argument, context, checkCancelled)
        return if (fraction != null && !fraction.numerator.rem(BigInteger.TWO).isZero()) result.negate() else result
    }

    internal fun integerPower(base: Value, exponent: Decimal, context: MathContext, checkpoint: () -> Unit): Value {
        val magnitude = base.v.abs()
        if (magnitude == Decimal.ONE && base.exact) {
            val odd = exponent.remainder(Decimal(2)) != Decimal.ZERO
            return Value(if (base.v.signum() < 0 && odd) Decimal.ONE.negate() else Decimal.ONE)
        }
        if (exponent.abs() > Decimal(Int.MAX_VALUE)) throw CalcException(CalcError.OVERFLOW)
        val n = exponent.toInt()
        // A decade bound avoids evaluating a logarithm for ordinary integer powers.
        val decade = magnitude.precision().toLong() - magnitude.scale() - 1
        val low = minOf(decade * n, (decade + 1) * n)
        val high = maxOf(decade * n, (decade + 1) * n)
        if (base.exact && low > 10_050) throw CalcException(CalcError.OVERFLOW)
        if (base.exact && high < -10_050) return underflow()
        if (magnitude.signum() != 0 && (low < -10_050 || high > 10_050)) {
            val work = MathContext(24)
            val absoluteBase = if (base.v.signum() < 0) base.negate() else base
            val logarithm = DecimalLogarithm.natural(absoluteBase, work, checkpoint)
            val estimate = logarithm.divide((context.constants ?: DecimalConstants(checkpoint)).lnTen(work), work).multiply(Value(Decimal(n)), work)
            val radius = estimate.error ?: throw DomainNeedsPrecision()
            if (estimate.v.subtract(radius) > Decimal(10_050)) throw CalcException(CalcError.OVERFLOW)
            if (estimate.v.add(radius) < Decimal(-10_050)) return underflow()
            if (estimate.v.abs() > Decimal(10_050)) throw DomainNeedsPrecision()
        }
        val work = context.withPrecision(context.precision + 12)
        val one = Value(Decimal.ONE, symbolic = null)
        var factor = Value(base.v, error = base.error, symbolic = null)
        var result = one
        var remaining = abs(n)
        while (remaining != 0) {
            checkpoint()
            if (remaining and 1 == 1) result = result.multiply(factor, work)
            remaining = remaining shr 1
            if (remaining != 0) factor = factor.multiply(factor, work)
        }
        if (n < 0) result = one.divide(result, work)
        return roundedValue(result.v, result.error, context).copy(retryable = base.retryable)
    }

    private fun underflow() = Value(Decimal.ZERO, error = Decimal("1E-10000"), retryable = false)

    internal fun exponential(input: Value, context: MathContext, checkCancelled: () -> Unit): Value {
        if (input.exact && input.v.signum() == 0) return Value(Decimal.ONE)
        val half = Decimal("0.5")
        val uncertainty = input.error
        if (uncertainty != null && uncertainty > half) throw DomainNeedsPrecision()
        if (input.v > Decimal(23_030)) throw CalcException(CalcError.OVERFLOW)
        if (input.v < Decimal(-23_030)) {
            val radius = input.error
            if (radius != null && input.v.add(radius) <= Decimal(-23_030)) {
                return underflow()
            }
            throw DomainNeedsPrecision()
        }

        // Evaluate a positive reduced argument, then square back and invert for negative inputs.
        val work = context.withPrecision(context.precision + 12)
        var argument = input.v.abs()
        var squarings = 0
        while (argument > half) {
            argument = argument.multiply(half)
            squarings++
        }
        val scale = work.precision + 8
        val unit = BigInteger.TEN.pow(scale)
        val x = if (argument.precision() - argument.scale() <= -scale) BigInteger.ZERO
            else argument.setScale(scale, RoundingMode.DOWN).unscaled
        var term = unit
        var sum = unit
        var index = 1
        while (true) {
            checkCancelled()
            term = term * x / unit / index
            if (term.isZero()) break
            sum += term
            index++
        }
        // Each recurrence truncates by < 1 unit, and the term ratio is at most
        // 1/2. Each stored term is within 2 units. The tail is below 4 units,
        // and truncating x changes e^x by less than another 2.
        var total = Value(sum.toDecimal(scale),
            error = Decimal(2 * index + 6).scaleByPowerOfTen(-scale), symbolic = null)
        repeat(squarings) {
            checkCancelled()
            total = total.multiply(total, work)
        }
        if (input.v.signum() < 0) total = Value(Decimal.ONE, symbolic = null).divide(total, work)
        val radius = if (uncertainty != null) {
            // e^r < 2 for r <= 1/2, so the derivative is at most twice the centre's upper bound.
            val upper = DecimalError.add(total.v.abs(), total.error)
            DecimalError.add(total.error, upper?.multiply(uncertainty)?.multiply(Decimal(2)))
        } else null
        return roundedValue(total.v, radius, context)
    }

    internal fun sqrt(x: Decimal, context: MathContext = MC): Decimal {
        if (x.signum() < 0) throw CalcException(CalcError.DOMAIN)
        return positiveRoot(x, 2, context)
    }

    internal fun cbrt(x: Decimal, context: MathContext): Decimal {
        val root = positiveRoot(x.abs(), 3, context)
        return if (x.signum() < 0) root.negate() else root
    }

    private fun positiveRoot(x: Decimal, degree: Int, context: MathContext): Decimal {
        if (x.signum() == 0) return Decimal.ZERO
        val magnitude = x.precision() - x.scale() - 1
        val divisor = Decimal(degree)
        val weight = Decimal(degree - 1)
        var guess = Decimal.ONE.scaleByPowerOfTen(magnitude.floorDiv(degree) + 1)
        fun step(current: Decimal, radicand: Decimal, work: MathContext): Decimal {
            context.constants?.check()
            val denominator = if (degree == 2) current else current.multiply(current, work)
            val correction = radicand.divide(denominator, work)
            return current.multiply(weight, work).add(correction, work).divide(divisor, work)
        }

        val target = context.precision + 6
        var digits = minOf(20, target)
        val seedContext = context.withPrecision(digits)
        val seedRadicand = x.round(seedContext)
        for (iteration in 0 until 24) {
            val next = step(guess, seedRadicand, seedContext)
            if (next == guess) break
            guess = next
        }
        while (digits < target) {
            // Keep a few digits below the precision Newton's quadratic convergence provides.
            digits = minOf(2 * digits - 4, target)
            val work = context.withPrecision(digits)
            guess = step(guess, x.round(work), work)
        }
        val work = context.withPrecision(target)
        return step(guess, x.round(work), work).round(context)
    }

    private fun factorial(x: Decimal, checkCancelled: () -> Unit, context: MathContext): Value {
        val stripped = x.stripTrailingZeros()
        if (x.signum() < 0 || stripped.scale() > 0) throw CalcException(CalcError.DOMAIN)
        if (x > Decimal(3000)) throw CalcException(CalcError.OVERFLOW)
        val last = x.toInt()
        var exact = BigInteger.ONE
        var next = 2
        while (next <= last) {
            checkCancelled()
            val product = exact * next
            if (product.bitLength() > SymbolicNumber.MAX_BITS) break
            exact = product
            next++
        }
        if (next > last) return Value(Decimal(exact))

        val work = context.withPrecision(context.precision + 12)
        var result = roundedValue(Decimal(exact), Decimal.ZERO, work)
        for (i in next..last) {
            checkCancelled()
            result = result.multiply(Value(Decimal(i), symbolic = null), work)
        }
        return roundedValue(result.v, result.error, context)
    }
}
