package org.solovyev.android.calculator.variables

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "var")
data class OldVar(
    @field:Element
    var name: String = "",
    
    @field:Element(required = false)
    var value: String? = null,
    
    @field:Element
    var system: Boolean = false,
    
    @field:Element(required = false)
    var description: String? = null
)
