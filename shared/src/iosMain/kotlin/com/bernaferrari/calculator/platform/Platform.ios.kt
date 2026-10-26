package com.bernaferrari.calculator.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.popoverPresentationController
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.bernaferrari.calculator.ui.calculator.Separators
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun systemSeparators(): Separators {
    val formatter = NSNumberFormatter().apply { locale = NSLocale.currentLocale }
    return Separators(
        decimal = formatter.decimalSeparator.firstOrNull() ?: '.',
        grouping = formatter.groupingSeparator.firstOrNull() ?: ',',
    )
}

actual fun formatDate(epochMillis: Long): String = NSDateFormatter().apply {
    dateStyle = NSDateFormatterMediumStyle
    timeStyle = NSDateFormatterNoStyle
}.stringFromDate(NSDate.dateWithTimeIntervalSince1970(epochMillis / 1000.0))

actual class KeyValueStore(private val defaults: NSUserDefaults) : StringStore {
    actual override fun get(key: String): String? = defaults.stringForKey(key)

    actual override fun put(key: String, value: String?) {
        if (value == null) defaults.removeObjectForKey(key) else defaults.setObject(value, key)
    }
}

/** Shared with the widget and Siri through the app group. */
const val APP_GROUP = "group.com.bernaferrari.calcula"

actual fun keyValueStore(): KeyValueStore = KeyValueStore(NSUserDefaults(suiteName = APP_GROUP))

@Composable
actual fun rememberKeySounds(): (KeySound) -> Unit = remember {
    { sound ->
        // The keyboard's own clicks: 1104 tap, 1155 delete, 1156 modifier (used for =).
        val id = when (sound) {
            KeySound.Tap -> 1104u
            KeySound.Delete -> 1155u
            KeySound.Equals, KeySound.Error -> 1156u
        }
        AudioServicesPlaySystemSound(id)
    }
}

@Composable
actual fun wallpaperColorScheme(dark: Boolean): ColorScheme? = null

actual val supportsDynamicColor: Boolean = false

@Composable
actual fun SystemBarsAppearance(darkTheme: Boolean) = Unit

actual val systemConfirmsCopy: Boolean = false

actual fun ImageBitmap.encodePng(): ByteArray =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: ByteArray(0)

@Composable
actual fun rememberTextSharer(): (text: String) -> Unit = remember {
    { text ->
        var top = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        if (top != null) {
            val sheet = UIActivityViewController(listOf(text), null)
            sheet.popoverPresentationController?.sourceView = top.view
            top.presentViewController(sheet, animated = true, completion = null)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun rememberImageSharer(): (png: ByteArray, fileName: String) -> Unit = remember {
    { png, _ ->
        val data = png.usePinned { NSData.create(bytes = it.addressOf(0), length = png.size.toULong()) }
        val image = UIImage.imageWithData(data)
        var top = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (top?.presentedViewController != null) top = top.presentedViewController
        if (image != null && top != null) {
            val sheet = UIActivityViewController(listOf(image), null)
            // iPad shows the sheet as a popover, which needs somewhere to point.
            sheet.popoverPresentationController?.sourceView = top.view
            top.presentViewController(sheet, animated = true, completion = null)
        }
    }
}
