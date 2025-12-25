package org.solovyev.android.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

abstract class BaseIoLoader {

    suspend fun load(): CharSequence? = withContext(Dispatchers.IO) {
        getInputStream()?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                buildString {
                    reader.lineSequence().forEach { line ->
                        append(line).append("\n")
                    }
                }
            }
        }
    }

    protected abstract suspend fun getInputStream(): InputStream?
}
