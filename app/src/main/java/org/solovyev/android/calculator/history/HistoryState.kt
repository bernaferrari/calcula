package org.solovyev.android.calculator.history

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import kotlinx.parcelize.Parcelize
import org.json.JSONException
import org.json.JSONObject
import org.solovyev.android.Check
import org.solovyev.android.calculator.DisplayState
import org.solovyev.android.calculator.EditorState
import org.solovyev.android.calculator.json.Json
import org.solovyev.android.calculator.json.Jsonable

@Parcelize
data class HistoryState internal constructor(
    val id: Int,
    val editor: EditorState,
    val display: DisplayState,
    val time: Long,
    val comment: String
) : Parcelable, Jsonable {

    @Throws(JSONException::class)
    override fun toJson(): JSONObject {
        val json = JSONObject()
        json.put(JSON_EDITOR, editor.toJson())
        json.put(JSON_DISPLAY, display.toJson())
        json.put(JSON_TIME, time)
        if (!TextUtils.isEmpty(comment)) {
            json.put(JSON_COMMENT, comment)
        }
        return json
    }

    fun same(that: HistoryState): Boolean {
        return this.editor.same(that.editor) && this.display.same(that.display)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is HistoryState) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    fun isEmpty(): Boolean {
        return display.isEmpty() && editor.isEmpty() && TextUtils.isEmpty(comment)
    }

    class Builder internal constructor(
        private var state: HistoryState
    ) {
        private var built = false

        internal constructor(editor: EditorState, display: DisplayState) : this(
            HistoryState(
                id = System.identityHashCode(Any()),
                editor = editor,
                display = display,
                time = now(),
                comment = ""
            )
        )

        internal constructor(state: HistoryState, newState: Boolean) : this(
            HistoryState(
                id = if (newState) System.identityHashCode(Any()) else state.id,
                editor = state.editor,
                display = state.display,
                time = if (newState) now() else state.time,
                comment = state.comment
            )
        )

        fun withTime(time: Long): Builder {
            Check.isTrue(!built)
            state = state.copy(time = time)
            return this
        }

        fun withComment(comment: String?): Builder {
            Check.isTrue(!built)
            state = state.copy(comment = comment ?: "")
            return this
        }

        fun build(): HistoryState {
            built = true
            return state
        }
    }

    companion object {
        private const val JSON_EDITOR = "e"
        private const val JSON_DISPLAY = "d"
        private const val JSON_TIME = "t"
        private const val JSON_COMMENT = "c"

        @JvmStatic
        fun builder(editor: EditorState, display: DisplayState): Builder {
            return Builder(editor, display)
        }

        @JvmStatic
        fun builder(state: HistoryState, newState: Boolean): Builder {
            return Builder(state, newState)
        }

        @JvmStatic
        @Throws(JSONException::class)
        fun create(json: JSONObject): HistoryState {
            val editor = EditorState.create(json.getJSONObject(JSON_EDITOR))
            val display = DisplayState.create(json.getJSONObject(JSON_DISPLAY))
            val time = json.optLong(JSON_TIME, 0L)
            val comment = json.optString(JSON_COMMENT, "")
            return HistoryState(
                id = System.identityHashCode(Any()),
                editor = editor,
                display = display,
                time = time,
                comment = comment
            )
        }

        @JvmField
        val JSON_CREATOR = object : Json.Creator<HistoryState> {
            @Throws(JSONException::class)
            override fun create(json: JSONObject): HistoryState {
                return HistoryState.create(json)
            }
        }

        private fun now(): Long = System.currentTimeMillis()
    }
}
