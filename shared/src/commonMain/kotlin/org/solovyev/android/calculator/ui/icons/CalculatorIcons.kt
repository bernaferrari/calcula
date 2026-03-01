package org.solovyev.android.calculator.ui.icons

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * KMP-compatible Material Icons wrapper.
 * Uses expect/actual to provide icons across platforms.
 */
object CalculatorIcons {
    // Navigation & Actions
    val Back: IconType get() = getBackIcon()
    val Close: IconType get() = getCloseIcon()
    val MoreVert: IconType get() = getMoreVertIcon()
    
    // Content
    val Add: IconType get() = getAddIcon()
    val Clear: IconType get() = getClearIcon()
    val Delete: IconType get() = getDeleteIcon()
    val Edit: IconType get() = getEditIcon()
    val ContentCopy: IconType get() = getContentCopyIcon()
    
    // Communication
    val LocationOn: IconType get() = getLocationOnIcon()
    
    // Device
    val BrightnessAuto: IconType get() = getBrightnessAutoIcon()
    val BrightnessHigh: IconType get() = getBrightnessHighIcon()
    val BrightnessLow: IconType get() = getBrightnessLowIcon()
    val Fullscreen: IconType get() = getFullscreenIcon()
    val ScreenRotation: IconType get() = getScreenRotationIcon()
    val Vibration: IconType get() = getVibrationIcon()
    
    // Editor
    val Calculate: IconType get() = getCalculateIcon()
    val Code: IconType get() = getCodeIcon()
    val TextFields: IconType get() = getTextFieldsIcon()
    
    // Hardware
    val Keyboard: IconType get() = getKeyboardIcon()
    
    // Home
    val Settings: IconType get() = getSettingsIcon()
    
    // Image
    val Contrast: IconType get() = getContrastIcon()
    val FlashOn: IconType get() = getFlashOnIcon()
    val Palette: IconType get() = getPaletteIcon()
    
    // Navigation
    val ArrowBack: IconType get() = getArrowBackIcon()
    val ArrowForward: IconType get() = getArrowForwardIcon()
    
    // Places
    val History: IconType get() = getHistoryIcon()
    
    // Social
    val Share: IconType get() = getShareIcon()
    
    // Toggle
    val Star: IconType get() = getStarIcon()
    val Check: IconType get() = getCheckIcon()
    
    // AV
    val PlayArrow: IconType get() = getPlayArrowIcon()
    val Schedule: IconType get() = getScheduleIcon()
    val Speed: IconType get() = getSpeedIcon()
    
    // File
    val Save: IconType get() = getSaveIcon()
    
    // Notification
    val PriorityHigh: IconType get() = getPriorityHighIcon()
}

/**
 * Sealed class representing an icon that can be either a vector or a painter
 */
sealed class IconType {
    abstract val contentDescription: String?
}

data class VectorIcon(
    val imageVector: ImageVector,
    override val contentDescription: String? = null
) : IconType()

data class PainterIcon(
    val painter: Painter,
    override val contentDescription: String? = null
) : IconType()

// expect declarations for all icons
internal expect fun getBackIcon(): IconType
internal expect fun getCloseIcon(): IconType
internal expect fun getMoreVertIcon(): IconType
internal expect fun getAddIcon(): IconType
internal expect fun getClearIcon(): IconType
internal expect fun getDeleteIcon(): IconType
internal expect fun getEditIcon(): IconType
internal expect fun getContentCopyIcon(): IconType
internal expect fun getLocationOnIcon(): IconType
internal expect fun getBrightnessAutoIcon(): IconType
internal expect fun getBrightnessHighIcon(): IconType
internal expect fun getBrightnessLowIcon(): IconType
internal expect fun getFullscreenIcon(): IconType
internal expect fun getScreenRotationIcon(): IconType
internal expect fun getVibrationIcon(): IconType
internal expect fun getCalculateIcon(): IconType
internal expect fun getCodeIcon(): IconType
internal expect fun getTextFieldsIcon(): IconType
internal expect fun getKeyboardIcon(): IconType
internal expect fun getSettingsIcon(): IconType
internal expect fun getContrastIcon(): IconType
internal expect fun getFlashOnIcon(): IconType
internal expect fun getPaletteIcon(): IconType
internal expect fun getArrowBackIcon(): IconType
internal expect fun getArrowForwardIcon(): IconType
internal expect fun getHistoryIcon(): IconType
internal expect fun getShareIcon(): IconType
internal expect fun getStarIcon(): IconType
internal expect fun getCheckIcon(): IconType
internal expect fun getPlayArrowIcon(): IconType
internal expect fun getScheduleIcon(): IconType
internal expect fun getSpeedIcon(): IconType
internal expect fun getSaveIcon(): IconType
internal expect fun getPriorityHighIcon(): IconType
