package jscl.math.polynomial

abstract class Ordering : Comparator<Monomial> {
    abstract override fun compare(m1: Monomial, m2: Monomial): Int
}
