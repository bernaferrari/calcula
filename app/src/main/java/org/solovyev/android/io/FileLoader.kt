package org.solovyev.android.io

import okio.FileSystem
import okio.Path
import okio.Source

class FileLoader(
    private val file: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : BaseIoLoader() {

    override suspend fun getSource(): Source? =
        if (fileSystem.exists(file)) fileSystem.source(file) else null

    companion object {
        suspend fun load(file: Path): CharSequence? = FileLoader(file).load()
    }
}
