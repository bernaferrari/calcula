package org.solovyev.android.calculator.ui.adaptive

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.*

/**
 * iOS implementation of FoldableDetector.
 *
 * iOS devices currently do not support foldable screens,
 * so this returns a flat state always.
 */
actual class FoldableDetector {
    private val _stateFlow = MutableStateFlow(FoldableState())

    actual fun isFoldable(): Boolean = false

    actual fun getFoldableState(): FoldableState = _stateFlow.value

    actual fun observeFoldableState(): StateFlow<FoldableState> {
        return _stateFlow.asStateFlow()
    }
}

/**
 * iOS implementation - always returns flat state.
 */
@Composable
actual fun rememberFoldableState(): FoldableState {
    return FoldableState(posture = FoldPosture.FLAT)
}
