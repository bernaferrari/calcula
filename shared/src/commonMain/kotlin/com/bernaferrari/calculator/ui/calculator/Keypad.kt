package com.bernaferrari.calculator.ui.calculator

import com.bernaferrari.calculator.resources.*
import org.jetbrains.compose.resources.stringResource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.ui.graphics.vector.ImageVector
import com.bernaferrari.calculator.domain.model.FlickHints
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlinx.coroutines.delay

private val DigitStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 34.sp, fontWeight = FontWeight.Normal)
private val OperatorStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 38.sp, fontWeight = FontWeight.Normal)
private val FunctionStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 26.sp, fontWeight = FontWeight.Medium)
private val LayerStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 24.sp, fontWeight = FontWeight.Medium)
private val LayerInverseStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 12.sp, fontWeight = FontWeight.Medium)
private val ScientificStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 19.sp, fontWeight = FontWeight.Medium)

private val KeyGap = 6.dp
private val MemoryStyle @Composable get() = TextStyle(fontFamily = GoogleSansFlex, fontSize = 17.sp, fontWeight = FontWeight.Medium)

internal enum class Tone { Digit, Function, Operator, Equals }

/**
 * A secondary function: what the key shows, and the key it types. [named] spells a flick-down
 * function out below the key in Labels mode; the rest (mostly inverses) get a dot there.
 */
internal data class Alt(
    val label: String,
    val key: Key,
    val description: String = label,
    val named: Boolean = false,
    /** Typed as is instead of [key]: a constant's value, a function of the person's own. */
    val insert: String? = null,
) {
    /** Text never has nothing to act on; a function key might (1/x with nothing typed). */
    fun isInert(inert: Set<Key>) = insert == null && key in inert
}

internal data class Spec(
    val key: Key,
    val label: String,
    val tone: Tone,
    val description: String = label,
    /** Flick up; also what the key becomes on the ƒ layer when [flips] is set. */
    val up: Alt? = null,
    /** Flick down; on the ƒ layer it is shown small under the function. */
    val down: Alt? = null,
    val flips: Boolean = up != null,
    /** Drawn instead of [label]. */
    val icon: ImageVector? = null,
)

/** The memory keys' row, when it's turned on. */
enum class MemoryOp { CLEAR, ADD, SUBTRACT, RECALL }

private val MemoryRowHeight = 40.dp

/** How much taller the keypad gets with the memory row. */
val MemoryRowExtra = MemoryRowHeight + KeyGap

