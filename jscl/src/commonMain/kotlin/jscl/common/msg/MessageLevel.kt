package jscl.common.msg

/**
 * See [MessageType] as default implementation of this class
 */
interface MessageLevel {

    companion object {
        const val INFO_LEVEL = 100
        const val WARNING_LEVEL = 500
        const val ERROR_LEVEL = 1000
    }

    /**
     * Position of current message level in some message level hierarchy.
     * By default, one can use [MessageType] implementation which uses next levels:
     * 100         500           1000          level
     * --------|-----------|--------------|------------->
     * Info       Warning         Error
     *
     * @return int message level
     */
    fun getMessageLevel(): Int

    /**
     * Some string id for level (might be used in logs)
     *
     * @return string level identifier
     */
    fun getName(): String
}
