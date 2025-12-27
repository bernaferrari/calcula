package org.solovyev.android.calculator.errors

import android.os.Parcel
import android.os.Parcelable
import org.solovyev.android.calculator.CalculatorMessages
import org.solovyev.common.msg.Message
import org.solovyev.common.msg.MessageType

data class FixableError(
    val message: String,
    val messageType: MessageType,
    val error: FixableErrorType?
) : Parcelable {

    constructor(message: Message) : this(
        message = message.getLocalizedMessage(),
        messageType = CalculatorMessages.toMessageType(message.getMessageLevel().getMessageLevel()),
        error = FixableErrorType.getErrorByCode(message.getMessageCode())
    )

    private constructor(parcel: Parcel) : this(
        message = parcel.readString()!!,
        messageType = MessageType.values()[parcel.readInt()],
        error = parcel.readInt().let { if (it == -1) null else FixableErrorType.values()[it] }
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(out: Parcel, flags: Int) {
        out.writeString(message)
        out.writeInt(messageType.ordinal)
        out.writeInt(error?.ordinal ?: -1)
    }

    companion object CREATOR : Parcelable.Creator<FixableError> {
        override fun createFromParcel(parcel: Parcel): FixableError = FixableError(parcel)
        override fun newArray(size: Int): Array<FixableError?> = arrayOfNulls(size)
    }
}
