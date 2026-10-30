@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.calculator.ui.library

import androidx.compose.ui.unit.em
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.input.ImeAction
import com.bernaferrari.calculator.domain.input.CalcState
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Editor
import com.bernaferrari.calculator.domain.input.ExpressionEditor
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.ui.calculator.MainKeypad
import kotlinx.coroutines.delay
import com.bernaferrari.calculator.domain.model.AngleUnit
import com.bernaferrari.calculator.domain.model.FlickHints
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.domain.library.CONSTANTS
import com.bernaferrari.calculator.domain.library.Constant
import com.bernaferrari.calculator.domain.library.ConstantGroup
import com.bernaferrari.calculator.domain.math.CalcEngine
import com.bernaferrari.calculator.domain.math.NumberFormatter
import com.bernaferrari.calculator.domain.math.UserFunction
import com.bernaferrari.calculator.domain.math.UserFunctions
import com.bernaferrari.calculator.domain.model.Memory
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.Haptics
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class LibraryTab { MORE, CONSTANTS, FUNCTIONS, MEMORY }

/**
 * Everything worth keeping at hand, in one sheet: constants of science, functions you wrote,
 * values you kept. Tap one and it lands at the cursor.
 */
@Composable
fun LibrarySheet(
    initialTab: LibraryTab,
    memories: List<Memory>,
    functions: List<UserFunction>,
    formatter: DisplayFormatter,
    haptics: Haptics,
    onInsert: (String) -> Unit,
    onRecallMemory: (Int) -> Unit,
    onStoreMemory: () -> Unit,
    /** Whether there's a number on screen worth keeping. */
    canStore: Boolean = true,
    onDeleteMemory: (Int) -> Unit,
    onRenameMemory: (Int, String) -> Unit,
    onSaveFunction: (UserFunction) -> Unit,
    onDeleteFunction: (String) -> Unit,
    onDismiss: () -> Unit,
    hints: FlickHints = FlickHints.DOTS,
    /** The tabs this screen offers: the graph has no memory, for one. */
    tabs: List<LibraryTab> = LibraryTab.entries,
) {
    var tab by remember { mutableStateOf(initialTab) }
    val take: (String) -> Unit = { haptics.tap(); onInsert(it); onDismiss() }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(0.86f)) {
            Tabs(tab, tabs) { haptics.tap(); tab = it }
            AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab", modifier = Modifier.weight(1f)) { current ->
                when (current) {
                    LibraryTab.MORE -> MoreTab(take)
                    LibraryTab.CONSTANTS -> ConstantsTab(formatter, take)
                    LibraryTab.FUNCTIONS -> FunctionsTab(functions, formatter, haptics, onPick = { take("${it.name}(") }, onSave = onSaveFunction, onDelete = onDeleteFunction, hints = hints)
                    LibraryTab.MEMORY -> MemoryTab(
                        memories, formatter,
                        onRecall = { haptics.tap(); onRecallMemory(it); onDismiss() },
                        onStore = { haptics.confirm(); onStoreMemory() },
                        canStore = canStore,
                        onDelete = onDeleteMemory,
                        onRename = onRenameMemory,
                    )
                }
            }
        }
    }
}

