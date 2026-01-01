package org.solovyev.android.calculator

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import jscl.common.msg.Message

/**
 * Declarative notification events that the UI can observe and display.
 */
sealed class NotificationEvent {
    data class ShowMessage(val message: String) : NotificationEvent()
    data class ShowFixableErrorDialog(val messages: List<Message>) : NotificationEvent()
}

/**
 * A declarative Notifier that emits events instead of directly showing UI.
 * The Compose UI layer observes [events] and displays Snackbars/Dialogs accordingly.
 */
class Notifier {
    private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<NotificationEvent> = _events.asSharedFlow()

    fun showMessage(message: String) {
        _events.tryEmit(NotificationEvent.ShowMessage(message))
    }

    fun showFixableErrorDialog(messages: List<Message>) {
        _events.tryEmit(NotificationEvent.ShowFixableErrorDialog(messages))
    }
}
