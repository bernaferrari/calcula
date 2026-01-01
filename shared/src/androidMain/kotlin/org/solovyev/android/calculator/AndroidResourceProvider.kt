package org.solovyev.android.calculator

import android.content.Context

class AndroidResourceProvider(private val context: Context) : ResourceProvider {
    override fun getString(id: String): String {
        // Try to get string resource by name, fallback to id itself if not found
        val resId = context.resources.getIdentifier(id, "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            id
        }
    }
}