/** The keypad's keys and their flicks, as shipped; [withFlicks] lays the person's choices over them. */
internal fun keypadRows(
    decimal: Char,
    rpn: Boolean = false,
    variable: Boolean = false,
    nextParameter: Char? = null,
    undo: String = "undo",
    redo: String = "redo",
): List<List<Spec>> =
    listOf(
        listOf(
            Spec(Key.CLEAR, "C", Tone.Function, "Clear"),
            if (variable) Spec(
                Key.VARIABLE, "x", Tone.Operator, "x",
                up = Alt("y", Key.VARIABLE_Y),
                down = nextParameter?.let { Alt(it.toString(), parameterKey(it), "parameter $it") },
                flips = false,
            ) else Spec(Key.PERCENT, "%", Tone.Function, "Percent"),
            // Backspace up top, by the operators and away from C, as on the old keypad.
            // Flick up to undo, down to redo; hold to clear.
        Spec(Key.DELETE, "⌫", Tone.Function, "Backspace, hold to clear", up = Alt(undo, Key.UNDO), down = Alt(redo, Key.REDO, named = true), flips = false),
            // 1/x is "1 ÷ x", and sits under × (xʸ) like its counterpart.
            Spec(Key.DIVIDE, "÷", Tone.Operator, "Divide", up = Alt("1/x", Key.RECIPROCAL, "reciprocal")),
        ),
        listOf(
            Spec(Key.D7, "7", Tone.Digit, up = Alt("sin", Key.SIN), down = Alt("sin⁻¹", Key.ASIN, "arcsine")),
            Spec(Key.D8, "8", Tone.Digit, up = Alt("cos", Key.COS), down = Alt("cos⁻¹", Key.ACOS, "arccosine")),
            Spec(Key.D9, "9", Tone.Digit, up = Alt("tan", Key.TAN), down = Alt("tan⁻¹", Key.ATAN, "arctangent")),
            Spec(Key.TIMES, "×", Tone.Operator, "Multiply", up = Alt("xʸ", Key.POWER, "power"), down = Alt("ʸ√x", Key.ROOT, "root")),
        ),
        listOf(
            Spec(Key.D4, "4", Tone.Digit, up = Alt("ln", Key.LN, "natural log"), down = Alt("eˣ", Key.EXP, "e to the power")),
            Spec(Key.D5, "5", Tone.Digit, up = Alt("log", Key.LOG, "log base 10"), down = Alt("10ˣ", Key.POW10, "10 to the power")),
            // The constants end the row.
            Spec(Key.D6, "6", Tone.Digit, up = Alt("π", Key.PI, "pi"), down = Alt("e", Key.E, "Euler's number", named = true)),
            Spec(Key.MINUS, "−", Tone.Operator, "Minus", down = if (rpn) Alt("±", Key.NEGATE, "change sign", named = true) else null, flips = false),
        ),
        listOf(
            // Each number carries its own power and root: 1 over x, x², √ on 2, x³, ∛ on 3.
            Spec(Key.D1, "1", Tone.Digit, up = Alt("x!", Key.FACTORIAL, "factorial"), down = Alt("|x|", Key.ABS, "absolute value")),
            Spec(Key.D2, "2", Tone.Digit, up = Alt("x²", Key.SQUARE, "squared"), down = Alt("√", Key.SQRT, "square root", named = true)),
            Spec(Key.D3, "3", Tone.Digit, up = Alt("x³", Key.CUBE, "cubed"), down = Alt("∛", Key.CBRT, "cube root")),
            Spec(Key.PLUS, "+", Tone.Operator, "Plus"),
        ),
        listOf(
            // 0 under 2 and the point under 3, like a phone's dial pad; parentheses take the corner.
            if (rpn) Spec(Key.PARENS, "x⇄y", Tone.Function, "Swap x and y", up = Alt("R↓", Key.LPAREN, "roll down"), down = Alt("R↑", Key.RPAREN, "roll up", named = true), flips = false)
            else Spec(Key.PARENS, "( )", Tone.Function, "Parentheses", up = Alt("(", Key.LPAREN, "open parenthesis"), down = Alt(")", Key.RPAREN, "close parenthesis", named = true), flips = false),
            Spec(Key.D0, "0", Tone.Digit, up = Alt("00", Key.DOUBLE_ZERO), down = Alt("000", Key.TRIPLE_ZERO), flips = false),
            Spec(Key.DOT, decimal.toString(), Tone.Digit, "Decimal point"),
            if (rpn) Spec(Key.EQUALS, "↵", Tone.Equals, "Enter", icon = Icons.AutoMirrored.Rounded.KeyboardReturn)
            else Spec(Key.EQUALS, "=", Tone.Equals, "Equals"),
        ),
    )

/** The flicks the person may reassign: every key's except C, ⌫, ( ) and =. */
internal val Spec.customizable: Boolean get() = key !in setOf(Key.CLEAR, Key.DELETE, Key.PARENS, Key.EQUALS)

/** "D7.up" and "D7.down": how a flick slot is named in settings. */
internal fun Spec.slot(up: Boolean) = "${key.name}.${if (up) "up" else "down"}"

/** A flick the person chose: a built-in function, or text to insert (a constant's value, `tip(`). Null removes the flick. */
data class CustomFlick(val label: String, val key: Key? = null, val insert: String? = null)

internal fun CustomFlick.toAlt() = Alt(label, key ?: Key.EQUALS, label, named = true, insert = insert)

internal fun List<List<Spec>>.withFlicks(flicks: Map<String, CustomFlick?>): List<List<Spec>> =
    if (flicks.isEmpty()) this else map { row ->
        row.map { spec ->
            if (!spec.customizable) return@map spec
            val upSlot = spec.slot(true)
            val downSlot = spec.slot(false)
            val up = if (upSlot in flicks) flicks[upSlot]?.toAlt() else spec.up
            val down = if (downSlot in flicks) flicks[downSlot]?.toAlt() else spec.down
            // Digits flip to their up function on the ƒ layer; that holds for a chosen one too.
            spec.copy(up = up, down = down, flips = up != null && (spec.flips || spec.tone == Tone.Digit && spec.key != Key.D0))
        }
    }

/**
 * The 4×5 keypad. Every key shows one label. The ƒ layer flips the digits into their functions
 * (7 8 9 → trigonometry, 4 5 6 → logs and roots, 1 2 3 → constants and powers); the same functions
 * are always one flick away, up for the function and down for its inverse.
 */
