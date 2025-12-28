package org.solovyev.common.msg

object Messages {

    fun synchronizedMessageRegistry(messageRegistry: MessageRegistry): MessageRegistry {
        return SynchronizedMessageRegistry.wrap(messageRegistry)
    }

    // Localization is handled by the app/UI layer.
}
