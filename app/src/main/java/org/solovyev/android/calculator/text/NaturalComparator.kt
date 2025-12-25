package org.solovyev.android.calculator.text

object NaturalComparator : Comparator<Any> {
    override fun compare(lhs: Any, rhs: Any): Int = lhs.toString().compareTo(rhs.toString())
}
