package jscl.math

class JsclBoolean(content: Long) : ModularInteger(content, 2) {

    override fun newinstance(content: Long): ModularInteger {
        return if (content % 2 == 0L) zero else one
    }

    companion object {
        val factory = JsclBoolean(0)
        private val zero = factory
        private val one = JsclBoolean(1)
    }
}
