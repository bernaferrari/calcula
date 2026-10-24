package com.bernaferrari.calculator.domain.programmer

import kotlinx.serialization.Serializable

@Serializable
enum class Base(val radix: Int) { HEX(16), DEC(10), OCT(8), BIN(2) }

/** Integer arithmetic for programmers: two's complement in a chosen word size. */
@Serializable
enum class WordSize(val bits: Int) { QWORD(64), DWORD(32), WORD(16), BYTE(8) }

enum class ProgKey(val text: String) {
    D0("0"), D1("1"), D2("2"), D3("3"), D4("4"), D5("5"), D6("6"), D7("7"), D8("8"), D9("9"),
    A("A"), B("B"), C("C"), D("D"), E("E"), F("F"),
    PLUS("+"), MINUS("−"), TIMES("×"), DIVIDE("÷"), MOD("mod"),
    AND("AND"), OR("OR"), XOR("XOR"), NOT("NOT"), SHL("<<"), SHR(">>"),
    LPAREN("("), RPAREN(")"), NEGATE("±"),
    CLEAR("C"), DELETE("⌫"), EQUALS("=");

    val digit: Int? get() = text.singleOrNull()?.digitToIntOrNull(16)?.takeIf { ordinal <= F.ordinal }
}

enum class ProgError { DIVIDE_BY_ZERO, SYNTAX }

/**
 * An expression as tokens: numbers (digits in [base]) and operators. Simpler than the main
 * calculator's text editing, and enough for programmer work: type, fix the end, evaluate.
 */
data class ProgState(
    val tokens: List<String> = emptyList(),
    val base: Base = Base.DEC,
    val word: WordSize = WordSize.QWORD,
    val resultShown: Boolean = false,
    val error: ProgError? = null,
)

object Programmer {
    private val BINARY = setOf("+", "−", "×", "÷", "mod", "AND", "OR", "XOR", "<<", ">>")
    private val PREFIX = setOf("NOT", "−u")

    fun press(state: ProgState, key: ProgKey): ProgState {
        val s = state.copy(error = null)
        val digit = key.digit
        if (digit != null) {
            if (digit >= s.base.radix) return s
            val tokens = if (s.resultShown) emptyList() else s.tokens
            val last = tokens.lastOrNull()
            return if (last != null && isNumber(last)) {
                val next = if (last == "0") key.text else last + key.text
                if (parse(next, s.base, s.word) == null) s else s.copy(tokens = tokens.dropLast(1) + next, resultShown = false)
            } else if (last == ")") s
            else s.copy(tokens = tokens + key.text, resultShown = false)
        }
        val last = s.tokens.lastOrNull()
        val operand = last != null && (isNumber(last) || last == ")")
        return when (key) {
            ProgKey.CLEAR -> s.copy(tokens = emptyList(), resultShown = false)
            ProgKey.DELETE -> when {
                s.resultShown || last == null -> s.copy(tokens = emptyList(), resultShown = false)
                isNumber(last) && last.length > 1 -> s.copy(tokens = s.tokens.dropLast(1) + last.dropLast(1))
                else -> s.copy(tokens = s.tokens.dropLast(1))
            }
            ProgKey.EQUALS -> {
                if (s.tokens.isEmpty() || s.resultShown) return s
                when (val r = evaluate(close(s.tokens), s.base, s.word)) {
                    is Result.Value -> s.copy(tokens = tokensFor(r.value, s.base, s.word), resultShown = true)
                    is Result.Failed -> s.copy(error = r.error)
                }
            }
            ProgKey.NEGATE -> when {
                // Negates the number being typed, or the whole result.
                last != null && isNumber(last) -> {
                    val value = parse(last, s.base, s.word) ?: return s
                    val before = s.tokens.dropLast(1)
                    if (before.lastOrNull() == "−u") s.copy(tokens = before.dropLast(1) + last)
                    else if (s.base == Base.DEC) s.copy(tokens = before + "−u" + last, resultShown = false)
                    else s.copy(tokens = before + format(wrap(-value, s.word), s.base, s.word), resultShown = false)
                }
                else -> s
            }
            ProgKey.NOT, ProgKey.LPAREN -> {
                val tokens = if (s.resultShown) s.tokens else s.tokens
                if (operand) s.copy(tokens = tokens + "×" + key.text, resultShown = false)
                else s.copy(tokens = tokens + key.text, resultShown = false)
            }
            ProgKey.RPAREN -> if (operand && depth(s.tokens) > 0) s.copy(tokens = s.tokens + ")") else s
            else -> {
                val op = key.text
                when {
                    operand -> s.copy(tokens = s.tokens + op, resultShown = false)
                    last != null && last in BINARY -> s.copy(tokens = s.tokens.dropLast(1) + op)
                    // A minus with nothing before it starts a negative number.
                    op == "−" && (last == null || last == "(" || last in PREFIX) -> s.copy(tokens = s.tokens + "−u")
                    else -> s
                }
            }
        }
    }

