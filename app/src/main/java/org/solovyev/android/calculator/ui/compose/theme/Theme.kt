/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator.ui.compose.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Blue850,
    onPrimary = CppButtonText,
    primaryContainer = Blue900,
    onPrimaryContainer = Grey100,
    secondary = Teal500,
    onSecondary = CppButtonText,
    secondaryContainer = Teal600,
    onSecondaryContainer = Grey100,
    tertiary = Pink900,
    onTertiary = CppButtonText,
    error = RedA700,
    onError = CppButtonText,
    errorContainer = Red300,
    onErrorContainer = Grey900,
    background = CppBg,
    onBackground = CppText,
    surface = CppEditorBg,
    onSurface = CppText,
    surfaceVariant = Grey850,
    onSurfaceVariant = Grey300,
    outline = Grey600,
    outlineVariant = Grey800,
    scrim = Black,
    inverseSurface = Grey100,
    inverseOnSurface = Grey900,
    inversePrimary = Blue800,
    surfaceDim = Grey950,
    surfaceBright = Grey800,
    surfaceContainerLowest = Grey965,
    surfaceContainerLow = Grey950,
    surfaceContainer = Grey900,
    surfaceContainerHigh = Grey850,
    surfaceContainerHighest = Grey800
)

private val LightColorScheme = lightColorScheme(
    primary = Blue800,
    onPrimary = CppButtonText,
    primaryContainer = Blue900,
    onPrimaryContainer = CppButtonText,
    secondary = Teal500,
    onSecondary = CppButtonText,
    secondaryContainer = Teal400,
    onSecondaryContainer = Grey900,
    tertiary = Pink900,
    onTertiary = CppButtonText,
    error = RedA700,
    onError = CppButtonText,
    errorContainer = Red300,
    onErrorContainer = Grey900,
    background = CppBgLight,
    onBackground = CppTextInverse,
    surface = Grey100,
    onSurface = CppTextInverse,
    surfaceVariant = Grey300,
    onSurfaceVariant = Grey800,
    outline = Grey600,
    outlineVariant = Grey300,
    scrim = Black,
    inverseSurface = Grey900,
    inverseOnSurface = Grey100,
    inversePrimary = Blue850,
    surfaceDim = Grey300,
    surfaceBright = Grey100,
    surfaceContainerLowest = Grey100,
    surfaceContainerLow = Grey100,
    surfaceContainer = Grey100,
    surfaceContainerHigh = Grey300,
    surfaceContainerHighest = Grey300
)

// Material dark theme variant (deep blue)
private val MaterialDarkColorScheme = darkColorScheme(
    primary = DeepBlue850,
    onPrimary = CppButtonText,
    primaryContainer = DeepBlue900,
    onPrimaryContainer = Grey100,
    secondary = Teal500,
    onSecondary = CppButtonText,
    secondaryContainer = Teal600,
    onSecondaryContainer = Grey100,
    tertiary = Pink900,
    onTertiary = CppButtonText,
    error = RedA700,
    onError = CppButtonText,
    errorContainer = Red300,
    onErrorContainer = Grey900,
    background = DeepBlue900,
    onBackground = CppText,
    surface = DeepBlue800,
    onSurface = CppText,
    surfaceVariant = DeepBlue850,
    onSurfaceVariant = Grey300,
    outline = Grey600,
    outlineVariant = Grey800,
    scrim = Black,
    inverseSurface = Grey100,
    inverseOnSurface = DeepBlue900,
    inversePrimary = Blue800
)

enum class CalculatorTheme {
    MATERIAL_DARK,
    MATERIAL_LIGHT,
    METRO_DARK,
    METRO_LIGHT
}

@Composable
fun CalculatorTheme(
    theme: CalculatorTheme = CalculatorTheme.METRO_DARK,
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        theme == CalculatorTheme.MATERIAL_DARK -> MaterialDarkColorScheme
        theme == CalculatorTheme.MATERIAL_LIGHT -> LightColorScheme
        theme == CalculatorTheme.METRO_LIGHT -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CalculatorTypography,
        content = content
    )
}
