package jscl.math

/**
 * User: serso
 * Date: 12/26/11
 * Time: 9:45 AM
 */
internal class Clifford {
    var p: Int
    var n: Int
    var operator: Array<IntArray>

    constructor(algebra: IntArray) : this(algebra[0], algebra[1])

    constructor(p: Int, q: Int) {
        this.p = p
        n = p + q
        val m = 1 shl n
        operator = Array(m) { IntArray(m) }
        for (i in 0 until m) {
            for (j in 0 until m) {
                val a = combination(i, n)
                val b = combination(j, n)
                val c = a xor b
                val l = location(c, n)
                val s = sign(a, b)
                val k = l + 1
                operator[i][j] = if (s) -k else k
            }
        }
    }

    fun sign(a: Int, b: Int): Boolean {
        var s = false
        for (i in 0 until n) {
            if ((b and (1 shl i)) > 0) {
                for (j in i until n) {
                    if ((a and (1 shl j)) > 0 && (j > i || i >= p)) s = !s
                }
            }
        }
        return s
    }

    fun operator(): Array<IntArray> = operator

    companion object {
        @JvmStatic
        fun combination(l: Int, n: Int): Int {
            if (n <= 2) return l
            val b = IntArray(1)
            val l1 = decimation(l, n, b)
            val c = combination(l1, n - 1)
            return (c shl 1) + b[0]
        }

        @JvmStatic
        fun location(c: Int, n: Int): Int {
            if (n <= 2) return c
            val c1 = c shr 1
            val b = c and 1
            val l1 = location(c1, n - 1)
            return dilatation(l1, n, intArrayOf(b))
        }

        @JvmStatic
        fun decimation(l: Int, n: Int, b: IntArray): Int {
            val p = grade(l, n - 1, 1)
            val p1 = (p + 1) shr 1
            b[0] = p and 1
            return l - sum(p1, n - 1)
        }

        @JvmStatic
        fun dilatation(l: Int, n: Int, b: IntArray): Int {
            val p1 = grade(l, n - 1)
            return l + sum(p1 + b[0], n - 1)
        }

        @JvmStatic
        @JvmOverloads
        fun grade(l: Int, n: Int, d: Int = 0): Int {
            var s = 0
            var p = 0
            while (true) {
                s += binomial(n, p shr d)
                if (s <= l) p++
                else break
            }
            return p
        }

        @JvmStatic
        fun sum(p: Int, n: Int): Int {
            var q = 0
            var s = 0
            while (q < p) s += binomial(n, q++)
            return s
        }

        @JvmStatic
        fun binomial(n: Int, p: Int): Int {
            var a = 1
            var b = 1
            for (i in n - p + 1..n) a *= i
            for (i in 2..p) b *= i
            return a / b
        }

        @JvmStatic
        fun log2e(n: Int): Int {
            var num = n
            var i = 0
            while (num > 1) {
                num = num shr 1
                i++
            }
            return i
        }
    }
}
