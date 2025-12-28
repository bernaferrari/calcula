package org.solovyev.common.collections

class SortedMutableMap<K, V>(
    private val comparator: Comparator<in K>
) : MutableMap<K, V> {

    companion object {
        fun <K : Comparable<K>, V> naturalOrder(): SortedMutableMap<K, V> {
            return SortedMutableMap(Comparator { a, b -> a.compareTo(b) })
        }
    }

    private data class Entry<K, V>(
        override val key: K,
        override var value: V
    ) : MutableMap.MutableEntry<K, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            return old
        }
    }

    private val entriesList = mutableListOf<Entry<K, V>>()

    override val size: Int
        get() = entriesList.size

    override fun isEmpty(): Boolean = entriesList.isEmpty()

    override fun containsKey(key: K): Boolean = findIndex(key) >= 0

    override fun containsValue(value: V): Boolean = entriesList.any { it.value == value }

    override fun get(key: K): V? {
        val index = findIndex(key)
        return if (index >= 0) entriesList[index].value else null
    }

    override fun put(key: K, value: V): V? {
        val index = findIndex(key)
        return if (index >= 0) {
            entriesList[index].setValue(value)
        } else {
            val insertAt = -index - 1
            entriesList.add(insertAt, Entry(key, value))
            null
        }
    }

    override fun putAll(from: Map<out K, V>) {
        for ((key, value) in from) {
            put(key, value)
        }
    }

    override fun remove(key: K): V? {
        val index = findIndex(key)
        return if (index >= 0) {
            entriesList.removeAt(index).value
        } else {
            null
        }
    }

    override fun clear() {
        entriesList.clear()
    }

    override val keys: MutableSet<K>
        get() = LinkedHashSet(entriesList.map { it.key })

    override val values: MutableCollection<V>
        get() = entriesList.map { it.value }.toMutableList()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = LinkedHashSet(entriesList)

    fun headMap(toKey: K): SortedMutableMap<K, V> {
        val result = SortedMutableMap<K, V>(comparator)
        for (entry in entriesList) {
            if (comparator.compare(entry.key, toKey) >= 0) break
            result.entriesList.add(entry.copy())
        }
        return result
    }

    fun firstKey(): K {
        return entriesList.first().key
    }

    fun lastKey(): K {
        return entriesList.last().key
    }

    fun tailMap(fromKey: K): SortedMutableMap<K, V> {
        val result = SortedMutableMap<K, V>(comparator)
        for (entry in entriesList) {
            if (comparator.compare(entry.key, fromKey) >= 0) {
                result.entriesList.add(entry.copy())
            }
        }
        return result
    }

    private fun findIndex(key: K): Int {
        var low = 0
        var high = entriesList.size - 1
        while (low <= high) {
            val mid = (low + high).ushr(1)
            val cmp = comparator.compare(entriesList[mid].key, key)
            when {
                cmp < 0 -> low = mid + 1
                cmp > 0 -> high = mid - 1
                else -> return mid
            }
        }
        return -(low + 1)
    }
}
