package org.solovyev.android.calculator.ads

import android.os.Handler
import android.os.Looper
import android.view.View
import org.solovyev.android.calculator.AdView
import org.solovyev.android.calculator.R
import org.solovyev.android.checkout.CppCheckout
import org.solovyev.android.checkout.Inventory
import org.solovyev.android.checkout.ProductTypes.IN_APP
import javax.inject.Inject

class AdUi @Inject constructor(
    private val checkout: CppCheckout,
    private val handler: Handler
) {
    private var adView: AdView? = null
    private var adFree: Boolean? = null

    fun onCreate() {
        checkout.start()
    }

    fun onResume() {
        adView?.resume()

        if (adFree != null) {
            updateAdView()
        } else {
            checkout.loadInventory(
                Inventory.Request.create().loadAllPurchases(),
                onMainThread(object : Inventory.Callback {
                    override fun onLoaded(products: Inventory.Products) {
                        adFree = products.get(IN_APP).isPurchased("ad_free")
                        updateAdView()
                    }
                })
            )
        }
    }

    private fun updateAdView() {
        val view = adView ?: return
        val isFree = adFree ?: return

        if (isFree) {
            view.hide()
        } else {
            view.show()
        }
    }

    private fun onMainThread(callback: Inventory.Callback): Inventory.Callback {
        return object : Inventory.Callback {
            override fun onLoaded(products: Inventory.Products) {
                if (handler.looper == Looper.myLooper()) {
                    callback.onLoaded(products)
                    return
                }
                handler.post {
                    callback.onLoaded(products)
                }
            }
        }
    }

    fun onCreateView(view: View) {
        adView = view.findViewById(R.id.cpp_ad)
    }

    fun onPause() {
        adFree = null
        adView?.pause()
    }

    fun onDestroyView() {
        adView?.destroy()
        adView = null
    }

    fun onDestroy() {
        checkout.stop()
    }
}
