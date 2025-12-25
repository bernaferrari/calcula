package jscl.math.polynomial.groebner

internal class Sugar private constructor() : Comparator<Pair> {
    companion object {
        @JvmField
        val comparator: Comparator<Pair> = Sugar()
    }

    override fun compare(o1: Pair, o2: Pair): Int {
        if (o1.sugar < o2.sugar) return -1
        else if (o1.sugar > o2.sugar) return 1
        else return o1.compareTo(o2)
    }
}
