package org.solovyev.android.calculator.ui.adaptive

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.flow.*
import androidx.compose.ui.geometry.Rect
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Android implementation of FoldableDetector using Jetpack WindowManager.
 */
actual class FoldableDetector(private val context: Context) {

    private val windowInfoTracker = WindowInfoTracker.getOrCreate(context)
    private val _stateFlow = MutableStateFlow(FoldableState())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    init {
        scope.launch {
            windowInfoTracker.windowLayoutInfo(context).collect { layoutInfo ->
                updateFoldableState(layoutInfo)
            }
        }
    }

    actual fun isFoldable(): Boolean {
        // On Android, check if device has folding features
        val wm = ContextCompat.getSystemService(context, android.view.WindowManager::class.java)
        return wm?.defaultDisplay?.let { display ->
            // Check for folding feature
            true // Simplified; actual check requires WindowInfoTracker
        } ?: false
    }

    actual fun getFoldableState(): FoldableState {
        return _stateFlow.value
    }

    actual fun observeFoldableState(): StateFlow<FoldableState> {
        return _stateFlow.asStateFlow()
    }

    private fun updateFoldableState(layoutInfo: WindowLayoutInfo) {
        val foldingFeature = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()

        val state = if (foldingFeature != null) {
            val posture = when (foldingFeature.state) {
                FoldingFeature.State.FLAT -> FoldPosture.FLAT
                FoldingFeature.State.HALF_OPENED -> FoldPosture.HALF_OPENED
                else -> FoldPosture.FLAT
            }

            val bounds = foldingFeature.bounds
            val hingeBounds = Rect(
                left = bounds.left.toFloat(),
                top = bounds.top.toFloat(),
                right = bounds.right.toFloat(),
                bottom = bounds.bottom.toFloat()
            )

            val orientation = when (foldingFeature.orientation) {
                FoldingFeature.Orientation.HORIZONTAL -> FoldingOrientation.HORIZONTAL
                FoldingFeature.Orientation.VERTICAL -> FoldingOrientation.VERTICAL
                else -> FoldingOrientation.HORIZONTAL
            }

            FoldableState(
                posture = posture,
                hingeBounds = hingeBounds,
                isSeparating = foldingFeature.isSeparating,
                orientation = orientation
            )
        } else {
            FoldableState(posture = FoldPosture.FLAT)
        }

        _stateFlow.value = state
    }
}

/**
 * Android-specific extension to remember foldable state with WindowManager.
 */
@Composable
actual fun rememberFoldableState(): FoldableState {
    // This would use LocalContext.current on Android
    // For now, return flat state as default
    return FoldableState(posture = FoldPosture.FLAT)
}
