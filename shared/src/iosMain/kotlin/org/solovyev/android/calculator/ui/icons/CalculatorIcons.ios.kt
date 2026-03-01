package org.solovyev.android.calculator.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// iOS implementations using simple vector paths
// In a production app, these would be actual vector assets or SF Symbols

internal actual fun getBackIcon(): IconType = VectorIcon(createSimpleArrowBackVector())
internal actual fun getCloseIcon(): IconType = VectorIcon(createSimpleCloseVector())
internal actual fun getMoreVertIcon(): IconType = VectorIcon(createSimpleMoreVertVector())
internal actual fun getAddIcon(): IconType = VectorIcon(createSimpleAddVector())
internal actual fun getClearIcon(): IconType = VectorIcon(createSimpleClearVector())
internal actual fun getDeleteIcon(): IconType = VectorIcon(createSimpleDeleteVector())
internal actual fun getEditIcon(): IconType = VectorIcon(createSimpleEditVector())
internal actual fun getContentCopyIcon(): IconType = VectorIcon(createSimpleCopyVector())
internal actual fun getLocationOnIcon(): IconType = VectorIcon(createSimpleLocationVector())
internal actual fun getBrightnessAutoIcon(): IconType = VectorIcon(createSimpleBrightnessVector())
internal actual fun getBrightnessHighIcon(): IconType = VectorIcon(createSimpleBrightnessHighVector())
internal actual fun getBrightnessLowIcon(): IconType = VectorIcon(createSimpleBrightnessLowVector())
internal actual fun getFullscreenIcon(): IconType = VectorIcon(createSimpleFullscreenVector())
internal actual fun getScreenRotationIcon(): IconType = VectorIcon(createSimpleRotationVector())
internal actual fun getVibrationIcon(): IconType = VectorIcon(createSimpleVibrationVector())
internal actual fun getCalculateIcon(): IconType = VectorIcon(createSimpleCalculateVector())
internal actual fun getCodeIcon(): IconType = VectorIcon(createSimpleCodeVector())
internal actual fun getTextFieldsIcon(): IconType = VectorIcon(createSimpleTextFieldsVector())
internal actual fun getKeyboardIcon(): IconType = VectorIcon(createSimpleKeyboardVector())
internal actual fun getSettingsIcon(): IconType = VectorIcon(createSimpleSettingsVector())
internal actual fun getContrastIcon(): IconType = VectorIcon(createSimpleContrastVector())
internal actual fun getFlashOnIcon(): IconType = VectorIcon(createSimpleFlashVector())
internal actual fun getPaletteIcon(): IconType = VectorIcon(createSimplePaletteVector())
internal actual fun getArrowBackIcon(): IconType = VectorIcon(createSimpleArrowBackVector())
internal actual fun getArrowForwardIcon(): IconType = VectorIcon(createSimpleArrowForwardVector())
internal actual fun getHistoryIcon(): IconType = VectorIcon(createSimpleHistoryVector())
internal actual fun getShareIcon(): IconType = VectorIcon(createSimpleShareVector())
internal actual fun getStarIcon(): IconType = VectorIcon(createSimpleStarVector())
internal actual fun getCheckIcon(): IconType = VectorIcon(createSimpleCheckVector())
internal actual fun getPlayArrowIcon(): IconType = VectorIcon(createSimplePlayVector())
internal actual fun getScheduleIcon(): IconType = VectorIcon(createSimpleScheduleVector())
internal actual fun getSpeedIcon(): IconType = VectorIcon(createSimpleSpeedVector())
internal actual fun getSaveIcon(): IconType = VectorIcon(createSimpleSaveVector())
internal actual fun getPriorityHighIcon(): IconType = VectorIcon(createSimplePriorityVector())

// Simple vector icon builders - these create basic shapes as fallbacks
// In production, these should be replaced with actual vector assets

private fun createSimpleArrowBackVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(20f, 11f)
        lineTo(7.83f, 11f)
        lineTo(13.42f, 5.41f)
        lineTo(12f, 4f)
        lineTo(4f, 12f)
        lineTo(12f, 20f)
        lineTo(13.41f, 18.59f)
        lineTo(7.83f, 13f)
        lineTo(20f, 13f)
        close()
    }
}.build()

