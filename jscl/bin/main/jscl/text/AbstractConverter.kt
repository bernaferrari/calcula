package jscl.text

internal abstract class AbstractConverter<T, K>(
    protected val parser: Parser<T>
) : Parser<K>
