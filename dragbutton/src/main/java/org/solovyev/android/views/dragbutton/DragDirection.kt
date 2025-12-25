package org.solovyev.android.views.dragbutton

import androidx.annotation.StyleableRes

enum class DragDirection(
    val angleFrom: Float,
    val angleTo: Float,
    @StyleableRes val textAttr: Int,
    @StyleableRes val scaleAttr: Int,
    @StyleableRes val paddingAttr: Int
) {
    up(
        angleFrom = 180f - 45f,
        angleTo = 180f - 0f,
        textAttr = R.styleable.DirectionText_directionTextUp,
        scaleAttr = R.styleable.DirectionText_directionTextScaleUp,
        paddingAttr = R.styleable.DirectionText_directionTextPaddingUp
    ),
    down(
        angleFrom = 0f,
        angleTo = 45f,
        textAttr = R.styleable.DirectionText_directionTextDown,
        scaleAttr = R.styleable.DirectionText_directionTextScaleDown,
        paddingAttr = R.styleable.DirectionText_directionTextPaddingDown
    ),
    left(
        angleFrom = 90f - 45f,
        angleTo = 90f + 45f,
        textAttr = R.styleable.DirectionText_directionTextLeft,
        scaleAttr = R.styleable.DirectionText_directionTextScaleLeft,
        paddingAttr = R.styleable.DirectionText_directionTextPaddingLeft
    ),
    right(
        angleFrom = 90f - 45f,
        angleTo = 90f + 45f,
        textAttr = R.styleable.DirectionText_directionTextRight,
        scaleAttr = R.styleable.DirectionText_directionTextScaleRight,
        paddingAttr = R.styleable.DirectionText_directionTextPaddingRight
    )
}
