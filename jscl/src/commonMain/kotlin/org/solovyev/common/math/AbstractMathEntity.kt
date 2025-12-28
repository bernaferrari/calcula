package org.solovyev.common.math

abstract class AbstractMathEntity : MathEntity {

    override var name: String = ""
        protected set
    private var system: Boolean = false

    override fun isSystem(): Boolean {
        return system
    }
}
