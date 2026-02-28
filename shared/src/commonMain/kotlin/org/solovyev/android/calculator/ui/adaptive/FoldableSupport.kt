package org.solovyev.android.calculator.ui.adaptive

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foldable state tracking for adaptive layouts.
 *
 * Represents the current state of a foldable device including:
 * - Fold posture (flat, half-opened, closed)
 * - Hinge position and bounds
 * - Display features (seam, avoid area)
 */
data class FoldableState(
    val posture: FoldPosture = FoldPosture.FLAT,
    val hingeBounds: Rect? = null,
    val isSeparating: Boolean = false,
    val orientation: FoldingOrientation = FoldingOrientation.HORIZONTAL
) {
    val isFlat: Boolean get() = posture == FoldPosture.FLAT
    val isHalfOpened: Boolean get() = posture == FoldPosture.HALF_OPENED
    val isClosed: Boolean get() = posture == FoldPosture.CLOSED

    /**
     * Returns true if content should avoid the hinge area.
     */
    fun shouldAvoidHinge(): Boolean = isSeparating && hingeBounds != null

    /**
     * Returns the hinge bounds in Dp.
     */
    @Composable
    fun getHingeBoundsDp(): Rect? {
        val density = LocalDensity.current
        return hingeBounds?.let { rect ->
            Rect(
                left = with(density) { rect.left.toDp().value },
                top = with(density) { rect.top.toDp().value },
                right = with(density) { rect.right.toDp().value },
                bottom = with(density) { rect.bottom.toDp().value }
            )
        }
    }
}

/**
 * Posture of a foldable device hinge.
 */
enum class FoldPosture {
    /** Device is flat, no fold is active */
    FLAT,
    /** Device is partially folded (like a laptop) */
    HALF_OPENED,
    /** Device is fully folded closed */
    CLOSED
}

/**
 * Orientation of the folding feature.
 */
enum class FoldingOrientation {
    /** Horizontal fold (portrait book mode) */
    HORIZONTAL,
    /** Vertical fold (landscape tabletop mode) */
    VERTICAL
}

/**
 * Window size classes following Material 3 guidelines.
 */
enum class WindowWidthClass {
    COMPACT,   // < 600dp (phones in portrait)
    MEDIUM,    // 600dp - 840dp (small tablets, foldables)
    EXPANDED   // > 840dp (large tablets, desktops)
}

enum class WindowHeightClass {
    COMPACT,   // < 480dp
    MEDIUM,    // 480dp - 900dp
    EXPANDED   // > 900dp
}

/**
 * Complete window size class information.
 */
data class WindowSizeClass(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass,
    val widthDp: Dp,
    val heightDp: Dp
) {
    val orientation: Orientation
        get() = if (widthDp > heightDp) Orientation.Landscape else Orientation.Portrait

    val isCompactWidth: Boolean get() = widthClass == WindowWidthClass.COMPACT
    val isMediumWidth: Boolean get() = widthClass == WindowWidthClass.MEDIUM
    val isExpandedWidth: Boolean get() = widthClass == WindowWidthClass.EXPANDED

    val isCompactHeight: Boolean get() = heightClass == WindowHeightClass.COMPACT
    val isExpandedHeight: Boolean get() = heightClass == WindowHeightClass.EXPANDED
}

enum class Orientation {
    Portrait, Landscape
}

/**
 * Calculates the current window size class from constraints.
 */
@Composable
fun calculateWindowSizeClass(): WindowSizeClass {
    var windowSizeClass by remember { mutableStateOf<WindowSizeClass?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth
        val heightDp = maxHeight

        val widthClass = when {
            widthDp < 600.dp -> WindowWidthClass.COMPACT
            widthDp < 840.dp -> WindowWidthClass.MEDIUM
            else -> WindowWidthClass.EXPANDED
        }

        val heightClass = when {
            heightDp < 480.dp -> WindowHeightClass.COMPACT
            heightDp < 900.dp -> WindowHeightClass.MEDIUM
            else -> WindowHeightClass.EXPANDED
        }

        windowSizeClass = WindowSizeClass(
            widthClass = widthClass,
            heightClass = heightClass,
            widthDp = widthDp,
            heightDp = heightDp
        )
    }

    return windowSizeClass ?: WindowSizeClass(
        widthClass = WindowWidthClass.COMPACT,
        heightClass = WindowHeightClass.MEDIUM,
        widthDp = 400.dp,
        heightDp = 800.dp
    )
}

/**
 * Remember foldable state (returns flat state on non-foldable devices).
 *
 * On Android, this would use the WindowManager Jetpack library.
 * On other platforms, returns a default flat state.
 */
