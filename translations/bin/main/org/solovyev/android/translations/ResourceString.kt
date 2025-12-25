package org.solovyev.android.translations

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Root
import org.simpleframework.xml.Text

@Suppress("unused")
@Root(name = "string", strict = false)
class ResourceString {
    @field:Attribute
    var name: String? = null

    @field:Attribute(required = false)
    var comment: String? = null

    @field:Text(required = false)
    var value: String? = null

    constructor()

    constructor(name: String?, value: String?) {
        this.name = name
        this.value = value
    }
}
