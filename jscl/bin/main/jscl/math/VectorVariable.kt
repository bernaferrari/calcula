package jscl.math

class VectorVariable(generic: Generic?) : GenericVariable(generic) {
    override fun newInstance(): Variable = VectorVariable(null)
}
