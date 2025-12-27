package org.solovyev.android.calculator.history

import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import org.simpleframework.xml.Serializer
import org.simpleframework.xml.core.Persister

@Root(name = "History")
internal class OldHistory() {

    @field:ElementList(type = OldHistoryState::class, name = "historyItems")
    @get:JvmName("itemsProperty")
    var items: MutableList<OldHistoryState> = ArrayList()

    fun getItems(): List<OldHistoryState> = items

    companion object {
        @JvmStatic
        fun fromXml(xml: String?): OldHistory? {
            if (xml == null) {
                return null
            }
            val serializer: Serializer = Persister()
            return serializer.read(OldHistory::class.java, xml)
        }
    }
}
