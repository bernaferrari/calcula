package org.solovyev.android.io

import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class FileLoader(private val file: File) : BaseIoLoader() {

    override suspend fun getInputStream(): InputStream? =
        if (file.exists()) FileInputStream(file) else null

    companion object {
        suspend fun load(file: File): CharSequence? = FileLoader(file).load()
    }
}
