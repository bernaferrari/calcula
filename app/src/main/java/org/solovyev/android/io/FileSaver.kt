package org.solovyev.android.io

import java.io.File
import java.io.FileOutputStream

class FileSaver private constructor(
    private val file: File,
    data: CharSequence
) : BaseIoSaver(data) {

    override suspend fun getOutputStream(): FileOutputStream {
        file.parentFile?.let { dir ->
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
        return FileOutputStream(file)
    }

    companion object {
        suspend fun save(file: File, data: CharSequence) {
            FileSaver(file, data).save()
        }
    }
}
