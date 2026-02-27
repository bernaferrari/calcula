package jscl.math.function

import com.ionspin.kotlin.bignum.integer.BigInteger
import jscl.math.Generic
import jscl.math.JsclInteger
import jscl.math.NotIntegrableException
import jscl.math.NotIntegerException
import jscl.math.Variable
import kotlinx.atomicfu.atomic

object BitwiseRuntimeConfig {
    private const val DEFAULT_WORD_SIZE = 64
    private val wordSizeBits = atomic(DEFAULT_WORD_SIZE)
    private val signedMode = atomic(true)

    data class Snapshot(
        val wordSize: Int,
        val signed: Boolean
    )

    fun snapshot(): Snapshot = Snapshot(
        wordSize = wordSizeBits.value.coerceIn(1, Long.SIZE_BITS),
        signed = signedMode.value
    )

    fun update(wordSize: Int, signed: Boolean) {
        wordSizeBits.value = wordSize.coerceIn(1, Long.SIZE_BITS)
        signedMode.value = signed
    }
}

internal object BitwiseFunctionSupport {
    fun unary(
        parameters: Array<Generic>?,
        operation: (Long, BitwiseRuntimeConfig.Snapshot) -> Long
    ): Generic? {
        val config = BitwiseRuntimeConfig.snapshot()
        val value = parameters?.getOrNull(0)?.asWordBitsOrNull(config) ?: return null
        val result = operation(value, config)
        return bitsToGeneric(result, config)
    }

    fun binary(
        parameters: Array<Generic>?,
        operation: (Long, Long, BitwiseRuntimeConfig.Snapshot) -> Long
    ): Generic? {
        val config = BitwiseRuntimeConfig.snapshot()
        val left = parameters?.getOrNull(0)?.asWordBitsOrNull(config) ?: return null
        val right = parameters.getOrNull(1)?.asWordBitsOrNull(config) ?: return null
        val result = operation(left, right, config)
        return bitsToGeneric(result, config)
    }

    private fun Generic.asWordBitsOrNull(config: BitwiseRuntimeConfig.Snapshot): Long? {
        return try {
            val value = integerValue().content()
            val reduced = value.mod(twoPow(config.wordSize))
            reduced.toString().toULongOrNull()?.toLong()
        } catch (_: NotIntegerException) {
            null
        }
    }

    private fun bitsToGeneric(bits: Long, config: BitwiseRuntimeConfig.Snapshot): Generic {
        val normalized = normalizeBits(bits, config.wordSize)
        return if (config.signed) {
            JsclInteger.valueOf(signExtend(normalized, config.wordSize))
        } else {
            JsclInteger.valueOf(normalized.toULong().toString())
        }
    }

    private fun normalizeBits(value: Long, wordSize: Int): Long {
        return if (wordSize >= Long.SIZE_BITS) {
            value
        } else {
            value and ((1L shl wordSize) - 1L)
        }
    }

    private fun signExtend(bits: Long, wordSize: Int): Long {
        if (wordSize >= Long.SIZE_BITS) return bits
        val signBit = 1L shl (wordSize - 1)
        return if ((bits and signBit) != 0L) {
            bits or (-1L shl wordSize)
        } else {
            bits
        }
    }

    private fun shiftDistance(raw: Long): Int = (raw and 0x3F).toInt()

    private val two = BigInteger.fromInt(2)

    private fun twoPow(bits: Int): BigInteger = two.pow(bits)

    fun and(left: Long, right: Long, config: BitwiseRuntimeConfig.Snapshot): Long {
        return normalizeBits(left and right, config.wordSize)
    }

    fun or(left: Long, right: Long, config: BitwiseRuntimeConfig.Snapshot): Long {
        return normalizeBits(left or right, config.wordSize)
    }

    fun xor(left: Long, right: Long, config: BitwiseRuntimeConfig.Snapshot): Long {
        return normalizeBits(left xor right, config.wordSize)
    }

    fun not(value: Long, config: BitwiseRuntimeConfig.Snapshot): Long {
        return normalizeBits(value.inv(), config.wordSize)
    }

    fun shiftLeft(value: Long, shift: Long, config: BitwiseRuntimeConfig.Snapshot): Long {
        val distance = shiftDistance(shift)
        val normalizedValue = normalizeBits(value, config.wordSize)
        return normalizeBits(normalizedValue shl distance, config.wordSize)
    }

