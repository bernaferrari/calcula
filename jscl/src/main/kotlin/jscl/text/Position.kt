package jscl.text

class Position(var value: Int = 0) {
    fun toInt(): Int = value

    fun increment() {
        value++
    }

    fun add(delta: Int) {
        value += delta
    }
}
