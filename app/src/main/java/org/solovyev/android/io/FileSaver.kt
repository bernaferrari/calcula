package org.solovyev.android.io

import okio.FileSystem
import okio.Path
import okio.Sink

class FileSaver private constructor(
    private val file: Path,
    data: CharSequence,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : BaseIoSaver(data) {

    override suspend fun getSink(): Sink {
        file.parent?.let { dir ->
            fileSystem.createDirectories(dir)
        }
        return fileSystem.sink(file)
    }

    companion object {
        suspend fun save(file: Path, data: CharSequence) {
            FileSaver(file, data).save()
        }
    }
}