    /** Moves to another base: every number keeps its value and is rewritten in the new one. */
    fun withBase(state: ProgState, base: Base): ProgState = state.copy(
        base = base,
        tokens = state.tokens.flatMapIndexed { i, t ->
            if (!isNumber(t)) return@flatMapIndexed listOf(t)
            var v = parse(t, state.base, state.word) ?: 0L
            // A negative typed in decimal can't keep its minus in hex: fold it into the number.
            val negated = state.tokens.getOrNull(i - 1) == "−u"
            if (negated && base != Base.DEC) v = wrap(-v, state.word)
            tokensFor(v, base, state.word)
        }.let { tokens ->
            if (base == Base.DEC) tokens
            else tokens.filterIndexed { i, t -> !(t == "−u" && tokens.getOrNull(i + 1)?.let(::isNumber) == true) }
        },
        error = null,
    )

    /** Changes the word size: numbers are cut down to fit, as the hardware would. */
    fun withWord(state: ProgState, word: WordSize): ProgState = state.copy(
        word = word,
        tokens = state.tokens.flatMap { t -> if (isNumber(t)) tokensFor(wrap(parse(t, state.base, state.word) ?: 0L, word), state.base, word) else listOf(t) },
        error = null,
    )

    /** The value so far, for the base readouts: ignores a dangling operator and open parentheses. */
    fun value(state: ProgState): Long? {
        var tokens = state.tokens
        while (tokens.isNotEmpty() && (tokens.last() in BINARY || tokens.last() in PREFIX || tokens.last() == "(")) tokens = tokens.dropLast(1)
        if (tokens.isEmpty()) return null
        return (evaluate(close(tokens), state.base, state.word) as? Result.Value)?.value
    }

    /** How the expression reads: operators spaced out, a minus hugging its number. */
    fun display(state: ProgState): String = buildString {
        state.tokens.forEachIndexed { i, t ->
            when {
                t == "−u" -> { if (i > 0) append(' '); append('−') }
                t == "(" -> { if (i > 0 && state.tokens[i - 1] != "−u" && state.tokens[i - 1] != "(") append(' '); append('(') }
                t == ")" -> append(')')
                isNumber(t) -> {
                    val prev = state.tokens.getOrNull(i - 1)
                    if (i > 0 && prev != "−u" && prev != "(") append(' ')
                    append(group(t, state.base))
                }
                else -> { if (i > 0) append(' '); append(t) }
            }
        }
    }

    /** A value as tokens: a negative decimal is a minus and its digits. */
    private fun tokensFor(value: Long, base: Base, word: WordSize): List<String> {
        val text = format(value, base, word)
        return if (text.startsWith("-")) listOf("−u", text.drop(1)) else listOf(text)
    }

    /** The number in [base]: signed in decimal, the raw bits in the others. */
    fun format(value: Long, base: Base, word: WordSize): String {
        if (base == Base.DEC) return value.toString()
        val mask = if (word.bits == 64) -1L else (1L shl word.bits) - 1
        return (value and mask).toULong().toString(base.radix).uppercase()
    }

