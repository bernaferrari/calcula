package org.solovyev.common.text

/**
 * Class represents an interface for mapping string value to the typed object.
 *
 * @param T
 * @see Formatter
 * @see Parser
 */
interface Mapper<T> : Formatter<T>, Parser<T>
