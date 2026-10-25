package com.bernaferrari.calculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

private val TopBarHeight = 56.dp

/** Where scrolling content starts, so it begins below the bar and can then scroll up under it. */
@Composable
fun topBarInset(): Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + TopBarHeight

/** A screen's title row and the status bar above it, pinned over the content that scrolls beneath. */
@Composable
fun BoxScope.TopBar(content: @Composable RowScope.() -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(1f)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(horizontal = 4.dp)
            .height(TopBarHeight),
        content = content,
    )
}
