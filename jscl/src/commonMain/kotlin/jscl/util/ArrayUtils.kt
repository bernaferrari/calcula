package jscl.util

object ArrayUtils {

    private const val BINARY_SEARCH_THRESHOLD = 5000

    fun concat(o1: Array<Any?>, o2: Array<Any?>, res: Array<Any?>): Array<Any?> {
        o1.copyInto(res, 0, 0, 0 + o1.size)
        o2.copyInto(res, o1.size, 0, 0 + o2.size)
        return res
    }

    fun <T> toArray(list: List<T>, res: Array<T>): Array<T> {
        val n = list.size
        for (i in 0 until n) {
            res[i] = list[i]
        }
        return res
    }

    fun toArray(list: List<Int>, result: IntArray): IntArray {
        val n = list.size
        for (i in 0 until n) {
            result[i] = list[i]
        }
        return result
    }

    fun <T> toList(collection: Collection<T>): List<T> {
        return collection.toMutableList()
    }

    fun toString(array: Array<Any>): String {
        val result = StringBuilder()
        result.append("{")
        for (i in array.indices) {
            result.append(array[i]).append(if (i < array.size - 1) ", " else "")
        }
        result.append("}")
        return result.toString()
    }

    fun <T : Comparable<T>> binarySearch(list: List<T>, key: T): Int {
        return if (list is RandomAccess || list.size < BINARY_SEARCH_THRESHOLD)
            indexedBinarySearch(list, key)
        else
            iteratorBinarySearch(list, key)
    }

    private fun <T : Comparable<T>> indexedBinarySearch(list: List<T>, key: T): Int {
        var low = 0
        var high = list.size - 1

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = list[mid]
            val cmp = midVal.compareTo(key)

            when {
                cmp < 0 -> low = mid + 1
                cmp > 0 -> high = mid - 1
                else -> return mid // key found
            }
        }
        return -(low + 1)  // key not found
    }

    private fun <T : Comparable<T>> iteratorBinarySearch(list: List<T>, key: T): Int {
        var low = 0
        var high = list.size - 1
        val it = list.listIterator()

        while (low <= high) {
            val mid = (low + high) ushr 1
            val midVal = get(it, mid)
            val cmp = midVal.compareTo(key)

            when {
                cmp < 0 -> low = mid + 1
                cmp > 0 -> high = mid - 1
                else -> return mid // key found
            }
        }
        return -(low + 1)  // key not found
    }

    private fun <T> get(it: ListIterator<T>, index: Int): T {
        var result: T

        var pos = it.nextIndex()
        if (pos <= index) {
            do {
                result = it.next()
            } while (pos++ < index)
        } else {
            do {
                result = it.previous()
            } while (--pos > index)
        }

        return result
    }
}
