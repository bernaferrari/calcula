package org.solovyev.android.calculator

import android.content.Context
import android.content.res.Configuration

actual fun App.isTablet(context: Any): Boolean {
    val ctx = context as Context
    val screenLayout = ctx.resources.configuration.screenLayout
    return (screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
}