private fun createSimpleArrowForwardVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 4f)
        lineTo(10.59f, 5.41f)
        lineTo(16.17f, 11f)
        lineTo(4f, 11f)
        lineTo(4f, 13f)
        lineTo(16.17f, 13f)
        lineTo(10.59f, 18.59f)
        lineTo(12f, 20f)
        lineTo(20f, 12f)
        close()
    }
}.build()

private fun createSimpleCloseVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(19f, 6.41f)
        lineTo(17.59f, 5f)
        lineTo(12f, 10.59f)
        lineTo(6.41f, 5f)
        lineTo(5f, 6.41f)
        lineTo(10.59f, 12f)
        lineTo(5f, 17.59f)
        lineTo(6.41f, 19f)
        lineTo(12f, 13.41f)
        lineTo(17.59f, 19f)
        lineTo(19f, 17.59f)
        lineTo(13.41f, 12f)
        close()
    }
}.build()

private fun createSimpleMoreVertVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 8f)
        curveTo(13.1f, 8f, 14f, 7.1f, 14f, 6f)
        curveTo(14f, 4.9f, 13.1f, 4f, 12f, 4f)
        curveTo(10.9f, 4f, 10f, 4.9f, 10f, 6f)
        curveTo(10f, 7.1f, 10.9f, 8f, 12f, 8f)
        close()
        moveTo(12f, 14f)
        curveTo(13.1f, 14f, 14f, 13.1f, 14f, 12f)
        curveTo(14f, 10.9f, 13.1f, 10f, 12f, 10f)
        curveTo(10.9f, 10f, 10f, 10.9f, 10f, 12f)
        curveTo(10f, 13.1f, 10.9f, 14f, 12f, 14f)
        close()
        moveTo(12f, 20f)
        curveTo(13.1f, 20f, 14f, 19.1f, 14f, 18f)
        curveTo(14f, 16.9f, 13.1f, 16f, 12f, 16f)
        curveTo(10.9f, 16f, 10f, 16.9f, 10f, 18f)
        curveTo(10f, 19.1f, 10.9f, 20f, 12f, 20f)
        close()
    }
}.build()

private fun createSimpleAddVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(19f, 13f)
        lineTo(13f, 13f)
        lineTo(13f, 19f)
        lineTo(11f, 19f)
        lineTo(11f, 13f)
        lineTo(5f, 13f)
        lineTo(5f, 11f)
        lineTo(11f, 11f)
        lineTo(11f, 5f)
        lineTo(13f, 5f)
        lineTo(13f, 11f)
        lineTo(19f, 11f)
        close()
    }
}.build()

private fun createSimpleClearVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(19f, 6.41f)
        lineTo(17.59f, 5f)
        lineTo(12f, 10.59f)
        lineTo(6.41f, 5f)
        lineTo(5f, 6.41f)
        lineTo(10.59f, 12f)
        lineTo(5f, 17.59f)
        lineTo(6.41f, 19f)
        lineTo(12f, 13.41f)
        lineTo(17.59f, 19f)
        lineTo(19f, 17.59f)
        lineTo(13.41f, 12f)
        close()
    }
}.build()

private fun createSimpleDeleteVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(6f, 19f)
        curveTo(6f, 20.1f, 6.9f, 21f, 8f, 21f)
        lineTo(16f, 21f)
        curveTo(17.1f, 21f, 18f, 20.1f, 18f, 19f)
        lineTo(18f, 7f)
        lineTo(6f, 7f)
        lineTo(6f, 19f)
        close()
        moveTo(19f, 4f)
        lineTo(15.5f, 4f)
        lineTo(14.5f, 3f)
        lineTo(9.5f, 3f)
        lineTo(8.5f, 4f)
        lineTo(5f, 4f)
        lineTo(5f, 6f)
        lineTo(19f, 6f)
        close()
    }
}.build()

