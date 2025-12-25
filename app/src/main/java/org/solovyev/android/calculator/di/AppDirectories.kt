package org.solovyev.android.calculator.di

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.solovyev.android.calculator.App
import java.io.File
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
    /**
     * The app's internal files directory.
     */
    val filesDir: File by lazy {
        val dir = context.filesDir ?: File(context.applicationInfo.dataDir, "files")
        // Ensure directory exists asynchronously
        appScope.launch(dispatchers.io) {
            if (!dir.exists() && !dir.mkdirs()) {
                Log.e(App.TAG, "Can't create files dirs")
            }
        }
        dir
    }

    /**
     * Get a file within the files directory.
     */
    fun getFile(name: String): File = File(filesDir, name)

    /**
     * The app's cache directory.
     */
    val cacheDir: File
        get() = context.cacheDir

    /**
     * The app's external files directory (may be null if not available).
     */
    val externalFilesDir: File?
        get() = context.getExternalFilesDir(null)
}
