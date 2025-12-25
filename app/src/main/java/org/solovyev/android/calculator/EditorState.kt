/*
 * Copyright 2013 serso aka se.solovyev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Contact details
 *
 * Email: se.solovyev@gmail.com
 * Site:  http://se.solovyev.org
 */

package org.solovyev.android.calculator

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import org.json.JSONException
import org.json.JSONObject

data class EditorState(
    val sequence: Long,
    val text: CharSequence,
    val selection: Int
) : Parcelable {

    private var textString: String? = null

    private constructor(text: CharSequence, selection: Int) : this(
        sequence = Calculator.nextSequence(),
        text = text,
        selection = selection
    )

    private constructor(json: JSONObject) : this(
        json.optString(JSON_TEXT),
        json.optInt(JSON_SELECTION)
    )

    private constructor(parcel: Parcel) : this(
        sequence = Calculator.NO_SEQUENCE,
        text = parcel.readString() ?: "",
        selection = parcel.readInt()
    ) {
        textString = parcel.readString()
    }

    fun getTextString(): String {
        if (textString == null) {
            textString = text.toString()
        }
        return textString!!
    }

    fun same(that: EditorState): Boolean {
        return TextUtils.equals(text, that.text) && selection == that.selection
    }

    fun isEmpty(): Boolean {
        return TextUtils.isEmpty(text)
    }

    override fun toString(): String {
        return "EditorState{sequence=$sequence, text=$text, selection=$selection}"
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(JSON_TEXT, getTextString())
            put(JSON_SELECTION, selection)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(selection)
        dest.writeString(textString)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<EditorState> {
            override fun createFromParcel(parcel: Parcel): EditorState {
                return EditorState(parcel)
            }

            override fun newArray(size: Int): Array<EditorState?> {
                return arrayOfNulls(size)
            }
        }

        private const val JSON_TEXT = "t"
        private const val JSON_SELECTION = "s"

        @JvmStatic
        fun empty(): EditorState {
            return EditorState("", 0)
        }

        @JvmStatic
        fun forNewSelection(state: EditorState, selection: Int): EditorState {
            return EditorState(state.text, selection)
        }

        @JvmStatic
        fun create(text: CharSequence, selection: Int): EditorState {
            return EditorState(text, selection)
        }

        @JvmStatic
        fun create(json: JSONObject): EditorState {
            return EditorState(json)
        }
    }
}
