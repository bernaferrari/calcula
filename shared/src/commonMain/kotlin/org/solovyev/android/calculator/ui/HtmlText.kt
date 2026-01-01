package org.solovyev.android.calculator.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

@Composable
fun rememberHtml(text: String): AnnotatedString {
    return remember(text) { parseHtml(text) }
}

fun parseHtml(text: String): AnnotatedString {
    // Regex for tags
    val tagRegex = "</?([a-zA-Z]+)(.*?)>".toRegex()
    
    return buildAnnotatedString {
        var cursor = 0
        val activeStyles = mutableListOf<SpanStyle>()
        
        tagRegex.findAll(text).forEach { matchResult ->
            // Append text before the tag
            if (matchResult.range.first > cursor) {
                append(text.substring(cursor, matchResult.range.first))
            }
            
            val tagContent = matchResult.groupValues[0] // Full tag
            val tagName = matchResult.groupValues[1].lowercase()
            val isClosing = tagContent.startsWith("</")
            
            if (tagName == "br") {
                append("\n")
            } else if (!isClosing) {
                // Opening tag
                val style = when (tagName) {
                    "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                    "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
                    else -> null
                }
                if (style != null) {
                    pushStyle(style)
                    activeStyles.add(style)
                }
            } else {
                // Closing tag
                // Pop simple style (stack-based assumption)
                // Note: This assumes well-formed HTML.
                // If we encounter </b> we pop the last pushed style if it matches?
                // Compose buildAnnotatedString uses pushStyle/pop.
                // We should track what we pushed.
                // For simple usage, just pop.
                 if (tagName == "b" || tagName == "strong" || tagName == "i" || tagName == "em") {
                     try {
                         pop()
                     } catch (e: Exception) {
                         // Ignore stack underflow
                     }
                 }
            }
            
            cursor = matchResult.range.last + 1
        }
        
        // Append remaining text
        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
