package org.solovyev.android.calculator.functions

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Transient
import java.io.Serializable

@Root(name = "function")
data class OldFunction(
    @field:Transient
    var id: Int? = null,
    
    @field:Element
    var name: String = "",
    
    @field:Element(name = "body")
    var content: String = "",
    
    @field:ElementList(type = String::class)
    var parameterNames: MutableList<String> = mutableListOf(),
    
    @field:Element
    var system: Boolean = false,
    
    @field:Element(required = false)
    var description: String = ""
) : Serializable
