package org.solovyev.android.calculator.view

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.solovyev.android.calculator.BaseNumberBuilder
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.LiteNumberBuilder
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.text.TextProcessor
import org.solovyev.android.calculator.text.TextProcessorEditorResult

class CommonTextHighlighter(
    val color: Color,
    private val formatNumber: Boolean,
    private val engine: Engine
) : Highlighter {

    override fun process(text: String): TextProcessorEditorResult {
        val buildResult = buildAnnotatedString {
            // This is a simplified version. Full implementation would need to handle 
            // the NumberBuilder logic without SpannableStringBuilder.
            // For now, let's focus on basic highlighting.
            
            var i = 0
            val result = MathType.Result()
            while (i < text.length) {
                MathType.getType(text, i, false, result, engine)
                val match = result.match
                
                when (result.type) {
                    MathType.function -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(match)
                        }
                        i += match.length - 1
                    }
                    MathType.constant, MathType.numeral_base -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(match)
                        }
                        i += match.length - 1
                    }
                    else -> {
                        append(text[i])
                    }
                }
                i++
            }
        }

        return object : TextProcessorEditorResult {
            override fun getCharSequence(): CharSequence = buildResult.toString() // Simplified
            override val offset: Int = 0
        }
    }
}
