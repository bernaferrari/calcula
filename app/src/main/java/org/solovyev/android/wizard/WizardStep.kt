package org.solovyev.android.wizard

import android.os.Bundle
import androidx.fragment.app.Fragment

interface WizardStep {
    val fragmentTag: String
    val fragmentClass: Class<out Fragment>
    val fragmentArgs: Bundle?
    val titleResId: Int
    val nextButtonTitleResId: Int
    val isVisible: Boolean
    val name: String

    fun onNext(fragment: Fragment): Boolean
    fun onPrev(fragment: Fragment): Boolean
}
