package org.solovyev.android.calculator.di

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.solovyev.android.calculator.App
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Domain class that provides access to application directories.
 * This replaces the @Named("dir-files") File pattern with a type-safe approach.
 */
@Singleton
class AppDirectories @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: AppDispatchers,
    private val appScope: AppCoroutineScope
) {
    private val fileSystem = FileSystem.SYSTEM
    /**
     * The app's internal files directory.
     */
    val filesDir: Path by lazy {
        val dir = context.filesDir?.absolutePath?.toPath()
            ?: context.applicationInfo.dataDir.toPath().resolve("files")
        // Ensure directory exists asynchronously
        appScope.launch(dispatchers.io) {
            try {
                fileSystem.createDirectories(dir)
            } catch (e: Exception) {
                Log.e(App.TAG, "Can't create files dirs", e)
            }
        }
        dir
    }

    /**
     * Get a file within the files directory.
     */
    fun getFile(name: String): Path = filesDir.resolve(name)

    /**
     * The app's cache directory.
     */
    val cacheDir: Path
        get() = context.cacheDir.absolutePath.toPath()

    /**
     * The app's external files directory (may be null if not available).
     */
    val externalFilesDir: Path?
        get() = context.getExternalFilesDir(null)?.absolutePath?.toPath()
}
