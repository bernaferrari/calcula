package org.solovyev.android.calculator.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.solovyev.android.calculator.di.AppCoroutineScope
import kotlin.coroutines.resume
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appScope: AppCoroutineScope
) : PurchasesUpdatedListener {

    private val _adFreePurchased = MutableStateFlow(false)
    val adFreePurchased: StateFlow<Boolean> = _adFreePurchased.asStateFlow()

    private val connectionMutex = Mutex()
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .setListener(this)
        .build()

    private var productDetails: ProductDetails? = null

    fun start() {
        appScope.launchMain {
            ensureReady()
            refreshPurchases()
            cacheProductDetails()
        }
    }

    fun stop() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }

    suspend fun launchAdFreePurchase(activity: Activity): BillingResult? {
        ensureReady()
        val details = productDetails ?: cacheProductDetails()
        if (details == null) {
            return null
        }
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()
                )
            )
            .build()
        return billingClient.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // No-op
        } else {
            _adFreePurchased.value = false
        }
    }

    private suspend fun ensureReady() {
        connectionMutex.withLock {
            if (billingClient.isReady) return
            suspendCancellableCoroutine { cont ->
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(result: BillingResult) {
                        cont.resume(Unit)
                    }

                    override fun onBillingServiceDisconnected() {
                        // Will reconnect on next request.
                    }
                })
            }
        }
    }

    private suspend fun cacheProductDetails(): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(AD_FREE_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        return suspendCancellableCoroutine { cont ->
            BillingClientBridge.queryProductDetailsAsync(
                billingClient,
                params
            ) { _: BillingResult, detailsResult: QueryProductDetailsResult ->
                productDetails = detailsResult.productDetailsList.firstOrNull()
                cont.resume(productDetails)
            }
        }
    }

    private fun refreshPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { _, purchases ->
            handlePurchases(purchases)
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var purchased = false
        purchases.forEach { purchase ->
            if (purchase.products.contains(AD_FREE_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            ) {
                purchased = true
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
            }
        }
        _adFreePurchased.value = purchased
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) { _ ->
            refreshPurchases()
        }
    }

    companion object {
        const val AD_FREE_PRODUCT_ID = "ad_free"
    }
}