private fun createSimpleEditVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(3f, 17.25f)
        lineTo(3f, 21f)
        lineTo(6.75f, 21f)
        lineTo(17.81f, 9.94f)
        lineTo(14.06f, 6.19f)
        close()
        moveTo(20.71f, 7.04f)
        curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
        lineTo(18.37f, 3.29f)
        curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
        lineTo(15.13f, 5.12f)
        lineTo(18.88f, 8.87f)
        close()
    }
}.build()

private fun createSimpleCopyVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(16f, 1f)
        lineTo(4f, 1f)
        curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
        lineTo(2f, 17f)
        lineTo(4f, 17f)
        lineTo(4f, 3f)
        lineTo(16f, 3f)
        close()
        moveTo(20f, 5f)
        lineTo(8f, 5f)
        curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
        lineTo(6f, 21f)
        curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
        lineTo(20f, 23f)
        curveTo(21.1f, 23f, 22f, 22.1f, 22f, 21f)
        lineTo(22f, 7f)
        curveTo(22f, 5.9f, 21.1f, 5f, 20f, 5f)
        close()
        moveTo(20f, 21f)
        lineTo(8f, 21f)
        lineTo(8f, 7f)
        lineTo(20f, 7f)
        close()
    }
}.build()

private fun createSimpleLocationVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 2f)
        curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f)
        curveTo(5f, 14.25f, 12f, 22f, 12f, 22f)
        curveTo(12f, 22f, 19f, 14.25f, 19f, 9f)
        curveTo(19f, 5.13f, 15.87f, 2f, 12f, 2f)
        close()
        moveTo(12f, 11.5f)
        curveTo(10.62f, 11.5f, 9.5f, 10.38f, 9.5f, 9f)
        curveTo(9.5f, 7.62f, 10.62f, 6.5f, 12f, 6.5f)
        curveTo(13.38f, 6.5f, 14.5f, 7.62f, 14.5f, 9f)
        curveTo(14.5f, 10.38f, 13.38f, 11.5f, 12f, 11.5f)
        close()
    }
}.build()

private fun createSimpleBrightnessVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 7f)
        curveTo(9.24f, 7f, 7f, 9.24f, 7f, 12f)
        curveTo(7f, 14.76f, 9.24f, 17f, 12f, 17f)
        curveTo(14.76f, 17f, 17f, 14.76f, 17f, 12f)
        curveTo(17f, 9.24f, 14.76f, 7f, 12f, 7f)
        close()
        moveTo(2f, 13f)
        lineTo(4f, 13f)
        lineTo(4f, 11f)
        lineTo(2f, 11f)
        close()
        moveTo(20f, 13f)
        lineTo(22f, 13f)
        lineTo(22f, 11f)
        lineTo(20f, 11f)
        close()
        moveTo(11f, 2f)
        lineTo(11f, 4f)
        lineTo(13f, 4f)
        lineTo(13f, 2f)
        close()
        moveTo(11f, 20f)
        lineTo(11f, 22f)
        lineTo(13f, 22f)
        lineTo(13f, 20f)
        close()
        moveTo(5.99f, 4.58f)
        lineTo(4.58f, 5.99f)
        lineTo(5.99f, 7.4f)
        lineTo(7.4f, 5.99f)
        close()
        moveTo(18.01f, 19.42f)
        lineTo(19.42f, 18.01f)
        lineTo(18.01f, 16.6f)
        lineTo(16.6f, 18.01f)
        close()
    }
}.build()

private fun createSimpleBrightnessHighVector(): ImageVector = createSimpleBrightnessVector()
private fun createSimpleBrightnessLowVector(): ImageVector = createSimpleBrightnessVector()

