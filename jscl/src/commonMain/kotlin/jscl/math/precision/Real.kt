package jscl.math.precision

import kotlinx.datetime.Clock
/**
 * <b>Java integer implementation of 63-bit precision floating point.</b>
 * <br><i>Version 1.13</i>
 * <p/>
 * <p>Copyright 2003-2009 Roar Lauritzsen <roarl@pvv.org>
 * <p/>
 * <blockquote>
 * <p/>
 * <p>This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * <p/>
 * <p>This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <p/>
 * <p>The following link provides a copy of the GNU General Public License:
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;<a
 * href="http://www.gnu.org/licenses/gpl.txt">http://www.gnu.org/licenses/gpl.txt</a>
 * <br>If you are unable to obtain the copy from this address, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * <p/>
 * </blockquote>
 * <p/>
 * <p><b>General notes</b>
 * <ul>
 * <p/>
 * <li><code>Real</code> objects are not immutable, like Java
 * <code>Double</code> or <code>BigDecimal</code>. This means that you
 * should not think of a <code>Real</code> object as a "number", but rather
 * as a "register holding a number". This design choice is done to encourage
 * object reuse and limit garbage production for more efficient execution on
 * e.g. a limited MIDP device. The design choice is reflected in the API,
 * where an operation like {@link #add(Real) add} does not return a new
 * object containing the result (as with {@link
 * java.math.BigDecimal#add(java.math.BigDecimal) BigDecimal}), but rather
 * adds the argument to the object itself, and returns nothing.
 * <p/>
 * <li>This library implements infinities and NaN (Not-a-Number) following
 * the IEEE 754 logic. If an operation produces a result larger (in
 * magnitude) than the largest representable number, a value representing
 * positive or negative infinity is generated. If an operation produces a
 * result smaller than the smallest representable number, a positive or
 * negative zero is generated. If an operation is undefined, a NaN value is
 * produced. Abnormal numbers are often fine to use in further
 * calculations. In most cases where the final result would be meaningful,
 * abnormal numbers accomplish this, e.g. atan(1/0)=&pi;/2. In most cases
 * where the final result is not meaningful, a NaN will be produced.
 * <i>No exception is ever (deliberately) thrown.</i>
 * <p/>
 * <li>Error bounds listed under <a href="#method_detail">Method Detail</a>
 * are calculated using William Rossi's <a
 * href="http://dfp.sourceforge.net/">rossi.dfp.dfp</a> at 40 decimal digits
 * accuracy. Error bounds are for "typical arguments" and may increase when
 * results approach zero or
 * infinity. The abbreviation {@link Math#ulp(Double) ULP} means Unit in the
 * Last Place. An error bound of œ ULP means that the result is correctly
 * rounded. The relative execution time listed under each method is the
 * average from running on SonyEricsson T610 (R3C), K700i, and Nokia 6230i.
 * <p/>
 * <li>The library is not thread-safe. Static <code>Real</code> objects are
 * used extensively as temporary values to avoid garbage production and the
 * overhead of <code>new</code>. To make the library thread-safe, references
 * to all these static objects must be replaced with code that instead
 * allocates new <code>Real</code> objects in their place.
 * <p/>
 * <li>There is one bug that occurs again and again and is really difficult
 * to debug. Although the pre-calculated constants are declared <code>static
 * final</code>, Java cannot really protect the contents of the objects in
 * the same way as <code>const</code>s are protected in C/C++. Consequently,
 * you can accidentally change these values if you send them into a function
 * that modifies its arguments. If you were to modify {@link #ONE Real.ONE}
 * for instance, many of the succeeding calculations would be wrong because
 * the same variable is used extensively in the internal calculations of
 * Real.java.
 * <p/>
 * </ul>
 */
