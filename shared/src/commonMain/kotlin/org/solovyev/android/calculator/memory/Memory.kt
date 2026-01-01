package org.solovyev.android.calculator.memory

import kotlinx.coroutines.flow.SharedFlow

interface Memory {
    val valueReadyEvents: SharedFlow<String>
}