private fun createSimpleFullscreenVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(7f, 14f)
        lineTo(5f, 14f)
        lineTo(5f, 19f)
        lineTo(10f, 19f)
        lineTo(10f, 17f)
        lineTo(7f, 17f)
        close()
        moveTo(5f, 10f)
        lineTo(7f, 10f)
        lineTo(7f, 7f)
        lineTo(10f, 7f)
        lineTo(10f, 5f)
        lineTo(5f, 5f)
        close()
        moveTo(17f, 17f)
        lineTo(14f, 17f)
        lineTo(14f, 19f)
        lineTo(19f, 19f)
        lineTo(19f, 14f)
        lineTo(17f, 14f)
        close()
        moveTo(14f, 5f)
        lineTo(14f, 7f)
        lineTo(17f, 7f)
        lineTo(17f, 10f)
        lineTo(19f, 10f)
        lineTo(19f, 5f)
        close()
    }
}.build()

private fun createSimpleRotationVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(16.48f, 2.52f)
        curveTo(14.75f, 0.84f, 12.53f, 0f, 10f, 0f)
        lineTo(10f, 2f)
        curveTo(11.89f, 2f, 13.46f, 2.62f, 14.71f, 3.87f)
        curveTo(15.96f, 5.12f, 16.58f, 6.69f, 16.58f, 8.58f)
        lineTo(19.29f, 5.87f)
        lineTo(20.71f, 7.29f)
        lineTo(16.29f, 11.71f)
        lineTo(11.87f, 7.29f)
        lineTo(13.29f, 5.87f)
        lineTo(16f, 8.58f)
        curveTo(16f, 6.15f, 15.22f, 4.15f, 13.66f, 2.58f)
        close()
        moveTo(10f, 22f)
        curveTo(12.53f, 22f, 14.75f, 21.16f, 16.48f, 19.48f)
        curveTo(18.21f, 17.8f, 19.08f, 15.57f, 19.08f, 12.8f)
        lineTo(16.37f, 15.51f)
        lineTo(14.95f, 14.09f)
        lineTo(19.37f, 9.67f)
        lineTo(23.79f, 14.09f)
        lineTo(22.37f, 15.51f)
        lineTo(19.66f, 12.8f)
        curveTo(19.66f, 15.23f, 18.88f, 17.23f, 17.32f, 18.8f)
        curveTo(15.76f, 20.37f, 13.69f, 21.15f, 11.1f, 21.15f)
        lineTo(10f, 22f)
        close()
    }
}.build()

private fun createSimpleVibrationVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(0f, 15f)
        lineTo(2f, 15f)
        lineTo(2f, 9f)
        lineTo(0f, 9f)
        close()
        moveTo(22f, 9f)
        lineTo(22f, 15f)
        lineTo(24f, 15f)
        lineTo(24f, 9f)
        close()
        moveTo(3f, 17f)
        lineTo(5f, 17f)
        lineTo(5f, 7f)
        lineTo(3f, 7f)
        close()
        moveTo(19f, 7f)
        lineTo(19f, 17f)
        lineTo(21f, 17f)
        lineTo(21f, 7f)
        close()
        moveTo(6f, 19f)
        lineTo(18f, 19f)
        curveTo(19.1f, 19f, 20f, 18.1f, 20f, 17f)
        lineTo(20f, 7f)
        curveTo(20f, 5.9f, 19.1f, 5f, 18f, 5f)
        lineTo(6f, 5f)
        curveTo(4.9f, 5f, 4f, 5.9f, 4f, 7f)
        lineTo(4f, 17f)
        curveTo(4f, 18.1f, 4.9f, 19f, 6f, 19f)
        close()
        moveTo(18f, 17f)
        lineTo(6f, 17f)
        lineTo(6f, 7f)
        lineTo(18f, 7f)
        close()
    }
}.build()

private fun createSimpleCalculateVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(19f, 3f)
        lineTo(5f, 3f)
        curveTo(3.9f, 3f, 3f, 3.9f, 3f, 5f)
        lineTo(3f, 19f)
        curveTo(3f, 20.1f, 3.9f, 21f, 5f, 21f)
        lineTo(19f, 21f)
        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
        lineTo(21f, 5f)
        curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
        close()
        moveTo(13f, 7f)
        lineTo(15f, 7f)
        lineTo(15f, 9f)
        lineTo(13f, 9f)
        close()
        moveTo(17f, 17f)
        lineTo(7f, 17f)
        lineTo(7f, 15f)
        lineTo(17f, 15f)
        close()
        moveTo(17f, 13f)
        lineTo(7f, 13f)
        lineTo(7f, 11f)
        lineTo(17f, 11f)
        close()
        moveTo(9f, 9f)
        lineTo(7f, 9f)
        lineTo(7f, 7f)
        lineTo(9f, 7f)
        close()
    }
}.build()