@Composable
fun MainKeypad(
    decimal: Char,
    haptics: Haptics,
    functionLayer: Boolean,
    onKey: (Key) -> Unit,
    onFunctionKey: (Key) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    hints: FlickHints = FlickHints.DOTS,
    bottomInset: Dp = 0.dp,
    /** For the graph: the `%` key becomes the variable `x` (flick up for `y`), in the accent so it's easy to find. */
    variable: Boolean = false,
    /** For the graph: the slider parameter a flick down on `x` types, or null when none is left. */
    nextParameter: Char? = null,
    /** Functions that would do nothing right now (`1/x` with nothing typed): shown dimmed, and inert. */
    inert: Set<Key> = emptySet(),
    /** Reverse Polish: `=` is ↵, `( )` swaps x and y (flicks roll the stack), `−` flicks down to ±. */
    rpn: Boolean = false,
    /** Shows MC M+ M− MR above the keys; [memoryFull] lights up the ones that need something stored. */
    onMemory: ((MemoryOp) -> Unit)? = null,
    memoryFull: Boolean = false,
    /** Whether there's a number on screen for M+ and M− to add or take away. */
    canStore: Boolean = true,
    /** Flicks the person reassigned, by slot ("D7.up"); a null value takes that flick away. */
    flicks: Map<String, CustomFlick?> = emptyMap(),
    /** Where a chosen flick that types text sends it. */
    onInsertText: (String) -> Unit = {},
) {
    val rows = keypadRows(decimal, rpn, variable, nextParameter, stringResource(Res.string.key_undo), stringResource(Res.string.key_redo)).withFlicks(flicks)
    val reduceMotion = LocalReduceMotion.current
    BoxWithConstraints(modifier) {
    // Labels follow the keys' size, so a keypad dragged short or tall keeps its proportions.
    val memoryRow = if (onMemory != null) MemoryRowExtra else 0.dp
    val rowHeight = (maxHeight - KeyGap * 7 - bottomInset - memoryRow) / 5
    val density = LocalDensity.current
    // Gentler than the keys themselves: text follows about 60% of the change.
    val scale = (0.4f + 0.6f * (rowHeight / 82.dp)).coerceIn(0.68f, 1.12f)
    CompositionLocalProvider(LocalDensity provides Density(density.density, density.fontScale * scale)) {
    Column(
        verticalArrangement = Arrangement.spacedBy(KeyGap),
        modifier = Modifier.fillMaxSize().padding(start = KeyGap * 2, end = KeyGap * 2, top = KeyGap, bottom = KeyGap * 2 + bottomInset),
    ) {
        if (onMemory != null) MemoryRow(memoryFull, canStore, haptics, onMemory, Modifier.fillMaxWidth().height(MemoryRowHeight))
        rows.forEachIndexed { r, row ->
            var pressedIndex by remember { mutableIntStateOf(-1) }
            Row(horizontalArrangement = Arrangement.spacedBy(KeyGap), modifier = Modifier.weight(1f).fillMaxWidth()) {
                row.forEachIndexed { c, spec ->
                    // The pressed key widens and its row-mates make room, then everything springs back.
                    val weight by animateFloatAsState(
                        when {
                            reduceMotion -> 1f
                            pressedIndex == -1 -> 1f
                            pressedIndex == c -> 1.1f
                            else -> 0.967f
                        },
                        spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                        label = "squeeze",
                    )
                    KeypadKey(
                        spec = spec,
                        haptics = haptics,
                        functionLayer = functionLayer,
                        // A wave from the top-left corner when the layer flips.
                        flipDelay = r * 35L + c * 25L,
                        // Names need a roomy key; shorter ones (the graph's) fall back to dots, and
                        // the shortest show nothing. The flicks always work.
                        hints = when {
                            hints == FlickHints.LABELS && rowHeight >= 60.dp -> FlickHints.LABELS
                            hints != FlickHints.NONE && rowHeight >= 44.dp -> FlickHints.DOTS
                            else -> FlickHints.NONE
                        },
                        onKey = onKey,
                        onFunctionKey = onFunctionKey,
                        onClearAll = onClearAll,
                        onPressedChange = { down -> pressedIndex = if (down) c else if (pressedIndex == c) -1 else pressedIndex },
                        modifier = Modifier.weight(weight).fillMaxHeight(),
                        inert = inert,
                        onInsertText = onInsertText,
                    )
                }
            }
        }
    }
    }
    }
}