@Composable
private fun Tabs(selected: LibraryTab, tabs: List<LibraryTab>, onSelect: (LibraryTab) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        tabs.forEachIndexed { index, tab ->
            ToggleButton(
                checked = tab == selected,
                onCheckedChange = { if (it) onSelect(tab) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    tabs.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier.weight(1f).height(44.dp),
            ) {
                Text(
                    stringResource(
                        when (tab) {
                            LibraryTab.MORE -> Res.string.library_more
                            LibraryTab.CONSTANTS -> Res.string.library_constants
                            LibraryTab.FUNCTIONS -> Res.string.library_functions
                            LibraryTab.MEMORY -> Res.string.library_memory
                        },
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ConstantsTab(formatter: DisplayFormatter, onPick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val names = CONSTANTS.associate { it.id to stringResource(it.name()) }
    val shown = CONSTANTS.filter {
        query.isBlank() || names.getValue(it.id).contains(query.trim(), ignoreCase = true) || it.symbol.contains(query.trim(), ignoreCase = true)
    }
    Column {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text(stringResource(Res.string.library_search)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            ConstantGroup.entries.forEach { group ->
                val items = shown.filter { it.group == group }
                if (items.isEmpty()) return@forEach
                item(key = group) { GroupHeader(stringResource(group.title())) }
                items(items, key = { it.id }) { c ->
                    ListItem(
                        headlineContent = { Text(names.getValue(c.id)) },
                        supportingContent = {
                            Text(
                                formatter.number(NumberFormatter.format(CalcEngine.evaluate(c.value), 10)) + if (c.unit.isEmpty()) "" else "  ${c.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        leadingContent = { Symbol(c.symbol) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { onPick(c.value) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FunctionsTab(
    functions: List<UserFunction>,
    formatter: DisplayFormatter,
    haptics: Haptics,
    onPick: (UserFunction) -> Unit,
    onSave: (UserFunction) -> Unit,
    onDelete: (String) -> Unit,
    hints: FlickHints,
) {
    var editing by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        if (editing) {
            FunctionEditor(
                functions, formatter, haptics, hints,
                onSave = { haptics.confirm(); onSave(it); editing = false },
                onCancel = { editing = false },
                modifier = Modifier.fillMaxSize(),
            )
            return@Column
        } else {
            FilledTonalButton(onClick = { haptics.tap(); editing = true }, modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.function_new))
            }
        }
        if (functions.isEmpty() && !editing) {
            Text(
                stringResource(Res.string.functions_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(functions, key = { it.name }) { f ->
                ListItem(
                    headlineContent = {
                        Text(
                            "${f.name}(x) = " + formatter.format(f.body, 0, null).text.text,
                            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp),
                        )
                    },
                    trailingContent = {
                        IconButton(onClick = { haptics.tap(); onDelete(f.name) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.library_delete))
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onPick(f) },
                )
            }
        }
    }
}

/**
 * A name and a body in x, with a live try-out (`tip(10) = 1.8`) so a typo shows before saving.
 * The name is letters, so it gets the system keyboard; the body is math, so it gets ours, with
 * the `%` key turned into x and every function a flick away, just like the calculator.
 */
@Composable
private fun FunctionEditor(
    functions: List<UserFunction>,
    formatter: DisplayFormatter,
    haptics: Haptics,
    hints: FlickHints,
    onSave: (UserFunction) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("") }
    var body by remember { mutableStateOf(Editor()) }
    var naming by remember { mutableStateOf(true) }
    val focus = LocalFocusManager.current
    val nameFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { nameFocus.requestFocus() }
    val text = body.text
    val validName = UserFunctions.isValidName(name, taken = functions.map { it.name })
    val shownName = name.ifEmpty { "f" }
    val draft = UserFunction(shownName, text)
    val trial = if (text.isEmpty()) null else runCatching {
        CalcEngine.evaluateOrNull(UserFunctions.expand("$shownName(10)", functions + draft))
    }.getOrNull()
    val validBody = text.isNotBlank() && runCatching { com.bernaferrari.calculator.domain.math.Plot.compile(UserFunctions.expand("$shownName(x)", functions + draft)) != null }.getOrDefault(false)
    val scheme = MaterialTheme.colorScheme
    val editBody = {
        naming = false
        focus.clearFocus()
    }
    Column(modifier) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = scheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.lowercase().filter { c -> c in 'a'..'z' }.take(8) },
                        singleLine = true,
                        label = { Text(stringResource(Res.string.function_name)) },
                        isError = name.isNotEmpty() && !validName,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            keyboardType = KeyboardType.Ascii,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = { editBody() }),
                        modifier = Modifier
                            .weight(0.42f)
                            .focusRequester(nameFocus)
                            .onFocusChanged { if (it.isFocused) naming = true },
                    )
                    Text("(x) =", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp), modifier = Modifier.padding(horizontal = 8.dp))
                    BodyField(body, formatter, active = !naming, onTap = { raw ->
                        editBody()
                        if (raw != null) body = ExpressionEditor.moveCursor(body, raw)
                    }, modifier = Modifier.weight(0.58f))
                }
                Text(
                    when {
                        name.isNotEmpty() && !validName -> stringResource(Res.string.function_name_rule)
                        trial != null -> "$shownName(10) = " + formatter.number(NumberFormatter.format(trial))
                        else -> " "
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (name.isNotEmpty() && !validName) scheme.error else scheme.onSurfaceVariant,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text(stringResource(Res.string.function_cancel)) }
                    TextButton(onClick = { onSave(UserFunction(name, text)) }, enabled = validName && validBody) {
                        Text(stringResource(Res.string.function_save))
                    }
                }
            }
        }
        // While the name has the system keyboard, ours steps aside.
        AnimatedVisibility(!naming, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically(), modifier = Modifier.weight(1f)) {
            val press: (Key) -> Unit = { key ->
                when (key) {
                    Key.CLEAR -> body = Editor()
                    // `=` finishes the function, like Save.
                    Key.EQUALS -> if (validName && validBody) onSave(UserFunction(name, text)) else haptics.reject()
                    else -> body = Calculator.press(CalcState(editor = body, angleUnit = AngleUnit.RADIANS), key).first.editor
                }
            }
            MainKeypad(
                decimal = formatter.decimal,
                haptics = haptics,
                functionLayer = false,
                onKey = press,
                onFunctionKey = press,
                onClearAll = { body = Editor() },
                hints = hints,
                variable = true,
                inert = setOf(Key.UNDO, Key.REDO, Key.VARIABLE_Y),
                onInsertText = { body = ExpressionEditor.insertRaw(body, it) },
                bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                modifier = Modifier.fillMaxSize().padding(top = 12.dp),
            )
        }
    }
}

/** The body as the calculator shows math, with a cursor, in a box that reads like a text field. */
@Composable
private fun BodyField(editor: Editor, formatter: DisplayFormatter, active: Boolean, onTap: (Int?) -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val plain = remember(editor.text) { formatter.format(editor.text, 0, null) }
    val caret = if (editor.text.isEmpty()) 0 else plain.rawToDisplay[editor.cursor.coerceIn(0, editor.text.length)]
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val currentPlain by rememberUpdatedState(plain)
    val blink = remember { Animatable(1f) }
    LaunchedEffect(active, editor.text, caret) {
        blink.snapTo(1f)
        while (active) {
            delay(550)
            blink.animateTo(0f, tween(150))
            delay(350)
            blink.animateTo(1f, tween(150))
        }
    }
    val border by animateColorAsState(if (active) scheme.primary else scheme.outline, label = "border")
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .padding(top = 8.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(if (active) 2.dp else 1.dp, border, RoundedCornerShape(4.dp))
            .pointerInput(Unit) {
                detectTapGestures { position ->
                    val l = layout
                    val map = currentPlain.displayToRaw
                    if (l == null || map.isEmpty()) return@detectTapGestures onTap(null)
                    // The text starts 16dp in; taps past its end land at the end.
                    val offset = l.getOffsetForPosition(position.copy(x = position.x - 16.dp.toPx()))
                    onTap(map[offset.coerceIn(0, map.lastIndex)])
                }
            }
            .padding(horizontal = 16.dp),
    ) {
        Text(
            if (editor.text.isEmpty()) AnnotatedString("x×0.18")
            else formatter.format(editor.text, 0, DisplayFormatter.Colors(scheme.primary, scheme.onSurfaceVariant, scheme.outline)).text,
            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 20.sp),
            color = if (editor.text.isEmpty()) scheme.outline else scheme.onSurface,
            maxLines = 1,
            onTextLayout = { layout = it },
            modifier = Modifier.drawWithContent {
                drawContent()
                val l = layout ?: return@drawWithContent
                if (!active) return@drawWithContent
                val rect = l.getCursorRect(caret.coerceIn(0, l.layoutInput.text.length))
                val w = 2.dp.toPx()
                drawRect(
                    scheme.primary.copy(alpha = blink.value),
                    topLeft = Offset(rect.left - w / 2, rect.top + rect.height * 0.12f),
                    size = Size(w, rect.height * 0.76f),
                )
            },
        )
    }
}

@Composable
private fun MemoryTab(
    memories: List<Memory>,
    formatter: DisplayFormatter,
    onRecall: (Int) -> Unit,
    onStore: () -> Unit,
    canStore: Boolean,
    onDelete: (Int) -> Unit,
    onRename: (Int, String) -> Unit,
) {
    var renaming by remember { mutableStateOf<Int?>(null) }
    Column(Modifier.padding(top = 8.dp)) {
        FilledTonalButton(onClick = onStore, enabled = canStore, modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.memory_store))
        }
        if (memories.isEmpty()) {
            Text(
                stringResource(Res.string.memory_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(24.dp),
            )
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            itemsIndexed(memories) { i, m ->
                ListItem(
                    overlineContent = { Text(m.label ?: "M${i + 1}") },
                    headlineContent = { Text(formatter.number(m.value), style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 24.sp)) },
                    leadingContent = { Symbol("M${i + 1}".takeIf { m.label == null } ?: m.label!!.take(1).uppercase()) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { renaming = i }) { Icon(Icons.Outlined.Edit, contentDescription = stringResource(Res.string.library_rename)) }
                            IconButton(onClick = { onDelete(i) }) { Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.library_delete)) }
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { onRecall(i) },
                )
            }
        }
    }
    renaming?.let { index ->
        var label by remember(index) { mutableStateOf(memories.getOrNull(index)?.label.orEmpty()) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text(stringResource(Res.string.library_rename)) },
            text = { OutlinedTextField(value = label, onValueChange = { label = it.take(24) }, singleLine = true, label = { Text(stringResource(Res.string.memory_name)) }) },
            confirmButton = { TextButton(onClick = { onRename(index, label); renaming = null }) { Text(stringResource(Res.string.function_save)) } },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text(stringResource(Res.string.function_cancel)) } },
        )
    }
}

