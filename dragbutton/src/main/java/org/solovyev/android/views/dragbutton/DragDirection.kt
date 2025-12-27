package org.solovyev.android.views.dragbutton

/**
 * Represents the four cardinal directions for drag gestures.
 */
enum class DragDirection(
    val angleFrom: Float,
    val angleTo: Float
) {
    up(
        angleFrom = 180f - 45f,
        angleTo = 180f
    ),
    down(
        angleFrom = 0f,
        angleTo = 45f
    ),
    left(
        angleFrom = 90f - 45f,
        angleTo = 90f + 45f
    ),
    right(
        angleFrom = 90f - 45f,
        angleTo = 90f + 45f
    )
}