private fun createSimpleCodeVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(9.4f, 16.6f)
        lineTo(4.8f, 12f)
        lineTo(9.4f, 7.4f)
        lineTo(8f, 6f)
        lineTo(2f, 12f)
        lineTo(8f, 18f)
        close()
        moveTo(15f, 6f)
        lineTo(13.6f, 7.4f)
        lineTo(18.2f, 12f)
        lineTo(13.6f, 16.6f)
        lineTo(15f, 18f)
        lineTo(21f, 12f)
        close()
    }
}.build()

private fun createSimpleTextFieldsVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(2.5f, 4f)
        verticalLineTo(7f)
        horizontalLineTo(5.5f)
        verticalLineTo(19f)
        horizontalLineTo(8.5f)
        verticalLineTo(7f)
        horizontalLineTo(11.5f)
        verticalLineTo(4f)
        close()
        moveTo(14.5f, 4f)
        verticalLineTo(11f)
        horizontalLineTo(17.5f)
        verticalLineTo(4f)
        close()
        moveTo(14.5f, 14f)
        verticalLineTo(17f)
        horizontalLineTo(17.5f)
        verticalLineTo(14f)
        close()
        moveTo(20.5f, 4f)
        verticalLineTo(7f)
        horizontalLineTo(23.5f)
        verticalLineTo(4f)
        close()
        moveTo(20.5f, 11f)
        verticalLineTo(19f)
        horizontalLineTo(23.5f)
        verticalLineTo(11f)
        close()
    }
}.build()

private fun createSimpleKeyboardVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(20f, 5f)
        horizontalLineTo(4f)
        curveTo(2.9f, 5f, 2f, 5.9f, 2f, 7f)
        verticalLineTo(17f)
        curveTo(2f, 18.1f, 2.9f, 19f, 4f, 19f)
        horizontalLineTo(20f)
        curveTo(21.1f, 19f, 22f, 18.1f, 22f, 17f)
        verticalLineTo(7f)
        curveTo(22f, 5.9f, 21.1f, 5f, 20f, 5f)
        close()
        moveTo(11f, 17f)
        horizontalLineTo(9f)
        verticalLineTo(15f)
        horizontalLineTo(11f)
        verticalLineTo(17f)
        close()
        moveTo(11f, 13f)
        horizontalLineTo(9f)
        verticalLineTo(11f)
        horizontalLineTo(11f)
        verticalLineTo(13f)
        close()
        moveTo(15f, 17f)
        horizontalLineTo(13f)
        verticalLineTo(15f)
        horizontalLineTo(15f)
        verticalLineTo(17f)
        close()
        moveTo(15f, 13f)
        horizontalLineTo(13f)
        verticalLineTo(11f)
        horizontalLineTo(15f)
        verticalLineTo(13f)
        close()
        moveTo(19f, 17f)
        horizontalLineTo(17f)
        verticalLineTo(11f)
        horizontalLineTo(19f)
        verticalLineTo(17f)
        close()
        moveTo(7f, 17f)
        horizontalLineTo(5f)
        verticalLineTo(15f)
        horizontalLineTo(7f)
        verticalLineTo(17f)
        close()
        moveTo(7f, 13f)
        horizontalLineTo(5f)
        verticalLineTo(11f)
        horizontalLineTo(7f)
        verticalLineTo(13f)
        close()
        moveTo(7f, 9f)
        horizontalLineTo(5f)
        verticalLineTo(7f)
        horizontalLineTo(7f)
        verticalLineTo(9f)
        close()
    }
}.build()

