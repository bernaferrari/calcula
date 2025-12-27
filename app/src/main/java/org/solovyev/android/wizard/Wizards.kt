package org.solovyev.android.wizard

import android.app.Activity
import android.os.Bundle

interface Wizards {

    @Throws(IllegalArgumentException::class)
    fun getWizard(name: String?, arguments: Bundle? = null): Wizard
}
