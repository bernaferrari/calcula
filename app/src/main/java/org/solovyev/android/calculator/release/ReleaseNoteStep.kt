package org.solovyev.android.calculator.release

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.solovyev.android.wizard.WizardStep

open class ReleaseNoteStep : WizardStep {

    private val version: Int

    constructor(version: Int) {
        this.version = version
    }

    constructor(arguments: Bundle) {
        this.version = arguments.getInt(ReleaseNoteFragment.ARG_VERSION, 0)
    }

    override val fragmentTag: String
        get() = name

    override val fragmentClass: Class<out Fragment>
        get() = ReleaseNoteFragment::class.java

    override val fragmentArgs: Bundle
        get() = Bundle().apply {
            putInt(ReleaseNoteFragment.ARG_VERSION, version)
        }

    override val titleResId: Int
        get() = 0

    override val nextButtonTitleResId: Int
        get() = 0

    override fun onNext(fragment: Fragment): Boolean = false

    override fun onPrev(fragment: Fragment): Boolean = false

    override val isVisible: Boolean
        get() = false

    override val name: String
        get() = "release-note-$version"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReleaseNoteStep) return false
        return version == other.version
    }

    override fun hashCode(): Int = version
}
