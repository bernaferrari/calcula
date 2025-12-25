package org.solovyev.android.prefs

import android.text.TextUtils
import org.solovyev.common.text.Mapper

class CachingMapper<T> private constructor(
    private val mapper: Mapper<T>
) : Mapper<T> {

    private data class CachedEntry<T>(
        var value: String? = null,
        var obj: T? = null
    )

    private var cachedEntry: CachedEntry<T>? = null

    @Synchronized
    override fun parseValue(value: String?): T? {
        val entry = cachedEntry ?: CachedEntry<T>().also { cachedEntry = it }
        
        if (TextUtils.equals(entry.value, value)) {
            return entry.obj
        }
        
        entry.value = value
        entry.obj = mapper.parseValue(value)
        return entry.obj
    }

    @Synchronized
    override fun formatValue(value: T?): String? {
        val entry = cachedEntry ?: CachedEntry<T>().also { cachedEntry = it }
        
        if (entry.obj == value) {
            return entry.value
        }
        
        entry.obj = value
        entry.value = mapper.formatValue(value)
        return entry.value
    }

    companion object {
        @JvmStatic
        fun <T> of(mapper: Mapper<T>): Mapper<T> {
            return if (mapper is CachingMapper) {
                mapper
            } else {
                CachingMapper(mapper)
            }
        }
    }
}
