package com.bernaferrari.calculator.ui.calculator

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import androidx.compose.runtime.withFrameNanos

/**
 * A shader ripple effect that creates a distortion ripple when triggered.
 * Only works on Android 13+ (API 33+)
 */
@Composable
fun ShaderRippleEffect(
    modifier: Modifier = Modifier,
    rippleOrigin: Offset?,
    onRippleComplete: () -> Unit = {},
    amplitude: Float = 8f,
    frequency: Float = 12f,
    decay: Float = 6f,
    speed: Float = 1500f,
    animationDuration: Float = 1f,
    content: @Composable () -> Unit
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        // Fallback for older devices
        Box(modifier) { content() }
        return
    }
    
    var elapsedTime by remember { mutableFloatStateOf(0f) }
    var isAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(rippleOrigin) {
        if (rippleOrigin != null) {
            isAnimating = true
            elapsedTime = 0f
            val startTime = withFrameNanos { it }
            do {
                val now = withFrameNanos { it }
                elapsedTime = (now - startTime) / 1_000_000_000f
                if (elapsedTime >= animationDuration) {
                    isAnimating = false
                    onRippleComplete()
                    break
                }
                awaitFrame()
            } while (isAnimating)
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && isAnimating && rippleOrigin != null) {
                    Modifier.graphicsLayer {
                        renderEffect = createRippleRenderEffect(
                            screenWidth = screenWidth,
                            screenHeight = screenHeight,
                            origin = rippleOrigin,
                            elapsedTime = elapsedTime,
                            amplitude = amplitude,
                            frequency = frequency,
                            decay = decay,
                            speed = speed
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        content()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun createRippleRenderEffect(
    screenWidth: Float,
    screenHeight: Float,
    origin: Offset,
    elapsedTime: Float,
    amplitude: Float,
    frequency: Float,
    decay: Float,
    speed: Float
): androidx.compose.ui.graphics.RenderEffect {
    val shaderCode = """
        uniform shader inputShader;
        uniform float2 uResolution;
        uniform float2 uOrigin;
        uniform float uTime;
        uniform float uAmplitude;
        uniform float uFrequency;
        uniform float uDecay;
        uniform float uSpeed;
        
        half4 main(float2 fragCoord) {
            float2 pos = fragCoord;
            float distance = length(pos - uOrigin);
            float delay = distance / uSpeed;
            float time = max(0.0, uTime - delay);
            float rippleAmount = uAmplitude * sin(uFrequency * time) * exp(-uDecay * time);
            float2 n = normalize(pos - uOrigin);
            float2 newPos = pos + rippleAmount * n;
            return inputShader.eval(newPos);
        }
    """.trimIndent()

    val runtimeShader = RuntimeShader(shaderCode)
    runtimeShader.setFloatUniform("uResolution", floatArrayOf(screenWidth, screenHeight))
    runtimeShader.setFloatUniform("uOrigin", floatArrayOf(origin.x, origin.y))
    runtimeShader.setFloatUniform("uTime", elapsedTime)
    runtimeShader.setFloatUniform("uAmplitude", amplitude)
    runtimeShader.setFloatUniform("uFrequency", frequency)
    runtimeShader.setFloatUniform("uDecay", decay)
    runtimeShader.setFloatUniform("uSpeed", speed)

    return RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputShader").asComposeRenderEffect()
}