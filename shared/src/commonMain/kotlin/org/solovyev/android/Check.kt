package org.solovyev.android

object Check {
    fun shouldNotHappen() {
        // Logging or exception
        println("Check.shouldNotHappen() called")
    }
    
    fun isNotEmpty(s: String?) {
        require(!s.isNullOrEmpty()) { "String must not be empty" }
    }
    
    fun isTrue(condition: Boolean) {
        require(condition) { "Condition must be true" }
    }

    fun isMainThread() {
        // In KMP common, we don't strictly check thread unless using specific platform tools
    }
}
