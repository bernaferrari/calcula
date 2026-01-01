package org.solovyev.android.calculator

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle

@OptIn(ExperimentalForeignApi::class)
class IosAppInfo : AppInfo {
    override val versionName: String
        get() = NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "Unknown"

    override val versionCode: Int
        get() = (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)?.toIntOrNull() ?: 0
}
