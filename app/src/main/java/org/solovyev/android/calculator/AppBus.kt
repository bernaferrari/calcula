package org.solovyev.android.calculator

import android.os.Handler
import android.os.Looper
import com.squareup.otto.Bus

class AppBus(private val handler: Handler) : Bus() {
    override fun post(event: Any) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.post(event)
            return
        }
        handler.post { super.post(event) }
    }
}
