package jscl.math.polynomial.groebner

internal class Natural private constructor() : Comparator<Pair> {
    companion object {
        val comparator: Comparator<Pair> = Natural()
    }

    override fun compare(o1: Pair, o2: Pair): Int {
        return o1.compareTo(o2)
    }
}
