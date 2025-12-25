package org.solovyev.android.plotter.meshes

import android.content.Context
import org.solovyev.android.plotter.Color

data class MeshSpec(
    val color: Color,
    val width: Int
) {
    var pointsCount: Int = 100

    companion object {
        const val MIN_WIDTH = 1
        const val MAX_WIDTH = 10

        @JvmStatic
        fun create(color: Color, width: Int): MeshSpec {
            return MeshSpec(color, width)
        }

        @JvmStatic
        fun defaultWidth(context: Context): Int {
            return 3
        }
    }

    object LightColors {
        private val colors = intArrayOf(
            0xFF0099CC.toInt(),
            0xFF9933CC.toInt(),
            0xFF669900.toInt(),
            0xFFFF8800.toInt(),
            0xFFCC0000.toInt(),
            0xFF0033CC.toInt(),
            0xFFFF4444.toInt(),
            0xFFAA66CC.toInt(),
            0xFF99CC00.toInt(),
            0xFFFFBB33.toInt()
        )

        fun asIntArray(): IntArray = colors
    }
}
