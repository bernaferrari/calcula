package jscl.math.polynomial

import jscl.math.Generic

open class Term(val monomial: Monomial?, open val coef: Generic?) : Comparable<Term> {

    fun subtract(term: Term): Term {
        return Term(monomial, coef!!.subtract(term.coef!!))
    }

    fun multiply(generic: Generic): Term {
        return Term(monomial, coef!!.multiply(generic))
    }

    fun multiply(monomial: Monomial, generic: Generic): Term {
        return Term(this.monomial!!.multiply(monomial), coef!!.multiply(generic))
    }

    fun multiply(monomial: Monomial): Term {
        return Term(this.monomial!!.multiply(monomial), coef)
    }

    fun divide(generic: Generic): Term {
        return Term(monomial, coef!!.divide(generic))
    }

    fun divide(monomial: Monomial): Term {
        return Term(this.monomial!!.divide(monomial), coef)
    }

    fun negate(): Term {
        return Term(monomial, coef!!.negate())
    }

    fun signum(): Int {
        return coef!!.signum()
    }

    fun monomial(): Monomial {
        return monomial!!
    }

    fun coef(): Generic {
        return coef!!
    }

    override fun compareTo(other: Term): Int {
        return monomial!!.compareTo(other.monomial!!)
    }

    override fun toString(): String {
        return "($coef, $monomial)"
    }
}
