@file:OptIn(ExperimentalWasmJsInterop::class)

package com.bernaferrari.calculator.platform

import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import com.bernaferrari.calculator.ui.calculator.Separators
import kotlinx.browser.localStorage

private fun localeDecimal(): String = js("(1.1).toLocaleString().charAt(1)")

private fun localeGrouping(): String = js("(1000).toLocaleString().replace(/[0-9]/g, '').charAt(0) || ','")

private fun jsFormatDate(millis: Double): String =
    js("new Date(millis).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })")

actual fun systemSeparators(): Separators =
    Separators(decimal = localeDecimal().firstOrNull() ?: '.', grouping = localeGrouping().firstOrNull() ?: ',')

actual fun formatDate(epochMillis: Long): String = jsFormatDate(epochMillis.toDouble())

actual class KeyValueStore {
    actual fun get(key: String): String? = localStorage.getItem(key)

    actual fun put(key: String, value: String?) {
        if (value == null) localStorage.removeItem(key) else localStorage.setItem(key, value)
    }
}

actual fun keyValueStore(): KeyValueStore = KeyValueStore()

@Composable
actual fun rememberKeySounds(): (KeySound) -> Unit = { }

@Composable
actual fun wallpaperColorScheme(dark: Boolean): ColorScheme? = null

actual val supportsDynamicColor: Boolean = false

@Composable
actual fun SystemBarsAppearance(darkTheme: Boolean) = Unit

actual val systemConfirmsCopy: Boolean = false

actual fun ImageBitmap.encodePng(): ByteArray =
    Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes ?: ByteArray(0)

private fun download(url: String, name: String): Unit =
    js("{ const a = document.createElement('a'); a.href = url; a.download = name; document.body.appendChild(a); a.click(); a.remove(); }")

/** Browsers have no share sheet for files everywhere, so the image downloads. */
@OptIn(ExperimentalEncodingApi::class)
@Composable
actual fun rememberImageSharer(): (png: ByteArray, fileName: String) -> Unit = remember {
    { png, fileName -> download("data:image/png;base64," + Base64.encode(png), fileName) }
}
