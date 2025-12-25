package org.solovyev.android.translations

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Transient

@Root(strict = false)
class Resources {
    @field:ElementList(inline = true)
    var strings: MutableList<ResourceString> = ArrayList()

    @field:Transient
    var comment: String? = null
}