private fun createSimpleSettingsVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(19.14f, 12.94f)
        curveTo(19.19f, 12.64f, 19.22f, 12.33f, 19.22f, 12f)
        curveTo(19.22f, 11.68f, 19.19f, 11.36f, 19.14f, 11.06f)
        lineTo(21.28f, 9.34f)
        curveTo(21.46f, 9.19f, 21.51f, 8.93f, 21.38f, 8.73f)
        lineTo(19.38f, 5.27f)
        curveTo(19.25f, 5.07f, 19f, 4.99f, 18.78f, 5.07f)
        lineTo(16.17f, 6.17f)
        curveTo(15.63f, 5.75f, 15.04f, 5.4f, 14.4f, 5.14f)
        lineTo(14f, 2.3f)
        curveTo(13.97f, 2.07f, 13.76f, 1.9f, 13.52f, 1.9f)
        lineTo(9.48f, 1.9f)
        curveTo(9.24f, 1.9f, 9.03f, 2.07f, 9f, 2.3f)
        lineTo(8.6f, 5.14f)
        curveTo(7.96f, 5.4f, 7.37f, 5.75f, 6.83f, 6.17f)
        lineTo(4.22f, 5.07f)
        curveTo(4f, 4.99f, 3.75f, 5.07f, 3.62f, 5.27f)
        lineTo(1.62f, 8.73f)
        curveTo(1.49f, 8.93f, 1.54f, 9.19f, 1.72f, 9.34f)
        lineTo(3.86f, 11.06f)
        curveTo(3.81f, 11.36f, 3.78f, 11.68f, 3.78f, 12f)
        curveTo(3.78f, 12.33f, 3.81f, 12.64f, 3.86f, 12.94f)
        lineTo(1.72f, 14.66f)
        curveTo(1.54f, 14.81f, 1.49f, 15.07f, 1.62f, 15.27f)
        lineTo(3.62f, 18.73f)
        curveTo(3.75f, 18.93f, 4f, 19.01f, 4.22f, 18.93f)
        lineTo(6.83f, 17.83f)
        curveTo(7.37f, 18.25f, 7.96f, 18.6f, 8.6f, 18.86f)
        lineTo(9f, 21.7f)
        curveTo(9.03f, 21.93f, 9.24f, 22.1f, 9.48f, 22.1f)
        lineTo(13.52f, 22.1f)
        curveTo(13.76f, 22.1f, 13.97f, 21.93f, 14f, 21.7f)
        lineTo(14.4f, 18.86f)
        curveTo(15.04f, 18.6f, 15.63f, 18.25f, 16.17f, 17.83f)
        lineTo(18.78f, 18.93f)
        curveTo(19f, 19.01f, 19.25f, 18.93f, 19.38f, 18.73f)
        lineTo(21.38f, 15.27f)
        curveTo(21.51f, 15.07f, 21.46f, 14.81f, 21.28f, 14.66f)
        lineTo(19.14f, 12.94f)
        close()
        moveTo(11.5f, 15.5f)
        curveTo(9.57f, 15.5f, 8f, 13.93f, 8f, 12f)
        curveTo(8f, 10.07f, 9.57f, 8.5f, 11.5f, 8.5f)
        curveTo(13.43f, 8.5f, 15f, 10.07f, 15f, 12f)
        curveTo(15f, 13.93f, 13.43f, 15.5f, 11.5f, 15.5f)
        close()
    }
}.build()

private fun createSimpleContrastVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        close()
        moveTo(12f, 20f)
        lineTo(12f, 4f)
        curveTo(16.42f, 4f, 20f, 7.58f, 20f, 12f)
        curveTo(20f, 16.42f, 16.42f, 20f, 12f, 20f)
        close()
    }
}.build()

private fun createSimpleFlashVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(7f, 2f)
        verticalLineTo(13f)
        horizontalLineTo(10f)
        verticalLineTo(22f)
        lineTo(17f, 10f)
        horizontalLineTo(13f)
        verticalLineTo(2f)
        close()
    }
}.build()

