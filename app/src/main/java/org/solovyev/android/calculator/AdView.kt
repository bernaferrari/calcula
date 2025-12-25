package org.solovyev.android.calculator

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError

class AdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var admobView: com.google.android.gms.ads.AdView? = null
    private var admobListener: AdViewListener? = null

    init {
        visibility = GONE
        id = R.id.cpp_ad
    }

    fun destroy() {
        destroyAdmobView()
    }

    private fun destroyAdmobView() {
        admobView?.destroy()
        admobView = null

        admobListener?.destroy()
        admobListener = null
    }

    fun pause() {
        admobView?.pause()
    }

    fun resume() {
        admobView?.resume()
    }

    fun show() {
        if (admobView != null) {
            return
        }

        admobView = addAdmobView()
        admobListener = AdViewListener(this)
        admobListener?.let {
            admobView?.adListener = it
        }

        val builder = AdRequest.Builder()
        admobView?.loadAd(builder.build())
    }

    private fun addAdmobView(): com.google.android.gms.ads.AdView {
        val view = com.google.android.gms.ads.AdView(context)
        view.visibility = GONE
        view.setAdSize(AdSize.SMART_BANNER)
        view.adUnitId = resources.getString(R.string.admob)

        val layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
        addView(view, layoutParams)

        return view
    }

    fun hide() {
        if (admobView == null) {
            return
        }

        visibility = GONE

        admobView?.apply {
            visibility = GONE
            pause()
        }
        destroyAdmobView()
    }

    private class AdViewListener(private var adView: AdView?) : AdListener() {

        fun destroy() {
            adView = null
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            adView?.hide()
            adView = null
        }

        override fun onAdLoaded() {
            adView?.let { view ->
                view.admobView?.visibility = VISIBLE
                view.visibility = VISIBLE
                adView = null
            }
        }
    }
}
