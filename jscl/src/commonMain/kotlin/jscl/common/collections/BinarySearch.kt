package jscl.common.collections

fun <T : Comparable<T>> Array<T?>.binarySearch(key: T): Int {
    var low = 0
    var high = size - 1
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val midVal = this[mid]!!
        val cmp = midVal.compareTo(key)
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid
        }
    }
    return -(low + 1)
}

fun <T> Array<T?>.binarySearch(key: T, comparator: Comparator<in T>): Int {
    var low = 0
    var high = size - 1
    while (low <= high) {
        val mid = (low + high).ushr(1)
        val midVal = this[mid]!!
        val cmp = comparator.compare(midVal, key)
        when {
            cmp < 0 -> low = mid + 1
            cmp > 0 -> high = mid - 1
            else -> return mid
        }
    }
    return -(low + 1)
}
