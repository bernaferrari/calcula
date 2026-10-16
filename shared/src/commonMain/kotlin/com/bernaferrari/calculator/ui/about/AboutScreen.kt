@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.calculator.ui.about

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.calculator.APP_VERSION
import com.bernaferrari.calculator.resources.*
import com.bernaferrari.calculator.ui.calculator.rememberHaptics
import com.bernaferrari.calculator.ui.containerTransform
import com.bernaferrari.calculator.ui.fadeBottomEdge
import com.bernaferrari.calculator.ui.theme.GoogleSansFlex
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private const val SOURCE_URL = "https://github.com/bernaferrari/android-calculatorpp"
private const val FEEDBACK_EMAIL = "bernaferrari2@gmail.com"

/**
 * About, in the Expressive style: the ++ mark on a slowly turning shape you can poke, the two
 * things people come here for (the code and a way to reach the author) as bold cards, the story
 * in a few lines, the author's other places, and a plain privacy promise.
 */
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val uri = LocalUriHandler.current
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val open: (String) -> Unit = { runCatching { uri.openUri(it) } }

    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.containerTransform("about", isScreen = true).fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp).height(56.dp)) {
                IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null) }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                LazyColumn(
                    Modifier.widthIn(max = 600.dp).fillMaxWidth().clipToBounds().fadeBottomEdge(bottomInset + 12.dp),
                    contentPadding = PaddingValues(bottom = bottomInset + 32.dp),
                ) {
                    item(key = "hero") { Hero() }
                    item(key = "actions") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(start = 16.dp, end = 16.dp, top = 32.dp),
                        ) {
                            ActionCard(
                                title = stringResource(Res.string.about_source),
                                body = stringResource(Res.string.about_source_body),
                                icon = Icons.Rounded.Code,
                                container = MaterialTheme.colorScheme.primaryContainer,
                                content = MaterialTheme.colorScheme.onPrimaryContainer,
                                badge = MaterialShapes.Cookie9Sided.toShape(),
                                shape = RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp, topEnd = 12.dp, bottomEnd = 12.dp),
                                onClick = { open(SOURCE_URL) },
                                modifier = Modifier.weight(1f),
                            )
                            ActionCard(
                                title = stringResource(Res.string.about_feedback),
                                body = stringResource(Res.string.about_feedback_body),
                                icon = Icons.Rounded.MailOutline,
                                container = MaterialTheme.colorScheme.secondaryContainer,
                                content = MaterialTheme.colorScheme.onSecondaryContainer,
                                badge = MaterialShapes.Clover4Leaf.toShape(),
                                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 32.dp, bottomEnd = 32.dp),
                                onClick = { open("mailto:$FEEDBACK_EMAIL?subject=Calculator%2B%2B%20$APP_VERSION") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    item(key = "story") { Story(Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)) }
                    item(key = "elsewhere") {
                        Text(
                            stringResource(Res.string.about_elsewhere),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 32.dp, top = 28.dp, bottom = 8.dp),
                        )
                        Elsewhere(open)
                    }
                    item(key = "privacy") { Footer() }
                }
            }
        }
    }
}

/** The one hero moment: ++ on a slowly turning cookie. Tap it and it spins up and settles. */
@Composable
private fun Hero() {
    val haptics = rememberHaptics(enabled = { true })
    val scope = rememberCoroutineScope()
    val drift by rememberInfiniteTransition(label = "hero").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(48_000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift",
    )
    val kick = remember { Animatable(0f) }
    val squash = remember { Animatable(1f) }
    val shape = MaterialShapes.Cookie12Sided.toShape()
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 8.dp)
                .size(176.dp)
                .graphicsLayer { scaleX = squash.value; scaleY = squash.value }
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button) {
                    haptics.confirm()
                    scope.launch {
                        squash.snapTo(0.9f)
                        squash.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium))
                    }
                    scope.launch { kick.animateTo(kick.value + 180f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessVeryLow)) }
                },
        ) {
            Box(Modifier.fillMaxSize().graphicsLayer { rotationZ = drift + kick.value }.clip(shape).background(MaterialTheme.colorScheme.primaryContainer))
            Text(
                "++",
                fontFamily = GoogleSansFlex,
                fontWeight = FontWeight.Bold,
                fontSize = 64.sp,
                letterSpacing = (-2).sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(
            stringResource(Res.string.about_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.padding(top = 16.dp)) {
            Text(
                stringResource(Res.string.settings_version, APP_VERSION),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

/** A bold, colored card for a primary action, its icon on an Expressive shape. */
@Composable
private fun ActionCard(
    title: String,
    body: String,
    icon: ImageVector,
    container: Color,
    content: Color,
    badge: Shape,
    shape: Shape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(onClick = onClick, shape = shape, color = container, contentColor = content, modifier = modifier.fillMaxHeight().heightIn(min = 152.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(48.dp).clip(badge).background(content), contentAlignment = Alignment.Center) {
                    Icon(icon, null, Modifier.size(22.dp), tint = container)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, null, Modifier.size(18.dp), tint = content.copy(alpha = 0.7f))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
            Text(body, style = MaterialTheme.typography.bodySmall, color = content.copy(alpha = 0.8f), maxLines = 2)
        }
    }
}

/** Where the app comes from, set like a quote. */
@Composable
private fun Story(modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp, 12.dp, 32.dp, 32.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.FormatQuote, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(Res.string.about_story), style = MaterialTheme.typography.bodyLarge)
            Text(
                "— " + stringResource(Res.string.about_story_author),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/** The author's other places as one connected group: round outer ends, tight inner joins. */
@Composable
private fun Elsewhere(open: (String) -> Unit) {
    val links: List<Triple<String, String, DrawableResource>> = listOf(
        Triple("GitHub", "https://github.com/bernaferrari", Res.drawable.github_logo),
        Triple("X", "https://x.com/bernaferrari", Res.drawable.x_logo),
        Triple("Reddit", "https://www.reddit.com/user/bernaferrari", Res.drawable.reddit_logo),
        Triple("LinkedIn", "https://www.linkedin.com/in/bernaferrari", Res.drawable.linkedin_logo),
    )
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        links.forEachIndexed { index, (label, url, icon) ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 28.dp, bottomStart = 28.dp, topEnd = 8.dp, bottomEnd = 8.dp)
                links.lastIndex -> RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp, topEnd = 28.dp, bottomEnd = 28.dp)
                else -> RoundedCornerShape(8.dp)
            }
            Surface(
                onClick = { open(url) },
                shape = shape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f).heightIn(min = 56.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(painterResource(icon), contentDescription = label, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/** A plain privacy promise and credit to the original author. */
@Composable
private fun Footer() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp, start = 32.dp, end = 32.dp),
    ) {
        Icon(Icons.Outlined.VerifiedUser, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            stringResource(Res.string.about_privacy),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(Res.string.about_credit),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