@Composable
expect fun rememberFoldableState(): FoldableState

/**
 * Controller for managing foldable state.
 */
class FoldableStateController {
    private val _state = MutableStateFlow(FoldableState())
    val state: StateFlow<FoldableState> = _state.asStateFlow()

    fun updateState(newState: FoldableState) {
        _state.value = newState
    }

    fun updatePosture(posture: FoldPosture) {
        _state.value = _state.value.copy(posture = posture)
    }

    fun updateHingeBounds(bounds: Rect?) {
        _state.value = _state.value.copy(hingeBounds = bounds)
    }

    fun updateSeparating(isSeparating: Boolean) {
        _state.value = _state.value.copy(isSeparating = isSeparating)
    }
}

/**
 * Modifier to avoid placing content over the hinge area.
 */
@Composable
fun Modifier.avoidHinge(foldableState: FoldableState): Modifier {
    if (!foldableState.shouldAvoidHinge() || foldableState.hingeBounds == null) {
        return this
    }

    val hingeDp = foldableState.getHingeBoundsDp() ?: return this

    return this.then(
        Modifier.padding(
            start = hingeDp.left.dp,
            top = hingeDp.top.dp,
            end = hingeDp.right.dp,
            bottom = hingeDp.bottom.dp
        )
    )
}

/**
 * Modifier that applies different padding based on hinge position.
 */
@Composable
fun Modifier.hingeAwarePadding(
    foldableState: FoldableState,
    horizontalPadding: Dp = 16.dp,
    verticalPadding: Dp = 16.dp
): Modifier {
    val baseModifier = this.padding(horizontal = horizontalPadding, vertical = verticalPadding)

    if (!foldableState.shouldAvoidHinge()) {
        return baseModifier
    }

    return when (foldableState.orientation) {
        FoldingOrientation.HORIZONTAL -> {
            // Horizontal hinge - avoid top or bottom
            val hingeBounds = foldableState.hingeBounds ?: return baseModifier
            if (hingeBounds.top < 500f) {
                // Hinge at top, add extra top padding
                baseModifier.padding(top = hingeBounds.height().dp + verticalPadding)
            } else {
                // Hinge at bottom, add extra bottom padding
                baseModifier.padding(bottom = hingeBounds.height().dp + verticalPadding)
            }
        }
        FoldingOrientation.VERTICAL -> {
            // Vertical hinge - avoid left or right
            val hingeBounds = foldableState.hingeBounds ?: return baseModifier
            if (hingeBounds.left < 500f) {
                // Hinge at left, add extra left padding
                baseModifier.padding(start = hingeBounds.width().dp + horizontalPadding)
            } else {
                // Hinge at right, add extra right padding
                baseModifier.padding(end = hingeBounds.width().dp + horizontalPadding)
            }
        }
    }
}

/**
 * Layout that positions content to avoid the hinge in half-opened mode.
 *
 * @param topContent Content for the top/left screen
 * @param bottomContent Content for the bottom/right screen
 * @param foldableState Current foldable state
 * @param modifier Modifier for the layout
 */
@Composable
fun HingeAwareLayout(
    topContent: @Composable () -> Unit,
    bottomContent: @Composable () -> Unit,
    foldableState: FoldableState,
    modifier: Modifier = Modifier
) {
    when {
        !foldableState.isHalfOpened -> {
            // Not half-opened, just stack normally
            Column(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) { topContent() }
                Box(modifier = Modifier.weight(1f)) { bottomContent() }
            }
        }
        foldableState.orientation == FoldingOrientation.HORIZONTAL -> {
            // Horizontal hinge - stack vertically
            Column(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) { topContent() }
                Spacer(modifier = Modifier.height(foldableState.hingeBounds?.height()?.dp ?: 24.dp))
                Box(modifier = Modifier.weight(1f)) { bottomContent() }
            }
        }
        else -> {
            // Vertical hinge - side by side
            Row(modifier = modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) { topContent() }
                Spacer(modifier = Modifier.width(foldableState.hingeBounds?.width()?.dp ?: 24.dp))
                Box(modifier = Modifier.weight(1f)) { bottomContent() }
            }
        }
    }
}

/**
 * Determines optimal keyboard layout based on device state.
 */
