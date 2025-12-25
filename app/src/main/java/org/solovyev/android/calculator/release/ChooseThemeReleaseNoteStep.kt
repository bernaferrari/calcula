package org.solovyev.android.calculator.release

import android.os.Bundle
import androidx.fragment.app.Fragment

class ChooseThemeReleaseNoteStep : ReleaseNoteStep {

    constructor(version: Int) : super(version)
    constructor(arguments: Bundle) : super(arguments)

    override val fragmentClass: Class<out Fragment>
        get() = ChooseThemeReleaseNoteFragment::class.java

    companion object {
        const val VERSION_CODE = 137
    }
}
