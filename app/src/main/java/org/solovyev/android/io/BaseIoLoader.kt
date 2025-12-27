package org.solovyev.android.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Source
import okio.buffer

abstract class BaseIoLoader {

    suspend fun load(): CharSequence? = withContext(Dispatchers.IO) {
        getSource()?.use { source ->
            source.buffer().use { it.readUtf8() }
        }
    }

    protected abstract suspend fun getSource(): Source?
}
