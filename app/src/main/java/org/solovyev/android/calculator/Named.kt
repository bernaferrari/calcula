package org.solovyev.android.calculator

import android.content.Context
import androidx.annotation.StringRes

data class Named<T>(
    val item: T,
    val name: CharSequence
) {
    override fun toString(): String = name.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Named<*>

        return item == other.item
    }

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }

    companion object {
        @JvmStatic
        fun <T> create(item: T, name: String): Named<T> {
            return Named(item, name)
        }

        @JvmStatic
        fun <T> create(item: T, @StringRes name: Int, context: Context): Named<T> {
            val nameString = if (name == 0) item.toString() else context.getString(name)
            return Named(item, nameString)
        }
    }
}
