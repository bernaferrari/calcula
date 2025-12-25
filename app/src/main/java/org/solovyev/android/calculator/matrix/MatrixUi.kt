package org.solovyev.android.calculator.matrix

import java.io.Serializable

/**
 * User: serso
 * Date: 7/11/13
 * Time: 4:54 PM
 */
internal data class MatrixUi(
    val bakingArray: Array<Array<String>> = emptyArray()
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatrixUi

        if (!bakingArray.contentDeepEquals(other.bakingArray)) return false

        return true
    }

    override fun hashCode(): Int {
        return bakingArray.contentDeepHashCode()
    }
}
