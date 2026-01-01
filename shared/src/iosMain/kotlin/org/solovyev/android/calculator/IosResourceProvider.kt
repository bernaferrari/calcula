package org.solovyev.android.calculator

import platform.Foundation.NSBundle

class IosResourceProvider : ResourceProvider {
    override fun getString(id: String): String {
        return NSBundle.mainBundle.localizedStringForKey(id, value = null, table = null)
    }
}
