package com.bernaferrari.calculator.ui.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.calculator.R
import com.bernaferrari.calculator.domain.input.Calculator
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.model.AngleUnit

@Composable
fun CalculatorScreen(
    initialExpression: String,
    onOpenSettings: () -> Unit,
    viewModel: CalculatorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(initialExpression) { viewModel.loadExpression(initialExpression) }

    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DisplayFormatter(locale) }
    val haptics = rememberHaptics { state.haptics }
    val context = LocalContext.current

    var layer by rememberSaveable { mutableStateOf(FunctionLayer.Off) }
    var historyOpen by rememberSaveable { mutableStateOf(false) }
    // Back closes history mode before leaving the app.
    BackHandler(enabled = historyOpen) { historyOpen = false }

    val snackbar = remember { SnackbarHostState() }
    val deletedOne = stringResource(R.string.history_deleted)
    val deletedAll = stringResource(R.string.history_cleared)
    val undo = stringResource(R.string.undo)
    LaunchedEffect(Unit) {
        viewModel.removed.collect { entries ->
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(if (entries.size == 1) deletedOne else deletedAll, actionLabel = undo, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.onRestore(entries)
        }
    }

    val copy: (String) -> Unit = { raw ->
        val text = if (Calculator.isPlainNumber(raw)) raw.replace('.', formatter.decimal).replace('−', '-') else raw
        context.copyToClipboard(text)
    }

    val display: @Composable (Modifier, Boolean) -> Unit = { modifier, compact ->
        CalculatorDisplay(
            state = state,
            formatter = formatter,
            haptics = haptics,
            events = viewModel.events,
            onCursorMoved = viewModel::onCursorMoved,
            onNudgeCursor = viewModel::onNudgeCursor,
            onToggleAngle = viewModel::onToggleAngleUnit,
            onCopy = copy,
            // Reusing a result means going back to calculating.
            onUseResult = { viewModel.onUseResult(it); historyOpen = false },
            onEditExpression = { viewModel.onEditExpression(it); historyOpen = false },
            onDeleteEntry = viewModel::onDeleteEntry,
            onClearHistory = viewModel::onClearHistory,
            historyOpen = historyOpen,
            entryMaxFontSize = if (compact) 52.sp else 72.sp,
            modifier = modifier,
        )
    }
    val topBar: @Composable (Boolean) -> Unit = { SettingsButton(onOpenSettings) }

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        val landscape = maxWidth > maxHeight && maxHeight < 600.dp
        if (landscape) {
            Row(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.displayCutout).only(WindowInsetsSides.Horizontal)),
            ) {
                Box(Modifier.weight(0.42f).fillMaxHeight().padding(bottom = bottomInset)) {
                    display(Modifier.fillMaxSize().statusBarsPadding(), true)
                    Box(Modifier.statusBarsPadding()) { topBar(false) }
                }
                Column(Modifier.weight(0.3f).fillMaxHeight()) {
                    Spacer(Modifier.fillMaxWidth().statusBarsPadding())
                    ScientificGrid(
                        inverse = state.inverse,
                        angleUnit = state.angleUnit,
                        haptics = haptics,
                        onKey = viewModel::onKey,
                        onToggleAngle = viewModel::onToggleAngleUnit,
                        modifier = Modifier.weight(1f).fillMaxWidth().padding(bottom = bottomInset),
                    )
                }
                MainKeypad(
                    decimal = formatter.decimal,
                    haptics = haptics,
                    functionLayer = false,
                    onKey = viewModel::onKey,
                    onFunctionKey = viewModel::onSwipeKey,
                    onClearAll = { viewModel.onKey(Key.CLEAR) },
                    showDots = false,
                    bottomInset = bottomInset,
                    modifier = Modifier.weight(0.4f).fillMaxHeight().statusBarsPadding(),
                )
            }
        } else {
            // Rows grow with the screen but never get cramped or comically tall.
            val rowHeight = (maxHeight * 0.47f / 5).coerceIn(60.dp, 92.dp)
            // History mode slides the keypad away and gives the whole screen to the tape.
            val keypadHeight by animateDpAsState(
                if (historyOpen) 0.dp else rowHeight * 5 + bottomInset,
                spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow),
                label = "keypad",
            )
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    display(Modifier.fillMaxSize().statusBarsPadding(), false)
                    SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
                }
                ToolRow(
                    historyOpen = historyOpen,
                    layer = layer,
                    onToggleHistory = { haptics.tap(); historyOpen = !historyOpen },
                    onLayer = { next -> haptics.tap(); layer = next; historyOpen = false },
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.padding(bottom = if (historyOpen) bottomInset else 0.dp),
                )
                MainKeypad(
                    decimal = formatter.decimal,
                    haptics = haptics,
                    functionLayer = layer != FunctionLayer.Off,
                    onKey = viewModel::onKey,
                    onFunctionKey = { key ->
                        viewModel.onSwipeKey(key)
                        // Like Shift: one function, then back to digits (unless locked).
                        if (layer == FunctionLayer.Once) layer = FunctionLayer.Off
                    },
                    onClearAll = { viewModel.onKey(Key.CLEAR) },
                    bottomInset = bottomInset,
                    modifier = Modifier.fillMaxWidth().height(keypadHeight).clipToBounds(),
                )
            }
        }
    }
}

@Composable
private fun SettingsButton(onOpenSettings: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(4.dp), contentAlignment = Alignment.TopEnd) {
        IconButton(onClick = onOpenSettings) {
            Icon(
                Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.menu_settings),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

private fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("Calculator++", text))
    // Android 13+ shows its own confirmation.
    if (Build.VERSION.SDK_INT < 33) Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
}
