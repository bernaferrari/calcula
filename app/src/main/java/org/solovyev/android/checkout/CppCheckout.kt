package org.solovyev.android.checkout

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * App-wide [Checkout] which counts how many times it has been started.
 */
@Singleton
class CppCheckout @Inject constructor(
    billing: Billing
) : Checkout(null, billing) {

    private var started = 0

    override fun stop() {
        Check.isMainThread()
        Check.isTrue(started > 0, "Must be started first")
        started--
        if (started == 0) {
            super.stop()
        }
        started = max(0, started)
    }

    override fun start(listener: Listener?) {
        Check.isMainThread()
        started++
        if (started == 1) {
            super.start(listener)
        }
    }
}
