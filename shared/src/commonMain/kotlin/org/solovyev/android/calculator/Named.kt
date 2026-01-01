package org.solovyev.android.calculator

/**
 * A simple wrapper for items with a displayable name.
 */
data class Named<T>(
    val item: T,
    val name: String
) {
    override fun toString(): String = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Named<*>

        return item == other.item
    }

    override fun hashCode(): Int {
        return item?.hashCode() ?: 0
    }

    companion object {
        fun <T> create(item: T, name: String): Named<T> {
            return Named(item, name)
        }
    }
}
