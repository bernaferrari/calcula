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
