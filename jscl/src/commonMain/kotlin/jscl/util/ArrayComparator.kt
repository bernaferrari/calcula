package jscl.util

class ArrayComparator : Comparator<Array<Comparable<*>?>> {

    override fun compare(l: Array<Comparable<*>?>, r: Array<Comparable<*>?>): Int {
        if (l.size < r.size) {
            return -1
        } else if (l.size > r.size) {
            return 1
        }

        for (i in l.size - 1 downTo 0) {
            when {
                l[i] == null && r[i] == null -> {
                    // continue
                }
                l[i] != null && r[i] == null -> return -1
                l[i] == null && r[i] != null -> return 1
                else -> {
                    @Suppress("UNCHECKED_CAST")
                    val cmp = (l[i] as Comparable<Any>).compareTo(r[i] as Any)
                    if (cmp < 0) {
                        return -1
                    } else if (cmp > 0) {
                        return 1
                    }
                }
            }
        }

        return 0
    }

    companion object {
        val comparator: Comparator<Array<Comparable<*>?>> = ArrayComparator()
    }
}