private fun createSimplePaletteVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 3f)
        curveTo(7.03f, 3f, 3f, 7.03f, 3f, 12f)
        curveTo(3f, 16.97f, 7.03f, 21f, 12f, 21f)
        curveTo(16.97f, 21f, 21f, 16.97f, 21f, 12f)
        curveTo(21f, 7.03f, 16.97f, 3f, 12f, 3f)
        close()
        moveTo(12f, 19f)
        curveTo(8.13f, 19f, 5f, 15.87f, 5f, 12f)
        curveTo(5f, 8.13f, 8.13f, 5f, 12f, 5f)
        curveTo(15.87f, 5f, 19f, 8.13f, 19f, 12f)
        curveTo(19f, 15.87f, 15.87f, 19f, 12f, 19f)
        close()
    }
}.build()

private fun createSimpleHistoryVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(13f, 3f)
        curveTo(8.03f, 3f, 4f, 7.03f, 4f, 12f)
        lineTo(1f, 12f)
        lineTo(4.89f, 15.89f)
        lineTo(4.96f, 16.03f)
        lineTo(9f, 12f)
        lineTo(6f, 12f)
        curveTo(6f, 8.13f, 9.13f, 5f, 13f, 5f)
        curveTo(16.87f, 5f, 20f, 8.13f, 20f, 12f)
        curveTo(20f, 15.87f, 16.87f, 19f, 13f, 19f)
        curveTo(11.07f, 19f, 9.32f, 18.21f, 8.06f, 16.94f)
        lineTo(6.64f, 18.36f)
        curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f)
        curveTo(17.97f, 21f, 22f, 16.97f, 22f, 12f)
        curveTo(22f, 7.03f, 17.97f, 3f, 13f, 3f)
        close()
        moveTo(12f, 8f)
        verticalLineTo(13f)
        lineTo(16.28f, 15.54f)
        lineTo(17f, 14.33f)
        lineTo(13.5f, 12.25f)
        verticalLineTo(8f)
        close()
    }
}.build()

private fun createSimpleShareVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(18f, 16.08f)
        curveTo(17.24f, 16.08f, 16.56f, 16.38f, 16.04f, 16.85f)
        lineTo(8.91f, 12.7f)
        curveTo(8.96f, 12.47f, 9f, 12.24f, 9f, 12f)
        curveTo(9f, 11.76f, 8.96f, 11.53f, 8.91f, 11.3f)
        lineTo(15.96f, 7.19f)
        curveTo(16.5f, 7.69f, 17.21f, 8f, 18f, 8f)
        curveTo(19.66f, 8f, 21f, 6.66f, 21f, 5f)
        curveTo(21f, 3.34f, 19.66f, 2f, 18f, 2f)
        curveTo(16.34f, 2f, 15f, 3.34f, 15f, 5f)
        curveTo(15f, 5.24f, 15.04f, 5.47f, 15.09f, 5.7f)
        lineTo(8.04f, 9.81f)
        curveTo(7.5f, 9.31f, 6.79f, 9f, 6f, 9f)
        curveTo(4.34f, 9f, 3f, 10.34f, 3f, 12f)
        curveTo(3f, 13.66f, 4.34f, 15f, 6f, 15f)
        curveTo(6.79f, 15f, 7.5f, 14.69f, 8.04f, 14.19f)
        lineTo(15.16f, 18.34f)
        curveTo(15.11f, 18.55f, 15.08f, 18.77f, 15.08f, 19f)
        curveTo(15.08f, 20.66f, 16.42f, 22f, 18.08f, 22f)
        curveTo(19.74f, 22f, 21.08f, 20.66f, 21.08f, 19f)
        curveTo(21.08f, 17.34f, 19.74f, 16f, 18.08f, 16f)
        close()
    }
}.build()

private fun createSimpleStarVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 17.27f)
        lineTo(18.18f, 21f)
        lineTo(16.54f, 13.97f)
        lineTo(22f, 9.24f)
        lineTo(14.81f, 8.62f)
        lineTo(12f, 2f)
        lineTo(9.19f, 8.62f)
        lineTo(2f, 9.24f)
        lineTo(7.45f, 13.97f)
        lineTo(5.82f, 21f)
        close()
    }
}.build()