/** MC M+ M− MR: slim keys, quieter than the digits; the ones that need memory dim while it's empty. */
@Composable
private fun MemoryRow(full: Boolean, canStore: Boolean, haptics: Haptics, onMemory: (MemoryOp) -> Unit, modifier: Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(KeyGap), modifier = modifier) {
        listOf(MemoryOp.CLEAR to "MC", MemoryOp.ADD to "M+", MemoryOp.SUBTRACT to "M−", MemoryOp.RECALL to "MR").forEach { (op, label) ->
            // MC and MR need something stored; M+ and M− need a number on screen.
            val enabled = if (op == MemoryOp.CLEAR || op == MemoryOp.RECALL) full else canStore
            val alpha by animateFloatAsState(if (enabled) 1f else 0.38f, label = "memory")
            Surface(
                onClick = { haptics.tap(); onMemory(op) },
                enabled = enabled,
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, style = MemoryStyle, modifier = Modifier.graphicsLayer { this.alpha = alpha })
                }
            }
        }
    }
}

@Composable
private fun Spec.containerColor(onLayer: Boolean): Color = with(MaterialTheme.colorScheme) {
    when {
        onLayer -> primaryContainer
        // Emphasis by tone: digits neutral, the top row a step up, the operators in the family's
        // secondary tint, and `=` the one solid color. C stands apart by its ink alone.
        tone == Tone.Digit -> surfaceContainerHigh
        tone == Tone.Equals -> primary
        tone == Tone.Operator -> secondaryContainer
        else -> surfaceContainerHighest
    }
}

@Composable
private fun Spec.contentColor(onLayer: Boolean): Color = with(MaterialTheme.colorScheme) {
    when {
        onLayer -> onPrimaryContainer
        tone == Tone.Digit -> onSurface
        tone == Tone.Function -> if (key == Key.CLEAR) tertiary else onSurfaceVariant
        tone == Tone.Operator -> onSecondaryContainer
        else -> onPrimary
    }
}

@Composable
private fun KeypadKey(
    spec: Spec,
    haptics: Haptics,
    functionLayer: Boolean,
    flipDelay: Long,
    hints: FlickHints,
    onKey: (Key) -> Unit,
    onFunctionKey: (Key) -> Unit,
    onClearAll: () -> Unit,
    onPressedChange: (Boolean) -> Unit,
    modifier: Modifier,
    inert: Set<Key> = emptySet(),
    onInsertText: (String) -> Unit = {},
) {
    val fire: (Alt) -> Unit = { alt -> if (alt.insert != null) onInsertText(alt.insert) else onFunctionKey(alt.key) }
    val wantsLayer = functionLayer && spec.flips && spec.up != null
    // The face flips over its horizontal axis: out to 90°, swap the label, then spring back in.
    val rotation = remember { Animatable(0f) }
    var onLayer by remember { mutableStateOf(wantsLayer) }
    val reduceMotion = LocalReduceMotion.current
    LaunchedEffect(wantsLayer) {
        if (onLayer == wantsLayer) return@LaunchedEffect
        if (reduceMotion) {
            onLayer = wantsLayer
            return@LaunchedEffect
        }
        delay(flipDelay)
        rotation.animateTo(90f, tween(110, easing = FastOutLinearInEasing))
        onLayer = wantsLayer
        rotation.snapTo(-90f)
        rotation.animateTo(0f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium))
    }

    val container by animateColorAsState(spec.containerColor(onLayer), label = "container")
    val content = spec.contentColor(onLayer)
    val isDelete = spec.key == Key.DELETE
    KeyCell(
        container = container,
        content = content,
        description = if (onLayer) spec.up!!.description else spec.description,
        haptics = haptics,
        // `=` answers with its own confirm/reject haptic once the result is known (↵ just pushes).
        pressHaptic = spec.key != Key.EQUALS || spec.icon != null,
        onClick = {
            when {
                // A dimmed function does nothing, and doesn't use up a one-shot ƒ either.
                onLayer && spec.up!!.isInert(inert) -> Unit
                onLayer -> fire(spec.up!!)
                else -> onKey(spec.key)
            }
        },
        up = spec.up?.let { Hint(it.label, it.description, enabled = !it.isInert(inert)) { fire(it) } },
        down = spec.down?.let { Hint(it.label, it.description, enabled = !it.isInert(inert), named = it.named) { fire(it) } },
        // On the ƒ layer the face already names both functions.
        showDots = hints == FlickHints.DOTS && !onLayer,
        labelFlicks = hints == FlickHints.LABELS && !onLayer,
        onLongClick = if (isDelete) ({ haptics.heavy(); onClearAll() }) else null,
        onPressedChange = onPressedChange,
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                rotationX = rotation.value
                cameraDistance = 16f * density
            },
        ) {
            when {
                onLayer -> BoxWithConstraints(contentAlignment = Alignment.Center) {
                    // Short keys (the graph's) have no room for the second line; its flick still works.
                    val roomy = maxHeight >= 48.dp
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val upAlpha by animateFloatAsState(if (spec.up!!.isInert(inert)) 0.35f else 1f, label = "upAlpha")
                        val downAlpha by animateFloatAsState(if (spec.down?.isInert(inert) == true) 0.25f else 0.6f, label = "downAlpha")
                        Text(spec.up!!.label, style = LayerStyle, color = content.copy(alpha = upAlpha))
                        if (roomy) spec.down?.let { Text(it.label, style = LayerInverseStyle, color = content.copy(alpha = downAlpha)) }
                    }
                }
                isDelete -> Icon(Icons.AutoMirrored.Outlined.Backspace, contentDescription = null, tint = content, modifier = Modifier.size(28.dp))
                spec.icon != null -> Icon(spec.icon, contentDescription = null, tint = content, modifier = Modifier.size(32.dp))
                else -> Text(
                    spec.label,
                    // A lowercase x is only x-height tall, so centred as text it sits low beside
                    // digits and operators; lifted, its middle lines up with theirs.
                    modifier = if (spec.key == Key.VARIABLE) Modifier.offset(y = (-4).dp) else Modifier,
                    style = when (spec.tone) {
                        Tone.Digit -> DigitStyle
                        Tone.Function -> FunctionStyle
                        Tone.Operator, Tone.Equals -> OperatorStyle
                    },
                    color = content,
                )
            }
        }
    }
}