@Composable
private fun GroupHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp),
    )
}

/** The symbol in a soft circle, like an avatar: what you'd see on paper. */
@Composable
private fun Symbol(text: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            symbolText(text),
            style = TextStyle(fontFamily = GoogleSansFlex, fontSize = if (text.replace("_", "").length > 2) 13.sp else 18.sp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
        )
    }
}

/** `m_e` as m with a small, lowered e. */
fun symbolText(symbol: String): AnnotatedString = buildAnnotatedString {
    val base = symbol.substringBefore('_')
    append(base)
    if ('_' in symbol) withStyle(SpanStyle(fontSize = 0.7.em, baselineShift = BaselineShift.Subscript)) { append(symbol.substringAfter('_')) }
}

private fun ConstantGroup.title(): StringResource = when (this) {
    ConstantGroup.MATH -> Res.string.constants_math
    ConstantGroup.PHYSICS -> Res.string.constants_physics
    ConstantGroup.CHEMISTRY -> Res.string.constants_chemistry
    ConstantGroup.ASTRONOMY -> Res.string.constants_astronomy
}

fun Constant.name(): StringResource = when (id) {
    "pi" -> Res.string.constant_pi
    "e" -> Res.string.constant_e
    "phi" -> Res.string.constant_phi
    "sqrt2" -> Res.string.constant_sqrt2
    "gamma" -> Res.string.constant_gamma
    "c" -> Res.string.constant_c
    "g" -> Res.string.constant_g
    "gconst" -> Res.string.constant_gconst
    "h" -> Res.string.constant_h
    "hbar" -> Res.string.constant_hbar
    "qe" -> Res.string.constant_qe
    "me" -> Res.string.constant_me
    "mp" -> Res.string.constant_mp
    "kb" -> Res.string.constant_kb
    "eps0" -> Res.string.constant_eps0
    "mu0" -> Res.string.constant_mu0
    "sigma" -> Res.string.constant_sigma
    "na" -> Res.string.constant_na
    "r" -> Res.string.constant_r
    "f" -> Res.string.constant_f
    "u" -> Res.string.constant_u
    "atm" -> Res.string.constant_atm
    "au" -> Res.string.constant_au
    "ly" -> Res.string.constant_ly
    "pc" -> Res.string.constant_pc
    "msun" -> Res.string.constant_msun
    "mearth" -> Res.string.constant_mearth
    else -> Res.string.constant_rearth
}
