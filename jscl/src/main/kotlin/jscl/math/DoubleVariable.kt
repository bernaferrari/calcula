package jscl.math

class DoubleVariable(generic: Generic?) : GenericVariable(generic) {

    fun symbolic(): JsclInteger {
        return content!!.integerValue()
    }

    override fun antiDerivative(variable: Variable): Generic {
        return expressionValue().multiply(variable.expressionValue())
    }

    override fun derivative(variable: Variable): Generic {
        return JsclInteger.valueOf(0)
    }

    override fun substitute(variable: Variable, generic: Generic): Generic {
        return if (isIdentity(variable)) generic else expressionValue()
    }

    override fun expand(): Generic = expressionValue()

    override fun factorize(): Generic = expressionValue()

    override fun elementary(): Generic = expressionValue()

    override fun simplify(): Generic = expressionValue()

    override fun newInstance(): Variable = DoubleVariable(null)
}
