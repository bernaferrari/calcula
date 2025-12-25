/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.preferences

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import org.solovyev.android.calculator.App
import org.solovyev.android.calculator.BaseDialogFragment
import org.solovyev.android.calculator.R
import org.solovyev.android.calculator.ga.Ga
import org.solovyev.android.checkout.ActivityCheckout
import org.solovyev.android.checkout.Billing
import org.solovyev.android.checkout.BillingRequests
import org.solovyev.android.checkout.Checkout
import org.solovyev.android.checkout.ProductTypes
import org.solovyev.android.checkout.Purchase
import org.solovyev.android.checkout.RequestListener
import javax.inject.Inject

@AndroidEntryPoint
class PurchaseDialogActivity : AppCompatActivity(), RequestListener<Purchase> {

    @Inject
    lateinit var billing: Billing

    @Inject
    lateinit var ga: Ga

    private lateinit var checkout: ActivityCheckout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            App.showDialog(PurchaseDialogFragment(), PurchaseDialogFragment.FRAGMENT_TAG, supportFragmentManager)
        }

        checkout = Checkout.forActivity(this, billing)
        checkout.start()
        checkout.createPurchaseFlow(this)
    }

    private fun purchase() {
        checkout.whenReady(object : Checkout.EmptyListener() {
            override fun onReady(requests: BillingRequests) {
                requests.purchase(ProductTypes.IN_APP, "ad_free", null, checkout.purchaseFlow)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkout.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        checkout.destroyPurchaseFlow()
        checkout.stop()
        super.onDestroy()
    }

    fun onDialogClosed() {
        val fragment = supportFragmentManager.findFragmentByTag(PurchaseDialogFragment.FRAGMENT_TAG)
        if (fragment == null) {
            // activity is closing
            return
        }
        finish()
    }

    override fun onSuccess(result: Purchase) {
        finish()
    }

    override fun onError(response: Int, e: Exception) {
        finish()
    }

    class PurchaseDialogFragment : BaseDialogFragment() {

        private var activity: PurchaseDialogActivity? = null

        override fun onAttach(activity: android.app.Activity) {
            super.onAttach(activity)
            this.activity = activity as PurchaseDialogActivity
        }

        override fun onPrepareDialog(builder: AlertDialog.Builder) {
            super.onPrepareDialog(builder)
            builder.setTitle(R.string.cpp_purchase_title)
            builder.setMessage(R.string.cpp_purchase_text)
            builder.setPositiveButton(R.string.cpp_continue, null)
        }

        override fun onClick(dialog: DialogInterface, which: Int) {
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> activity?.purchase()
                else -> super.onClick(dialog, which)
            }
        }

        override fun onDismiss(dialog: DialogInterface) {
            super.onDismiss(dialog)
            activity?.onDialogClosed()
            activity = null
        }

        companion object {
            const val FRAGMENT_TAG = "purchase-dialog"
        }
    }
}
