package com.bernaferrari.calculator.ui.usage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bernaferrari.calculator.domain.input.Key
import com.bernaferrari.calculator.domain.model.Usage
import com.bernaferrari.calculator.platform.encodePng
import com.bernaferrari.calculator.platform.formatDate
import com.bernaferrari.calculator.platform.rememberImageSharer
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.DisplayFormatter
import com.bernaferrari.calculator.ui.calculator.FlickCatalog
import com.bernaferrari.calculator.ui.calculator.keypadRows
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.calculator.separators
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.settings.SettingsViewModel
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

/**
 * "Your Calcula": how you use the calculator, as a card worth sharing. The keypad glows where
 * your thumb goes most; around it, the counts that make it yours. Counting stays on the device;
 * nothing leaves it unless you share the card.
 */
@Composable
fun UsageScreen(onNavigateBack: () -> Unit) {
    val settings: SettingsViewModel = viewModel { SettingsViewModel() }
    val prefs by settings.prefs.collectAsStateWithLifecycle()
    val haptics = rememberHaptics(enabled = { prefs.hapticFeedback }, sounds = { prefs.soundEnabled })
    val formatter = remember(prefs.decimalSeparator) { DisplayFormatter(prefs.decimalSeparator.separators()) }
    val share = rememberImageSharer()
    val scope = rememberCoroutineScope()
    val card = rememberGraphicsLayer()
    val usage = prefs.usage

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("settings", isScreen = true).fillMaxSize()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp).fillMaxWidth()) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
                Text(stringResource(Res.string.menu_usage), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Box(
                    Modifier
                        .widthIn(max = 440.dp)
                        .fillMaxWidth()
                        // What's drawn here is also kept, so sharing sends exactly this card.
                        .drawWithContent {
                            card.record { this@drawWithContent.drawContent() }
                            drawLayer(card)
                        },
                ) {
                    UsageCard(usage, formatter, prefs.decimalSeparator.separators().decimal)
                }
                if (usage.calculations == 0 && usage.keys.isEmpty()) {
                    Text(
                        stringResource(Res.string.usage_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp),
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        haptics.confirm()
                        scope.launch { share(card.toImageBitmap().encodePng(), "my-calcula.png") }
                    },
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(Res.string.usage_share))
                }
                Text(
                    stringResource(Res.string.usage_private),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

/** The card itself: brand, the big count, a keypad heatmap and three small facts. */
@Composable
private fun UsageCard(usage: Usage, formatter: DisplayFormatter, decimal: Char) {
    val colors = MaterialTheme.colorScheme
    val rows = remember(decimal) { keypadRows(decimal) }
    // Every key's label, flick functions included, to name the favourites.
    val labels = remember(rows) {
        buildMap {
            FlickCatalog.builtIn.forEach { (_, flick) -> flick.key?.let { put(it.name, flick.label) } }
            rows.flatten().forEach { spec -> put(spec.key.name, spec.label) }
        }
    }
    val max = (usage.keys.values.maxOrNull() ?: 0).coerceAtLeast(1)
    val digits = setOf(Key.D0, Key.D1, Key.D2, Key.D3, Key.D4, Key.D5, Key.D6, Key.D7, Key.D8, Key.D9).map { it.name }.toSet()
    val basics = setOf(Key.PLUS, Key.MINUS, Key.TIMES, Key.DIVIDE, Key.EQUALS, Key.CLEAR, Key.DELETE, Key.DOT, Key.PARENS, Key.LPAREN, Key.RPAREN, Key.DOUBLE_ZERO, Key.TRIPLE_ZERO, Key.NEGATE, Key.PERCENT).map { it.name }.toSet()
    val topKey = usage.keys.filterKeys { it in labels }.maxByOrNull { it.value }
    val favourite = usage.keys.filterKeys { it !in digits && it !in basics && it in labels }.maxByOrNull { it.value }
    val number = { n: Int -> formatter.number(n.toString()) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.tertiaryContainer)))
            .padding(22.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("C", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 18.sp, fontWeight = FontWeight.Bold), color = colors.onPrimary, textAlign = TextAlign.Center,
                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(colors.primary).padding(top = 1.dp))
            Spacer(Modifier.width(8.dp))
            Text("Calcula", style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 18.sp, fontWeight = FontWeight.Medium), color = colors.onPrimaryContainer)
            Spacer(Modifier.weight(1f))
            if (usage.since > 0) {
                Text(stringResource(Res.string.usage_since, formatDate(usage.since)), style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(number(usage.calculations), style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 64.sp, lineHeight = 64.sp), color = colors.onPrimaryContainer)
        Text(pluralStringResource(Res.plurals.usage_calculations, usage.calculations), style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer.copy(alpha = 0.8f))
        Spacer(Modifier.height(18.dp))
        // The keypad, each key lit by how often it's pressed (taps and flicks to its functions).
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    row.forEach { spec ->
                        val presses = (usage.keys[spec.key.name] ?: 0) + listOfNotNull(spec.up, spec.down).sumOf { usage.keys[it.key.name] ?: 0 }
                        val heat = kotlin.math.sqrt(presses.toFloat() / max).coerceIn(0f, 1f)
                        val fill = lerp(colors.surface.copy(alpha = 0.55f), colors.primary, heat)
                        val ink = if (fill.luminance() > 0.3f) Color(0xFF1B1B21) else Color.White
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.45f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(fill)
                                .then(if (spec.key.name == topKey?.key) Modifier.border(2.dp, colors.onPrimaryContainer, RoundedCornerShape(12.dp)) else Modifier),
                        ) {
                            Text(spec.label, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 17.sp), color = ink)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Fact(number(usage.flicks), pluralStringResource(Res.plurals.usage_flicks, usage.flicks))
            Fact(number(usage.days), pluralStringResource(Res.plurals.usage_days, usage.days))
            Fact(favourite?.let { labels[it.key] } ?: "—", stringResource(Res.string.usage_favorite))
        }
        Spacer(Modifier.height(16.dp))
        // The line that makes a shared card an invitation.
        Text(
            stringResource(Res.string.usage_tagline),
            style = MaterialTheme.typography.labelLarge,
            color = colors.onPrimaryContainer.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Fact(value: String, label: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(value, style = TextStyle(fontFamily = GoogleSansFlex, fontSize = 24.sp, fontWeight = FontWeight.Medium), color = colors.onSurface, maxLines = 1)
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, maxLines = 1)
    }
}