private data class SciSpec(val key: Key, val label: String, val inverseLabel: String = label, val description: String = label)

/** Landscape has room for every function as its own key, beside the main keypad. */
@Composable
fun ScientificGrid(
    inverse: Boolean,
    angleUnit: AngleUnit,
    haptics: Haptics,
    onKey: (Key) -> Unit,
    onToggleAngle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf(SciSpec(Key.INVERSE, "INV", description = "Inverse functions"), SciSpec(Key.SQRT, "√", "∛"), SciSpec(Key.PI, "π")),
        listOf(SciSpec(Key.SIN, "sin", "sin⁻¹"), SciSpec(Key.COS, "cos", "cos⁻¹"), SciSpec(Key.TAN, "tan", "tan⁻¹")),
        listOf(SciSpec(Key.LN, "ln", "eˣ"), SciSpec(Key.LOG, "log", "10ˣ"), SciSpec(Key.E, "e")),
        listOf(SciSpec(Key.POWER, "xʸ", "x⁻¹"), SciSpec(Key.SQUARE, "x²", "x³"), SciSpec(Key.FACTORIAL, "x!")),
        listOf(SciSpec(Key.LPAREN, "("), SciSpec(Key.RPAREN, ")")),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(KeyGap),
        modifier = modifier.padding(start = KeyGap * 2, top = KeyGap, bottom = KeyGap * 2),
    ) {
        rows.forEachIndexed { index, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(KeyGap), modifier = Modifier.weight(1f).fillMaxWidth()) {
                row.forEach { ScientificKey(it, inverse, haptics, onKey, Modifier.weight(1f).fillMaxHeight()) }
                if (index == rows.lastIndex) {
                    KeyCell(
                        container = MaterialTheme.colorScheme.surfaceContainer,
                        content = MaterialTheme.colorScheme.primary,
                        description = "Angle unit",
                        haptics = haptics,
                        onClick = onToggleAngle,
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                    ) {
                        Text(if (angleUnit == AngleUnit.DEGREES) "DEG" else "RAD", style = ScientificStyle, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScientificKey(spec: SciSpec, inverse: Boolean, haptics: Haptics, onKey: (Key) -> Unit, modifier: Modifier) {
    val isInverseKey = spec.key == Key.INVERSE
    val active = isInverseKey && inverse
    val container by animateColorAsState(
        if (active) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceContainer,
        label = "inverse",
    )
    val content = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val label = if (inverse) spec.inverseLabel else spec.label
    KeyCell(
        container = container,
        content = content,
        description = if (isInverseKey) (if (inverse) "Inverse functions on" else "Inverse functions off") else label,
        haptics = haptics,
        onClick = { onKey(spec.key) },
        modifier = modifier,
    ) {
        AnimatedContent(targetState = label, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "sci-label") {
            Text(it, style = ScientificStyle, color = content)
        }
    }
}

private fun parameterKey(name: Char): Key = when (name) {
    'a' -> Key.PARAM_A
    'b' -> Key.PARAM_B
    'c' -> Key.PARAM_C
    else -> Key.PARAM_D
}