@Suppress("unused", "StatementWithEmptyBody")
class Real {
    companion object {
        val ZERO = Real(0, 0x00000000, 0x0000000000000000L)
        val ONE = Real(0, 0x40000000, 0x4000000000000000L)
        val TWO = Real(0, 0x40000001, 0x4000000000000000L)
        val THREE = Real(0, 0x40000001, 0x6000000000000000L)
        val FIVE = Real(0, 0x40000002, 0x5000000000000000L)
        val TEN = Real(0, 0x40000003, 0x5000000000000000L)
        val HUNDRED = Real(0, 0x40000006, 0x6400000000000000L)
        val HALF = Real(0, 0x3fffffff, 0x4000000000000000L)
        val THIRD = Real(0, 0x3ffffffe, 0x5555555555555555L)
        val TENTH = Real(0, 0x3ffffffc, 0x6666666666666666L)
        val PERCENT = Real(0, 0x3ffffff9, 0x51eb851eb851eb85L)
        val SQRT2 = Real(0, 0x40000000, 0x5a827999fcef3242L)
        val SQRT1_2 = Real(0, 0x3fffffff, 0x5a827999fcef3242L)
        val PI2 = Real(0, 0x40000002, 0x6487ed5110b4611aL)
        val PI = Real(0, 0x40000001, 0x6487ed5110b4611aL)
        val PI_2 = Real(0, 0x40000000, 0x6487ed5110b4611aL)
        val PI_4 = Real(0, 0x3fffffff, 0x6487ed5110b4611aL)
        val PI_8 = Real(0, 0x3ffffffe, 0x6487ed5110b4611aL)
        val E = Real(0, 0x40000001, 0x56fc2a2c515da54dL)
        val LN2 = Real(0, 0x3fffffff, 0x58b90bfbe8e7bcd6L)
        val LN10 = Real(0, 0x40000001, 0x49aec6eed554560bL)
        val LOG2E = Real(0, 0x40000000, 0x5c551d94ae0bf85eL)
        val LOG10E = Real(0, 0x3ffffffe, 0x6f2dec549b9438cbL)
        val MAX = Real(0, 0x7fffffff, 0x7fffffffffffffffL)
        val MIN = Real(0, 0x00000000, 0x4000000000000000L)
        val NAN = Real(0, Int.MIN_VALUE, 0x4000000000000000L)
        val INF = Real(0, Int.MIN_VALUE, 0x0000000000000000L)
        val INF_N = Real(1, Int.MIN_VALUE, 0x0000000000000000L)
        val ZERO_N = Real(1, 0x00000000, 0x0000000000000000L)
        val ONE_N = Real(1, 0x40000000, 0x4000000000000000L)

        const val hexChar = "0123456789ABCDEF"
    /**
     * A <code>Real</code> constant holding the exact value of 0.  Among other
     * uses, this value is used as a result when a positive underflow occurs.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 1.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 2.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 3.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 5.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 10.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 100.
     */
    /**
     * A <code>Real</code> constant holding the exact value of 1/2.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to 1/3.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to 1/10.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to 1/100.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to the
     * square root of 2.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to the
     * square root of 1/2.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to 2&pi;.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to &pi;, the
     * ratio of the circumference of a circle to its diameter.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to &pi;/2.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to &pi;/4.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to &pi;/8.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to <i>e</i>,
     * the base of the natural logarithms.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to the
     * natural logarithm of 2.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to the
     * natural logarithm of 10.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to the
     * base-2 logarithm of <i>e</i>.
     */
    /**
     * A <code>Real</code> constant that is closer than any other to the
     * base-10 logarithm of <i>e</i>.
     */
    /**
     * A <code>Real</code> constant holding the maximum non-infinite positive
     * number = 4.197e323228496.
     */
    /**
     * A <code>Real</code> constant holding the minimum non-zero positive
     * number = 2.383e-323228497.
     */
    /**
     * A <code>Real</code> constant holding the value of NaN (not-a-number).
     * This value is always used as a result to signal an invalid operation.
     */
    /**
     * A <code>Real</code> constant holding the value of positive infinity.
     * This value is always used as a result to signal a positive overflow.
     */
    /**
     * A <code>Real</code> constant holding the value of negative infinity.
     * This value is always used as a result to signal a negative overflow.
     */
    /**
     * A <code>Real</code> constant holding the value of negative zero. This
     * value is used as a result e.g. when a negative underflow occurs.
     */
    /**
     * A <code>Real</code> constant holding the exact value of -1.
     */
    /**
     * This string holds the only valid characters to use in hexadecimal
     * numbers. Equals <code>"0123456789ABCDEF"</code>.
     * See {@link #assign(String, Int)}.
     */
        private const val clz_magic = 0x7c4acdd
        private val clz_tab = byteArrayOf(
            31.toByte(), 22.toByte(), 30.toByte(), 21.toByte(), 18.toByte(), 10.toByte(),
            29.toByte(), 2.toByte(), 20.toByte(), 17.toByte(), 15.toByte(), 13.toByte(),
            9.toByte(), 6.toByte(), 28.toByte(), 1.toByte(), 23.toByte(), 19.toByte(),
            11.toByte(), 3.toByte(), 16.toByte(), 14.toByte(), 7.toByte(), 24.toByte(),
            12.toByte(), 4.toByte(), 8.toByte(), 25.toByte(), 5.toByte(), 26.toByte(),
            27.toByte(), 0.toByte()
        )
        var magicRounding: Boolean = true
        var randSeedA: Long = 0x6487ed5110b4611aL
        var randSeedB: Long = 0x56fc2a2c515da54dL
    /**
     * Set to <code>false</code> during numerical algorithms to favor accuracy
     * over prettyness. This flag is initially set to <code>true</code>.
     * <p/>
     * <p>The flag controls the operation of a subtraction of two
     * almost-identical numbers that differ only in the last three bits of the
     * mantissa. With this flag enabled, the result of such a subtraction is
     * rounded down to zero. Probabilistically, this is the correct course of
     * action in an overwhelmingly large percentage of calculations.
     * However, certain numerical algorithms such as differentiation depend
     * on keeping maximum accuracy during subtraction.
     * <p/>
     * <p>Note, that because of <code>magicRounding</code>,
     * <code>a.sub(b)</code> may produce zero even though
     * <code>a.equalTo(b)</code> returns <code>false</code>. This must be
     * considered e.g. when trying to avoid division by zero.
     */
    /**
     * The seed of the first 64-bit CRC generator of the random
     * routine. Set this value to control the generated sequence of random
     * numbers. Should never be set to 0. See {@link #random()}.
     * Initialized to mantissa of pi.
     */
    /**
     * The seed of the second 64-bit CRC generator of the random
     * routine. Set this value to control the generated sequence of random
     * numbers. Should never be set to 0. See {@link #random()}.
     * Initialized to mantissa of e.
     */
    }
    private val digits = ByteArray(65)
    private val buf = StringBuilder(40)
    private val exp = StringBuilder(15)
    /**
     * The mantissa of a <code>Real</code>. <i>To maintain numbers in a
     * normalized state and to preserve the integrity of abnormal numbers, it
     * is discouraged to modify the inner representation of a
     * <code>Real</code> directly.</i>
     * <p/>
     * <p>The number represented by a <code>Real</code> equals:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-1<sup>sign</sup>&nbsp;·&nbsp;mantissa&nbsp;·&nbsp;2<sup>-62</sup>&nbsp;·&nbsp;2<sup>exponent-0x40000000</sup>
     * <p/>
     * <p>The normalized mantissa of a finite <code>Real</code> must be
     * between <code>0x4000000000000000L</code> and
     * <code>0x7fffffffffffffffL</code>. Using a denormalized
     * <code>Real</code> in <u>any</u> operation other than {@link
     * #normalize()} may produce undefined results. The mantissa of zero and
     * of an infinite value is <code>0x0000000000000000L</code>.
     * <p/>
     * <p>The mantissa of a NaN is any nonzero value. However, it is
     * recommended to use the value <code>0x4000000000000000L</code>. Any
     * other values are reserved for future extensions.
     */
    /**
     * The exponent of a <code>Real</code>. <i>To maintain numbers in a
     * normalized state and to preserve the integrity of abnormal numbers, it
     * is discouraged to modify the inner representation of a
     * <code>Real</code> directly.</i>
     * <p/>
     * <p>The exponent of a finite <code>Real</code> must be between
     * <code>0x00000000</code> and <code>0x7fffffff</code>. The exponent of
     * zero <code>0x00000000</code>.
     * <p/>
     * <p>The exponent of an infinite value and of a NaN is any negative
     * value. However, it is recommended to use the value
     * <code>0x80000000</code>. Any other values are reserved for future
     * extensions.
     */
    /**
     * The sign of a <code>Real</code>. <i>To maintain numbers in a normalized
     * state and to preserve the integrity of abnormal numbers, it is
     * discouraged to modify the inner representation of a <code>Real</code>
     * directly.</i>
     * <p/>
     * <p>The sign of a finite, zero or infinite <code>Real</code> is 0 for
     * positive values and 1 for negative values. Any other values may produce
     * undefined results.
     * <p/>
     * <p>The sign of a NaN is ignored. However, it is recommended to use the
     * value <code>0</code>. Any other values are reserved for future
     * extensions.
     */
    var mantissa: Long = 0
    var exponent: Int = 0
    var sign: Byte = 0
    private var tmp0: Real? = null
    private var tmp1: Real? = null
    private var tmp2: Real? = null
    private var tmp3: Real? = null
    private var tmp4: Real? = null
    private var tmp5: Real? = null
    private var recipTmp: Real? = null
    private var recipTmp2: Real? = null
    private var sqrtTmp: Real? = null
    private var expTmp: Real? = null
    private var expTmp2: Real? = null
    private var expTmp3: Real? = null
    /**
     * Creates a new <code>Real</code> with a value of zero.
     */
    constructor() {
    }
    /**
     * Creates a new <code>Real</code>, assigning the value of another
     * <code>Real</code>. See {@link #assign(Real)}.
     *
     * @param a the <code>Real</code> to assign.
     */
    constructor(a: Real) {
        this.mantissa = a.mantissa
        this.exponent = a.exponent
        this.sign = a.sign
    }
    /**
     * Creates a new <code>Real</code>, assigning the value of an integer. See
     * {@link #assign(Int)}.
     *
     * @param a the <code>Int</code> to assign.
     */
    constructor(a: Int) {
        assign(a)
    }
    /**
     * Creates a new <code>Real</code>, assigning the value of a Long
     * integer. See {@link #assign(Long)}.
     *
     * @param a the <code>Long</code> to assign.
     */
    constructor(a: Long) {
        assign(a)
    }
    /**
     * Creates a new <code>Real</code>, assigning the value encoded in a
     * <code>String</code> using base-10. See {@link #assign(String)}.
     *
     * @param a the <code>String</code> to assign.
     */
    constructor(a: String) {
        assign(a, 10)
    }
    /**
     * Creates a new <code>Real</code>, assigning the value encoded in a
     * <code>String</code> using the specified number base. See {@link
     * #assign(String, Int)}.
     *
     * @param a    the <code>String</code> to assign.
     * @param base the number base of <code>a</code>. Valid base values are 2,
     *             8, 10 and 16.
     */
    constructor(a: String, base: Int) {
        assign(a, base)
    }
    /**
     * Creates a new <code>Real</code>, assigning a value by directly setting
     * the fields of the internal representation. The arguments must represent
     * a valid, normalized <code>Real</code>. This is the fastest way of
     * creating a constant value.  See {@link #assign(Int, Int, Long)}.
     *
     * @param s {@link #sign} bit, 0 for positive sign, 1 for negative sign
     * @param e {@link #exponent}
     * @param m {@link #mantissa}
     */
    constructor(s: Int, e: Int, m: Long) {
        this.sign = s.toByte()
        this.exponent = e
        this.mantissa = m
    }
    /**
     * Creates a new <code>Real</code>, assigning the value previously encoded
     * into twelve consecutive bytes in a Byte array using {@link
     * #toBytes(ByteArray, Int) toBytes}. See {@link #assign(ByteArray, Int)}.
     *
     * @param data   Byte array to decode into this <code>Real</code>.
     * @param offset offset to start encoding from. The bytes
     *               <code>data[offset]...data[offset+11]</code> will be
     *               read.
     */
    constructor(data: ByteArray, offset: Int) {
        assign(data, offset)
    }
    fun ldiv(a: Long, b: Long): Long {
        // Calculate (a<<63)/b, where a<2**64, b<2**63, b<=a and a<2*b The
        // result will always be 63 bits, leading to a 3-stage radix-2**21
        // (very high radix) algorithm, as described here:
        // S.F. Oberman and M.J. Flynn, "Division Algorithms and
        // Implementations," IEEE Trans. Computers, vol. 46, no. 8,
        // pp. 833-854, Aug 1997 Section 4: "Very High Radix Algorithms"
        var aVar = a
        var bInv24: Int // Approximate 1/b, never more than 24 bits
        var aHi24: Int // High 24 bits of a (sometimes 25 bits)
        var next21: Int // The next 21 bits of result, possibly 1 less
        var q: Long // Resulting quotient: round((a<<63)/b)
        // Preparations
        bInv24 = (0x400000000000L / ((b ushr 40) + 1)).toInt()
        aHi24 = ((aVar shr 32).toInt() ushr 8)
        aVar = aVar shl 20 // aHi24 and a overlap by 4 bits
        // Now perform the division
        next21 = ((aHi24.toLong() * bInv24.toLong()) ushr 26).toInt()
        aVar -= next21.toLong() * b // Bits above 2**64 will always be cancelled
        // No need to remove remainder, this will be cared for in next block
        q = next21.toLong()
        aHi24 = ((aVar shr 32).toInt() ushr 7)
        aVar = aVar shl 21
        // Two more almost identical blocks...
        next21 = ((aHi24.toLong() * bInv24.toLong()) ushr 26).toInt()
        aVar -= next21.toLong() * b
        q = (q shl 21) + next21.toLong()
        aHi24 = ((aVar shr 32).toInt() ushr 7)
        aVar = aVar shl 21
        next21 = ((aHi24.toLong() * bInv24.toLong()) ushr 26).toInt()
        aVar -= next21.toLong() * b
        q = (q shl 21) + next21.toLong()
        // Remove final remainder
        if (aVar < 0 || aVar >= b) {
            q++
            aVar -= b
        }
        aVar = aVar shl 1
        // Round correctly
        if (aVar < 0 || aVar >= b) q++
        return q
    }
    //*************************************************************************
    // Calendar conversions taken from
    // http://www.fourmilab.ch/documents/calendar/
    fun floorDiv(a: Int, b: Int): Int {
        if (a >= 0) return a / b
        return -((-a + b - 1) / b)
    }
    fun floorMod(a: Int, b: Int): Int {
        if (a >= 0) return a % b
        return a + ((-a + b - 1) / b) * b
    }
    fun leap_gregorian(year: Int): Boolean {
        return (year % 4 == 0) && !((year % 100 == 0) && (year % 400 != 0))
    }
    // GREGORIAN_TO_JD -- Determine Julian day number from Gregorian
    // calendar date -- Except that we use 1/1-0 as day 0
    fun gregorian_to_jd(year: Int, month: Int, day: Int): Int {
        val correction = if (month <= 2) 0 else if (leap_gregorian(year)) -1 else -2
        return (366 - 1) +
                (365 * (year - 1)) +
                floorDiv(year - 1, 4) +
                (-floorDiv(year - 1, 100)) +
                floorDiv(year - 1, 400) +
                ((((367 * month) - 362) / 12) + correction + day)
    }
    // JD_TO_GREGORIAN -- Calculate Gregorian calendar date from Julian
    // day -- Except that we use 1/1-0 as day 0
    fun jd_to_gregorian(jd: Int): Int {
        var wjd: Int
        var depoch: Int
        var quadricent: Int
        var dqc: Int
        var cent: Int
        var dcent: Int
        var quad: Int
        var dquad: Int
        var yindex: Int
        var year: Int
        var yearday: Int
        var leapadj: Int
        var month: Int
        var day: Int
        wjd = jd
        depoch = wjd - 366
        quadricent = floorDiv(depoch, 146097)
        dqc = floorMod(depoch, 146097)
        cent = floorDiv(dqc, 36524)
        dcent = floorMod(dqc, 36524)
        quad = floorDiv(dcent, 1461)
        dquad = floorMod(dcent, 1461)
        yindex = floorDiv(dquad, 365)
        year = (quadricent * 400) + (cent * 100) + (quad * 4) + yindex
        if (!((cent == 4) || (yindex == 4))) {
            year++
        }
        yearday = wjd - gregorian_to_jd(year, 1, 1)
        leapadj = if (wjd < gregorian_to_jd(year, 3, 1)) 0 else if (leap_gregorian(year)) 1 else 2
        month = floorDiv(((yearday + leapadj) * 12) + 373, 367)
        day = (wjd - gregorian_to_jd(year, month, 1)) + 1
        return (year * 100 + month) * 100 + day
    }
    // 64 Bit CRC Generators
    //
    // The generators used here are not cryptographically secure, but
    // two weak generators are combined into one strong generator by
    // skipping bits from one generator whenever the other generator
    // produces a 0-bit.
    fun advanceBit() {
        randSeedA = (randSeedA shl 1) xor (if (randSeedA < 0) 0x1bL else 0L)
        randSeedB = (randSeedB shl 1) xor (if (randSeedB < 0) 0xb000000000000001uL.toLong() else 0L)
    }
    // Get next bits from the pseudo-random sequence
    fun nextBits(bits: Int): Long {
        var answer: Long = 0
        var bitsLeft = bits
        while (bitsLeft-- > 0) {
            while (randSeedA >= 0) {
                advanceBit()
            }
            answer = (answer shl 1) + if (randSeedB < 0) 1 else 0
            advanceBit()
        }
        return answer
    }
    /**
     * Accumulate more randomness into the random number generator, to
     * decrease the predictability of the output from {@link
     * #random()}. The input should contain data with some form of
     * inherent randomness e.g. System.currentTimeMillis().
     *
     * @param seed some extra randomness for the random number generator.
     */
    fun accumulateRandomness(seed: Long) {
        randSeedA = randSeedA xor (seed and 0x5555555555555555L)
        randSeedB = randSeedB xor (seed and 0xaaaaaaaaaaaaaaaauL.toLong())
        nextBits(63)
    }
    private fun tmp0(): Real {
        if (tmp0 == null) tmp0 = Real()
        return tmp0!!
    }
    private fun tmp1(): Real {
        if (tmp1 == null) tmp1 = Real()
        return tmp1!!
    }
    private fun tmp2(): Real {
        if (tmp2 == null) tmp2 = Real()
        return tmp2!!
    }
    private fun tmp3(): Real {
        if (tmp3 == null) tmp3 = Real()
        return tmp3!!
    }
    private fun tmp4(): Real {
        if (tmp4 == null) tmp4 = Real()
        return tmp4!!
    }
    private fun tmp5(): Real {
        if (tmp5 == null) tmp5 = Real()
        return tmp5!!
    }
    private fun recipTmp(): Real {
        if (recipTmp == null) recipTmp = Real()
        return recipTmp!!
    }
    private fun recipTmp2(): Real {
        if (recipTmp2 == null) recipTmp2 = Real()
        return recipTmp2!!
    }
    private fun sqrtTmp(): Real {
        if (sqrtTmp == null) sqrtTmp = Real()
        return sqrtTmp!!
    }
    private fun expTmp(): Real {
        if (expTmp == null) expTmp = Real()
        return expTmp!!
    }
    private fun expTmp2(): Real {
        if (expTmp2 == null) expTmp2 = Real()
        return expTmp2!!
    }
    private fun expTmp3(): Real {
        if (expTmp3 == null) expTmp3 = Real()
        return expTmp3!!
    }
    /**
     * Assigns this <code>Real</code> the value of another <code>Real</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to assign.
     */
    fun assign(a: Real?) {
        if (a == null) {
            makeZero()
            return
        }
        sign = a.sign
        exponent = a.exponent
        mantissa = a.mantissa
    }
    /**
     * Assigns this <code>Real</code> the value of an integer.
     * All integer values can be represented without loss of accuracy.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = (Double)a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.6
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to assign.
     */
    fun assign(a: Int) {
        if (a == 0) {
            makeZero()
            return
        }
        var value = a
        sign = 0
        if (value < 0) {
            sign = 1
            value = -value // Also works for 0x80000000
        }
        // Normalize Int
        var t = value
        t = t or (t shr 1)
        t = t or (t shr 2)
        t = t or (t shr 4)
        t = t or (t shr 8)
        t = t or (t shr 16)
        t = clz_tab[((t * clz_magic) ushr 27)].toInt() - 1
        exponent = 0x4000001E - t
        mantissa = value.toLong() shl (32 + t)
    }
    /**
     * Assigns this <code>Real</code> the value of a signed Long integer.
     * All Long values can be represented without loss of accuracy.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = (Double)a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Long</code> to assign.
     */
    fun assign(a: Long) {
        var value = a
        sign = 0
        if (value < 0) {
            sign = 1
            value = -value // Also works for 0x8000000000000000
        }
        exponent = 0x4000003E
        mantissa = value
        normalize()
    }
    /**
     * Assigns this <code>Real</code> a value encoded in a <code>String</code>
     * using base-10, as specified in {@link #assign(String, Int)}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Double.{@link Double#valueOf(String) valueOf}(a);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * œ-1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 80
     * </td></tr></table>
     *
     * @param a the <code>String</code> to assign.
     */
    fun assign(a: String) {
        assign(a, 10)
    }
    /**
     * Assigns this <code>Real</code> a value encoded in a <code>String</code>
     * using the specified number base. The string is parsed as follows:
     * <p/>
     * <ul>
     * <li>If the string is <code>null</code> or an empty string, zero is
     * assigned.
     * <li>Leading spaces are ignored.
     * <li>An optional sign, '+', '-' or '/', where '/' precedes a negative
     * two's-complement number, reading: "an infinite number of 1-bits
     * preceding the number".
     * <li>Optional digits preceding the radix, in the specified base.
     * <ul>
     * <li>In base-2, allowed digits are '01'.
     * <li>In base-8, allowed digits are '01234567'.
     * <li>In base-10, allowed digits are '0123456789'.
     * <li>In base-16, allowed digits are '0123456789ABCDEF'.
     * </ul>
     * <li>An optional radix character, '.' or ','.
     * <li>Optional digits following the radix.
     * <li>The following spaces are ignored.
     * <li>An optional exponent indicator, 'e'. If not base-16, or after a
     * space, 'E' is also accepted.
     * <li>An optional sign, '+' or '-'.
     * <li>Optional exponent digits <i><b>in base-10</b></i>.
     * </ul>
     * <p/>
     * <p><i>Valid examples:</i><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;base-2:  <code>"-.110010101e+5"</code><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;base-8:  <code>"+5462E-99"</code><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;base-10: <code>"&nbsp;&nbsp;3,1415927"</code><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;base-16: <code>"/FFF800C.CCCE e64"</code>
     * <p/>
     * <p>The number is parsed until the end of the string or an unknown
     * character is encountered. Note that in case of latter this Real becomes
     * NAN. Please note that specifying an
     * excessive number of digits in base-10 may in fact decrease the
     * accuracy of the result because of the extra multiplications performed.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td>
     * <td colspan="2">
     * <code>this = Double.{@link Double#valueOf(String) valueOf}(a);
     * // Works only for base-10</code>
     * </td></tr><tr><td valign="top" rowspan="2"><i>
     * Approximate&nbsp;error&nbsp;bound:</i>
     * </td><td width="1%">base-10</td><td>
     * œ-1 ULPs
     * </td></tr><tr><td>2/8/16</td><td>
     * œ ULPs
     * </td></tr><tr><td valign="top" rowspan="4"><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;</i>
     * </td><td width="1%">base-2</td><td>
     * 54
     * </td></tr><tr><td>base-8</td><td>
     * 60
     * </td></tr><tr><td>base-10</td><td>
     * 80
     * </td></tr><tr><td>base-16&nbsp;&nbsp;</td><td>
     * 60
     * </td></tr></table>
     *
     * @param a    the <code>String</code> to assign.
     * @param base the number base of <code>a</code>. Valid base values are
     *             2, 8, 10 and 16.
     */
    fun assign(a: String?, base: Int) {
        if (a.isNullOrEmpty()) {
            assign(ZERO)
            return
        }
        atof(a, base)
    }
    /**
     * Assigns this <code>Real</code> a value by directly setting the fields
     * of the internal representation. The arguments must represent a valid,
     * normalized <code>Real</code>. This is the fastest way of assigning a
     * constant value.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = (1-2*s) * m *
     * Math.{@link Math#pow(Double, Double)
     * pow}(2.0,e-0x400000e3);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @param s {@link #sign} bit, 0 for positive sign, 1 for negative sign
     * @param e {@link #exponent}
     * @param m {@link #mantissa}
     */
    fun assign(s: Int, e: Int, m: Long) {
        sign = s.toByte()
        exponent = e
        mantissa = m
    }
    /**
     * Assigns this <code>Real</code> a value previously encoded into into
     * twelve consecutive bytes in a Byte array using {@link
     * #toBytes(ByteArray, Int) toBytes}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.2
     * </td></tr></table>
     *
     * @param data   Byte array to decode into this <code>Real</code>.
     * @param offset offset to start encoding from. The bytes
     *               <code>data[offset]...data[offset+11]</code> will be
     *               read.
     */
    fun assign(data: ByteArray, offset: Int) {
        sign = (((data[offset + 4].toInt() ushr 7) and 1)).toByte()
        exponent = ((data[offset].toInt() and 0xff) shl 24) +
                ((data[offset + 1].toInt() and 0xff) shl 16) +
                ((data[offset + 2].toInt() and 0xff) shl 8) +
                ((data[offset + 3].toInt() and 0xff))
        mantissa = ((data[offset + 4].toLong() and 0x7f) shl 56) +
                ((data[offset + 5].toLong() and 0xff) shl 48) +
                ((data[offset + 6].toLong() and 0xff) shl 40) +
                ((data[offset + 7].toLong() and 0xff) shl 32) +
                ((data[offset + 8].toLong() and 0xff) shl 24) +
                ((data[offset + 9].toLong() and 0xff) shl 16) +
                ((data[offset + 10].toLong() and 0xff) shl 8) +
                ((data[offset + 11].toLong() and 0xff))
    }
    /**
     * Encodes an accurate representation of this <code>Real</code> value into
     * twelve consecutive bytes in a Byte array. Can be decoded using {@link
     * #assign(ByteArray, Int)}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.2
     * </td></tr></table>
     *
     * @param data   Byte array to save this <code>Real</code> in.
     * @param offset offset to start encoding to. The bytes
     *               <code>data[offset]...data[offset+11]</code> will be
     *               written.
     */
    fun toBytes(data: ByteArray, offset: Int) {
        data[offset] = (exponent shr 24).toByte()
        data[offset + 1] = (exponent shr 16).toByte()
        data[offset + 2] = (exponent shr 8).toByte()
        data[offset + 3] = exponent.toByte()
        data[offset + 4] = (((sign.toInt() shl 7) + (mantissa shr 56).toInt())).toByte()
        data[offset + 5] = (mantissa shr 48).toByte()
        data[offset + 6] = (mantissa shr 40).toByte()
        data[offset + 7] = (mantissa shr 32).toByte()
        data[offset + 8] = (mantissa shr 24).toByte()
        data[offset + 9] = (mantissa shr 16).toByte()
        data[offset + 10] = (mantissa shr 8).toByte()
        data[offset + 11] = mantissa.toByte()
    }
    /**
     * Assigns this <code>Real</code> the value corresponding to a given bit
     * representation. The argument is considered to be a representation of a
     * floating-point value according to the IEEE 754 floating-point "single
     * format" bit layout.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Float</code><i>&nbsp;code:</i></td><td>
     * <code>this = Float.{@link Float#intBitsToFloat(Int)
     * intBitsToFloat}(bits);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.6
     * </td></tr></table>
     *
     * @param bits a <code>Float</code> value encoded in an <code>Int</code>.
     */
    fun assignFloatBits(bits: Int) {
        sign = (bits ushr 31).toByte()
        exponent = (bits ushr 23) and 0xff
        mantissa = (bits and 0x007fffff).toLong() shl 39
        if (exponent == 0 && mantissa == 0L) return // Valid zero
        if (exponent == 0) {
            // Degenerate small Float
            exponent = 0x40000000 - 126
            normalize()
            return
        }
        if (exponent <= 254) {
            // Normal IEEE 754 Float
            exponent += 0x40000000 - 127
            mantissa = mantissa or (1L shl 62)
            return
        }
        if (mantissa == 0L)
            makeInfinity(sign.toInt())
        else
            makeNan()
    }
    /**
     * Assigns this <code>Real</code> the value corresponding to a given bit
     * representation. The argument is considered to be a representation of a
     * floating-point value according to the IEEE 754 floating-point "Double
     * format" bit layout.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Double.{@link Double#longBitsToDouble(Long)
     * longBitsToDouble}(bits);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.6
     * </td></tr></table>
     *
     * @param bits a <code>Double</code> value encoded in a <code>Long</code>.
     */
    fun assignDoubleBits(bits: Long) {
        sign = ((bits ushr 63) and 1L).toByte()
        exponent = ((bits ushr 52) and 0x7ffL).toInt()
        mantissa = (bits and 0x000fffffffffffffL) shl 10
        if (exponent == 0 && mantissa == 0L) return // Valid zero
        if (exponent == 0) {
            // Degenerate small Float
            exponent = 0x40000000 - 1022
            normalize()
            return
        }
        if (exponent <= 2046) {
            // Normal IEEE 754 Float
            exponent += 0x40000000 - 1023
            mantissa = mantissa or (1L shl 62)
            return
        }
        if (mantissa == 0L)
            makeInfinity(sign.toInt())
        else
            makeNan()
    }
    /**
     * Returns a representation of this <code>Real</code> according to the
     * IEEE 754 floating-point "single format" bit layout.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Float</code><i>&nbsp;code:</i></td><td>
     * <code>Float.{@link Float#floatToIntBits(Float)
     * floatToIntBits}(this)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.7
     * </td></tr></table>
     *
     * @return the bits that represent the floating-point number.
     */
    fun toFloatBits(): Int {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return 0x7fffffff // nan
        var e = exponent - 0x40000000 + 127
        var m = mantissa
        // Round properly!
        m += 1L shl 38
        if (m < 0) {
            m = m ushr 1
            e++
            if (exponent < 0) // Overflow
                return (sign.toInt() shl 31) or 0x7f800000 // inf
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || e > 254)
            return (sign.toInt() shl 31) or 0x7f800000 // inf
        if ((this.exponent == 0 && this.mantissa == 0L) || e < -22)
            return (sign.toInt() shl 31) // zero
        if (e <= 0) // Degenerate small Float
            return (sign.toInt() shl 31) or ((m ushr (40 - e)).toInt() and 0x007fffff)
        // Normal IEEE 754 Float
        return (sign.toInt() shl 31) or (e shl 23) or ((m ushr 39).toInt() and 0x007fffff)
    }
    /**
     * Returns a representation of this <code>Real</code> according to the
     * IEEE 754 floating-point "Double format" bit layout.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>Double.{@link Double#doubleToLongBits(Double)
     * doubleToLongBits}(this)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.7
     * </td></tr></table>
     *
     * @return the bits that represent the floating-point number.
     */
    fun toDoubleBits(): Long {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return 0x7fffffffffffffffL // nan
        var e = exponent - 0x40000000 + 1023
        var m = mantissa
        // Round properly!
        m += 1L shl 9
        if (m < 0) {
            m = m ushr 1
            e++
            if (exponent < 0)
                return (sign.toLong() shl 63) or 0x7ff0000000000000L // inf
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || e > 2046)
            return (sign.toLong() shl 63) or 0x7ff0000000000000L // inf
        if ((this.exponent == 0 && this.mantissa == 0L) || e < -51)
            return (sign.toLong() shl 63) // zero
        if (e <= 0) // Degenerate small Double
            return (sign.toLong() shl 63) or ((m ushr (11 - e)) and 0x000fffffffffffffL)
        // Normal IEEE 754 Double
        return (sign.toLong() shl 63) or (e.toLong() shl 52) or ((m ushr 10) and 0x000fffffffffffffL)
    }
    /**
     * Makes this <code>Real</code> the value of positive zero.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = 0;</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.2
     * </td></tr></table>
     */
    fun makeZero() {
        sign = 0
        mantissa = 0
        exponent = 0
    }
    /**
     * Makes this <code>Real</code> the value of zero with the specified sign.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = 0.0 * (1-2*s);</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.2
     * </td></tr></table>
     *
     * @param s sign bit, 0 to make a positive zero, 1 to make a negative zero
     */
    fun makeZero(s: Int) {
        sign = s.toByte()
        mantissa = 0
        exponent = 0
    }
    /**
     * Makes this <code>Real</code> the value of infinity with the specified
     * sign.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Double.{@link Double#POSITIVE_INFINITY POSITIVE_INFINITY}
     * * (1-2*s);</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @param s sign bit, 0 to make positive infinity, 1 to make negative
     *          infinity
     */
    fun makeInfinity(s: Int) {
        sign = s.toByte()
        mantissa = 0
        exponent = Int.MIN_VALUE
    }
    /**
     * Makes this <code>Real</code> the value of Not-a-Number (NaN).
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Double.{@link Double#NaN NaN};</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     */
    fun makeNan() {
        sign = 0
        mantissa = 0x4000000000000000L
        exponent = Int.MIN_VALUE
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code> is
     * zero, <code>false</code> otherwise.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this == 0)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object is
     * zero, <code>false</code> otherwise.
     */
    fun isZero(): Boolean {
        return (exponent == 0 && mantissa == 0L)
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code> is
     * infinite, <code>false</code> otherwise.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>Double.{@link Double#isInfinite(Double) isInfinite}(this)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object is
     * infinite, <code>false</code> if it is finite or NaN.
     */
    fun isInfinity(): Boolean {
        return (exponent < 0 && mantissa == 0L)
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code> is
     * Not-a-Number (NaN), <code>false</code> otherwise.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>Double.{@link Double#isNaN(Double) isNaN}(this)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object is
     * NaN, <code>false</code> otherwise.
     */
    fun isNan(): Boolean {
        return (exponent < 0 && mantissa != 0L);
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code> is
     * finite, <code>false</code> otherwise.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(!Double.{@link Double#isNaN(Double) isNaN}(this) &&
     * !Double.{@link Double#isInfinite(Double)
     * isInfinite}(this))</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object is
     * finite, <code>false</code> if it is infinite or NaN.
     */
    fun isFinite(): Boolean {
        // That is, non-infinite and non-nan
        return (exponent >= 0);
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code> is
     * finite and nonzero, <code>false</code> otherwise.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(!Double.{@link Double#isNaN(Double) isNaN}(this) &&
     * !Double.{@link Double#isInfinite(Double) isInfinite}(this) &&
     * (this!=0))</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object is
     * finite and nonzero, <code>false</code> if it is infinite, NaN or
     * zero.
     */
    fun isFiniteNonZero(): Boolean {
        // That is, non-infinite and non-nan and non-zero
        return (exponent >= 0 && mantissa != 0L);
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code> is
     * negative, <code>false</code> otherwise.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this < 0)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object
     * is negative, <code>false</code> if it is positive or NaN.
     */
    fun isNegative(): Boolean {
        return sign.toInt() != 0;
    }
    /**
     * Calculates the absolute value.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#abs(Double) abs}(this);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.2
     * </td></tr></table>
     */
    fun abs() {
        sign = 0
    }
    /**
     * Negates this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = -this;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.2
     * </td></tr></table>
     */
    fun neg() {
        if (!(this.exponent < 0 && this.mantissa != 0L))
            sign = (sign.toInt() xor 1).toByte()
    }
    /**
     * Copies the sign from <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#abs(Double)
     * abs}(this)*Math.{@link Math#signum(Double) signum}(a);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.2
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to copy the sign from.
     */
    fun copysign(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        sign = a.sign;
    }
    /**
     * Readjusts the mantissa of this <code>Real</code>. The exponent is
     * adjusted accordingly. This is necessary when the mantissa has been
     * {@link #mantissa modified directly} for some purpose and may be
     * denormalized. The normalized mantissa of a finite <code>Real</code>
     * must have bit 63 cleared and bit 62 set. Using a denormalized
     * <code>Real</code> in <u>any</u> other operation may produce undefined
     * results.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.7
     * </td></tr></table>
     */
    fun normalize() {
        if ((this.exponent >= 0)) {
            if (mantissa > 0) {
                var clz = 0
                var t = (mantissa ushr 32).toInt()
                if (t == 0) {
                    clz = 32
                    t = mantissa.toInt()
                }
                t = t or (t shr 1)
                t = t or (t shr 2)
                t = t or (t shr 4)
                t = t or (t shr 8)
                t = t or (t shr 16)
                clz += clz_tab[((t * clz_magic) ushr 27)].toInt() - 1
                mantissa = mantissa shl clz
                exponent -= clz
                if (exponent < 0) // Underflow
                    makeZero(sign.toInt())
            } else if (mantissa < 0) {
                mantissa = (mantissa + 1) ushr 1
                exponent++
                if (mantissa == 0L) { // Ooops, it was 0xffffffffffffffffL
                    mantissa = 0x4000000000000000L
                    exponent++
                }
                if (exponent < 0) // Overflow
                    makeInfinity(sign.toInt())
            } else // mantissa == 0L
            {
                exponent = 0
            }
        }
    }
    /**
     * Readjusts the mantissa of a <code>Real</code> with extended
     * precision. The exponent is adjusted accordingly. This is necessary when
     * the mantissa has been {@link #mantissa modified directly} for some
     * purpose and may be denormalized. The normalized mantissa of a finite
     * <code>Real</code> must have bit 63 cleared and bit 62 set. Using a
     * denormalized <code>Real</code> in <u>any</u> other operation may
     * produce undefined results.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2<sup>-64</sup> ULPs (i.e. of a normal precision <code>Real</code>)
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.7
     * </td></tr></table>
     *
     * @param extra the extra 64 bits of mantissa of this extended precision
     *              <code>Real</code>.
     * @return the extra 64 bits of mantissa of the resulting extended
     * precision <code>Real</code>.
     */
    fun normalize128(extra: Long): Long {
        if (!(this.exponent >= 0)) return 0
        var extraVar = extra
        if (mantissa == 0L) {
            if (extraVar == 0L) {
                exponent = 0
                return 0
            }
            mantissa = extraVar
            extraVar = 0
            exponent -= 64
            if (exponent < 0) { // Underflow
                makeZero(sign.toInt())
                return 0
            }
        }
        if (mantissa < 0) {
            extraVar = (mantissa shl 63) + (extraVar ushr 1)
            mantissa = mantissa ushr 1
            exponent++
            if (exponent < 0) { // Overflow
                makeInfinity(sign.toInt())
                return 0
            }
            return extraVar
        }
        var clz = 0
        var t = (mantissa ushr 32).toInt()
        if (t == 0) {
            clz = 32
            t = mantissa.toInt()
        }
        t = t or (t shr 1)
        t = t or (t shr 2)
        t = t or (t shr 4)
        t = t or (t shr 8)
        t = t or (t shr 16)
        clz += clz_tab[((t * clz_magic) ushr 27)].toInt() - 1
        if (clz == 0) return extraVar
        mantissa = (mantissa shl clz) + (extraVar ushr (64 - clz))
        extraVar = extraVar shl clz
        exponent -= clz
        if (exponent < 0) { // Underflow
            makeZero(sign.toInt())
            return 0
        }
        return extraVar
    }
    /**
     * Rounds an extended precision <code>Real</code> to the nearest
     * <code>Real</code> of normal precision. Replaces the contents of this
     * <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param extra the extra 64 bits of mantissa of this extended precision
     *              <code>Real</code>.
     */
    fun roundFrom128(extra: Long) {
        mantissa += (extra ushr 63) and 1L
        normalize()
    }
    /**
     * Returns <code>true</code> if this Java object is the same
     * object as <code>a</code>. Since a <code>Real</code> should be
     * thought of as a "register holding a number", this method compares the
     * object references, not the contents of the two objects.
     * This is very different from {@link #equalTo(Real)}.
     *
     * @param a the object to compare to this.
     * @return <code>true</code> if this object is the same as <code>a</code>.
     */
    override fun equals(other: Any?): Boolean {
        return this === other
    }
    fun compare(a: Real): Int {
        // Compare of normal floats, zeros, but not nan or equal-signed inf
        if ((this.exponent == 0 && this.mantissa == 0L) && (a.exponent == 0 && a.mantissa == 0L))
            return 0
        if (sign != a.sign)
            return a.sign.toInt() - sign.toInt()
        val s = if (this.sign.toInt() == 0) 1 else -1
        if ((this.exponent < 0 && this.mantissa == 0L))
            return s
        if ((a.exponent < 0 && a.mantissa == 0L))
            return -s
        if (exponent != a.exponent)
            return if (exponent < a.exponent) -s else s
        if (mantissa != a.mantissa)
            return if (mantissa < a.mantissa) -s else s
        return 0
    }
    fun invalidCompare(a: Real): Boolean {
        return ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L) ||
                ((this.exponent < 0 && this.mantissa == 0L) && (a.exponent < 0 && a.mantissa == 0L) && sign == a.sign))
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is equal to
     * <code>a</code>.
     * If the numbers are incomparable, i.e. the values are infinities of
     * the same sign or any of them is NaN, <code>false</code> is always
     * returned. This method must not be confused with {@link #equals(Object)}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this == a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * equal to the value represented by <code>a</code>. <code>false</code>
     * otherwise, or if the numbers are incomparable.
     */
    fun equalTo(a: Real): Boolean {
        return !invalidCompare(a) && compare(a) == 0;
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is equal to
     * the integer <code>a</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this == a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.7
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * equal to the integer <code>a</code>. <code>false</code>
     * otherwise.
     */
    fun equalTo(a: Int): Boolean {
        val tmp0 = tmp0()
        tmp0.assign(a);
        return equalTo(tmp0);
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is not equal to
     * <code>a</code>.
     * If the numbers are incomparable, i.e. the values are infinities of
     * the same sign or any of them is NaN, <code>false</code> is always
     * returned.
     * This distinguishes <code>notEqualTo(a)</code> from the expression
     * <code>!equalTo(a)</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this != a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is not
     * equal to the value represented by <code>a</code>. <code>false</code>
     * otherwise, or if the numbers are incomparable.
     */
    fun notEqualTo(a: Real): Boolean {
        return !invalidCompare(a) && compare(a) != 0;
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is not equal to
     * the integer <code>a</code>.
     * If this <code>Real</code> is NaN, <code>false</code> is always
     * returned.
     * This distinguishes <code>notEqualTo(a)</code> from the expression
     * <code>!equalTo(a)</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this != a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.7
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is not
     * equal to the integer <code>a</code>. <code>false</code>
     * otherwise, or if this <code>Real</code> is NaN.
     */
    fun notEqualTo(a: Int): Boolean {
        val tmp0 = tmp0()
        tmp0.assign(a);
        return notEqualTo(tmp0);
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is less than
     * <code>a</code>.
     * If the numbers are incomparable, i.e. the values are infinities of
     * the same sign or any of them is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &lt; a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * less than the value represented by <code>a</code>.
     * <code>false</code> otherwise, or if the numbers are incomparable.
     */
    fun lessThan(a: Real): Boolean {
        return !invalidCompare(a) && compare(a) < 0;
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is less than
     * the integer <code>a</code>.
     * If this <code>Real</code> is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &lt; a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.7
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * less than the integer <code>a</code>. <code>false</code> otherwise,
     * or if this <code>Real</code> is NaN.
     */
    fun lessThan(a: Int): Boolean {
        val tmp0 = tmp0()
        tmp0.assign(a);
        return lessThan(tmp0);
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is less than or
     * equal to <code>a</code>.
     * If the numbers are incomparable, i.e. the values are infinities of
     * the same sign or any of them is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &lt;= a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * less than or equal to the value represented by <code>a</code>.
     * <code>false</code> otherwise, or if the numbers are incomparable.
     */
    fun lessEqual(a: Real): Boolean {
        return !invalidCompare(a) && compare(a) <= 0;
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is less than or
     * equal to the integer <code>a</code>.
     * If this <code>Real</code> is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &lt;= a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.7
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * less than or equal to the integer <code>a</code>. <code>false</code>
     * otherwise, or if this <code>Real</code> is NaN.
     */
    fun lessEqual(a: Int): Boolean {
        val tmp0 = tmp0()
        tmp0.assign(a);
        return lessEqual(tmp0);
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is greater than
     * <code>a</code>.
     * If the numbers are incomparable, i.e. the values are infinities of
     * the same sign or any of them is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &gt; a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * greater than the value represented by <code>a</code>.
     * <code>false</code> otherwise, or if the numbers are incomparable.
     */
    fun greaterThan(a: Real): Boolean {
        return !invalidCompare(a) && compare(a) > 0;
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is greater than
     * the integer <code>a</code>.
     * If this <code>Real</code> is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &gt; a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.7
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * greater than the integer <code>a</code>.
     * <code>false</code> otherwise, or if this <code>Real</code> is NaN.
     */
    fun greaterThan(a: Int): Boolean {
        val tmp0 = tmp0()
        tmp0.assign(a);
        return greaterThan(tmp0);
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is greater than
     * or equal to <code>a</code>.
     * If the numbers are incomparable, i.e. the values are infinities of
     * the same sign or any of them is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &gt;= a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * greater than or equal to the value represented by <code>a</code>.
     * <code>false</code> otherwise, or if the numbers are incomparable.
     */
    fun greaterEqual(a: Real): Boolean {
        return !invalidCompare(a) && compare(a) >= 0;
    }
    /**
     * Returns <code>true</code> if this <code>Real</code> is greater than
     * or equal to the integer <code>a</code>.
     * If this <code>Real</code> is NaN, <code>false</code> is always
     * returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this &gt;= a)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.7
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to compare to this.
     * @return <code>true</code> if the value represented by this object is
     * greater than or equal to the integer <code>a</code>.
     * <code>false</code> otherwise, or if this <code>Real</code> is NaN.
     */
    fun greaterEqual(a: Int): Boolean {
        val tmp0 = tmp0()
        tmp0.assign(a);
        return greaterEqual(tmp0);
    }
    /**
     * Returns <code>true</code> if the absolute value of this
     * <code>Real</code> is less than the absolute value of
     * <code>a</code>.
     * If the numbers are incomparable, i.e. the values are both infinite
     * or any of them is NaN, <code>false</code> is always returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(Math.{@link Math#abs(Double) abs}(this) &lt;
     * Math.{@link Math#abs(Double) abs}(a))</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.5
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to compare to this.
     * @return <code>true</code> if the absolute of the value represented by
     * this object is less  than the absolute of the value represented by
     * <code>a</code>.
     * <code>false</code> otherwise, or if the numbers are incomparable.
     */
    fun absLessThan(a: Real): Boolean {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L) || (this.exponent < 0 && this.mantissa == 0L))
            return false;
        if ((a.exponent < 0 && a.mantissa == 0L))
            return true;
        if (exponent != a.exponent)
            return exponent < a.exponent;
        return mantissa < a.mantissa;
    }
    /**
     * Multiplies this <code>Real</code> by 2 to the power of <code>n</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * This operation is faster than normal multiplication since it only
     * involves adding to the exponent.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this *= Math.{@link Math#pow(Double, Double) pow}(2.0,n);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.3
     * </td></tr></table>
     *
     * @param n the integer argument.
     */
    fun scalbn(n: Int) {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return;
        exponent += n;
        if (exponent < 0) {
            if (n < 0)
                makeZero(sign.toInt()); // Underflow
            else
                makeInfinity(sign.toInt()); // Overflow
        }
    }
    /**
     * Calculates the next representable neighbour of this <code>Real</code>
     * in the direction towards <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * If the two values are equal, nothing happens.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this += Math.{@link Math#ulp(Double) ulp}(this)*Math.{@link
     * Math#signum(Double) signum}(a-this);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.8
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument.
     */
    fun nextafter(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan()
            return
        }
        if ((this.exponent < 0 && this.mantissa == 0L) && (a.exponent < 0 && a.mantissa == 0L) && sign == a.sign)
            return
        val dir = -compare(a)
        if (dir == 0)
            return
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            this.mantissa = MIN.mantissa
            this.exponent = MIN.exponent
            this.sign = MIN.sign
            sign = (if (dir < 0) 1 else 0).toByte()
            return
        }
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            this.mantissa = MAX.mantissa
            this.exponent = MAX.exponent
            this.sign = MAX.sign
            sign = (if (dir < 0) 0 else 1).toByte()
            return
        }
        if ((this.sign.toInt() == 0) xor (dir < 0)) {
            mantissa++
        } else {
            if (mantissa == 0x4000000000000000L) {
                mantissa = mantissa shl 1
                exponent--
            }
            mantissa--
        }
        normalize()
    }
    /**
     * Calculates the largest (closest to positive infinity)
     * <code>Real</code> value that is less than or equal to this
     * <code>Real</code> and is equal to a mathematical integer.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#floor(Double) floor}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.5
     * </td></tr></table>
     */
    fun floor() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return
        if (exponent < 0x40000000) {
            if ((this.sign.toInt() == 0))
                makeZero(sign.toInt())
            else {
                exponent = ONE.exponent
                mantissa = ONE.mantissa
                // sign unchanged!
            }
            return
        }
        val shift = 0x4000003e - exponent
        if (shift <= 0)
            return
        if ((this.sign.toInt() != 0))
            mantissa += ((1L shl shift) - 1)
        mantissa = mantissa and ((1L shl shift) - 1).inv()
        if ((this.sign.toInt() != 0))
            normalize()
    }
    /**
     * Calculates the smallest (closest to negative infinity)
     * <code>Real</code> value that is greater than or equal to this
     * <code>Real</code> and is equal to a mathematical integer.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#ceil(Double) ceil}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.8
     * </td></tr></table>
     */
    fun ceil() {
        neg();
        floor();
        neg();
    }
    /**
     * Rounds this <code>Real</code> value to the closest value that is equal
     * to a mathematical integer. If two <code>Real</code> values that are
     * mathematical integers are equally close, the result is the integer
     * value with the largest magnitude (positive or negative).  Replaces the
     * contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#rint(Double) rint}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.3
     * </td></tr></table>
     */
    fun round() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return
        if (exponent < 0x3fffffff) {
            makeZero(sign.toInt())
            return
        }
        val shift = 0x4000003e - exponent
        if (shift <= 0)
            return
        mantissa += 1L shl (shift - 1) // Bla-bla, this works almost
        mantissa = mantissa and ((1L shl shift) - 1).inv()
        normalize()
    }
    /**
     * Truncates this <code>Real</code> value to the closest value towards
     * zero that is equal to a mathematical integer.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = (Double)((Long)this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.2
     * </td></tr></table>
     */
    fun trunc() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return
        if (exponent < 0x40000000) {
            makeZero(sign.toInt())
            return
        }
        val shift = 0x4000003e - exponent
        if (shift <= 0)
            return
        mantissa = mantissa and ((1L shl shift) - 1).inv()
        normalize()
    }
    /**
     * Calculates the fractional part of this <code>Real</code> by subtracting
     * the closest value towards zero that is equal to a mathematical integer.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this -= (Double)((Long)this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.2
     * </td></tr></table>
     */
    fun frac() {
        if (!(this.exponent >= 0 && this.mantissa != 0L) || exponent < 0x40000000)
            return
        val shift = 0x4000003e - exponent
        if (shift <= 0) {
            makeZero(sign.toInt())
            return
        }
        mantissa = mantissa and ((1L shl shift) - 1)
        normalize()
    }
    /**
     * Converts this <code>Real</code> value to the closest <code>Int</code>
     * value towards zero.
     * <p/>
     * <p>If the value of this <code>Real</code> is too large, {@link
     * Integer#MAX_VALUE} is returned. However, if the value of this
     * <code>Real</code> is too small, <code>-Integer.MAX_VALUE</code> is
     * returned, not {@link Integer#MIN_VALUE}. This is done to ensure that
     * the sign will be correct if you calculate
     * <code>-this.toInteger()</code>. A NaN is converted to 0.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(Int)this</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.6
     * </td></tr></table>
     *
     * @return an <code>Int</code> representation of this <code>Real</code>.
     */
    fun toInteger(): Int {
        if ((this.exponent == 0 && this.mantissa == 0L) || (this.exponent < 0 && this.mantissa != 0L))
            return 0
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            return if (this.sign.toInt() == 0) Int.MAX_VALUE else Int.MIN_VALUE + 1
            // 0x80000001, so that you can take -x.toInteger()
        }
        if (exponent < 0x40000000)
            return 0
        val shift = 0x4000003e - exponent
        if (shift < 32) {
            return if (this.sign.toInt() == 0) Int.MAX_VALUE else Int.MIN_VALUE + 1
            // 0x80000001, so that you can take -x.toInteger()
        }
        return if (this.sign.toInt() == 0) {
            (mantissa ushr shift).toInt()
        } else {
            -(mantissa ushr shift).toInt()
        }
    }
    /**
     * Converts this <code>Real</code> value to the closest <code>Long</code>
     * value towards zero.
     * <p/>
     * <p>If the value of this <code>Real</code> is too large, {@link
     * Long#MAX_VALUE} is returned. However, if the value of this
     * <code>Real</code> is too small, <code>-Long.MAX_VALUE</code> is
     * returned, not {@link Long#MIN_VALUE}. This is done to ensure that the
     * sign will be correct if you calculate <code>-this.toLong()</code>.
     * A NaN is converted to 0.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(Long)this</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.5
     * </td></tr></table>
     *
     * @return a <code>Long</code> representation of this <code>Real</code>.
     */
    fun toLong(): Long {
        if ((this.exponent == 0 && this.mantissa == 0L) || (this.exponent < 0 && this.mantissa != 0L))
            return 0
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            return if (this.sign.toInt() == 0) Long.MAX_VALUE else Long.MIN_VALUE + 1
            // 0x8000000000000001L, so that you can take -x.toLong()
        }
        if (exponent < 0x40000000)
            return 0
        val shift = 0x4000003e - exponent
        if (shift < 0) {
            return if (this.sign.toInt() == 0) Long.MAX_VALUE else Long.MIN_VALUE + 1
            // 0x8000000000000001L, so that you can take -x.toLong()
        }
        return if (this.sign.toInt() == 0) {
            mantissa ushr shift
        } else {
            -(mantissa ushr shift)
        }
    }
    fun toDouble(): Double {
        return Double.fromBits(toDoubleBits())
    }
    /**
     * Returns <code>true</code> if the value of this <code>Real</code>
     * represents a mathematical integer. If the value is too large to
     * determine if it is an integer, <code>true</code> is returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>(this == (Long)this)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.6
     * </td></tr></table>
     *
     * @return <code>true</code> if the value represented by this object
     * represents a mathematical integer, <code>false</code> otherwise.
     */
    fun isIntegral(): Boolean {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return false
        if ((this.exponent == 0 && this.mantissa == 0L) || (this.exponent < 0 && this.mantissa == 0L))
            return true
        if (exponent < 0x40000000)
            return false
        val shift = 0x4000003e - exponent
        return shift <= 0 || (mantissa and ((1L shl shift) - 1)) == 0L
    }
    /**
     * Returns <code>true</code> if the mathematical integer represented
     * by this <code>Real</code> is odd. You <u>must</u> first determine
     * that the value is actually an integer using {@link
     * #isIntegral()}. If the value is too large to determine if the
     * integer is odd, <code>false</code> is returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>((((Long)this)&1) == 1)</code>
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.6
     * </td></tr></table>
     *
     * @return <code>true</code> if the mathematical integer represented by
     * this <code>Real</code> is odd, <code>false</code> otherwise.
     */
    fun isOdd(): Boolean {
        if (!(this.exponent >= 0 && this.mantissa != 0L) ||
                exponent < 0x40000000 || exponent > 0x4000003e)
            return false
        val shift = 0x4000003e - exponent
        return ((mantissa ushr shift) and 1L) != 0L
    }
    /**
     * Exchanges the contents of this <code>Real</code> and <code>a</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>tmp=this; this=a; a=tmp;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 0.5
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to exchange with this.
     */
    fun swap(a: Real) {
        val tmpMantissa = mantissa
        mantissa = a.mantissa;
        a.mantissa = tmpMantissa;
        val tmpExponent = exponent
        exponent = a.exponent;
        a.exponent = tmpExponent;
        val tmpSign = sign
        sign = a.sign;
        a.sign = tmpSign;
    }
    /**
     * Calculates the sum of this <code>Real</code> and <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this += a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * «« 1.0 »»
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to add to this.
     */
    fun add(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            if ((this.exponent < 0 && this.mantissa == 0L) && (a.exponent < 0 && a.mantissa == 0L) && sign != a.sign)
                makeNan()
            else
                makeInfinity((if (this.exponent < 0 && this.mantissa == 0L) sign else a.sign).toInt())
            return
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            if ((this.exponent == 0 && this.mantissa == 0L)) {
                this.mantissa = a.mantissa;
                this.exponent = a.exponent;
                this.sign = a.sign;
            }
            if ((this.exponent == 0 && this.mantissa == 0L))
                sign = 0
            return
        }
        var s: Byte
        var e: Int
        var m: Long
        if (exponent > a.exponent ||
                (exponent == a.exponent && mantissa >= a.mantissa)) {
            s = a.sign;
            e = a.exponent;
            m = a.mantissa;
        } else {
            s = sign;
            e = exponent;
            m = mantissa;
            sign = a.sign;
            exponent = a.exponent;
            mantissa = a.mantissa;
        }
        var shift = exponent - e
        if (shift >= 64)
            return
        if (sign == s) {
            mantissa += m ushr shift
            if (mantissa >= 0 && shift > 0 && ((m ushr (shift - 1)) and 1L) != 0L)
                mantissa++ // We don't need normalization, so round now
            if (mantissa < 0) {
                // Simplified normalize()
                mantissa = (mantissa + 1) ushr 1
                exponent++
                if (exponent < 0) { // Overflow
                    makeInfinity(sign.toInt())
                    return
                }
            }
        } else {
            if (shift > 0) {
                // Shift mantissa up to increase accuracy
                mantissa = mantissa shl 1
                exponent--
                shift--
            }
            m = -m
            mantissa += m shr shift
            if (mantissa >= 0 && shift > 0 && ((m ushr (shift - 1)) and 1L) != 0L)
                mantissa++ // We don't need to shift down, so round now
            if (mantissa < 0) {
                // Simplified normalize()
                mantissa = (mantissa + 1) ushr 1
                exponent++ // Can't overflow
            } else if (shift == 0) {
                // Operands have equal exponents => many bits may be cancelled
                // Magic rounding: if result of subtract leaves only a few bits
                // standing, the result should most likely be 0...
                if (magicRounding && mantissa > 0 && mantissa <= 7) {
                    // If arguments were integers <= 2^63-1, then don't
                    // do the magic rounding anyway.
                    // This is a bit "post mortem" investigation but it happens
                    // so seldom that it's no problem to spend the extra time.
                    m = -m;
                    if (exponent == 0x4000003c || exponent == 0x4000003d ||
                            (exponent == 0x4000003e && mantissa + m > 0)) {
                        val mask = (1L shl (0x4000003e - exponent)) - 1
                        if ((mantissa and mask) != 0L || (m and mask) != 0L)
                            mantissa = 0
                    } else
                        mantissa = 0
                }
                normalize()
            } // else... if (shift>=1 && mantissa>=0) it should be a-ok
        }
        if ((this.exponent == 0 && this.mantissa == 0L))
            sign = 0
    }
    /**
     * Calculates the sum of this <code>Real</code> and the integer
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this += a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.8
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to add to this.
     */
    fun add(a: Int) {
        val tmp0 = tmp0()
        tmp0.assign(a);
        add(tmp0);
    }
    /**
     * Calculates the sum of this <code>Real</code> and <code>a</code> with
     * extended precision.  Replaces the contents of this <code>Real</code>
     * with the result.  Returns the extra mantissa of the extended precision
     * result.
     * <p/>
     * <p>An extra 64 bits of mantissa is added to both arguments for extended
     * precision. If any of the arguments are not of extended precision, use
     * <code>0</code> for the extra mantissa.
     * <p/>
     * <p>Extended prevision can be useful in many situations. For instance,
     * when accumulating a lot of very small values it is advantageous for the
     * accumulator to have extended precision. To convert the extended
     * precision value back to a normal <code>Real</code> for further
     * processing, use {@link #roundFrom128(Long)}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this += a;</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2<sup>-62</sup> ULPs (i.e. of a normal precision <code>Real</code>)
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 2.0
     * </td></tr></table>
     *
     * @param extra  the extra 64 bits of mantissa of this extended precision
     *               <code>Real</code>.
     * @param a      the <code>Real</code> to add to this.
     * @param aExtra the extra 64 bits of mantissa of the extended precision
     *               value <code>a</code>.
     * @return the extra 64 bits of mantissa of the resulting extended
     * precision <code>Real</code>.
     */
    fun add128(extra: Long, a: Real, aExtra: Long): Long {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return 0;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            if ((this.exponent < 0 && this.mantissa == 0L) && (a.exponent < 0 && a.mantissa == 0L) && sign != a.sign)
                makeNan();
            else
                makeInfinity(if (this.exponent < 0 && this.mantissa == 0L) sign.toInt() else a.sign.toInt());
            return 0;
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            var extraVar = extra
            if ((this.exponent == 0 && this.mantissa == 0L)) {
                {
                    this.mantissa = a.mantissa;
                    this.exponent = a.exponent;
                    this.sign = a.sign;
                }
                extraVar = aExtra;
            }
            if ((this.exponent == 0 && this.mantissa == 0L))
                sign = 0;
            return extraVar;
        }
        var s: Byte
        var e: Int
        var m: Long
        var x: Long
        var extraVar = extra
        if (exponent > a.exponent ||
                (exponent == a.exponent && mantissa > a.mantissa) ||
                (exponent == a.exponent && mantissa == a.mantissa &&
                        (extraVar ushr 1) >= (aExtra ushr 1))) {
            s = a.sign;
            e = a.exponent;
            m = a.mantissa;
            x = aExtra;
        } else {
            s = sign;
            e = exponent;
            m = mantissa;
            x = extraVar;
            sign = a.sign;
            exponent = a.exponent;
            mantissa = a.mantissa;
            extraVar = aExtra;
        }
        val shift = exponent - e
        if (shift >= 127)
            return extraVar;
        if (shift >= 64) {
            x = m ushr (shift - 64);
            m = 0;
        } else if (shift > 0) {
            x = (x ushr shift) + (m shl (64 - shift));
            m = m ushr shift;
        }
        extraVar = extraVar ushr 1;
        x = x ushr 1;
        if (sign == s) {
            extraVar += x;
            mantissa += (extraVar shr 63) and 1;
            mantissa += m;
        } else {
            extraVar -= x;
            mantissa -= (extraVar shr 63) and 1;
            mantissa -= m;
            // Magic rounding: if result of subtract leaves only a few bits
            // standing, the result should most likely be 0...
            if (mantissa == 0L && extraVar > 0 && extraVar <= 0x1f)
                extraVar = 0;
        }
        extraVar = extraVar shl 1;
        extraVar = normalize128(extraVar);
        if ((this.exponent == 0 && this.mantissa == 0L))
            sign = 0;
        return extraVar;
    }
    /**
     * Calculates the difference between this <code>Real</code> and
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>(To achieve extended precision subtraction, it is enough to call
     * <code>a.{@link #neg() neg}()</code> before calling <code>{@link
     * #add128(Long, Real, Long) add128}(extra,a,aExtra)</code>, since only
     * the sign bit of <code>a</code> need to be changed.)
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this -= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 2.0
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to subtract from this.
     */
    fun sub(a: Real) {
        val tmp0 = tmp0()
        tmp0.mantissa = a.mantissa
        tmp0.exponent = a.exponent
        tmp0.sign = (a.sign.toInt() xor 1).toByte()
        add(tmp0)
    }
    /**
     * Calculates the difference between this <code>Real</code> and the
     * integer <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this -= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 2.4
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to subtract from this.
     */
    fun sub(a: Int) {
        val tmp0 = tmp0()
        tmp0.assign(a)
        sub(tmp0)
    }
    /**
     * Calculates the product of this <code>Real</code> and <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this *= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.3
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to multiply to this.
     */
    fun mul(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        sign = (sign.toInt() xor a.sign.toInt()).toByte()
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L))
                makeNan();
            else
                makeZero(sign.toInt());
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            makeInfinity(sign.toInt());
            return;
        }
        val a0 = mantissa and 0x7fffffff
        val a1 = mantissa ushr 31
        val b0 = a.mantissa and 0x7fffffff
        val b1 = a.mantissa ushr 31
        mantissa = a1 * b1
        // If we're going to need normalization, we don't want to round twice
        val round = if (mantissa < 0) 0 else 0x40000000
        mantissa += ((a0 * b1 + a1 * b0 + ((a0 * b0) ushr 31) + round) ushr 31)
        val aExp = a.exponent
        exponent += aExp - 0x40000000
        if (exponent < 0) {
            if (exponent == -1 && aExp < 0x40000000 && mantissa < 0) {
                // Not underflow after all, it will be corrected in the
                // normalization below
            } else {
                if (aExp < 0x40000000)
                    makeZero(sign.toInt()); // Underflow
                else
                    makeInfinity(sign.toInt()); // Overflow
                return;
            }
        }
        // Simplified normalize()
        if (mantissa < 0) {
            mantissa = (mantissa + 1) ushr 1
            exponent++;
            if (exponent < 0) // Overflow
                makeInfinity(sign.toInt());
        }
    }
    /**
     * Calculates the product of this <code>Real</code> and the integer
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this *= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.3
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to multiply to this.
     */
    fun mul(a: Int) {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return;
        var aVar = a
        if (aVar < 0) {
            sign = (sign.toInt() xor 1).toByte()
            aVar = -aVar
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || aVar == 0) {
            if ((this.exponent < 0 && this.mantissa == 0L))
                makeNan();
            else
                makeZero(sign.toInt());
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L))
            return;
        // Normalize Int
        var t = aVar
        t = t or (t shr 1)
        t = t or (t shr 2)
        t = t or (t shr 4)
        t = t or (t shr 8)
        t = t or (t shr 16)
        t = clz_tab[(t * clz_magic) ushr 27].toInt()
        exponent += 0x1F - t;
        aVar = aVar shl t
        if (exponent < 0) {
            makeInfinity(sign.toInt()); // Overflow
            return;
        }
        val a0 = mantissa and 0x7fffffff
        val a1 = mantissa ushr 31
        val b0 = aVar.toLong() and 0xffffffffL
        mantissa = a1 * b0
        // If we're going to need normalization, we don't want to round twice
        val round = if (mantissa < 0) 0 else 0x40000000
        mantissa += ((a0 * b0 + round) ushr 31)
        // Simplified normalize()
        if (mantissa < 0) {
            mantissa = (mantissa + 1) ushr 1
            exponent++;
            if (exponent < 0) // Overflow
                makeInfinity(sign.toInt());
        }
    }
    /**
     * Calculates the product of this <code>Real</code> and <code>a</code> with
     * extended precision.
     * Replaces the contents of this <code>Real</code> with the result.
     * Returns the extra mantissa of the extended precision result.
     * <p/>
     * <p>An extra 64 bits of mantissa is added to both arguments for
     * extended precision. If any of the arguments are not of extended
     * precision, use <code>0</code> for the extra mantissa. See also {@link
     * #add128(Long, Real, Long)}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this *= a;</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2<sup>-60</sup> ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 3.1
     * </td></tr></table>
     *
     * @param extra  the extra 64 bits of mantissa of this extended precision
     *               <code>Real</code>.
     * @param a      the <code>Real</code> to multiply to this.
     * @param aExtra the extra 64 bits of mantissa of the extended precision
     *               value <code>a</code>.
     * @return the extra 64 bits of mantissa of the resulting extended
     * precision <code>Real</code>.
     */
    fun mul128(extra: Long, a: Real, aExtra: Long): Long {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return 0;
        }
        sign = (sign.toInt() xor a.sign.toInt()).toByte()
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L))
                makeNan();
            else
                makeZero(sign.toInt());
            return 0;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            makeInfinity(sign.toInt());
            return 0;
        }
        val aExp = a.exponent
        exponent += aExp - 0x40000000;
        if (exponent < 0) {
            if (aExp < 0x40000000)
                makeZero(sign.toInt()); // Underflow
            else
                makeInfinity(sign.toInt()); // Overflow
            return 0;
        }
        var extraVar = extra
        val mask32 = 0xffffffffL
        var a0 = extraVar and mask32
        var a1 = extraVar ushr 32
        var a2 = mantissa and mask32
        val a3 = mantissa ushr 32
        var b0 = aExtra and mask32
        var b1 = aExtra ushr 32
        var b2 = a.mantissa and mask32
        val b3 = a.mantissa ushr 32
        a0 = ((a3 * b0 ushr 2) +
                (a2 * b1 ushr 2) +
                (a1 * b2 ushr 2) +
                (a0 * b3 ushr 2) +
                0x60000000) ushr 28
        //(a2*b0>>>34)+(a1*b1>>>34)+(a0*b2>>>34)+0x08000000)>>>28;
        a1 *= b3
        b0 = a2 * b2
        b1 *= a3
        a0 += ((a1 shl 2) and mask32) + ((b0 shl 2) and mask32) + ((b1 shl 2) and mask32)
        a1 = (a0 ushr 32) + (a1 ushr 30) + (b0 ushr 30) + (b1 ushr 30)
        a0 = a0 and mask32
        a2 *= b3
        b2 *= a3
        a1 += ((a2 shl 2) and mask32) + ((b2 shl 2) and mask32)
        extraVar = (a1 shl 32) + a0
        mantissa = ((a3 * b3) shl 2) + (a1 ushr 32) + (a2 ushr 30) + (b2 ushr 30)
        extraVar = normalize128(extraVar)
        return extraVar
    }
    fun mul10() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return;
        mantissa += (mantissa + 2) ushr 2
        exponent += 3
        if (mantissa < 0) {
            mantissa = (mantissa + 1) ushr 1
            exponent++
        }
        if (exponent < 0)
            makeInfinity(sign.toInt()); // Overflow
    }
    /**
     * Calculates the square of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = this*this;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.1
     * </td></tr></table>
     */
    fun sqr() {
        sign = 0
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return;
        val e = exponent
        exponent += exponent - 0x40000000
        if (exponent < 0) {
            if (e < 0x40000000)
                makeZero(sign.toInt()); // Underflow
            else
                makeInfinity(sign.toInt()); // Overflow
            return;
        }
        val a0 = mantissa and 0x7fffffff
        val a1 = mantissa ushr 31
        mantissa = a1 * a1
        // If we're going to need normalization, we don't want to round twice
        val round = if (mantissa < 0) 0 else 0x40000000
        mantissa += ((((a0 * a1) shl 1) + ((a0 * a0) ushr 31) + round) ushr 31)
        // Simplified normalize()
        if (mantissa < 0) {
            mantissa = (mantissa + 1) ushr 1
            exponent++
            if (exponent < 0) // Overflow
                makeInfinity(sign.toInt());
        }
    }
    /**
     * Calculates the quotient of this <code>Real</code> and <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>(To achieve extended precision division, call
     * <code>aExtra=a.{@link #recip128(Long) recip128}(aExtra)</code> before
     * calling <code>{@link #mul128(Long, Real, Long)
     * mul128}(extra,a,aExtra)</code>.)
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this /= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 2.6
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to divide this with.
     */
    fun div(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        sign = (sign.toInt() xor a.sign.toInt()).toByte()
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            if ((a.exponent < 0 && a.mantissa == 0L))
                makeNan();
            return;
        }
        if ((a.exponent < 0 && a.mantissa == 0L)) {
            makeZero(sign.toInt());
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            if ((a.exponent == 0 && a.mantissa == 0L))
                makeNan();
            return;
        }
        if ((a.exponent == 0 && a.mantissa == 0L)) {
            makeInfinity(sign.toInt());
            return;
        }
        exponent += 0x40000000 - a.exponent;
        if (mantissa < a.mantissa) {
            mantissa = mantissa shl 1
            exponent--
        }
        if (exponent < 0) {
            if (a.exponent >= 0x40000000)
                makeZero(sign.toInt()); // Underflow
            else
                makeInfinity(sign.toInt()); // Overflow
            return;
        }
        if (a.mantissa == 0x4000000000000000L)
            return;
        mantissa = ldiv(mantissa, a.mantissa);
    }
    /**
     * Calculates the quotient of this <code>Real</code> and the integer
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this /= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 2.6
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to divide this with.
     */
    fun div(a: Int) {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return;
        var aVar = a
        if (aVar < 0) {
            sign = (sign.toInt() xor 1).toByte()
            aVar = -aVar
        }
        if ((this.exponent < 0 && this.mantissa == 0L))
            return;
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            if (aVar == 0)
                makeNan();
            return;
        }
        if (aVar == 0) {
            makeInfinity(sign.toInt());
            return;
        }
        val denom = aVar.toLong() and 0xffffffffL
        var remainder = mantissa % denom
        mantissa /= denom
        // Normalizing mantissa and scaling remainder accordingly
        var clz = 0
        var t = (mantissa ushr 32).toInt()
        if (t == 0) {
            clz = 32
            t = mantissa.toInt()
        }
        t = t or (t shr 1)
        t = t or (t shr 2)
        t = t or (t shr 4)
        t = t or (t shr 8)
        t = t or (t shr 16)
        clz += clz_tab[(t * clz_magic) ushr 27] - 1
        mantissa = mantissa shl clz
        remainder = remainder shl clz
        exponent -= clz
        // Final division, correctly rounded
        remainder = (remainder + denom / 2) / denom
        mantissa += remainder
        if (exponent < 0) // Underflow
            makeZero(sign.toInt());
    }
    /**
     * Calculates the quotient of <code>a</code> and this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = a/this;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 3.1
     * </td></tr></table>
     *
     * @param a the <code>Real</code> to be divided by this.
     */
    fun rdiv(a: Real) {
        val recipTmp = recipTmp()
        recipTmp.assign(a)
        recipTmp.div(this)
        assign(recipTmp)
    }
    /**
     * Calculates the quotient of the integer <code>a</code> and this
     * <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = a/this;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 3.9
     * </td></tr></table>
     *
     * @param a the <code>Int</code> to be divided by this.
     */
    fun rdiv(a: Int) {
        val tmp0 = tmp0()
        tmp0.assign(a)
        rdiv(tmp0)
    }
    /**
     * Calculates the reciprocal of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = 1/this;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 2.3
     * </td></tr></table>
     */
    fun recip() {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return;
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            makeZero(sign.toInt());
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            makeInfinity(sign.toInt());
            return;
        }
        exponent = Int.MIN_VALUE - exponent
        if (mantissa == 0x4000000000000000L) {
            if (exponent < 0)
                makeInfinity(sign.toInt()); // Overflow
            return;
        }
        exponent--;
        mantissa = ldiv(Long.MIN_VALUE, mantissa)
    }
    /**
     * Calculates the reciprocal of this <code>Real</code> with
     * extended precision.
     * Replaces the contents of this <code>Real</code> with the result.
     * Returns the extra mantissa of the extended precision result.
     * <p/>
     * <p>An extra 64 bits of mantissa is added for extended precision.
     * If the argument is not of extended precision, use <code>0</code>
     * for the extra mantissa. See also {@link #add128(Long, Real, Long)}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = 1/this;</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2<sup>-60</sup> ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 17
     * </td></tr></table>
     *
     * @param extra the extra 64 bits of mantissa of this extended precision
     *              <code>Real</code>.
     * @return the extra 64 bits of mantissa of the resulting extended
     * precision <code>Real</code>.
     */
    fun recip128(extra: Long): Long {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return 0;
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            makeZero(sign.toInt());
            return 0;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            makeInfinity(sign.toInt());
            return 0;
        }
        val s = sign
        sign = 0
        // Special case, simple power of 2
        if (mantissa == 0x4000000000000000L && extra == 0L) {
            exponent = Int.MIN_VALUE - exponent
            if (exponent < 0) // Overflow
                makeInfinity(s.toInt());
            return 0;
        }
        // Normalize exponent
        val exp = 0x40000000 - exponent
        exponent = 0x40000000;
        // Save -A
        val recipTmp = recipTmp()
        recipTmp.assign(this)
        val recipTmpExtra = extra
        recipTmp.neg()
        // First establish approximate result (actually 63 bit accurate)
        recip();
        // Perform one Newton-Raphson iteration
        // Xn+1 = Xn + Xn*(1-A*Xn)
        val recipTmp2 = recipTmp2()
        recipTmp2.assign(this)
        var extraVar = mul128(0, recipTmp, recipTmpExtra)
        extraVar = add128(extraVar, ONE, 0)
        extraVar = mul128(extraVar, recipTmp2, 0)
        extraVar = add128(extraVar, recipTmp2, 0)
        // Fix exponent
        scalbn(exp);
        // Fix sign
        if (!isNan())
            sign = s
        return extraVar
    }
    /**
     * Calculates the mathematical integer that is less than or equal to
     * this <code>Real</code> divided by <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#floor(Double) floor}(this/a);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 22
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument.
     */
    fun divf(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            if ((a.exponent < 0 && a.mantissa == 0L))
                makeNan();
            return;
        }
        if ((a.exponent < 0 && a.mantissa == 0L)) {
            makeZero(sign.toInt());
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            if ((a.exponent == 0 && a.mantissa == 0L))
                makeNan();
            return;
        }
        if ((a.exponent == 0 && a.mantissa == 0L)) {
            makeInfinity(sign.toInt());
            return;
        }
        val tmp0 = tmp0()
        tmp0.assign(a)
        // tmp0 should be free
        // Perform same division as with mod, and don't round up
        var extra = tmp0.recip128(0)
        extra = mul128(0, tmp0, extra)
        if (((tmp0.sign.toInt() != 0) && (extra < 0 || extra > 0x1f)) ||
                (tmp0.sign.toInt() == 0 && extra < 0 && extra > 0xffffffe0)) {
            // For accurate floor()
            mantissa++
            normalize()
        }
        floor();
    }
    fun modInternal(a: Real, aExtra: Long) {
        val tmp0 = tmp0()
        tmp0.assign(a)
        // tmp0 should be free
        var extra = tmp0.recip128(aExtra)
        extra = tmp0.mul128(extra, this, 0/*thisExtra*/); // tmp0 == this/a
        if (tmp0.exponent > 0x4000003e) {
            // floor() will be inaccurate
            makeZero(a.sign.toInt()); // What else can be done? makeNan?
            return;
        }
        if (((tmp0.sign.toInt() != 0) && (extra < 0 || extra > 0x1f)) ||
                (tmp0.sign.toInt() == 0 && extra < 0 && extra > 0xffffffe0)) {
            // For accurate floor() with a bit of "magical rounding"
            tmp0.mantissa++
            tmp0.normalize()
        }
        tmp0.floor()
        tmp0.neg() // tmp0 == -floor(this/a)
        extra = tmp0.mul128(0, a, aExtra);
        extra = add128(0/*thisExtra*/, tmp0, extra);
        roundFrom128(extra)
    }
    /**
     * Calculates the value of this <code>Real</code> modulo <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The modulo in this case is defined as the remainder after subtracting
     * <code>a</code> multiplied by the mathematical integer that is less than
     * or equal to this <code>Real</code> divided by <code>a</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = this -
     * a*Math.{@link Math#floor(Double) floor}(this/a);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 27
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument.
     */
    fun mod(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            if ((a.exponent == 0 && a.mantissa == 0L))
                makeNan();
            else
                sign = a.sign;
            return;
        }
        if ((a.exponent < 0 && a.mantissa == 0L)) {
            if (sign != a.sign)
                makeInfinity(a.sign.toInt());
            return;
        }
        if ((a.exponent == 0 && a.mantissa == 0L)) {
            makeZero(a.sign.toInt());
            return;
        }
        modInternal(a, 0);
    }
    /**
     * Calculates the logical <i>AND</i> of this <code>Real</code> and
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>Semantics of bitwise logical operations exactly mimic those of
     * Java's bitwise integer operators. In these operations, the
     * internal binary representation of the numbers are used. If the
     * values represented by the operands are not mathematical
     * integers, the fractional bits are also included in the operation.
     * <p/>
     * <p>Negative numbers are interpreted as two's-complement,
     * generalized to real numbers: Negating the number inverts all
     * bits, including an infinite number of 1-bits before the radix
     * point and an infinite number of 1-bits after the radix point. The
     * infinite number of 1-bits after the radix is rounded upwards
     * producing an infinite number of 0-bits, until the first 0-bit is
     * encountered which will be switched to a 1 (rounded or not, these
     * two forms are mathematically equivalent). For example, the number
     * "1" negated, becomes (in binary form)
     * <code>...1111110.111111....</code> Rounding of the infinite
     * number of 1's after the radix gives the number
     * <code>...1111111.000000...</code>, which is exactly the way we
     * usually see "-1" as two's-complement.
     * <p/>
     * <p>This method calculates a negative value if and only
     * if this and <code>a</code> are both negative.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Int</code><i>&nbsp;code:</i></td><td>
     * <code>this &= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.5
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument
     */
    fun and(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            makeZero();
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            if (!(this.exponent < 0 && this.mantissa == 0L) && (this.sign.toInt() != 0)) {
                {
                    this.mantissa = a.mantissa;
                    this.exponent = a.exponent;
                    this.sign = a.sign;
                }
            } else if (!(a.exponent < 0 && a.mantissa == 0L) && (a.sign.toInt() != 0))
                ; // ASSIGN(this,this)
            else if ((this.exponent < 0 && this.mantissa == 0L) && (a.exponent < 0 && a.mantissa == 0L) &&
                    (this.sign.toInt() != 0) && (a.sign.toInt() != 0))
                ; // makeInfinity(1)
            else
                makeZero();
            return;
        }
        var s: Byte
        var e: Int
        var m: Long
        if (exponent >= a.exponent) {
            s = a.sign;
            e = a.exponent;
            m = a.mantissa;
        } else {
            s = sign;
            e = exponent;
            m = mantissa;
            sign = a.sign;
            exponent = a.exponent;
            mantissa = a.mantissa;
        }
        val shift = exponent - e
        if (shift >= 64) {
            if (s.toInt() == 0)
                makeZero(sign.toInt());
            return;
        }
        if (s.toInt() != 0)
            m = -m;
        if ((this.sign.toInt() != 0))
            mantissa = -mantissa;
        mantissa = mantissa and (m shr shift)
        sign = 0
        if (mantissa < 0) {
            mantissa = -mantissa
            sign = 1
        }
        normalize()
    }
    /**
     * Calculates the logical <i>OR</i> of this <code>Real</code> and
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>See {@link #and(Real)} for an explanation of the
     * interpretation of a <code>Real</code> in bitwise operations.
     * This method calculates a negative value if and only
     * if either this or <code>a</code> is negative.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Int</code><i>&nbsp;code:</i></td><td>
     * <code>this |= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.6
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument
     */
    fun or(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            if ((this.exponent == 0 && this.mantissa == 0L)) {
                this.mantissa = a.mantissa;
                this.exponent = a.exponent;
                this.sign = a.sign;
            }
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            if (!(this.exponent < 0 && this.mantissa == 0L) && (this.sign.toInt() != 0))
                ; // ASSIGN(this,this);
            else if (!(a.exponent < 0 && a.mantissa == 0L) && (a.sign.toInt() != 0)) {
                {
                    this.mantissa = a.mantissa;
                    this.exponent = a.exponent;
                    this.sign = a.sign;
                }
            } else
                makeInfinity(sign.toInt() or a.sign.toInt());
            return;
        }
        var s: Byte
        var e: Int
        var m: Long
        if (((this.sign.toInt() != 0) && exponent <= a.exponent) ||
                ((a.sign.toInt() == 0) && exponent >= a.exponent)) {
            s = a.sign;
            e = a.exponent;
            m = a.mantissa;
        } else {
            s = sign;
            e = exponent;
            m = mantissa;
            sign = a.sign;
            exponent = a.exponent;
            mantissa = a.mantissa;
        }
        val shift = exponent - e
        if (shift >= 64 || shift <= -64)
            return;
        if (s.toInt() != 0)
            m = -m;
        if ((this.sign.toInt() != 0))
            mantissa = -mantissa;
        if (shift >= 0)
            mantissa = mantissa or (m shr shift)
        else
            mantissa = mantissa or (m shl (-shift))
        sign = 0
        if (mantissa < 0) {
            mantissa = -mantissa
            sign = 1
        }
        normalize()
    }
    /**
     * Calculates the logical <i>XOR</i> of this <code>Real</code> and
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>See {@link #and(Real)} for an explanation of the
     * interpretation of a <code>Real</code> in bitwise operations.
     * This method calculates a negative value if and only
     * if exactly one of this and <code>a</code> is negative.
     * <p/>
     * <p>The operation <i>NOT</i> has been omitted in this library
     * because it cannot be generalized to fractional numbers. If this
     * <code>Real</code> represents a mathematical integer, the
     * operation <i>NOT</i> can be calculated as "this <i>XOR</i> -1",
     * which is equivalent to "this <i>XOR</i>
     * <code>/FFFFFFFF.0000</code>".
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Int</code><i>&nbsp;code:</i></td><td>
     * <code>this ^= a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.5
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument
     */
    fun xor(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L)) {
            if ((this.exponent == 0 && this.mantissa == 0L)) {
                this.mantissa = a.mantissa;
                this.exponent = a.exponent;
                this.sign = a.sign;
            }
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            makeInfinity(sign.toInt() xor a.sign.toInt());
            return;
        }
        var s: Byte
        var e: Int
        var m: Long
        if (exponent >= a.exponent) {
            s = a.sign;
            e = a.exponent;
            m = a.mantissa;
        } else {
            s = sign;
            e = exponent;
            m = mantissa;
            sign = a.sign;
            exponent = a.exponent;
            mantissa = a.mantissa;
        }
        val shift = exponent - e
        if (shift >= 64)
            return;
        if (s.toInt() != 0)
            m = -m;
        if ((this.sign.toInt() != 0))
            mantissa = -mantissa;
        mantissa = mantissa xor (m shr shift)
        sign = 0
        if (mantissa < 0) {
            mantissa = -mantissa
            sign = 1
        }
        normalize()
    }
    /**
     * Calculates the value of this <code>Real</code> <i>AND NOT</i>
     * <code>a</code>. The opeation is read as "bit clear".
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>See {@link #and(Real)} for an explanation of the
     * interpretation of a <code>Real</code> in bitwise operations.
     * This method calculates a negative value if and only
     * if this is negative and not <code>a</code> is negative.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Int</code><i>&nbsp;code:</i></td><td>
     * <code>this &= ~a;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 1.5
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument
     */
    fun bic(a: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L) || (a.exponent == 0 && a.mantissa == 0L))
            return;
        if ((this.exponent < 0 && this.mantissa == 0L) || (a.exponent < 0 && a.mantissa == 0L)) {
            if (!(this.exponent < 0 && this.mantissa == 0L)) {
                if ((this.sign.toInt() != 0))
                    if ((a.sign.toInt() != 0))
                        makeInfinity(0);
                    else
                        makeInfinity(1);
            } else if ((a.sign.toInt() != 0)) {
                if ((a.exponent < 0 && a.mantissa == 0L))
                    makeInfinity(0);
                else
                    makeZero();
            }
            return;
        }
        val shift = exponent - a.exponent
        if (shift >= 64 || (shift <= -64 && (this.sign.toInt() == 0)))
            return;
        var m = a.mantissa
        if ((a.sign.toInt() != 0))
            m = -m;
        if ((this.sign.toInt() != 0))
            mantissa = -mantissa;
        if (shift < 0) {
            if ((this.sign.toInt() != 0)) {
                if (shift <= -64)
                    mantissa = m.inv()
                else
                    mantissa = (mantissa shr (-shift)) and m.inv()
                exponent = a.exponent;
            } else
                mantissa = mantissa and (m shl (-shift)).inv()
        } else
            mantissa = mantissa and (m shr shift).inv()
        sign = 0
        if (mantissa < 0) {
            mantissa = -mantissa
            sign = 1
        }
        normalize()
    }
    fun compare(a: Int): Int {
        val tmp0 = tmp0()
        tmp0.assign(a)
        return compare(tmp0)
    }
    /**
     * Calculates the square root of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#sqrt(Double) sqrt}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 19
     * </td></tr></table>
     */
    fun sqrt() {
        /*
         * Adapted from:
         * Cephes Math Library Release 2.2:  December, 1990
         * Copyright 1984, 1990 by Stephen L. Moshier
         *
         * sqrtl.c
         *
         * Long Double sqrtl(Long Double x);
         */
        if ((this.exponent < 0 && this.mantissa != 0L))
            return;
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            sign = 0
            return;
        }
        if ((this.sign.toInt() != 0)) {
            makeNan();
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L))
            return;
        // Save X
        val recipTmp = recipTmp()
        recipTmp.assign(this)
        // normalize to range [0.5, 1)
        val e = exponent - 0x3fffffff
        exponent = 0x3fffffff
        // quadratic approximation, relative error 6.45e-4
        val recipTmp2 = recipTmp2()
        recipTmp2.assign(this)
        val sqrtTmp = sqrtTmp()
        sqrtTmp.sign = 1
        sqrtTmp.exponent = 0x3ffffffd
        sqrtTmp.mantissa = 0x68a7e193370ff21bL
        //-0.2044058315473477195990
        mul(sqrtTmp)
        sqrtTmp.sign = 0
        sqrtTmp.exponent = 0x3fffffff
        sqrtTmp.mantissa = 0x71f1e120690deae8L
        //0.89019407351052789754347
        add(sqrtTmp)
        mul(recipTmp2)
        sqrtTmp.sign = 0
        sqrtTmp.exponent = 0x3ffffffe
        sqrtTmp.mantissa = 0x5045ee6baf28677aL
        //0.31356706742295303132394
        add(sqrtTmp)
        // adjust for odd powers of 2
        if ((e and 1) != 0)
            mul(SQRT2)
        // calculate exponent
        exponent += e shr 1
        // Newton iteratios:
        //   Yn+1 = (Yn + X/Yn)/2
        for (i in 0 until 3) {
            recipTmp2.assign(recipTmp)
            recipTmp2.div(this)
            add(recipTmp2)
            scalbn(-1)
        }
    }
    /**
     * Calculates the reciprocal square root of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = 1/Math.{@link Math#sqrt(Double) sqrt}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 21
     * </td></tr></table>
     */
    fun rsqrt() {
        sqrt()
        recip()
    }
    /**
     * Calculates the cube root of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The cube root of a negative value is the negative of the cube
     * root of that value's magnitude.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#cbrt(Double) cbrt}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 32
     * </td></tr></table>
     */
    fun cbrt() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return;
        val s = sign
        sign = 0
        // Calculates recipocal cube root of normalized Real,
        // not zero, nan or infinity
        val start = 0x5120000000000000L
        // Save -A
        val recipTmp = recipTmp()
        recipTmp.assign(this)
        recipTmp.neg()
        // First establish approximate result
        mantissa = start - (mantissa ushr 2)
        val expRmd = if (exponent == 0) 2 else (exponent - 1) % 3
        exponent = 0x40000000 - (exponent - 0x40000000 - expRmd) / 3
        normalize()
        val recipTmp2 = recipTmp2()
        if (expRmd > 0) {
            recipTmp2.sign = 0
            recipTmp2.exponent = 0x3fffffff
            recipTmp2.mantissa = 0x6597fa94f5b8f20bL
            // cbrt(1/2)
            mul(recipTmp2)
            if (expRmd > 1)
                mul(recipTmp2)
        }
        // Now perform Newton-Raphson iteration
        // Xn+1 = (4*Xn - A*Xn**4)/3
        for (i in 0 until 4) {
            recipTmp2.assign(this)
            sqr()
            sqr()
            mul(recipTmp)
            recipTmp2.scalbn(2)
            add(recipTmp2)
            mul(THIRD)
        }
        recip()
        if (!(this.exponent < 0 && this.mantissa != 0L))
            sign = s
    }
    /**
     * Calculates the n'th root of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * For odd integer n, the n'th root of a negative value is the
     * negative of the n'th root of that value's magnitude.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#pow(Double, Double)
     * pow}(this,1/a);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 110
     * </td></tr></table>
     *
     * @param n the <code>Real</code> argument.
     */
    fun nroot(n: Real) {
        if ((n.exponent < 0 && n.mantissa != 0L)) {
            makeNan();
            return;
        }
        if (n.compare(THREE) == 0) {
            cbrt(); // Most probable application of nroot...
            return;
        } else if (n.compare(TWO) == 0) {
            sqrt(); // Also possible, should be optimized like this
            return;
        }
        var negative = false
        if ((this.sign.toInt() != 0) && n.isIntegral() && n.isOdd()) {
            negative = true;
            abs();
        }
        val tmp2 = tmp2()
        tmp2.assign(n);
        // Copy to temporary location in case of x.nroot(x)
        tmp2.recip();
        pow(tmp2);
        if (negative)
            neg();
    }
    /**
     * Calculates <code>sqrt(this*this+a*a)</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#hypot(Double, Double)
     * hypot}(this,a);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 24
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument.
     */
    fun hypot(a: Real) {
        val tmp1 = tmp1()
        tmp1.assign(this);
        // Copy to temporary location in case of x.hypot(x)
        tmp1.sqr();
        sqr();
        add(tmp1);
        sqrt();
    }
    fun exp2Internal(extra: Long) {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return;
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            if ((this.sign.toInt() != 0))
                makeZero(0);
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            {
                this.mantissa = ONE.mantissa;
                this.exponent = ONE.exponent;
                this.sign = ONE.sign;
            }
            return;
        }
        // Extract integer part
        val expTmp = expTmp()
        expTmp.assign(this);
        expTmp.add(HALF);
        expTmp.floor();
        val exp = expTmp.toInteger()
        if (exp > 0x40000000) {
            makeInfinity(sign.toInt());
            return;
        }
        if (exp < -0x40000000) {
            makeZero(sign.toInt());
            return;
        }
        // Subtract integer part (this is where we need the extra accuracy)
        expTmp.neg();
        add128(extra, expTmp, 0);
        /*
         * Adapted from:
         * Cephes Math Library Release 2.7:  May, 1998
         * Copyright 1984, 1991, 1998 by Stephen L. Moshier
         *
         * exp2l.c
         *
         * Long Double exp2l(Long Double x);
         */
        // Now -0.5<X<0.5
        // rational approximation
        // exp2(x) = 1 + 2x P(x²)/(Q(x²) - x P(x²))
        val expTmp2 = expTmp2()
        expTmp2.assign(this)
        expTmp2.sqr()
        // P(x²)
        expTmp.sign = 0
        expTmp.exponent = 0x40000005
        expTmp.mantissa = 0x793ace15b56b7fecL
        //60.614853552242266094567
        expTmp.mul(expTmp2)
        val expTmp3 = expTmp3()
        expTmp3.sign = 0
        expTmp3.exponent = 0x4000000e
        expTmp3.mantissa = 0x764ef8cf96e29a13L
        //30286.971917562792508623
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000014
        expTmp3.mantissa = 0x7efa0173e820bf60L
        //2080384.3631901852422887
        expTmp.add(expTmp3)
        mul(expTmp)
        // Q(x²)
        expTmp.assign(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x4000000a
        expTmp3.mantissa = 0x6d549a6b4dc9abadL
        //1749.2876999891839021063
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000012
        expTmp3.mantissa = 0x5002d27836ba71c6L
        //327725.15434906797273099
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000016
        expTmp3.mantissa = 0x5b98206867dd59bfL
        //6002720.4078348487957118
        expTmp.add(expTmp3)
        expTmp.sub(this)
        div(expTmp)
        scalbn(1)
        add(ONE)
        // Scale by power of 2
        scalbn(exp)
    }
    /**
     * Calculates <i>e</i> raised to the power of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#exp(Double) exp}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 31
     * </td></tr></table>
     */
    fun exp() {
        val expTmp = expTmp()
        expTmp.sign = 0
        expTmp.exponent = 0x40000000
        expTmp.mantissa = 0x5c551d94ae0bf85dL
        // log2(e)
        val extra = mul128(0, expTmp, 0xdf43ff68348e9f44uL.toLong())
        exp2Internal(extra)
    }
    /**
     * Calculates 2 raised to the power of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#exp(Double) exp}(this *
     * Math.{@link Math#log(Double) log}(2));</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 27
     * </td></tr></table>
     */
    fun exp2() {
        exp2Internal(0)
    }
    /**
     * Calculates 10 raised to the power of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#exp(Double) exp}(this *
     * Math.{@link Math#log(Double) log}(10));</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 31
     * </td></tr></table>
     */
    fun exp10() {
        val expTmp = expTmp()
        expTmp.sign = 0
        expTmp.exponent = 0x40000001
        expTmp.mantissa = 0x6a4d3c25e68dc57fL
        // log2(10)
        val extra = mul128(0, expTmp, 0x2495fb7fa6d7eda6L)
        exp2Internal(extra)
    }
    fun lnInternal(): Int {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return 0;
        if ((this.sign.toInt() != 0)) {
            makeNan();
            return 0;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            makeInfinity(1);
            return 0;
        }
        if ((this.exponent < 0 && this.mantissa == 0L))
            return 0;
        /*
         * Adapted from:
         * Cephes Math Library Release 2.7:  May, 1998
         * Copyright 1984, 1990, 1998 by Stephen L. Moshier
         *
         * logl.c
         *
         * Long Double logl(Long Double x);
         */
        // normalize to range [0.5, 1)
        var e = exponent - 0x3fffffff
        exponent = 0x3fffffff;
        // rational appriximation
        // log(1+x) = x - x²/2 + x³ P(x)/Q(x)
        if (this.compare(SQRT1_2) < 0) {
            e--;
            exponent++;
        }
        sub(ONE);
        val expTmp2 = expTmp2()
        expTmp2.assign(this);
        // P(x)
        this.sign = 0
        this.exponent = 0x3ffffff1
        this.mantissa = 0x5ef0258ace5728ddL
        //4.5270000862445199635215E-5
        mul(expTmp2)
        val expTmp3 = expTmp3()
        expTmp3.sign = 0
        expTmp3.exponent = 0x3ffffffe
        expTmp3.mantissa = 0x7fa06283f86a0ce8L
        //0.4985410282319337597221
        add(expTmp3)
        mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000002
        expTmp3.mantissa = 0x69427d1bd3e94ca1L
        //6.5787325942061044846969
        add(expTmp3)
        mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000004
        expTmp3.mantissa = 0x77a5ce2e32e7256eL
        //29.911919328553073277375
        add(expTmp3)
        mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000005
        expTmp3.mantissa = 0x79e63ae1b0cd4222L
        //60.949667980987787057556
        add(expTmp3)
        mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000005
        expTmp3.mantissa = 0x7239d65d1e6840d6L
        //57.112963590585538103336
        add(expTmp3)
        mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000004
        expTmp3.mantissa = 0x502880b6660c265fL
        //20.039553499201281259648
        add(expTmp3)
        // Q(x)
        val expTmp = expTmp()
        expTmp.assign(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000003
        expTmp3.mantissa = 0x7880d67a40f8dc5cL
        //15.062909083469192043167
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000006
        expTmp3.mantissa = 0x530c2d4884d25e18L
        //83.047565967967209469434
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000007
        expTmp3.mantissa = 0x6ee19643f3ed5776L
        //221.76239823732856465394
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000008
        expTmp3.mantissa = 0x4d465177242295efL
        //309.09872225312059774938
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000007
        expTmp3.mantissa = 0x6c36c4f923819890L
        //216.42788614495947685003
        expTmp.add(expTmp3)
        expTmp.mul(expTmp2)
        expTmp3.sign = 0
        expTmp3.exponent = 0x40000005
        expTmp3.mantissa = 0x783cc111991239a3L
        //60.118660497603843919306
        expTmp.add(expTmp3)
        div(expTmp)
        expTmp3.mantissa = expTmp2.mantissa
        expTmp3.exponent = expTmp2.exponent
        expTmp3.sign = expTmp2.sign
        expTmp3.sqr();
        mul(expTmp3);
        mul(expTmp2);
        expTmp3.scalbn(-1);
        sub(expTmp3);
        add(expTmp2);
        return e;
    }
    /**
     * Calculates the natural logarithm (base-<i>e</i>) of this
     * <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#log(Double) log}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 51
     * </td></tr></table>
     */
    fun ln() {
        val exp = lnInternal()
        val expTmp = expTmp()
        expTmp.assign(exp);
        expTmp.mul(LN2);
        add(expTmp);
    }
    /**
     * Calculates the base-2 logarithm of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#log(Double) log}(this)/Math.{@link
     * Math#log(Double) log}(2);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 51
     * </td></tr></table>
     */
    fun log2() {
        val exp = lnInternal()
        mul(LOG2E);
        add(exp);
    }
    /**
     * Calculates the base-10 logarithm of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#log10(Double) log10}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 53
     * </td></tr></table>
     */
    fun log10() {
        val exp = lnInternal()
        val expTmp = expTmp()
        expTmp.assign(exp);
        expTmp.mul(LN2);
        add(expTmp);
        mul(LOG10E);
    }
    /**
     * Calculates the closest power of 10 that is less than or equal to this
     * <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The base-10 exponent of the result is returned.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>Int exp = (Int)(Math.{@link Math#floor(Double)
     * floor}(Math.{@link Math#log10(Double) log10}(this)));
     * <br>this = Math.{@link Math#pow(Double, Double) pow}(10, exp);<br>
     * return exp;</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 3.6
     * </td></tr></table>
     *
     * @return the base-10 exponent
     */
    fun lowPow10(): Int {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return 0
        val tmp2 = tmp2()
        tmp2.assign(this)
        // Approximate log10 using exponent only
        var e = exponent - 0x40000000
        if (e < 0) // it's important to achieve floor(exponent*ln2/ln10)
            e = -((((-e).toLong() * 0x4d104d43L + ((1L shl 32) - 1)) shr 32).toInt())
        else
            e = ((e.toLong() * 0x4d104d43L) shr 32).toInt()
        // Now, e < log10(this) < e+1
        this.mantissa = TEN.mantissa
        this.exponent = TEN.exponent
        this.sign = TEN.sign
        pow(e)
        val tmp3 = tmp3()
        if ((this.exponent == 0 && this.mantissa == 0L)) { // A *really* small number, then
            tmp3.assign(TEN)
            tmp3.pow(e + 1)
        } else {
            tmp3.assign(this)
            tmp3.mul10()
        }
        if (tmp3.compare(tmp2) <= 0) {
            // First estimate of log10 was too low
            e++
            assign(tmp3)
        }
        return e
    }
    /**
     * Calculates the value of this <code>Real</code> raised to the power of
     * <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p> Special cases:
     * <ul>
     * <li> if a is 0.0 or -0.0 then result is 1.0
     * <li> if a is NaN then result is NaN
     * <li> if this is NaN and a is not zero then result is NaN
     * <li> if a is 1.0 then result is this
     * <li> if |this| > 1.0 and a is +Infinity then result is +Infinity
     * <li> if |this| < 1.0 and a is -Infinity then result is +Infinity
     * <li> if |this| > 1.0 and a is -Infinity then result is +0
     * <li> if |this| < 1.0 and a is +Infinity then result is +0
     * <li> if |this| = 1.0 and a is ±Infinity then result is NaN
     * <li> if this = +0 and a > 0 then result is +0
     * <li> if this = +0 and a < 0 then result is +Inf
     * <li> if this = -0 and a > 0, and odd integer then result is -0
     * <li> if this = -0 and a < 0, and odd integer then result is -Inf
     * <li> if this = -0 and a > 0, not odd integer then result is +0
     * <li> if this = -0 and a < 0, not odd integer then result is +Inf
     * <li> if this = +Inf and a > 0 then result is +Inf
     * <li> if this = +Inf and a < 0 then result is +0
     * <li> if this = -Inf and a not integer then result is NaN
     * <li> if this = -Inf and a > 0, and odd integer then result is -Inf
     * <li> if this = -Inf and a > 0, not odd integer then result is +Inf
     * <li> if this = -Inf and a < 0, and odd integer then result is -0
     * <li> if this = -Inf and a < 0, not odd integer then result is +0
     * <li> if this < 0 and a not integer then result is NaN
     * <li> if this < 0 and a odd integer then result is -(|this|<sup>a</sup>)
     * <li> if this < 0 and a not odd integer then result is |this|<sup>a</sup>
     * <li> else result is exp(ln(this)*a)
     * </ul>
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#pow(Double, Double) pow}(this, a);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 110
     * </td></tr></table>
     *
     * @param a the <code>Real</code> argument.
     */
    fun pow(a: Real) {
        if ((a.exponent == 0 && a.mantissa == 0L)) {
            {
                this.mantissa = ONE.mantissa;
                this.exponent = ONE.exponent;
                this.sign = ONE.sign;
            }
            return;
        }
        if ((this.exponent < 0 && this.mantissa != 0L) || (a.exponent < 0 && a.mantissa != 0L)) {
            makeNan();
            return;
        }
        if (a.compare(ONE) == 0)
            return;
        val tmp1 = tmp1()
        if ((a.exponent < 0 && a.mantissa == 0L)) {
            tmp1.assign(this);
            tmp1.abs();
            val test = tmp1.compare(ONE)
            if (test > 0) {
                if ((a.sign.toInt() == 0))
                    makeInfinity(0);
                else
                    makeZero();
            } else if (test < 0) {
                if ((a.sign.toInt() != 0))
                    makeInfinity(0);
                else
                    makeZero();
            } else {
                makeNan();
            }
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            if ((this.sign.toInt() == 0)) {
                if ((a.sign.toInt() == 0))
                    makeZero();
                else
                    makeInfinity(0);
            } else {
                if (a.isIntegral() && a.isOdd()) {
                    if ((a.sign.toInt() == 0))
                        makeZero(1);
                    else
                        makeInfinity(1);
                } else {
                    if ((a.sign.toInt() == 0))
                        makeZero();
                    else
                        makeInfinity(0);
                }
            }
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            if ((this.sign.toInt() == 0)) {
                if ((a.sign.toInt() == 0))
                    makeInfinity(0);
                else
                    makeZero();
            } else {
                if (a.isIntegral()) {
                    if (a.isOdd()) {
                        if ((a.sign.toInt() == 0))
                            makeInfinity(1);
                        else
                            makeZero(1);
                    } else {
                        if ((a.sign.toInt() == 0))
                            makeInfinity(0);
                        else
                            makeZero();
                    }
                } else {
                    makeNan();
                }
            }
            return;
        }
        if (a.isIntegral() && a.exponent <= 0x4000001e) {
            pow(a.toInteger());
            return;
        }
        var s = 0
        if ((this.sign.toInt() != 0)) {
            if (a.isIntegral()) {
                if (a.isOdd())
                    s = 1
            } else {
                makeNan();
                return;
            }
            sign = 0
        }
        tmp1.assign(a)
        val tmp2 = tmp2()
        val tmp3 = tmp3()
        if (tmp1.exponent <= 0x4000001e) {
            // For increased accuracy, exponentiate with integer part of
            // exponent by successive squaring
            // (I really don't know why this works)
            tmp2.assign(tmp1)
            tmp2.floor()
            tmp3.assign(this)
            tmp3.pow(tmp2.toInteger())
            tmp1.sub(tmp2)
        } else {
            tmp3.assign(ONE)
        }
        // Do log2 and maintain accuracy
        val e = lnInternal()
        tmp2.sign = 0
        tmp2.exponent = 0x40000000
        tmp2.mantissa = 0x5c551d94ae0bf85dL
        // log2(e)
        var extra = mul128(0, tmp2, 0xdf43ff68348e9f44uL.toLong())
        tmp2.assign(e)
        extra = add128(extra, tmp2, 0)
        // Do exp2 of this multiplied by (fractional part of) exponent
        extra = tmp1.mul128(0, this, extra)
        tmp1.exp2Internal(extra)
        this.mantissa = tmp1.mantissa
        this.exponent = tmp1.exponent
        this.sign = tmp1.sign
        mul(tmp3)
        if (!(this.exponent < 0 && this.mantissa != 0L))
            sign = s.toByte()
    }
    /**
     * Calculates the value of this <code>Real</code> raised to the power of
     * the integer <code>a</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#pow(Double, Double) pow}(this, a);</code>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 84
     * </td></tr></table>
     *
     * @param a the integer argument.
     */
    fun pow(a: Int) {
        // Calculate power of integer by successive squaring
        var recp = false
        var aVar = a
        if (aVar < 0) {
            aVar = -aVar // Also works for 0x80000000
            recp = true
        }
        var extra = 0L
        var expTmpExtra = 0L
        val expTmp = expTmp()
        expTmp.assign(this)
        assign(ONE)
        while (aVar != 0) {
            if ((aVar and 1) != 0)
                extra = mul128(extra, expTmp, expTmpExtra)
            expTmpExtra = expTmp.mul128(expTmpExtra, expTmp, expTmpExtra)
            aVar = aVar ushr 1
        }
        if (recp)
            extra = recip128(extra)
        roundFrom128(extra)
    }
    fun sinInternal() {
        /*
         * Adapted from:
         * Cephes Math Library Release 2.7:  May, 1998
         * Copyright 1985, 1990, 1998 by Stephen L. Moshier
         *
         * sinl.c
         *
         * Long Double sinl(Long Double x);
         */
        // X<PI/4
        // polynomial approximation
        // sin(x) = x + x³ P(x²)
        val tmp1 = tmp1()
        tmp1.assign(this)
        val tmp2 = tmp2()
        tmp2.assign(this)
        tmp2.sqr()
        this.sign = 1
        this.exponent = 0x3fffffd7
        this.mantissa = 0x6aa891c4f0eb2713L
        //-7.578540409484280575629E-13
        mul(tmp2)
        val tmp3 = tmp3()
        tmp3.sign = 0
        tmp3.exponent = 0x3fffffdf
        tmp3.mantissa = 0x58482311f383326cL
        //1.6058363167320443249231E-10
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 1
        tmp3.exponent = 0x3fffffe6
        tmp3.mantissa = 0x6b9914a35f9a00d8L
        //-2.5052104881870868784055E-8
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 0
        tmp3.exponent = 0x3fffffed
        tmp3.mantissa = 0x5c778e94cc22e47bL
        //2.7557319214064922217861E-6
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 1
        tmp3.exponent = 0x3ffffff3
        tmp3.mantissa = 0x680680680629b28aL
        //-1.9841269841254799668344E-4
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 0
        tmp3.exponent = 0x3ffffff9
        tmp3.mantissa = 0x4444444444442b4dL
        //8.3333333333333225058715E-3
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 1
        tmp3.exponent = 0x3ffffffd
        tmp3.mantissa = 0x555555555555554cL
        //-1.6666666666666666640255E-1
        add(tmp3)
        mul(tmp2)
        mul(tmp1)
        add(tmp1)
    }
    fun cosInternal() {
        /*
         * Adapted from:
         * Cephes Math Library Release 2.7:  May, 1998
         * Copyright 1985, 1990, 1998 by Stephen L. Moshier
         *
         * sinl.c
         *
         * Long Double cosl(Long Double x);
         */
        // X<PI/4
        // polynomial approximation
        // cos(x) = 1 - x²/2 + x**4 Q(x²)
        {
            val tmp1 = tmp1()
            tmp1.assign(this)
        }
        val tmp2 = tmp2()
        tmp2.assign(this)
        tmp2.sqr()
        this.sign = 0
        this.exponent = 0x3fffffd3
        this.mantissa = 0x6aaf461d37ccba1bL
        //4.7377507964246204691685E-14
        mul(tmp2)
        val tmp3 = tmp3()
        tmp3.sign = 1
        tmp3.exponent = 0x3fffffdb
        tmp3.mantissa = 0x64e4c907ac7a179bL
        //-1.147028484342535976567E-11
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 0
        tmp3.exponent = 0x3fffffe3
        tmp3.mantissa = 0x47bb632432cf29a8L
        //2.0876754287081521758361E-9
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 1
        tmp3.exponent = 0x3fffffea
        tmp3.mantissa = 0x49f93edd7ae32696L
        //-2.7557319214999787979814E-7
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 0
        tmp3.exponent = 0x3ffffff0
        tmp3.mantissa = 0x68068068063329f7L
        //2.4801587301570552304991E-5L
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 1
        tmp3.exponent = 0x3ffffff6
        tmp3.mantissa = 0x5b05b05b05b03db3L
        //-1.3888888888888872993737E-3
        add(tmp3)
        mul(tmp2)
        tmp3.sign = 0
        tmp3.exponent = 0x3ffffffb
        tmp3.mantissa = 0x555555555555554dL
        //4.1666666666666666609054E-2
        add(tmp3)
        mul(tmp2)
        sub(HALF)
        mul(tmp2)
        add(ONE)
    }
    /**
     * Calculates the trigonometric sine of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The input value is treated as an angle measured in radians.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#sin(Double) sin}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 28
     * </td></tr></table>
     */
    fun sin() {
        if (!(this.exponent >= 0 && this.mantissa != 0L)) {
            if (!(this.exponent == 0))
                makeNan();
            return;
        }
        // Since sin(-x) = -sin(x) we can make sure that x > 0
        var negative = false
        if ((this.sign.toInt() != 0)) {
            abs();
            negative = true;
        }
        // Then reduce the argument to the range of 0 < x < pi*2
        if (this.compare(PI2) > 0)
            modInternal(PI2, 0x62633145c06e0e69L);
        // Since sin(pi*2 - x) = -sin(x) we can reduce the range 0 < x < pi
        if (this.compare(PI) > 0) {
            sub(PI2);
            neg();
            negative = !negative;
        }
        // Since sin(x) = sin(pi - x) we can reduce the range to 0 < x < pi/2
        if (this.compare(PI_2) > 0) {
            sub(PI);
            neg();
        }
        // Since sin(x) = cos(pi/2 - x) we can reduce the range to 0 < x < pi/4
        if (this.compare(PI_4) > 0) {
            sub(PI_2);
            neg();
            cosInternal();
        } else {
            sinInternal();
        }
        if (negative)
            neg();
        if ((this.exponent == 0 && this.mantissa == 0L))
            abs(); // Remove confusing "-"
    }
    /**
     * Calculates the trigonometric cosine of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The input value is treated as an angle measured in radians.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#cos(Double) cos}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 1 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 37
     * </td></tr></table>
     */
    fun cos() {
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            {
                this.mantissa = ONE.mantissa;
                this.exponent = ONE.exponent;
                this.sign = ONE.sign;
            }
            return;
        }
        if ((this.sign.toInt() != 0))
            abs();
        if (this.compare(PI_4) < 0) {
            cosInternal();
        } else {
            add(PI_2);
            sin();
        }
    }
    /**
     * Calculates the trigonometric tangent of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The input value is treated as an angle measured in radians.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#tan(Double) tan}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 70
     * </td></tr></table>
     */
    fun tan() {
        val tmp4 = tmp4()
        tmp4.assign(this);
        tmp4.cos();
        sin();
        div(tmp4);
    }
    /**
     * Calculates the trigonometric arc sine of this <code>Real</code>,
     * in the range -&pi;/2 to &pi;/2.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#asin(Double) asin}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 3 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 68
     * </td></tr></table>
     */
    fun asin() {
        val tmp1 = tmp1()
        tmp1.assign(this);
        sqr();
        neg();
        add(ONE);
        rsqrt();
        mul(tmp1);
        atan();
    }
    /**
     * Calculates the trigonometric arc cosine of this <code>Real</code>,
     * in the range 0.0 to &pi;.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#acos(Double) acos}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 67
     * </td></tr></table>
     */
    fun acos() {
        val negative = (this.sign.toInt() != 0)
        abs();
        val tmp1 = tmp1()
        tmp1.assign(this);
        sqr();
        neg();
        add(ONE);
        sqrt();
        div(tmp1);
        atan();
        if (negative) {
            neg();
            add(PI);
        }
    }
    /**
     * Calculates the trigonometric arc tangent of this <code>Real</code>,
     * in the range -&pi;/2 to &pi;/2.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#atan(Double) atan}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 37
     * </td></tr></table>
     */
    fun atan() {
        /*
         * Adapted from:
         * Cephes Math Library Release 2.7:  May, 1998
         * Copyright 1984, 1990, 1998 by Stephen L. Moshier
         *
         * atanl.c
         *
         * Long Double atanl(Long Double x);
         */
        if ((this.exponent == 0 && this.mantissa == 0L) || (this.exponent < 0 && this.mantissa != 0L))
            return;
        if ((this.exponent < 0 && this.mantissa == 0L)) {
            val s = sign
            this.mantissa = PI_2.mantissa
            this.exponent = PI_2.exponent
            this.sign = PI_2.sign
            sign = s
            return
        }
        val s = sign
        sign = 0
        // range reduction
        var addPI_2 = false
        var addPI_4 = false
        val tmp1 = tmp1()
        tmp1.assign(SQRT2)
        tmp1.add(ONE)
        if (this.compare(tmp1) > 0) {
            addPI_2 = true
            recip()
            neg()
        } else {
            tmp1.sub(TWO)
            if (this.compare(tmp1) > 0) {
                addPI_4 = true
                tmp1.assign(this)
                tmp1.add(ONE)
                sub(ONE)
                div(tmp1)
            }
        }
        // Now |X|<sqrt(2)-1
        // rational approximation
        // atan(x) = x + x³ P(x²)/Q(x²)
        tmp1.assign(this)
        val tmp2 = tmp2()
        tmp2.assign(this)
        tmp2.sqr()
        mul(tmp2)
        val tmp3 = tmp3()
        tmp3.sign = 1
        tmp3.exponent = 0x3fffffff
        tmp3.mantissa = 0x6f2f89336729c767L
        //-0.8686381817809218753544
        tmp3.mul(tmp2)
        val tmp4 = tmp4()
        tmp4.sign = 1
        tmp4.exponent = 0x40000003
        tmp4.mantissa = 0x7577d35fd03083f3L
        //-14.683508633175792446076
        tmp3.add(tmp4)
        tmp3.mul(tmp2)
        tmp4.sign = 1
        tmp4.exponent = 0x40000005
        tmp4.mantissa = 0x7ff42abff948a9f7L
        //-63.976888655834347413154
        tmp3.add(tmp4)
        tmp3.mul(tmp2)
        tmp4.sign = 1
        tmp4.exponent = 0x40000006
        tmp4.mantissa = 0x63fd1f9f76d37cebL
        //-99.988763777265819915721
        tmp3.add(tmp4)
        tmp3.mul(tmp2)
        tmp4.sign = 1
        tmp4.exponent = 0x40000005
        tmp4.mantissa = 0x65c9c9b0b55e5b62L
        //-50.894116899623603312185
        tmp3.add(tmp4)
        mul(tmp3)
        tmp3.assign(tmp2)
        tmp4.sign = 0
        tmp4.exponent = 0x40000004
        tmp4.mantissa = 0x5bed73b744a72a6aL
        //22.981886733594175366172
        tmp3.add(tmp4)
        tmp3.mul(tmp2)
        tmp4.sign = 0
        tmp4.exponent = 0x40000007
        tmp4.mantissa = 0x47fed7d13d233b5cL
        //143.99096122250781605352
        tmp3.add(tmp4);
        tmp3.mul(tmp2);
        {
            tmp4.sign = 0;
            tmp4.exponent = 0x40000008;
            tmp4.mantissa = 0x5a5c35f774e071d5L;
        }
        //361.44079386152023162701
        tmp3.add(tmp4);
        tmp3.mul(tmp2);
        tmp4.sign = 0
        tmp4.exponent = 0x40000008
        tmp4.mantissa = 0x61e4d84c2853d5e0L
        //391.57570175111990631099
        tmp3.add(tmp4)
        tmp3.mul(tmp2)
        tmp4.sign = 0
        tmp4.exponent = 0x40000007
        tmp4.mantissa = 0x4c5757448806c48eL
        //152.68235069887081006606
        tmp3.add(tmp4)
        div(tmp3)
        add(tmp1)
        if (addPI_2)
            add(PI_2)
        if (addPI_4)
            add(PI_4)
        if (s.toInt() != 0)
            neg()
    }
    /**
     * Calculates the trigonometric arc tangent of this
     * <code>Real</code> divided by <code>x</code>, in the range -&pi;
     * to &pi;. The signs of both arguments are used to determine the
     * quadrant of the result. Replaces the contents of this
     * <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#atan2(Double, Double)
     * atan2}(this,x);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 48
     * </td></tr></table>
     *
     * @param x the <code>Real</code> argument.
     */
    fun atan2(x: Real) {
        if ((this.exponent < 0 && this.mantissa != 0L) || (x.exponent < 0 && x.mantissa != 0L) || ((this.exponent < 0 && this.mantissa == 0L) && (x.exponent < 0 && x.mantissa == 0L))) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L) && (x.exponent == 0 && x.mantissa == 0L))
            return;
        val s = sign
        val s2 = x.sign
        sign = 0
        x.sign = 0
        div(x)
        atan()
        if (s2.toInt() != 0) {
            neg()
            add(PI)
        }
        sign = s
    }
    /**
     * Calculates the hyperbolic sine of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#sinh(Double) sinh}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 67
     * </td></tr></table>
     */
    fun sinh() {
        val tmp1 = tmp1()
        tmp1.assign(this)
        tmp1.neg()
        tmp1.exp()
        exp()
        sub(tmp1)
        scalbn(-1)
    }
    /**
     * Calculates the hyperbolic cosine of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#cosh(Double) cosh}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 66
     * </td></tr></table>
     */
    fun cosh() {
        val tmp1 = tmp1()
        tmp1.assign(this)
        tmp1.neg()
        tmp1.exp()
        exp()
        add(tmp1)
        scalbn(-1)
    }
    /**
     * Calculates the hyperbolic tangent of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#tanh(Double) tanh}(this);</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 70
     * </td></tr></table>
     */
    fun tanh() {
        val tmp1 = tmp1()
        tmp1.assign(this);
        tmp1.neg();
        tmp1.exp();
        exp();
        val tmp2 = tmp2()
        tmp2.assign(this);
        tmp2.add(tmp1);
        sub(tmp1);
        div(tmp2);
    }
    /**
     * Calculates the hyperbolic arc sine of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 77
     * </td></tr></table>
     */
    fun asinh() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return;
        // Use symmetry to prevent underflow error for very large negative
        // values
        val s = sign
        sign = 0;
        val tmp1 = tmp1()
        tmp1.assign(this);
        tmp1.sqr();
        tmp1.add(ONE);
        tmp1.sqrt();
        add(tmp1);
        ln();
        if (!(this.exponent < 0 && this.mantissa != 0L))
            sign = s;
    }
    /**
     * Calculates the hyperbolic arc cosine of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 75
     * </td></tr></table>
     */
    fun acosh() {
        val tmp1 = tmp1()
        tmp1.assign(this);
        tmp1.sqr();
        tmp1.sub(ONE);
        tmp1.sqrt();
        add(tmp1);
        ln();
    }
    /**
     * Calculates the hyperbolic arc tangent of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 57
     * </td></tr></table>
     */
    fun atanh() {
        val tmp1 = tmp1()
        tmp1.assign(this);
        tmp1.neg();
        tmp1.add(ONE);
        add(ONE);
        div(tmp1);
        ln();
        scalbn(-1);
    }
    //*************************************************************************
    /**
     * Calculates the factorial of this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * The definition is generalized to all real numbers (not only integers),
     * by using the fact that <code>(n!)={@link #gamma() gamma}(n+1)</code>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 15 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 8-190
     * </td></tr></table>
     */
    fun fact() {
        if (!(this.exponent >= 0))
            return;
        if (!this.isIntegral() || this.compare(ZERO) < 0 || this.compare(200) > 0) {
            // x<0, x>200 or not integer: fact(x) = gamma(x+1)
            add(ONE);
            gamma();
            return;
        }
        val tmp1 = tmp1()
        tmp1.assign(this);
        {
            this.mantissa = ONE.mantissa;
            this.exponent = ONE.exponent;
            this.sign = ONE.sign;
        }
        while (tmp1.compare(ONE) > 0) {
            mul(tmp1);
            tmp1.sub(ONE);
        }
    }
    /**
     * Calculates the gamma function for this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 100+ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 190
     * </td></tr></table>
     */
    fun gamma() {
        if (!(this.exponent >= 0))
            return;
        // x<0: gamma(-x) = -pi/(x*gamma(x)*sin(pi*x))
        val negative = (this.sign.toInt() != 0)
        abs();
        val tmp1 = tmp1()
        tmp1.assign(this);
        // x<n: gamma(x) = gamma(x+m)/x*(x+1)*(x+2)*...*(x+m-1)
        // n=20
        val tmp2 = tmp2()
        tmp2.assign(ONE);
        var divide = false
        while (this.compare(20) < 0) {
            divide = true;
            tmp2.mul(this);
            add(ONE);
        }
        // x>n: gamma(x) = exp((x-1/2)*ln(x) - x + ln(2*pi)/2 + 1/12x - 1/360x³
        //                     + 1/1260x**5 - 1/1680x**7+1/1188x**9)
        val tmp3 = tmp3()
        tmp3.assign(this);
        // x
        val tmp4 = tmp4()
        tmp4.assign(this);
        tmp4.sqr(); // x²
        // (x-1/2)*ln(x)-x
        ln();
        val tmp5 = tmp5()
        tmp5.assign(tmp3);
        tmp5.sub(HALF);
        mul(tmp5);
        sub(tmp3);
        // + ln(2*pi)/2
        {
            tmp5.sign = 0;
            tmp5.exponent = 0x3fffffff;
            tmp5.mantissa = 0x759fc72192fad29aL;
        }
        add(tmp5);
        // + 1/12x
        tmp5.assign(12);
        tmp5.mul(tmp3);
        tmp5.recip();
        add(tmp5);
        tmp3.mul(tmp4);
        // - 1/360x³
        tmp5.assign(360);
        tmp5.mul(tmp3);
        tmp5.recip();
        sub(tmp5);
        tmp3.mul(tmp4);
        // + 1/1260x**5
        tmp5.assign(1260);
        tmp5.mul(tmp3);
        tmp5.recip();
        add(tmp5);
        tmp3.mul(tmp4);
        // - 1/1680x**7
        tmp5.assign(1680);
        tmp5.mul(tmp3);
        tmp5.recip();
        sub(tmp5);
        tmp3.mul(tmp4);
        // + 1/1188x**9
        tmp5.assign(1188);
        tmp5.mul(tmp3);
        tmp5.recip();
        add(tmp5);
        exp();
        if (divide)
            div(tmp2);
        if (negative) {
            {
                tmp5.mantissa = tmp1.mantissa;
                tmp5.exponent = tmp1.exponent;
                tmp5.sign = tmp1.sign;
            }
            // sin() uses tmp1
            // -pi/(x*gamma(x)*sin(pi*x))
            mul(tmp5);
            tmp5.scalbn(-1);
            tmp5.frac();
            tmp5.mul(PI2); // Fixes integer inaccuracy
            tmp5.sin();
            mul(tmp5);
            recip();
            mul(PI);
            neg();
        }
    }
    fun erfc1Internal() {
        //                                3       5        7        9
        //                 2    /        x       x        x        x                  // erfc(x) = 1 - ------ | x  -  ---  +  ----  -  ----  +  ----  - ... |
        //              sqrt(pi)\        3      2!*5     3!*7     4!*9        /
        //
        var extra = 0L
        var tmp1Extra = 0L
        var tmp2Extra = 0L
        var tmp3Extra = 0L
        var tmp4Extra = 0L
        val tmp1 = tmp1()
        tmp1.assign(this)
        tmp1Extra = 0L
        val tmp2 = tmp2()
        tmp2.assign(this)
        tmp2Extra = tmp2.mul128(0, tmp2, 0)
        tmp2.neg()
        val tmp3 = tmp3()
        tmp3.assign(ONE)
        tmp3Extra = 0L
        var i = 1
        val tmp4 = tmp4()
        do {
            tmp1Extra = tmp1.mul128(tmp1Extra, tmp2, tmp2Extra)
            tmp4.assign(i)
            tmp3Extra = tmp3.mul128(tmp3Extra, tmp4, 0)
            tmp4.assign(2 * i + 1)
            tmp4Extra = tmp4.mul128(0, tmp3, tmp3Extra)
            tmp4Extra = tmp4.recip128(tmp4Extra)
            tmp4Extra = tmp4.mul128(tmp4Extra, tmp1, tmp1Extra)
            extra = add128(extra, tmp4, tmp4Extra)
            i++
        } while (exponent - tmp4.exponent < 128)
        tmp1.sign = 1
        tmp1.exponent = 0x40000000
        tmp1.mantissa = 0x48375d410a6db446L
        // -2/sqrt(pi)
        extra = mul128(extra, tmp1, 0xb8ea453fb5ff61a2uL.toLong())
        extra = add128(extra, ONE, 0)
        roundFrom128(extra)
    }
    fun erfc2Internal() {
        //             -x² -1
        //            e   x   /      1      3       3*5     3*5*7                // erfc(x) = -------- | 1 - --- + ------ - ------ + ------ - ... |
        //           sqrt(pi) \     2x²        2        3        4       /
        //                                (2x²)    (2x²)    (2x²)
        // Calculate iteration stop criteria
        val tmp1 = tmp1()
        tmp1.assign(this)
        tmp1.sqr()
        val tmp2 = tmp2()
        tmp2.sign = 0
        tmp2.exponent = 0x40000000
        tmp2.mantissa = 0x5c3811b4bfd0c8abL
        // 1/0.694
        tmp2.mul(tmp1)
        tmp2.sub(HALF)
        var digits = tmp2.toInteger() // number of accurate digits = x*x/0.694-0.5
        if (digits > 64)
            digits = 64
        tmp1.scalbn(1)
        val dxq = tmp1.toInteger() + 1
        tmp1.assign(this)
        recip()
        tmp2.assign(this)
        val tmp3 = tmp3()
        tmp3.assign(this)
        tmp3.sqr()
        tmp3.neg()
        tmp3.scalbn(-1)
        assign(ONE)
        val tmp4 = tmp4()
        tmp4.assign(ONE)
        var i = 1
        do {
            tmp4.mul(2 * i - 1)
            tmp4.mul(tmp3)
            add(tmp4)
            i++
        } while (tmp4.exponent - 0x40000000 > -(digits + 2) && 2 * i - 1 < dxq)
        mul(tmp2)
        tmp1.sqr()
        tmp1.neg()
        tmp1.exp()
        mul(tmp1)
        tmp1.sign = 0
        tmp1.exponent = 0x3fffffff
        tmp1.mantissa = 0x48375d410a6db447L
        // 1/sqrt(pi)
        mul(tmp1)
    }
    /**
     * Calculates the complementary error function for this <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>The complementary error function is defined as the integral from
     * x to infinity of 2/&#8730;<span style="text-decoration:
     * overline;">&pi;</span>&nbsp;·<i>e</i><sup>-t²</sup>&nbsp;dt. It is
     * related to the error function, <i>erf</i>, by the formula
     * erfc(x)=1-erf(x).
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2<sup>19</sup> ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 80-4900
     * </td></tr></table>
     */
    fun erfc() {
        if ((this.exponent < 0 && this.mantissa != 0L))
            return;
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            {
                this.mantissa = ONE.mantissa;
                this.exponent = ONE.exponent;
                this.sign = ONE.sign;
            }
            return;
        }
        if ((this.exponent < 0 && this.mantissa == 0L) || toInteger() > 27281) {
            if ((this.sign.toInt() != 0)) {
                {
                    this.mantissa = TWO.mantissa;
                    this.exponent = TWO.exponent;
                    this.sign = TWO.sign;
                }
            } else
                makeZero(0);
            return;
        }
        val s = sign
        sign = 0
        val tmp1 = tmp1()
        tmp1.sign = 0
        tmp1.exponent = 0x40000002
        tmp1.mantissa = 0x570a3d70a3d70a3dL
        // 5.44
        if (this.lessThan(tmp1))
            erfc1Internal()
        else
            erfc2Internal()
        if (s.toInt() != 0) {
            neg()
            add(TWO)
        }
    }
    /**
     * Calculates the inverse complementary error function for this
     * <code>Real</code>.
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * 2<sup>19</sup> ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 240-5100
     * </td></tr></table>
     */
    fun inverfc() {
        if ((this.exponent < 0 && this.mantissa != 0L) || (this.sign.toInt() != 0) || this.greaterThan(TWO)) {
            makeNan();
            return;
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            makeInfinity(0);
            return;
        }
        if (this.equalTo(TWO)) {
            makeInfinity(1);
            return;
        }
        val sign = ONE.compare(this)
        if (sign.toInt() == 0) {
            makeZero();
            return;
        }
        if (sign < 0) {
            neg();
            add(TWO);
        }
        // Using invphi to calculate inverfc, like this
        // inverfc(x) = -invphi(x/2)/(sqrt(2))
        scalbn(-1);
        // Inverse Phi Algorithm (phi(Z)=P, so invphi(P)=Z)
        // ------------------------------------------------
        // Part 1: Numerical Approximation Method for Inverse Phi
        // This accepts input of P and outputs approximate Z as Y
        // Source:Odeh & Evans. 1974. AS 70. Applied Statistics.
        // R = sqrt(Ln(1/(Q²)))
        val tmp1 = tmp1()
        tmp1.assign(this)
        tmp1.ln()
        tmp1.mul(-2)
        tmp1.sqrt()
        // Y = -(R+((((P4*R+P3)*R+P2)*R+P1)*R+P0)/((((Q4*R+Q3)*R*Q2)*R+Q1)*R+Q0))
        val tmp2 = tmp2()
        tmp2.sign = 1
        tmp2.exponent = 0x3ffffff1
        tmp2.mantissa = 0x5f22bb0fb4698674L
        // P4=-0.0000453642210148
        tmp2.mul(tmp1)
        val tmp3 = tmp3()
        tmp3.sign = 1
        tmp3.exponent = 0x3ffffffa
        tmp3.mantissa = 0x53a731ce1ea0be15L
        // P3=-0.0204231210245
        tmp2.add(tmp3)
        tmp2.mul(tmp1)
        tmp3.sign = 1
        tmp3.exponent = 0x3ffffffe
        tmp3.mantissa = 0x579d2d719fc517f3L
        // P2=-0.342242088547
        tmp2.add(tmp3)
        tmp2.mul(tmp1)
        tmp2.add(-1); // P1=-1
        tmp2.mul(tmp1)
        tmp3.sign = 1
        tmp3.exponent = 0x3ffffffe
        tmp3.mantissa = 0x527dd3193bc8dd4cL
        // P0=-0.322232431088
        tmp2.add(tmp3)
        tmp3.sign = 0
        tmp3.exponent = 0x3ffffff7
        tmp3.mantissa = 0x7e5b0f681d161e7dL
        // Q4=0.0038560700634
        tmp3.mul(tmp1)
        val tmp4 = tmp4()
        tmp4.sign = 0
        tmp4.exponent = 0x3ffffffc
        tmp4.mantissa = 0x6a05ccf9917da0a8L
        // Q3=0.103537752850
        tmp3.add(tmp4)
        tmp3.mul(tmp1)
        tmp4.sign = 0
        tmp4.exponent = 0x3fffffff
        tmp4.mantissa = 0x43fb32c0d3c14ec4L
        // Q2=0.531103462366
        tmp3.add(tmp4)
        tmp3.mul(tmp1)
        tmp4.sign = 0
        tmp4.exponent = 0x3fffffff
        tmp4.mantissa = 0x4b56a41226f4ba95L
        // Q1=0.588581570495
        tmp3.add(tmp4)
        tmp3.mul(tmp1)
        tmp4.sign = 0
        tmp4.exponent = 0x3ffffffc
        tmp4.mantissa = 0x65bb9a7733dd5062L
        // Q0=0.0993484626060
        tmp3.add(tmp4)
        tmp2.div(tmp3)
        tmp1.add(tmp2)
        tmp1.neg()
        val sqrtTmp = sqrtTmp()
        sqrtTmp.assign(tmp1)
        // sqrtTmp and tmp5 not used by erfc() and exp()
        // Part 2: Refine to accuracy of erfc Function
        // This accepts inputs Y and P (from above) and outputs Z
        // (Using Halley's third order method for finding roots of equations)
        // Q = erfc(-Y/sqrt(2))/2-P
        val tmp5 = tmp5()
        tmp5.assign(sqrtTmp)
        tmp5.mul(SQRT1_2)
        tmp5.neg()
        tmp5.erfc()
        tmp5.scalbn(-1)
        tmp5.sub(this)
        // R = Q*sqrt(2*pi)*e^(Y²/2)
        tmp3.assign(sqrtTmp)
        tmp3.sqr()
        tmp3.scalbn(-1)
        tmp3.exp()
        tmp5.mul(tmp3)
        tmp3.sign = 0
        tmp3.exponent = 0x40000001
        tmp3.mantissa = 0x50364c7fd89c1659L
        // sqrt(2*pi)
        tmp5.mul(tmp3)
        // Z = Y-R/(1+R*Y/2)
        assign(sqrtTmp)
        mul(tmp5)
        scalbn(-1)
        add(ONE)
        rdiv(tmp5)
        neg()
        add(sqrtTmp)
        // calculate inverfc(x) = -invphi(x/2)/(sqrt(2))
        mul(SQRT1_2);
        if (sign > 0)
            neg()
    }
    /**
     * Converts this <code>Real</code> from "hours" to "days, hours,
     * minutes and seconds".
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>The format converted to is encoded into the digits of the
     * number (in decimal form):
     * "<code>DDDDhh.mmss</code>". Here "<code>DDDD</code>," is number
     * of days, "<code>hh</code>" is hours (0-23), "<code>mm</code>" is
     * minutes (0-59) and "<code>ss</code>" is seconds
     * (0-59). Additional digits represent fractions of a second.
     * <p/>
     * <p>If the number of hours of the input is greater or equal to
     * 8784 (number of hours in year <code>0</code>), the format
     * converted to is instead "<code>YYYYMMDDhh.mmss</code>". Here
     * "<code>YYYY</code>" is the number of years since the imaginary
     * year <code>0</code> in the Gregorian calendar, extrapolated back
     * from year 1582. "<code>MM</code>" is the month (1-12) and
     * "<code>DD</code>" is the day of the month (1-31). See a thorough
     * discussion of date calculations <a
     * href="http://midp-calc.sourceforge.net/Calc.html#DateNote">here</a>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * ?
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 19
     * </td></tr></table>
     */
    fun toDHMS() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return
        val negative = (this.sign.toInt() != 0)
        abs()
        var D: Int
        var m: Int
        var h: Long
        h = toLong()
        frac()
        val tmp1 = tmp1()
        tmp1.assign(60)
        mul(tmp1)
        m = toInteger()
        frac()
        mul(tmp1)
        // MAGIC ROUNDING: Check if we are 2**-16 sec short of a whole minute
        // i.e. "seconds" > 59.999985
        val tmp2 = tmp2()
        tmp2.assign(ONE)
        tmp2.scalbn(-16)
        add(tmp2)
        if (this.compare(tmp1) >= 0) {
            // Yes. So set zero secs instead and carry over to mins and hours
            this.mantissa = ZERO.mantissa
            this.exponent = ZERO.exponent
            this.sign = ZERO.sign
            m++
            if (m >= 60) {
                m -= 60
                h++
            }
            // Phew! That was close. From now on it is integer arithmetic...
        } else {
            // Nope. So try to undo the damage...
            sub(tmp2)
        }
        D = (h / 24).toInt()
        h %= 24
        if (D >= 366)
            D = jd_to_gregorian(D)
        add(m * 100)
        div(10000)
        tmp1.assign(D * 100L + h)
        add(tmp1)
        if (negative)
            neg()
    }
    /**
     * Converts this <code>Real</code> from "days, hours, minutes and
     * seconds" to "hours".
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>The format converted from is encoded into the digits of the
     * number (in decimal form):
     * "<code>DDDDhh.mmss</code>". Here "<code>DDDD</code>" is number of
     * days, "<code>hh</code>" is hours (0-23), "<code>mm</code>" is
     * minutes (0-59) and "<code>ss</code>" is seconds
     * (0-59). Additional digits represent fractions of a second.
     * <p/>
     * <p>If the number of days in the input is greater than or equal to
     * 10000, the format converted from is instead
     * "<code>YYYYMMDDhh.mmss</code>". Here "<code>YYYY</code>" is the
     * number of years since the imaginary year <code>0</code> in the
     * Gregorian calendar, extrapolated back from year
     * 1582. "<code>MM</code>" is the month (1-12) and
     * "<code>DD</code>" is the day of the month (1-31). If month or day
     * is 0 it is treated as 1. See a thorough discussion of date
     * calculations <a
     * href="http://midp-calc.sourceforge.net/Calc.html#DateNote">here</a>.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * ?
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 19
     * </td></tr></table>
     */
    fun fromDHMS() {
        if (!(this.exponent >= 0 && this.mantissa != 0L))
            return;
        val negative = (this.sign.toInt() != 0)
        abs();
        var Y: Int
        var M: Int
        var D: Int
        var m: Int
        var h: Long
        h = toLong();
        frac();
        val tmp1 = tmp1()
        tmp1.assign(100);
        mul(tmp1);
        m = toInteger();
        frac();
        mul(tmp1);
        // MAGIC ROUNDING: Check if we are 2**-10 second short of 100 seconds
        // i.e. "seconds" > 99.999
        val tmp2 = tmp2()
        tmp2.assign(ONE);
        tmp2.scalbn(-10);
        add(tmp2);
        if (this.compare(tmp1) >= 0) {
            // Yes. So set zero secs instead and carry over to mins and hours
            {
                this.mantissa = ZERO.mantissa;
                this.exponent = ZERO.exponent;
                this.sign = ZERO.sign;
            }
            m++;
            if (m >= 100) {
                m -= 100;
                h++;
            }
            // Phew! That was close. From now on it is integer arithmetic...
        } else {
            // Nope. So try to undo the damage...
            sub(tmp2);
        }
        D = (h / 100).toInt();
        h %= 100;
        if (D >= 10000) {
            M = D / 100;
            D %= 100;
            if (D == 0) D = 1;
            Y = M / 100;
            M %= 100;
            if (M == 0) M = 1;
            D = gregorian_to_jd(Y, M, D);
        }
        add(m * 60);
        div(3600);
        tmp1.assign(D * 24L + h);
        add(tmp1);
        if (negative)
            neg();
    }
    /**
     * Assigns this <code>Real</code> the current time. The time is
     * encoded into the digits of the number (in decimal form), using the
     * format "<code>hh.mmss</code>", where "<code>hh</code>" is hours,
     * "<code>mm</code>" is minutes and "code>ss</code>" is seconds.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * œ ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 8.9
     * </td></tr></table>
     */
    fun time() {
        var now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        now /= 1000;
        val s: Int = (now % 60).toInt();
        now /= 60;
        val m: Int = (now % 60).toInt();
        now /= 60;
        val h: Int = (now % 24).toInt();
        assign((h * 100 + m) * 100 + s);
        div(10000);
    }
    /**
     * Assigns this <code>Real</code> the current date. The date is
     * encoded into the digits of the number (in decimal form), using
     * the format "<code>YYYYMMDD00</code>", where "<code>YYYY</code>"
     * is the year, "<code>MM</code>" is the month (1-12) and
     * "<code>DD</code>" is the day of the month (1-31). The
     * "<code>00</code>" in this format is a sort of padding to make it
     * compatible with the format used by {@link #toDHMS()} and {@link
     * #fromDHMS()}.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <i>none</i>
     * </td></tr><tr><td><i>Error&nbsp;bound:</i></td><td>
     * 0 ULPs
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 30
     * </td></tr></table>
     */
    fun date() {
        var now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        now /= 86400000; // days
        now *= 24; // hours
        assign(now);
        add(719528 * 24); // 1970-01-01 era
        toDHMS();
    }
    /**
     * Calculates a pseudorandom number in the range [0,&nbsp;1).
     * Replaces the contents of this <code>Real</code> with the result.
     * <p/>
     * <p>The algorithm used is believed to be cryptographically secure,
     * combining two relatively weak 64-bit CRC generators into a strong
     * generator by skipping bits from one generator whenever the other
     * generator produces a 0-bit. The algorithm passes the <a
     * href="http://www.fourmilab.ch/random/">ent</a> test.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this = Math.{@link Math#random() random}();</code>
     * </td></tr><tr><td><i>Approximate&nbsp;error&nbsp;bound:</i></td><td>
     * -
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 81
     * </td></tr></table>
     */
    fun random() {
        sign = 0;
        exponent = 0x3fffffff;
        while (nextBits(1) == 0L)
            exponent--;
        mantissa = 0x4000000000000000L + nextBits(62);
    }
    //*************************************************************************
    fun digit(a: Char, base: Int, twosComplement: Boolean): Int {
        var digit = -1
        if (a >= '0' && a <= '9')
            digit = a - '0';
        else if (a >= 'A' && a <= 'F')
            digit = a - 'A' + 10;
        if (digit >= base)
            return -1;
        if (twosComplement)
            digit = digit xor base - 1
        return digit;
    }
    fun shiftUp(base: Int) {
        if (base == 2)
            scalbn(1);
        else if (base == 8)
            scalbn(3);
        else if (base == 16)
            scalbn(4);
        else
            mul10();
    }
    fun atof(a: String, base: Int) {
        makeZero()
        val length = a.length
        var index = 0
        var tmpSign = 0
        var compl = false
        while (index < length && a[index] == ' ')
            index++
        if (index < length && a[index] == '-') {
            tmpSign = 1
            index++
        } else if (index < length && a[index] == '+') {
            index++
        } else if (index < length && a[index] == '/') {
            // Input is twos complemented negative number
            compl = true
            tmpSign = 1
            index++
        }
        var d: Int
        while (index < length) {
            d = digit(a[index], base, compl)
            if (d < 0)
                break
            shiftUp(base)
            add(d)
            index++
        }
        var exp = 0
        if (index < length && (a[index] == '.' || a[index] == ',')) {
            index++
            while (index < length) {
                d = digit(a[index], base, compl)
                if (d < 0)
                    break
                shiftUp(base)
                add(d)
                exp--
                index++
            }
        }
        if (compl)
            add(ONE)
        while (index < length && a[index] == ' ')
            index++
        if (index < length && (a[index] == 'e' || a[index] == 'E')) {
            index++
            var exp2 = 0
            var expNeg = false
            if (index < length && a[index] == '-') {
                expNeg = true
                index++
            } else if (index < length && a[index] == '+') {
                index++
            }
            while (index < length && a[index] >= '0' &&
                    a[index] <= '9') {
                // This takes care of overflows and makes inf or 0
                if (exp2 < 400000000)
                    exp2 = exp2 * 10 + (a[index] - '0')
                index++
            }
            if (expNeg)
                exp2 = -exp2
            exp += exp2
        }
        if (base == 2)
            scalbn(exp)
        else if (base == 8)
            scalbn(exp * 3)
        else if (base == 16)
            scalbn(exp * 4)
        else {
            val tmp1 = tmp1()
            if (exp > 300000000 || exp < -300000000) {
                // Kludge to be able to enter very large and very small
                // numbers without causing over/underflows
                tmp1.assign(TEN)
                if (exp < 0) {
                    tmp1.pow(-exp / 2)
                    div(tmp1)
                } else {
                    tmp1.pow(exp / 2)
                    mul(tmp1)
                }
                exp -= exp / 2
            }
            tmp1.assign(TEN)
            if (exp < 0) {
                tmp1.pow(-exp)
                div(tmp1)
            } else if (exp > 0) {
                tmp1.pow(exp)
                mul(tmp1)
            }
        }
        sign = tmpSign.toByte()
        if (index != length) {
            // signal error
            assign(NAN)
        }
    }
    //*************************************************************************
    fun normalizeDigits(digits: ByteArray, nDigits: Int, base: Int) {
        var carry = 0
        var isZero = true
        for (i in nDigits - 1 downTo 0) {
            val digitValue = digits[i].toInt()
            if (digitValue != 0)
                isZero = false
            var sum = digitValue + carry
            carry = 0
            if (sum >= base) {
                sum -= base
                carry = 1
            }
            digits[i] = sum.toByte()
        }
        if (isZero) {
            exponent = 0
            return
        }
        if (carry != 0) {
            if (digits[nDigits - 1].toInt() >= base / 2)
                digits[nDigits - 2] = (digits[nDigits - 2] + 1).toByte() // Rounding, may be inaccurate
            digits.copyInto(digits, 1, 0, 0 + nDigits - 1)
            digits[0] = carry.toByte()
            exponent++
            if (digits[nDigits - 1].toInt() >= base) {
                // Oh, no, not again!
                normalizeDigits(digits, nDigits, base)
            }
        }
        while (digits[0].toInt() == 0) {
            digits.copyInto(digits, 0, 1, 1 + nDigits - 1)
            digits[nDigits - 1] = 0
            exponent--
        }
    }
    fun getDigits(digits: ByteArray, base: Int): Int {
        if (base == 10) {
            val tmp1 = tmp1()
            tmp1.assign(this)
            tmp1.abs()
            val tmp2 = tmp2()
            tmp2.assign(tmp1)
            exponent = tmp1.lowPow10()
            var exp = exponent
            exp -= 18
            val exp_neg = exp <= 0
            exp = kotlin.math.abs(exp)
            if (exp > 300000000) {
                // Kludge to be able to print very large and very small numbers
                // without causing over/underflows
                tmp1.mantissa = TEN.mantissa
                tmp1.exponent = TEN.exponent
                tmp1.sign = TEN.sign
                tmp1.pow(exp / 2); // So, divide twice by not-so-extreme numbers
                if (exp_neg)
                    tmp2.mul(tmp1)
                else
                    tmp2.div(tmp1)
                tmp1.mantissa = TEN.mantissa
                tmp1.exponent = TEN.exponent
                tmp1.sign = TEN.sign
                tmp1.pow(exp - (exp / 2))
            } else {
                tmp1.mantissa = TEN.mantissa
                tmp1.exponent = TEN.exponent
                tmp1.sign = TEN.sign
                tmp1.pow(exp)
            }
            if (exp_neg)
                tmp2.mul(tmp1)
            else
                tmp2.div(tmp1)
            var a: Long
            if (tmp2.exponent > 0x4000003e) {
                tmp2.exponent--
                tmp2.round()
                a = tmp2.toLong()
                if (a >= 5000000000000000000L) { // Rounding up gave 20 digits
                    exponent++
                    a /= 5;
                    digits[18] = (a % 10).toByte()
                    a /= 10;
                } else {
                    digits[18] = ((a % 5) * 2).toByte()
                    a /= 5;
                }
            } else {
                tmp2.round()
                a = tmp2.toLong()
                digits[18] = (a % 10).toByte()
                a /= 10;
            }
            for (i in 17 downTo 0) {
                digits[i] = (a % 10).toByte()
                a /= 10;
            }
            digits[19] = 0
            return 19
        }
        var accurateBits = 64
        val bitsPerDigit = when (base) {
            2 -> 1
            8 -> 3
            else -> 4
        }
        if ((this.exponent == 0 && this.mantissa == 0L)) {
            sign = 0 // Two's complement cannot display -0
        } else {
            if ((this.sign.toInt() != 0)) {
                mantissa = -mantissa
                if (((mantissa shr 62) and 3L) == 3L) {
                    mantissa = mantissa shl 1
                    exponent--
                    accurateBits-- // ?
                }
            }
            exponent -= 0x40000000 - 1
            val shift = bitsPerDigit - 1 - floorMod(exponent, bitsPerDigit)
            exponent = floorDiv(exponent, bitsPerDigit)
            if (shift == bitsPerDigit - 1) {
                // More accurate to shift up instead
                mantissa = mantissa shl 1
                exponent--
                accurateBits--
            } else if (shift > 0) {
                mantissa = (mantissa + (1L shl (shift - 1))) ushr shift
                if ((this.sign.toInt() != 0)) {
                    // Need to fill in some 1's at the top
                    // (">>", not ">>>")
                    mantissa = mantissa or (Long.MIN_VALUE shr (shift - 1))
                }
            }
        }
        val accurateDigits = (accurateBits + bitsPerDigit - 1) / bitsPerDigit
        for (i in 0 until accurateDigits) {
            digits[i] = (mantissa ushr (64 - bitsPerDigit)).toByte()
            mantissa = mantissa shl bitsPerDigit
        }
        digits[accurateDigits] = 0
        return accurateDigits
    }
    fun carryWhenRounded(digits: ByteArray, nDigits: Int, base: Int): Boolean {
        if (digits[nDigits].toInt() < base / 2)
            return false; // no rounding up, no carry
        for (i in nDigits - 1 downTo 0)
            if (digits[i].toInt() < base - 1)
                return false; // carry would not propagate
        exponent++
        digits[0] = 1
        for (i in 1 until digits.size)
            digits[i] = 0
        return true
    }
    fun round(digits: ByteArray, nDigits: Int, base: Int) {
        if (digits[nDigits].toInt() >= base / 2) {
            digits[nDigits - 1] = (digits[nDigits - 1] + 1).toByte()
            normalizeDigits(digits, nDigits, base)
        }
    }
    fun align(s: StringBuilder, format: NumberFormat): String {
        if (format.align == NumberFormat.ALIGN_LEFT) {
            while (s.length < format.maxwidth)
                s.append(' ')
        } else if (format.align == NumberFormat.ALIGN_RIGHT) {
            while (s.length < format.maxwidth)
                s.insert(0, ' ')
        } else if (format.align == NumberFormat.ALIGN_CENTER) {
            while (s.length < format.maxwidth) {
                s.append(' ')
                if (s.length < format.maxwidth)
                    s.insert(0, ' ')
            }
        }
        return s.toString()
    }
    fun ftoa(format: NumberFormat): String {
        buf.setLength(0)
        if (this.exponent < 0 && this.mantissa != 0L) {
            buf.append("NaN")
            return align(buf, format)
        }
        if (this.exponent < 0 && this.mantissa == 0L) {
            buf.append(if (this.sign.toInt() != 0) "-∞" else "∞")
            return align(buf, format)
        }
        val digitsPerThousand = digitsPerThousand(format)
        val tmp = Real()
        tmp.assign(this)
        var accurateDigits = tmp.getDigits(digits, format.base)
        if (format.base == 10 && (exponent > 0x4000003e || !isIntegral()))
            accurateDigits = 16 // Only display 16 digits for non-integers
        var precision: Int
        var pointPos = 0
        do {
            var width = format.maxwidth - 1 // subtract 1 for decimal point
            var prefix = 0
            if (format.base != 10)
                prefix = 1 // want room for at least one "0" or "f/7/1"
            else if ((tmp.sign.toInt() != 0))
                width-- // subtract 1 for sign
            var useExp = false
            when (format.fse) {
                NumberFormat.FSE_SCI -> {
                    precision = format.precision + 1
                    useExp = true
                }
                NumberFormat.FSE_ENG -> {
                    pointPos = floorMod(tmp.exponent, 3)
                    precision = format.precision + 1 + pointPos
                    useExp = true
                }
                NumberFormat.FSE_FIX, NumberFormat.FSE_NONE -> {
                    precision = 1000
                    if (format.fse == NumberFormat.FSE_FIX)
                        precision = format.precision + 1
                    if (tmp.exponent + 1 >
                        width - (tmp.exponent + prefix) / digitsPerThousand - prefix +
                        (if (format.removePoint) 1 else 0) ||
                        tmp.exponent + 1 > accurateDigits ||
                        -tmp.exponent >= width ||
                        -tmp.exponent >= precision
                    ) {
                        useExp = true
                    } else {
                        pointPos = tmp.exponent
                        precision += tmp.exponent
                        if (tmp.exponent > 0)
                            width -= (tmp.exponent + prefix) / digitsPerThousand
                        if (format.removePoint && tmp.exponent == width - prefix) {
                            // Add 1 for the decimal point that will be removed
                            width++
                        }
                    }
                }
                else -> {
                    precision = format.precision + 1
                }
            }
            if (prefix != 0 && pointPos >= 0)
                width -= prefix;
            exp.setLength(0);
            if (useExp) {
                exp.append('e')
                exp.append(tmp.exponent - pointPos)
                width -= exp.length
            }
            if (precision > accurateDigits)
                precision = accurateDigits
            if (precision > width)
                precision = width
            if (precision > width + pointPos) // In case of negative pointPos
                precision = width + pointPos
            if (precision <= 0)
                precision = 1
        }
        while (tmp.carryWhenRounded(digits, precision, format.base))
        tmp.round(digits, precision, format.base)
        // Start generating the string. First the sign
        if ((tmp.sign.toInt() != 0) && format.base == 10)
            buf.append('-')
        // Save pointPos for hex/oct/bin prefixing with thousands-sep
        var pointPos2 = if (pointPos < 0) 0 else pointPos
        // Add leading zeros (or f/7/1)
        val prefixChar = if (format.base == 10 || (tmp.sign.toInt() == 0)) '0' else
            hexChar[format.base - 1]
        if (pointPos < 0) {
            buf.append(prefixChar)
            buf.append(format.point)
            while (pointPos < -1) {
                buf.append(prefixChar)
                pointPos++
            }
        }
        // Add fractional part
        for (i in 0 until precision) {
            buf.append(hexChar[digits[i].toInt()])
            if (pointPos > 0 && pointPos % digitsPerThousand == 0)
                buf.append(format.thousand)
            if (pointPos == 0)
                buf.append(format.point)
            pointPos--
        }
        if (format.fse == NumberFormat.FSE_NONE) {
            // Remove trailing zeros
            while (buf[buf.length - 1] == '0')
                buf.setLength(buf.length - 1)
        }
        if (format.removePoint) {
            // Remove trailing point
            if (buf[buf.length - 1] == format.point)
                buf.setLength(buf.length - 1)
        }
        // Add exponent
        buf.append(exp)
        // In case hex/oct/bin number, prefix with 0's or f/7/1's
        if (format.base != 10) {
            while (buf.length < format.maxwidth) {
                pointPos2++
                if (pointPos2 > 0 && pointPos2 % digitsPerThousand == 0)
                    buf.insert(0, format.thousand)
                if (buf.length < format.maxwidth)
                    buf.insert(0, prefixChar)
            }
            if (buf[0] == format.thousand)
                buf.deleteAt(0)
        }
        return align(buf, format)
    }
    fun digitsPerThousand(format: NumberFormat): Int {
        if (format.thousand == '\u0000') {
            return 1000 // Disable thousands separator
        }
        return when (format.base) {
            2, 8 -> 4
            16 -> 2
            else -> 3
        }
    }
    /**
     * Converts this <code>Real</code> to a <code>String</code> using
     * the default <code>NumberFormat</code>.
     * <p/>
     * <p>See {@link Real.NumberFormat NumberFormat} for a description
     * of the default way that numbers are formatted.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td><td>
     * <code>this.toString()
     * </td></tr><tr><td><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;
     * </i></td><td>
     * 130
     * </td></tr></table>
     *
     * @return a <code>String</code> representation of this <code>Real</code>.
     */
    override fun toString(): String {
        val format = NumberFormat()
        format.base = 10
        return ftoa(format)
    }
    /**
     * Converts this <code>Real</code> to a <code>String</code> using
     * the default <code>NumberFormat</code> with <code>base</code> set
     * according to the argument.
     * <p/>
     * <p>See {@link Real.NumberFormat NumberFormat} for a description
     * of the default way that numbers are formatted.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td>
     * <td colspan="2">
     * <code>this.toString()  // Works only for base-10</code>
     * </td></tr><tr><td rowspan="4" valign="top"><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;</i>
     * </td><td width="1%">base-2</td><td>
     * 120
     * </td></tr><tr><td>base-8</td><td>
     * 110
     * </td></tr><tr><td>base-10</td><td>
     * 130
     * </td></tr><tr><td>base-16&nbsp;&nbsp;</td><td>
     * 120
     * </td></tr></table>
     *
     * @param base the base for the conversion. Valid base values are
     *             2, 8, 10 and 16.
     * @return a <code>String</code> representation of this <code>Real</code>.
     */
    fun toString(base: Int): String {
        val format = NumberFormat()
        format.base = base
        return ftoa(format)
    }
    /**
     * Converts this <code>Real</code> to a <code>String</code> using
     * the given <code>NumberFormat</code>.
     * <p/>
     * <p>See {@link Real.NumberFormat NumberFormat} for a description of the
     * various ways that numbers may be formatted.
     * <p/>
     * <p><table border="1" width="100%" cellpadding="3" cellspacing="0"
     * bgcolor="#e8d0ff"><tr><td width="1%"><i>
     * Equivalent&nbsp;</i><code>Double</code><i>&nbsp;code:</i></td>
     * <td colspan="2">
     * <code>String.format("%...g",this);  // Works only for base-10</code>
     * </td></tr><tr><td rowspan="4" valign="top"><i>
     * Execution&nbsp;time&nbsp;relative&nbsp;to&nbsp;add:&nbsp;&nbsp;</i>
     * </td><td width="1%">base-2</td><td>
     * 120
     * </td></tr><tr><td>base-8</td><td>
     * 110
     * </td></tr><tr><td>base-10</td><td>
     * 130
     * </td></tr><tr><td>base-16&nbsp;&nbsp;</td><td>
     * 120
     * </td></tr></table>
     *
     * @param format the number format to use in the conversion.
     * @return a <code>String</code> representation of this <code>Real</code>.
     */
    fun toString(format: NumberFormat): String {
        return ftoa(format)
    }
    /**
     * The number format used to convert <code>Real</code> values to
     * <code>String</code> using {@link Real#toString(Real.NumberFormat)
     * Real.toString()}. The default number format uses base-10, maximum
     * precision, removal of trailing zeros and '.' as radix point.
     * <p/>
     * <p>Note that the fields of <code>NumberFormat</code> are not
     * protected in any way, the user is responsible for setting the
     * correct values to get a correct result.
     */
    class NumberFormat {
        /**
         * Normal output {@linkplain #fse format}
         */
        companion object {
            const val FSE_NONE = 0
            /**
             * <i>FIX</i> output {@linkplain #fse format}
             */
            const val FSE_FIX = 1
            /**
             * <i>SCI</i> output {@linkplain #fse format}
             */
            const val FSE_SCI = 2
            /**
             * <i>ENG</i> output {@linkplain #fse format}
             */
            const val FSE_ENG = 3
            /**
             * No {@linkplain #align alignment}
             */
            const val ALIGN_NONE = 0
            /**
             * Left {@linkplain #align alignment}
             */
            const val ALIGN_LEFT = 1
            /**
             * Right {@linkplain #align alignment}
             */
            const val ALIGN_RIGHT = 2
            /**
             * Center {@linkplain #align alignment}
             */
            const val ALIGN_CENTER = 3
        }
        /**
         * The number base of the conversion. The default value is 10,
         * valid options are 2, 8, 10 and 16. See {@link Real#and(Real)
         * Real.and()} for an explanation of the interpretation of a
         * <code>Real</code> in base 2, 8 and 16.
         * <p/>
         * <p>Negative numbers output in base-2, base-8 and base-16 are
         * shown in two's complement form. This form guarantees that a
         * negative number starts with at least one digit that is the
         * maximum digit for that base, i.e. '1', '7', and 'F',
         * respectively. A positive number is guaranteed to start with at
         * least one '0'. Both positive and negative numbers are extended
         * to the left using this digit, until {@link #maxwidth} is
         * reached.
         */
        var base = 10
        /**
         * Maximum width of the converted string. The default value is 30.
         * If the conversion of a <code>Real</code> with a given {@link
         * #precision} would produce a string wider than
         * <code>maxwidth</code>, <code>precision</code> is reduced until
         * the number fits within the given width. If
         * <code>maxwidth</code> is too small to hold the number with its
         * sign, exponent and a <code>precision</code> of 1 digit, the
         * string may become wider than <code>maxwidth</code>.
         * <p/>
         * <p>If <code>align</code> is set to anything but
         * <code>ALIGN_NONE</code> and the converted string is shorter
         * than <code>maxwidth</code>, the resulting string is padded with
         * spaces to the specified width according to the alignment.
         */
        var maxwidth = 30
        /**
         * The precision, or number of digits after the radix point in the
         * converted string when using the <i>FIX</i>, <i>SCI</i> or
         * <i>ENG</i> format (see {@link #fse}). The default value is 16,
         * valid values are 0-16 for base-10 and base-16 conversion, 0-21
         * for base-8 conversion, and 0-63 for base-2 conversion.
         * <p/>
         * <p>The <code>precision</code> may be reduced to make the number
         * fit within {@link #maxwidth}. The <code>precision</code> is
         * also reduced if it is set higher than the actual numbers of
         * significant digits in a <code>Real</code>. When
         * <code>fse</code> is set to <code>FSE_NONE</code>, i.e. "normal"
         * output, the precision is always at maximum, but trailing zeros
         * are removed.
         */
        var precision = 16
        /**
         * The special output formats <i>FIX</i>, <i>SCI</i> or <i>ENG</i>
         * are enabled with this field. The default value is
         * <code>FSE_NONE</code>. Valid options are listed below.
         * <p/>
         * <p>Numbers are output in one of two main forms, according to
         * this setting. The normal form has an optional sign, one or more
         * digits before the radix point, and zero or more digits after the
         * radix point, for example like this:<br>
         * <code>&nbsp;&nbsp;&nbsp;3.14159</code><br>
         * The exponent form is like the normal form, followed by an
         * exponent marker 'e', an optional sign and one or more exponent
         * digits, for example like this:<br>
         * <code>&nbsp;&nbsp;&nbsp;-3.4753e-13</code>
         * <p/>
         * <p><dl>
         * <dt>{@link #FSE_NONE}
         * <dd>Normal output. Numbers are output with maximum precision,
         * trailing zeros are removed. The format is changed to
         * exponent form if the number is larger than the number of
         * significant digits allows, or if the resulting string would
         * exceed <code>maxwidth</code> without the exponent form.
         * <p/>
         * <dt>{@link #FSE_FIX}
         * <dd>Like normal output, but the numbers are output with a
         * fixed number of digits after the radix point, according to
         * {@link #precision}. Trailing zeros are not removed.
         * <p/>
         * <dt>{@link #FSE_SCI}
         * <dd>The numbers are always output in the exponent form, with
         * one digit before the radix point, and a fixed number of
         * digits after the radix point, according to
         * <code>precision</code>. Trailing zeros are not removed.
         * <p/>
         * <dt>{@link #FSE_ENG}
         * <dd>Like the <i>SCI</i> format, but the output shows one to
         * three digits before the radix point, so that the exponent is
         * always divisible by 3.
         * </dl>
         */
        var fse = FSE_NONE
        /**
         * The character used as the radix point. The default value is
         * <code>'.'</code>. Theoretcally any character that does not
         * otherwise occur in the output can be used, such as
         * <code>','</code>.
         * <p/>
         * <p>Note that setting this to anything but <code>'.'</code> and
         * <code>','</code> is not supported by any conversion method from
         * <code>String</code> back to <code>Real</code>.
         */
        var point = '.'
        /**
         * Set to <code>true</code> to remove the radix point if this is
         * the last character in the converted string. This is the
         * default.
         */
        var removePoint = true
        /**
         * The character used as the thousands separator. The default
         * value is the character code <code>0</code>, which disables
         * thousands-separation. Theoretcally any character that does not
         * otherwise occur in the output can be used, such as
         * <code>','</code> or <code>' '</code>.
         * <p/>
         * <p>When <code>thousand!=0</code>, this character is inserted
         * between every 3rd digit to the left of the radix point in
         * base-10 conversion. In base-16 conversion, the separator is
         * inserted between every 4th digit, and in base-2 conversion the
         * separator is inserted between every 8th digit. In base-8
         * conversion, no separator is ever inserted.
         * <p/>
         * <p>Note that tousands separators are not supported by any
         * conversion method from <code>String</code> back to
         * <code>Real</code>, so use of a thousands separator is meant
         * only for the presentation of numbers.
         */
        var thousand = '\u0000'
        /**
         * The alignment of the output string within a field of {@link
         * #maxwidth} characters. The default value is
         * <code>ALIGN_NONE</code>. Valid options are defined as follows:
         * <p/>
         * <p><dl>
         * <dt>{@link #ALIGN_NONE}
         * <dd>The resulting string is not padded with spaces.
         * <p/>
         * <dt>{@link #ALIGN_LEFT}
         * <dd>The resulting string is padded with spaces on the right side
         * until a width of <code>maxwidth</code> is reached, making the
         * number left-aligned within the field.
         * <p/>
         * <dt>{@link #ALIGN_RIGHT}
         * <dd>The resulting string is padded with spaces on the left side
         * until a width of <code>maxwidth</code> is reached, making the
         * number right-aligned within the field.
         * <p/>
         * <dt>{@link #ALIGN_CENTER}
         * <dd>The resulting string is padded with spaces on both sides
         * until a width of <code>maxwidth</code> is reached, making the
         * number center-aligned within the field.
         * </dl>
         */
        var align = ALIGN_NONE
    }
}