    /** Readable digits: hex and binary in groups of four, decimal in thousands. */
    fun group(text: String, base: Base): String {
        val negative = text.startsWith("-")
        val digits = text.removePrefix("-")
        val size = if (base == Base.DEC || base == Base.OCT) 3 else 4
        val grouped = digits.reversed().chunked(size).joinToString(" ").reversed()
        return (if (negative) "−" else "") + grouped
    }

    fun isNumber(t: String) = t.isNotEmpty() && t.all { it.digitToIntOrNull(16) != null }

    /** Reads digits in [base] as an unsigned word, or null when they don't fit in it. */
    fun parse(text: String, base: Base, word: WordSize): Long? {
        val v = text.toULongOrNull(base.radix) ?: return null
        if (word.bits < 64 && v > ((1uL shl word.bits) - 1uL)) return null
        return wrap(v.toLong(), word)
    }

    /** Two's complement in the word: the top bit is the sign. */
    fun wrap(v: Long, word: WordSize): Long {
        if (word.bits == 64) return v
        val mask = (1L shl word.bits) - 1
        val x = v and mask
        return if (x and (1L shl (word.bits - 1)) != 0L) x or mask.inv() else x
    }

    private fun depth(tokens: List<String>) = tokens.count { it == "(" } - tokens.count { it == ")" }

    private fun close(tokens: List<String>) = tokens + List(depth(tokens).coerceAtLeast(0)) { ")" }

    private sealed interface Result {
        data class Value(val value: Long) : Result
        data class Failed(val error: ProgError) : Result
    }

    private class Fail(val error: ProgError) : Exception()

    private fun evaluate(tokens: List<String>, base: Base, word: WordSize): Result = try {
        val p = Parser(tokens, base, word)
        val v = p.or()
        if (p.pos != tokens.size) throw Fail(ProgError.SYNTAX)
        Result.Value(v)
    } catch (f: Fail) {
        Result.Failed(f.error)
    }

    /** C's precedence: OR < XOR < AND < shifts < + − < × ÷ mod < NOT and minus. */
    private class Parser(val tokens: List<String>, val base: Base, val word: WordSize) {
        var pos = 0
        fun peek() = tokens.getOrNull(pos)
        fun w(v: Long) = wrap(v, word)

        fun or(): Long { var v = xor(); while (peek() == "OR") { pos++; v = w(v or xor()) }; return v }
        fun xor(): Long { var v = and(); while (peek() == "XOR") { pos++; v = w(v xor and()) }; return v }
        fun and(): Long { var v = shift(); while (peek() == "AND") { pos++; v = w(v and shift()) }; return v }
        fun shift(): Long {
            var v = add()
            while (peek() == "<<" || peek() == ">>") {
                val op = tokens[pos++]
                val n = add().toInt().coerceIn(0, 63)
                v = w(if (op == "<<") v shl n else v shr n)
            }
            return v
        }
        fun add(): Long {
            var v = mul()
            while (peek() == "+" || peek() == "−") {
                val op = tokens[pos++]
                v = w(if (op == "+") v + mul() else v - mul())
            }
            return v
        }
        fun mul(): Long {
            var v = unary()
            while (peek() == "×" || peek() == "÷" || peek() == "mod") {
                val op = tokens[pos++]
                val r = unary()
                if (op != "×" && r == 0L) throw Fail(ProgError.DIVIDE_BY_ZERO)
                v = w(when (op) { "×" -> v * r; "÷" -> v / r; else -> v % r })
            }
            return v
        }
        fun unary(): Long = when (peek()) {
            "NOT" -> { pos++; w(unary().inv()) }
            "−u" -> { pos++; w(-unary()) }
            else -> primary()
        }
        fun primary(): Long {
            val t = tokens.getOrNull(pos++) ?: throw Fail(ProgError.SYNTAX)
            return when {
                t == "(" -> or().also { if (tokens.getOrNull(pos++) != ")") throw Fail(ProgError.SYNTAX) }
                isNumber(t) -> parse(t, base, word) ?: throw Fail(ProgError.SYNTAX)
                else -> throw Fail(ProgError.SYNTAX)
            }
        }
    }
}
