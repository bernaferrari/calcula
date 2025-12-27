package org.solovyev.android.calculator.release

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.solovyev.android.calculator.wizard.WizardPlaceholderFragment

class ChooseThemeReleaseNoteStep : ReleaseNoteStep {

    constructor(version: Int) : super(version)
    constructor(arguments: Bundle) : super(arguments)

    override val fragmentClass: Class<out Fragment>
        get() = WizardPlaceholderFragment::class.java

    companion object {
        const val VERSION_CODE = 137
    }
}
