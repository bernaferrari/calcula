package org.solovyev.android.calculator.release

import androidx.annotation.StringRes

data class ReleaseNote(
    val versionName: String,
    @StringRes val description: Int
)
