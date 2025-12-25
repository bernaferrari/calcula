package org.solovyev.android.calculator.history

import org.solovyev.android.Check
import java.util.LinkedList

class RecentHistory {

    private val list: MutableList<HistoryState> = LinkedList()
    private var current = -1

    fun add(state: HistoryState): Boolean {
        Check.isMainThread()
        if (isCurrent(state)) {
            return false
        }
        if (updateState(state)) {
            return true
        }
        while (current != list.size - 1) {
            list.removeAt(list.size - 1)
        }
        list.add(state)
        current++
        trim()
        return true
    }

    private fun updateState(state: HistoryState): Boolean {
        if (current == -1) {
            return false
        }
        val old = list[current]
        if (old.display.sequence == state.display.sequence &&
            old.editor.sequence == state.editor.sequence
        ) {
            // if recalculation is taking place we need to update current history item
            list[current] = state
            return true
        }
        return false
    }

    private fun trim() {
        while (list.size > MAX_HISTORY) {
            current--
            list.removeAt(0)
        }
    }

    fun addInitial(states: List<HistoryState>) {
        Check.isMainThread()
        for (state in states) {
            list.add(0, state)
        }
        current += states.size
        trim()
    }

    fun remove(state: HistoryState) {
        Check.isMainThread()
        for (i in list.indices) {
            val candidate = list[i]
            if (candidate.same(state)) {
                list.removeAt(i)
                if (current >= i) {
                    current--
                }
                break
            }
        }
    }

    private fun isCurrent(state: HistoryState): Boolean {
        val current = getCurrent()
        return current?.same(state) == true
    }

    fun getCurrent(): HistoryState? {
        Check.isMainThread()
        if (current == -1) {
            return null
        }
        return list[current]
    }

    fun redo(): HistoryState? {
        Check.isMainThread()
        if (current < list.size - 1) {
            current++
        }
        return getCurrent()
    }

    fun undo(): HistoryState? {
        Check.isMainThread()
        if (current == -1) {
            return null
        }
        if (current > 0) {
            current--
            return getCurrent()
        }
        return null
    }

    fun clear() {
        Check.isMainThread()
        list.clear()
        current = -1
    }

    fun isEmpty(): Boolean {
        return list.isEmpty()
    }

    fun asList(): List<HistoryState> {
        Check.isMainThread()
        if (current == -1) {
            return emptyList()
        }
        return list.subList(0, current + 1).toList()
    }

    companion object {
        private const val MAX_HISTORY = 100
    }
}
