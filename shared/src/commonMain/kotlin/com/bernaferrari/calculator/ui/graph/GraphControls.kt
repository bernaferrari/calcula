package com.bernaferrari.calculator.ui.graph

import com.bernaferrari.calculator.ui.motionSpec
import com.bernaferrari.calculator.ui.calculator.LocalReduceMotion
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.key
import androidx.compose.material3.Slider
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import org.jetbrains.compose.resources.stringResource

private val QuickExamples = listOf("x²", "a×sin(b×x)", "x²+y²=9", "x³−x", "ax²+bx+c", "sin(x)÷x", "y²=x", "x²÷9+y²÷4=1")

@Composable
internal fun FunctionList(
    state: GraphState,
    colors: List<Color>,
    formatter: DisplayFormatter,
    onActivate: (Int) -> Unit,
    /** A tap inside a function's text: make it active and put the cursor there (a raw offset). */
    onCursor: (index: Int, position: Int) -> Unit,
    onRemove: (Int) -> Unit,
    onExample: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        state.functions.forEachIndexed { index, function ->
            val active = index == state.active
            val field by animateColorAsState(if (active) scheme.surfaceContainerHigh else Color.Transparent, label = "field")
            val invalid = function.text.isNotBlank() && state.curves[index] == null && !state.pending(index)
            // The row being typed into stays on screen when the list scrolls.
            val inView = remember { BringIntoViewRequester() }
            LaunchedEffect(active, function.text) { if (active) inView.bringIntoView() }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .bringIntoViewRequester(inView)
                    .fillMaxWidth()
                    // Only the row being edited needs to be big; the others are a compact legend.
                    .heightIn(min = if (active) 48.dp else 36.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(field)
                    .clickable { onActivate(index) }
                    .padding(start = 14.dp, end = 4.dp),
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(colors[index % colors.size]))
                Spacer(Modifier.width(12.dp))
                val plain = remember(function.text) { formatter.format(function.text, 0, null) }
                val prefix = if (function.isEquation) 0 else "y = ".length
                // Where the cursor sits in the drawn text, after the `y = ` prefix.
                val caret = prefix + if (function.text.isEmpty()) 0 else plain.rawToDisplay[function.editor.cursor.coerceIn(0, function.text.length)]
                var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
                val currentPlain by rememberUpdatedState(plain)
                // A cursor like any text field's: solid while you type or move it, then blinking.
                val blink = remember { Animatable(1f) }
                LaunchedEffect(active, function.text, caret) {
                    blink.snapTo(1f)
                    while (active) {
                        delay(550)
                        blink.animateTo(0f, tween(150))
                        delay(350)
                        blink.animateTo(1f, tween(150))
                    }
                }
                val cursorColor = scheme.primary
                Text(
                    buildAnnotatedString {
                        if (!function.isEquation) withStyle(SpanStyle(color = scheme.onSurfaceVariant)) { append("y = ") }
                        if (function.text.isEmpty()) {
                            withStyle(SpanStyle(color = scheme.outline, fontSize = 16.sp)) { append(if (active) stringResource(Res.string.graph_type_hint) else "") }
                        } else {
                            append(formatter.format(function.text, 0, DisplayFormatter.Colors(scheme.primary, scheme.onSurfaceVariant, scheme.outline)).text)
                        }
                    },
                    style = TextStyle(fontFamily = GoogleSansFlex, fontSize = if (active) 22.sp else 18.sp),
                    color = if (invalid) scheme.error else scheme.onSurface,
                    maxLines = 1,
                    onTextLayout = { layout = it },
                    modifier = Modifier
                        .weight(1f)
                        // Tap in the text to put the cursor there, like the calculator's own display.
                        .pointerInput(index) {
                            detectTapGestures { position ->
                                val l = layout
                                val map = currentPlain.displayToRaw
                                if (l == null || map.isEmpty() || function.text.isEmpty()) {
                                    onActivate(index)
                                    return@detectTapGestures
                                }
                                val offset = l.getOffsetForPosition(position) - prefix
                                onCursor(index, map[offset.coerceIn(0, map.lastIndex)])
                            }
                        }
                        .drawWithContent {
                            drawContent()
                            val l = layout ?: return@drawWithContent
                            if (!active) return@drawWithContent
                            val rect = l.getCursorRect(caret.coerceIn(0, l.layoutInput.text.length))
                            val w = 2.dp.toPx()
                            drawRect(
                                cursorColor.copy(alpha = blink.value),
                                topLeft = Offset(rect.left - w / 2, rect.top + rect.height * 0.12f),
                                size = Size(w, rect.height * 0.76f),
                            )
                        },
                )
                if (state.functions.size > 1 || function.text.isNotEmpty()) {
                    // One size for every row, so the ✕ stays put as rows become active.
                    IconButton(onClick = { onRemove(index) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.Close, contentDescription = stringResource(Res.string.graph_remove), tint = scheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
        val active = state.activeFunction
        if (active.text.isEmpty()) {
            // Nothing typed yet: a few shapes worth seeing, one tap each.
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
                items(QuickExamples) { example ->
                    AssistChip(onClick = { onExample(example) }, label = { Text(formatter.format(example, 0, null).text.text) })
                }
            }
        }
    }
}

/**
 * A slider for each parameter the functions use: its letter (tap to type it again), its value,
 * and a play button that sweeps it back and forth so you can watch what it does.
 */
@Composable
internal fun ParameterSliders(
    state: GraphState,
    formatter: DisplayFormatter,
    onInsert: (Char) -> Unit,
    onChange: (Char, Double, Boolean) -> Unit,
    onTick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp).animateContentSize(motionSpec(androidx.compose.animation.core.spring()))) {
        state.used.forEach { name ->
            key(name) {
                val p = state.parameter(name)
                val currentP by rememberUpdatedState(p)
                var playing by remember { mutableStateOf(false) }
                LaunchedEffect(playing) {
                    if (!playing) return@LaunchedEffect
                    // Across the whole range in four seconds, bouncing off the ends.
                    var direction = 1.0
                    var last = withFrameNanos { it }
                    while (true) {
                        val now = withFrameNanos { it }
                        val q = currentP
                        var next = q.value + direction * (q.max - q.min) / 4.0 * ((now - last) / 1e9)
                        if (next >= q.max) { next = q.max; direction = -1.0; onTick() }
                        if (next <= q.min) { next = q.min; direction = 1.0; onTick() }
                        onChange(name, next, false)
                        last = now
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().height(44.dp)) {
                    Surface(
                        onClick = { onInsert(name) },
                        shape = CircleShape,
                        color = scheme.primaryContainer,
                        contentColor = scheme.onPrimaryContainer,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(name.toString(), style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 19.sp, fontWeight = FontWeight.Medium))
                        }
                    }
                    val decimals = max(0, -floor(log10(GraphViewModel.stepFor(p))).toInt())
                    Text(
                        // 2 rather than 2.0: whole values read whole.
                        "= " + formatter.number(label(p.value, decimals).let { if ('.' in it) it.trimEnd('0').trimEnd('.') else it }),
                        style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 18.sp),
                        color = scheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 10.dp).widthIn(min = 64.dp),
                    )
                    Slider(
                        value = p.value.toFloat(),
                        onValueChange = { playing = false; onChange(name, it.toDouble(), true) },
                        valueRange = p.min.toFloat()..p.max.toFloat(),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                    )
                    IconButton(onClick = { onTick(); playing = !playing }) {
                        Icon(
                            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(if (playing) Res.string.graph_pause else Res.string.graph_play),
                            tint = scheme.primary,
                        )
                    }
                }
            }
        }
    }
}
