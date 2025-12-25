package org.solovyev.android.io

import org.solovyev.android.calculator.ErrorReporter
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSystem @Inject constructor() {

    @Inject
    lateinit var errorReporter: ErrorReporter

    suspend fun writeSilently(file: File, data: String): Boolean = try {
        write(file, data)
        true
    } catch (e: Exception) {
        errorReporter.onException(e)
        false
    }

    suspend fun write(file: File, data: String) {
        FileSaver.save(file, data)
    }

    suspend fun read(file: File): CharSequence? = FileLoader.load(file)
}
