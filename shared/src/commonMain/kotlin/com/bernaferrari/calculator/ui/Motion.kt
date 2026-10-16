package com.bernaferrari.calculator.ui

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/** One expressive spring for every container transform, so they all move as a family. */
@OptIn(ExperimentalSharedTransitionApi::class)
private val ContainerSpring = BoundsTransform { _, _ -> spring(dampingRatio = 0.86f, stiffness = 380f) }

/**
 * Marks both ends of an M3 container transform: put it on the button that opens a screen and on
 * that screen's root with the same [key], and the button grows into the screen (and back).
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.containerTransform(key: String, isScreen: Boolean = false): Modifier {
    val shared = LocalSharedTransitionScope.current ?: return this
    val animated = LocalNavAnimatedContentScope.current
    return with(shared) {
        this@containerTransform.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = animated,
            enter = fadeIn(),
            exit = fadeOut(),
            boundsTransform = ContainerSpring,
            // The screen scales (no reflowing text) while the button keeps its size, so its icon
            // never balloons to screen size on the way back.
            resizeMode = if (isScreen) {
                SharedTransitionScope.ResizeMode.scaleToBounds(ContentScale.FillWidth, Alignment.TopCenter)
            } else {
                SharedTransitionScope.ResizeMode.RemeasureToBounds
            },
            clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(28.dp)),
        )
    }
}
