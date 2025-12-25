package jscl.math

class MatrixVariable(generic: Generic?) : GenericVariable(generic) {
    override fun newInstance(): Variable = MatrixVariable(null)
}
