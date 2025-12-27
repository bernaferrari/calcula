package org.solovyev.android.calculator.billing;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;

public final class BillingClientBridge {

    private BillingClientBridge() {
        // Utility class
    }

    public interface ProductDetailsCallback {
        void onResult(BillingResult result, QueryProductDetailsResult detailsResult);
    }

    public static void queryProductDetailsAsync(
        BillingClient client,
        QueryProductDetailsParams params,
        ProductDetailsCallback callback
    ) {
        client.queryProductDetailsAsync(
            params,
            (result, detailsResult) -> callback.onResult(result, detailsResult)
        );
    }
}