fun calculateKeyboardLayout(
    windowSizeClass: WindowSizeClass,
    foldableState: FoldableState
): KeyboardLayout {
    return when {
        foldableState.isHalfOpened -> KeyboardLayout.SPLIT_FOR_FOLDABLE
        windowSizeClass.isExpandedWidth && windowSizeClass.orientation == Orientation.Landscape ->
            KeyboardLayout.SCIENTIFIC_SIDEBAR
        windowSizeClass.isMediumWidth && windowSizeClass.orientation == Orientation.Landscape ->
            KeyboardLayout.EXPANDED_LANDSCAPE
        windowSizeClass.isCompactWidth && windowSizeClass.orientation == Orientation.Landscape ->
            KeyboardLayout.COMPACT_LANDSCAPE
        else -> KeyboardLayout.STANDARD_PORTRAIT
    }
}

enum class KeyboardLayout {
    STANDARD_PORTRAIT,
    COMPACT_LANDSCAPE,
    EXPANDED_LANDSCAPE,
    SCIENTIFIC_SIDEBAR,
    SPLIT_FOR_FOLDABLE
}

/**
 * Calculates optimal button size based on available space.
 */
fun calculateButtonSize(
    containerWidth: Dp,
    containerHeight: Dp,
    columns: Int = 4,
    rows: Int = 5,
    spacing: Dp = 8.dp
): Dp {
    val availableWidth = containerWidth - (spacing * (columns + 1))
    val availableHeight = containerHeight - (spacing * (rows + 1))

    val widthBasedSize = availableWidth / columns
    val heightBasedSize = availableHeight / rows

    // Return the smaller dimension to ensure buttons fit
    return minOf(widthBasedSize, heightBasedSize).coerceAtLeast(48.dp)
}

/**
 * Calculates keyboard metrics for responsive layouts.
 */
data class KeyboardMetrics(
    val buttonSize: Dp,
    val horizontalSpacing: Dp,
    val verticalSpacing: Dp,
    val fontScale: Float,
    val iconScale: Float
)

@Composable
fun calculateKeyboardMetrics(
    containerWidth: Dp,
    containerHeight: Dp,
    columns: Int = 4,
    rows: Int = 5
): KeyboardMetrics {
    val density = LocalDensity.current
    val minButtonSize = 48.dp
    val spacing = when {
        containerWidth < 360.dp -> 4.dp
        containerWidth < 600.dp -> 8.dp
        else -> 12.dp
    }

    val availableWidth = containerWidth - (spacing * (columns - 1))
    val availableHeight = containerHeight - (spacing * (rows - 1))

    val widthBasedSize = availableWidth / columns
    val heightBasedSize = availableHeight / rows

    val buttonSize = maxOf(minOf(widthBasedSize, heightBasedSize), minButtonSize)

    // Scale fonts and icons based on button size
    val baseButtonSize = 56.dp
    val scale = (buttonSize.value / baseButtonSize.value).coerceIn(0.8f, 1.5f)

    return KeyboardMetrics(
        buttonSize = buttonSize,
        horizontalSpacing = spacing,
        verticalSpacing = spacing,
        fontScale = scale,
        iconScale = scale
    )
}

/**
 * Platform-specific foldable detection.
 *
 * On Android, this would use WindowManager to detect foldables.
 * On other platforms, returns null (no foldable support).
 */
expect class FoldableDetector {
    fun isFoldable(): Boolean
    fun getFoldableState(): FoldableState
    fun observeFoldableState(): StateFlow<FoldableState>
}

/**
 * Default implementation for non-Android platforms.
 * Note: FoldableDetector is an expect class so we can't extend it directly.
 * Use createNoOpFoldableDetector() to get an instance.
 */
interface FoldableDetectorInterface {
    fun isFoldable(): Boolean
    fun getFoldableState(): FoldableState
    fun observeFoldableState(): StateFlow<FoldableState>
}

object NoOpFoldableDetector : FoldableDetectorInterface {
    override fun isFoldable(): Boolean = false
    override fun getFoldableState(): FoldableState = FoldableState()
    override fun observeFoldableState(): StateFlow<FoldableState> =
        MutableStateFlow(FoldableState()).asStateFlow()
}

fun createNoOpFoldableDetector(): FoldableDetectorInterface = NoOpFoldableDetector

/**
 * CompositionLocal for foldable state.
 */
val LocalFoldableState = staticCompositionLocalOf<FoldableState> {
    FoldableState(posture = FoldPosture.FLAT)
}

/**
 * CompositionLocal for window size class.
 */
val LocalWindowSizeClass = staticCompositionLocalOf<WindowSizeClass> {
    WindowSizeClass(
        widthClass = WindowWidthClass.COMPACT,
        heightClass = WindowHeightClass.MEDIUM,
        widthDp = 400.dp,
        heightDp = 800.dp
    )
}

private fun Rect.width(): Float = right - left
private fun Rect.height(): Float = bottom - top
