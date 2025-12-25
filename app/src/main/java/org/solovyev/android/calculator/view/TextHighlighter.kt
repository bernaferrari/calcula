package org.solovyev.android.calculator.view

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import org.solovyev.android.Check
import org.solovyev.android.calculator.BaseNumberBuilder
import org.solovyev.android.calculator.Engine
import org.solovyev.android.calculator.LiteNumberBuilder
import org.solovyev.android.calculator.NumberBuilder
import org.solovyev.android.calculator.math.MathType
import org.solovyev.android.calculator.text.TextProcessor
import org.solovyev.android.calculator.text.TextProcessorEditorResult

class TextHighlighter(
    color: Int,
    private val formatNumber: Boolean,
    private val engine: Engine
) : TextProcessor<TextProcessorEditorResult, String> {

    private val red: Int = color.red
    private val green: Int = color.green
    private val blue: Int = color.blue
    private val dark: Int = if (isDark(red, green, blue)) 1 else -1

    override fun process(text: String): TextProcessorEditorResult {
        val sb = SpannableStringBuilder()
        val nb: BaseNumberBuilder = if (!formatNumber) {
            LiteNumberBuilder(engine)
        } else {
            NumberBuilder(engine)
        }
        val result = MathType.Result()

        var offset = 0
        var groupsCount = 0
        var openGroupsCount = 0

        var i = 0
        while (i < text.length) {
            MathType.getType(text, i, nb.isHexMode(), result, engine)

            offset += nb.process(sb, result)

            val match = result.match
            when (result.type) {
                MathType.open_group_symbol -> {
                    openGroupsCount++
                    groupsCount = maxOf(groupsCount, openGroupsCount)
                    sb.append(text[i])
                }
                MathType.close_group_symbol -> {
                    openGroupsCount--
                    sb.append(text[i])
                }
                MathType.operator -> {
                    i += append(sb, match)
                }
                MathType.function -> {
                    i += append(sb, match)
                    makeItalic(sb, i + 1 - match.length, i + 1)
                }
                MathType.constant, MathType.numeral_base -> {
                    i += append(sb, match)
                    makeBold(sb, i + 1 - match.length, i + 1)
                }
                else -> {
                    if (result.type == MathType.text || match.length <= 1) {
                        sb.append(text[i])
                    } else {
                        i += append(sb, match)
                    }
                }
            }
            i++
        }

        if (nb is NumberBuilder) {
            offset += nb.processNumber(sb)
        }

        if (groupsCount == 0) {
            return TextProcessorEditorResult(sb, offset)
        }

        val groupSpans = mutableListOf<GroupSpan>()
        fillGroupSpans(sb, 0, 0, groupsCount, groupSpans)
        for (groupSpan in groupSpans.reversed()) {
            makeColor(sb, groupSpan.start, groupSpan.end, getColor(groupSpan.group, groupsCount))
        }
        return TextProcessorEditorResult(sb, offset)
    }

    private fun append(t: SpannableStringBuilder, match: String): Int {
        t.append(match)
        return if (match.length > 1) match.length - 1 else 0
    }

    private fun fillGroupSpans(
        sb: SpannableStringBuilder,
        start: Int,
        group: Int,
        groupsCount: Int,
        spans: MutableList<GroupSpan>
    ): Int {
        for (i in start until sb.length) {
            val c = sb[i]
            if (MathType.isOpenGroupSymbol(c)) {
                val newI = highlightGroup(sb, i, group + 1, groupsCount, spans)
                if (newI != i) return fillGroupSpans(sb, newI, group, groupsCount, spans)
            } else if (MathType.isCloseGroupSymbol(c)) {
                return i
            }
        }
        return sb.length
    }

    private fun highlightGroup(
        sb: SpannableStringBuilder,
        start: Int,
        group: Int,
        groupsCount: Int,
        spans: MutableList<GroupSpan>
    ): Int {
        val end = minOf(sb.length, fillGroupSpans(sb, start + 1, group, groupsCount, spans))
        if (start + 1 < end) {
            spans.add(GroupSpan(start + 1, end, group))
        }
        return end
    }

    private fun getColor(group: Int, groupsCount: Int): Int {
        val offset = (dark * 255 * 0.6 * group / (groupsCount + 1)).toInt()
        return (0xFF shl 24) or
                ((red + offset) shl 16) or
                ((green + offset) shl 8) or
                (blue + offset)
    }

    private data class GroupSpan(val start: Int, val end: Int, val group: Int) {
        init {
            Check.isTrue(start < end)
        }
    }

    companion object {
        private val Int.blue: Int get() = this and 0xFF
        private val Int.green: Int get() = (this shr 8) and 0xFF
        private val Int.red: Int get() = (this shr 16) and 0xFF

        @JvmStatic
        fun isDark(color: Int): Boolean =
            isDark(color.red, color.green, color.blue)

        @JvmStatic
        fun isDark(red: Int, green: Int, blue: Int): Boolean {
            val y = 0.2126f * red + 0.7152f * green + 0.0722f * blue
            return y < 128
        }

        private fun makeItalic(t: SpannableStringBuilder, start: Int, end: Int) {
            setSpan(t, StyleSpan(Typeface.ITALIC), start, end)
        }

        private fun makeBold(t: SpannableStringBuilder, start: Int, end: Int) {
            setSpan(t, StyleSpan(Typeface.BOLD), start, end)
        }

        private fun makeColor(t: SpannableStringBuilder, start: Int, end: Int, color: Int) {
            setSpan(t, ForegroundColorSpan(color), start, end)
        }

        private fun setSpan(t: SpannableStringBuilder, span: Any, start: Int, end: Int) {
            val clampedStart = start.coerceIn(0, t.length)
            val clampedEnd = end.coerceIn(0, t.length)
            if (clampedStart >= clampedEnd) {
                return
            }
            t.setSpan(span, clampedStart, clampedEnd, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}
