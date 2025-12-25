package org.solovyev.android.io

import android.util.Log
import java.io.Closeable

object Io {
    @JvmStatic
    fun close(closeable: Closeable?) {
        try {
            closeable?.close()
        } catch (e: Exception) {
            Log.e("Io", e.message ?: "Unknown error", e)
        }
    }
}