    fun shiftRight(value: Long, shift: Long, config: BitwiseRuntimeConfig.Snapshot): Long {
        val distance = shiftDistance(shift)
        val normalizedValue = normalizeBits(value, config.wordSize)
        val shifted = if (config.signed) {
            signExtend(normalizedValue, config.wordSize) shr distance
        } else {
            normalizedValue ushr distance
        }
        return normalizeBits(shifted, config.wordSize)
    }
}

class BitAnd(first: Generic?, second: Generic?) :
    Function(NAME, if (first != null && second != null) arrayOf(first, second) else null) {

    override fun getMinParameters(): Int = 2

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic = JsclInteger.valueOf(0)

    override fun selfExpand(): Generic =
        BitwiseFunctionSupport.binary(parameters, BitwiseFunctionSupport::and) ?: expressionValue()

    override fun selfElementary(): Generic = selfExpand()

    override fun selfSimplify(): Generic = selfExpand()

    override fun selfNumeric(): Generic = selfExpand()

    override fun newInstance(): Variable = BitAnd(null, null)

    companion object {
        const val NAME = "and"
    }
}

class BitOr(first: Generic?, second: Generic?) :
    Function(NAME, if (first != null && second != null) arrayOf(first, second) else null) {

    override fun getMinParameters(): Int = 2

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic = JsclInteger.valueOf(0)

    override fun selfExpand(): Generic =
        BitwiseFunctionSupport.binary(parameters, BitwiseFunctionSupport::or) ?: expressionValue()

    override fun selfElementary(): Generic = selfExpand()

    override fun selfSimplify(): Generic = selfExpand()

    override fun selfNumeric(): Generic = selfExpand()

    override fun newInstance(): Variable = BitOr(null, null)

    companion object {
        const val NAME = "or"
    }
}

class BitXor(first: Generic?, second: Generic?) :
    Function(NAME, if (first != null && second != null) arrayOf(first, second) else null) {

    override fun getMinParameters(): Int = 2

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic = JsclInteger.valueOf(0)

    override fun selfExpand(): Generic =
        BitwiseFunctionSupport.binary(parameters, BitwiseFunctionSupport::xor) ?: expressionValue()

    override fun selfElementary(): Generic = selfExpand()

    override fun selfSimplify(): Generic = selfExpand()

    override fun selfNumeric(): Generic = selfExpand()

    override fun newInstance(): Variable = BitXor(null, null)

    companion object {
        const val NAME = "xor"
    }
}

class BitNot(value: Generic?) :
    Function(NAME, if (value != null) arrayOf(value) else null) {

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic = JsclInteger.valueOf(0)

    override fun selfExpand(): Generic =
        BitwiseFunctionSupport.unary(parameters, BitwiseFunctionSupport::not) ?: expressionValue()

    override fun selfElementary(): Generic = selfExpand()

    override fun selfSimplify(): Generic = selfExpand()

    override fun selfNumeric(): Generic = selfExpand()

    override fun newInstance(): Variable = BitNot(null)

    companion object {
        const val NAME = "not"
    }
}

class BitShiftLeft(value: Generic?, shift: Generic?) :
    Function(NAME, if (value != null && shift != null) arrayOf(value, shift) else null) {

    override fun getMinParameters(): Int = 2

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic = JsclInteger.valueOf(0)

    override fun selfExpand(): Generic =
        BitwiseFunctionSupport.binary(parameters, BitwiseFunctionSupport::shiftLeft) ?: expressionValue()

    override fun selfElementary(): Generic = selfExpand()

    override fun selfSimplify(): Generic = selfExpand()

    override fun selfNumeric(): Generic = selfExpand()

    override fun newInstance(): Variable = BitShiftLeft(null, null)

    companion object {
        const val NAME = "shl"
    }
}

class BitShiftRight(value: Generic?, shift: Generic?) :
    Function(NAME, if (value != null && shift != null) arrayOf(value, shift) else null) {

    override fun getMinParameters(): Int = 2

    override fun antiDerivative(n: Int): Generic {
        throw NotIntegrableException(this)
    }

    override fun derivative(n: Int): Generic = JsclInteger.valueOf(0)

    override fun selfExpand(): Generic =
        BitwiseFunctionSupport.binary(parameters, BitwiseFunctionSupport::shiftRight) ?: expressionValue()

    override fun selfElementary(): Generic = selfExpand()

    override fun selfSimplify(): Generic = selfExpand()

    override fun selfNumeric(): Generic = selfExpand()

    override fun newInstance(): Variable = BitShiftRight(null, null)

    companion object {
        const val NAME = "shr"
    }
}
