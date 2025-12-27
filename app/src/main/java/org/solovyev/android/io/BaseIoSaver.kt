package org.solovyev.android.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Sink
import okio.buffer

abstract class BaseIoSaver(private val data: CharSequence) {

    suspend fun save() = withContext(Dispatchers.IO) {
        getSink().use { sink ->
            sink.buffer().use { it.writeUtf8(data.toString()) }
        }
    }

    protected abstract suspend fun getSink(): Sink
}
