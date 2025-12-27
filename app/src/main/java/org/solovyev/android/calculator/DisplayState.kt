package org.solovyev.android.calculator

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.view.ContextMenu
import jscl.math.Generic
import org.json.JSONException
import org.json.JSONObject
import org.solovyev.android.calculator.jscl.JsclOperation

data class DisplayState(
    @get:JvmName("textProperty")
    val text: String,
    val valid: Boolean,
    val sequence: Long,
    @Transient @get:JvmName("operationProperty") var operation: JsclOperation = JsclOperation.numeric,
    @Transient @get:JvmName("resultProperty") var result: Generic? = null
) : Parcelable, ContextMenu.ContextMenuInfo {

    internal constructor(json: JSONObject) : this(
        text = json.optString(JSON_TEXT),
        valid = json.optBoolean(JSON_VALID, true),
        sequence = Calculator.NO_SEQUENCE
    )

    private constructor(parcel: Parcel) : this(
        text = parcel.readString() ?: "",
        valid = parcel.readByte() != 0.toByte(),
        sequence = Calculator.NO_SEQUENCE
    )

    fun getResult(): Generic? = result

    fun getOperation(): JsclOperation = operation

    fun same(that: DisplayState): Boolean {
        return TextUtils.equals(text, that.text) && operation == that.operation
    }

    @Throws(JSONException::class)
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(JSON_TEXT, text)
            put(JSON_VALID, valid)
        }
    }

    override fun toString(): String {
        return "DisplayState{valid=$valid, sequence=$sequence, operation=$operation}"
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeByte(if (valid) 1 else 0)
    }

    fun isEmpty(): Boolean {
        return valid && TextUtils.isEmpty(text)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DisplayState> {
            override fun createFromParcel(parcel: Parcel): DisplayState {
                return DisplayState(parcel)
            }

            override fun newArray(size: Int): Array<DisplayState?> {
                return arrayOfNulls(size)
            }
        }

        private const val JSON_TEXT = "t"
        private const val JSON_VALID = "v"

        @JvmStatic
        fun empty(): DisplayState {
            return DisplayState("", true, Calculator.NO_SEQUENCE)
        }

        @JvmStatic
        fun create(json: JSONObject): DisplayState {
            return DisplayState(json)
        }

        @JvmStatic
        fun createError(
            operation: JsclOperation,
            errorMessage: String,
            sequence: Long
        ): DisplayState {
            return DisplayState(
                text = errorMessage,
                valid = false,
                sequence = sequence,
                operation = operation
            )
        }

        @JvmStatic
        fun createValid(
            operation: JsclOperation,
            result: Generic?,
            stringResult: String,
            sequence: Long
        ): DisplayState {
            return DisplayState(
                text = stringResult,
                valid = true,
                sequence = sequence,
                operation = operation,
                result = result
            )
        }
    }
}