private fun createSimpleCheckVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(9f, 16.17f)
        lineTo(4.83f, 12f)
        lineTo(3.41f, 13.41f)
        lineTo(9f, 19f)
        lineTo(21f, 7f)
        lineTo(19.59f, 5.59f)
        close()
    }
}.build()

private fun createSimplePlayVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(8f, 5f)
        verticalLineTo(19f)
        lineTo(19f, 12f)
        close()
    }
}.build()

private fun createSimpleScheduleVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(11.99f, 2f)
        curveTo(6.47f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.47f, 22f, 11.99f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 11.99f, 2f)
        close()
        moveTo(12f, 20f)
        curveTo(7.58f, 20f, 4f, 16.42f, 4f, 12f)
        curveTo(4f, 7.58f, 7.58f, 4f, 12f, 4f)
        curveTo(16.42f, 4f, 20f, 7.58f, 20f, 12f)
        curveTo(20f, 16.42f, 16.42f, 20f, 12f, 20f)
        close()
        moveTo(12.5f, 7f)
        horizontalLineTo(11f)
        verticalLineTo(13f)
        lineTo(16.25f, 16.15f)
        lineTo(17f, 14.92f)
        lineTo(12.5f, 12.25f)
        verticalLineTo(7f)
        close()
    }
}.build()

private fun createSimpleSpeedVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(20.38f, 8.57f)
        lineTo(17.1f, 7.5f)
        lineTo(16.5f, 6f)
        lineTo(13f, 7.75f)
        lineTo(13.5f, 9.25f)
        lineTo(16.9f, 10.5f)
        lineTo(17.5f, 12f)
        lineTo(21f, 10.25f)
        close()
        moveTo(7f, 10f)
        lineTo(6.5f, 8.5f)
        lineTo(3f, 10.25f)
        lineTo(3.62f, 11.75f)
        lineTo(6.9f, 12.83f)
        lineTo(7.5f, 14.33f)
        lineTo(11f, 12.58f)
        lineTo(10.5f, 11.08f)
        close()
        moveTo(13f, 14f)
        lineTo(12.5f, 12.5f)
        lineTo(9f, 14.25f)
        lineTo(9.62f, 15.75f)
        lineTo(12.9f, 16.83f)
        lineTo(13.5f, 18.33f)
        lineTo(17f, 16.58f)
        lineTo(16.5f, 15.08f)
        close()
    }
}.build()

private fun createSimpleSaveVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(17f, 3f)
        lineTo(5f, 3f)
        curveTo(3.89f, 3f, 3f, 3.9f, 3f, 5f)
        lineTo(3f, 19f)
        curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
        lineTo(19f, 21f)
        curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
        lineTo(21f, 7f)
        lineTo(17f, 3f)
        close()
        moveTo(12f, 19f)
        curveTo(10.34f, 19f, 9f, 17.66f, 9f, 16f)
        curveTo(9f, 14.34f, 10.34f, 13f, 12f, 13f)
        curveTo(13.66f, 13f, 15f, 14.34f, 15f, 16f)
        curveTo(15f, 17.66f, 13.66f, 19f, 12f, 19f)
        close()
        moveTo(15f, 9f)
        lineTo(5f, 9f)
        lineTo(5f, 5f)
        lineTo(15f, 5f)
        lineTo(15f, 9f)
        close()
    }
}.build()

private fun createSimplePriorityVector(): ImageVector = ImageVector.Builder(
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path {
        moveTo(12f, 2f)
        curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
        curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
        curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
        curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
        close()
        moveTo(13f, 17f)
        lineTo(11f, 17f)
        lineTo(11f, 11f)
        lineTo(13f, 11f)
        close()
        moveTo(13f, 9f)
        lineTo(11f, 9f)
        lineTo(11f, 7f)
        lineTo(13f, 7f)
        close()
    }
}.build()
