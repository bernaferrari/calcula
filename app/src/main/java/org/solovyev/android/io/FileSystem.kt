package org.solovyev.android.io

import okio.FileSystem as OkioFileSystem
import okio.Path
import org.solovyev.android.calculator.ErrorReporter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileSystem @Inject constructor() {
    private val okioFileSystem = OkioFileSystem.SYSTEM

    @Inject
    lateinit var errorReporter: ErrorReporter

    suspend fun writeSilently(file: Path, data: String): Boolean = try {
        write(file, data)
        true
    } catch (e: Exception) {
        errorReporter.onException(e)
        false
    }

    suspend fun write(file: Path, data: String) {
        FileSaver.save(file, data)
    }

    suspend fun read(file: Path): CharSequence? = FileLoader.load(file)

    fun exists(file: Path): Boolean = okioFileSystem.exists(file)
}
