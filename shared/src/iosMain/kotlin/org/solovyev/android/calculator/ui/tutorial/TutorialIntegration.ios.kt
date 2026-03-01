package org.solovyev.android.calculator.ui.tutorial

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSinceReferenceDate

// iOS implementation using NSDate
internal actual fun getCurrentTimeMillis(): Long = ((NSDate().timeIntervalSinceReferenceDate + 978307200) * 1000).toLong()
