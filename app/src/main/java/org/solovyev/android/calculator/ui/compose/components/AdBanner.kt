package org.solovyev.android.calculator.ui.compose.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun AdBanner(
    adUnitId: String,
    adFree: Boolean,
    modifier: Modifier = Modifier
) {
    if (adFree) {
        return
    }

    val context = LocalContext.current
    var adView: AdView? by remember { mutableStateOf(null) }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val adWidth = maxWidth.value.toInt().coerceAtLeast(1)
        val adSize = remember(adWidth) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { viewContext ->
                AdView(viewContext).apply {
                    setAdSize(adSize)
                    this.adUnitId = adUnitId
                    loadAd(AdRequest.Builder().build())
                    adView = this
                }
            },
            update = { view ->
                if (view.adSize != adSize) {
                    view.setAdSize(adSize)
                    view.loadAd(AdRequest.Builder().build())
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            adView?.destroy()
            adView = null
        }
    }
}
