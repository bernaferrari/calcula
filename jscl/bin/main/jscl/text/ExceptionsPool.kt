package jscl.text

class ExceptionsPool {

    private val list = ArrayList<ParseException>()

    fun obtain(position: Int, expression: String, messageCode: String): ParseException {
        return obtain(position, expression, messageCode, emptyList<Any>())
    }

    fun obtain(position: Int, expression: String, messageCode: String, messageArgs: Array<out Any?>?): ParseException {
        return obtain(
            position,
            expression,
            messageCode,
            if (messageArgs.isNullOrEmpty()) emptyList() else messageArgs.toList()
        )
    }

    fun obtain(position: Int, expression: String, messageCode: String, messagesArgs: List<*>): ParseException {
        val exception = if (list.isNotEmpty()) list.removeAt(list.size - 1) else ParseException()
        exception.set(position, expression, messageCode, messagesArgs)
        return exception
    }

    fun release(e: ParseException) {
        if (list.size >= MAX_COUNT) {
            return
        }
        list.add(e)
    }

    companion object {
        private const val MAX_COUNT = 20
    }
}
