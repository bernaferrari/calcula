package org.solovyev.android.wizard

import android.app.Activity
import android.os.Bundle

interface Wizards {
    val activityClassName: Class<out Activity>

    @Throws(IllegalArgumentException::class)
    fun getWizard(name: String?, arguments: Bundle? = null): Wizard
}
