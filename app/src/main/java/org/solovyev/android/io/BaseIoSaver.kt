package org.solovyev.android.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.OutputStreamWriter

abstract class BaseIoSaver(private val data: CharSequence) {

    suspend fun save() = withContext(Dispatchers.IO) {
        getOutputStream().use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(data.toString())
            }
        }
    }

    protected abstract suspend fun getOutputStream(): FileOutputStream
}
