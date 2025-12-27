package org.solovyev.common.collections

class SortedList<T> private constructor(
    private val comparator: Comparator<in T>,
    private var list: MutableList<T> = mutableListOf()
) : MutableList<T> {

    companion object {
        fun <T> newInstance(comparator: Comparator<in T>): SortedList<T> {
            return SortedList(comparator)
        }

        fun <T> newInstance(list: MutableList<T>, comparator: Comparator<in T>): SortedList<T> {
            return SortedList(comparator, list)
        }
    }

    override val size: Int
        get() = list.size

    override fun isEmpty(): Boolean = list.isEmpty()

    override fun contains(element: T): Boolean = list.contains(element)

    override fun iterator(): MutableIterator<T> {
        val it = list.iterator()
        return object : MutableIterator<T> {
            override fun hasNext(): Boolean = it.hasNext()

            override fun next(): T = it.next()

            override fun remove() {
                it.remove()
                // todo serso: think
                sort()
            }
        }
    }

    fun toArray(): Array<Any?> = list.toTypedArray()

    override fun add(element: T): Boolean {
        val result = list.add(element)
        insertionSort()
        return result
    }

    override fun remove(element: T): Boolean {
        val result = list.remove(element)
        insertionSort()
        return result
    }

    override fun containsAll(elements: Collection<T>): Boolean = list.containsAll(elements)

    override fun addAll(elements: Collection<T>): Boolean {
        val result = list.addAll(elements)
        sort()
        return result
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val result = list.addAll(index, elements)
        sort()
        return result
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val result = list.removeAll(elements.toSet())
        sort()
        return result
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val result = list.retainAll(elements.toSet())
        sort()
        return result
    }

    override fun clear() {
        list.clear()
    }

    override fun get(index: Int): T = list[index]

    override fun set(index: Int, element: T): T {
        val result = list.set(index, element)
        sort()
        return result
    }

    override fun add(index: Int, element: T) {
        list.add(index, element)
        insertionSort()
    }

    override fun removeAt(index: Int): T {
        val result = list.removeAt(index)
        insertionSort()
        return result
    }

    override fun indexOf(element: T): Int = list.indexOf(element)

    override fun lastIndexOf(element: T): Int = list.lastIndexOf(element)

    override fun listIterator(): MutableListIterator<T> = listIterator(0)

    override fun listIterator(index: Int): MutableListIterator<T> {
        val it = list.listIterator(index)
        return object : MutableListIterator<T> {
            override fun hasNext(): Boolean = it.hasNext()

            override fun next(): T = it.next()

            override fun hasPrevious(): Boolean = it.hasPrevious()

            override fun previous(): T = it.previous()

            override fun nextIndex(): Int = it.nextIndex()

            override fun previousIndex(): Int = it.previousIndex()

            override fun remove() {
                it.remove()
                sort()
            }

            override fun set(element: T) {
                it.set(element)
                sort()
            }

            override fun add(element: T) {
                it.add(element)
                sort()
            }
        }
    }

    fun sort() {
        list.sortWith(comparator)
    }

    private fun insertionSort() {
        for (i in 1 until list.size) {
            val t = list[i]

            var j = i - 1
            while (j >= 0 && comparator.compare(list[j], t) > 0) {
                list[j + 1] = list[j]
                j--
            }
            list[j + 1] = t
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<T> {
        return list.subList(fromIndex, toIndex)
    }
}
