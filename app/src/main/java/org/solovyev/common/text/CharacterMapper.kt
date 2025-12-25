package org.solovyev.common.text

import android.text.TextUtils

object CharacterMapper : Mapper<Char> {
    override fun formatValue(value: Char?): String =
        if (value == null || value == '\u0000') "" else value.toString()

    override fun parseValue(value: String?): Char =
        if (TextUtils.isEmpty(value)) '\u0000' else value!![0]
}
