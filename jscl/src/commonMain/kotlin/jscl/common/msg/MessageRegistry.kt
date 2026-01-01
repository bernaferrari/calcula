package jscl.common.msg

/**
 * Container for messages
 */
interface MessageRegistry {

    /**
     * Adds message to the registry.
     * Note: according to the implementation this method doesn't guarantee that new message will be added
     * in underlying container (e.g. if such message already exists)
     *
     * @param message message to be added
     */
    fun addMessage(message: Message)

    /**
     * @return true if there is any message available in the registry
     */
    fun hasMessage(): Boolean

    /**
     * Method returns message from registry and removes it from underlying container
     * Note: this method must be called after [MessageRegistry.hasMessage]
     *
     * @return message from the registry
     */
    fun getMessage(): Message
}
