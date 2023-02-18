package cc.zork.utils.math

class TimerUtil {
    var lastMS = currentMS
    private val currentMS: Long
        private get() = System.nanoTime() / 1000000L

    fun hasReached(milliseconds: Double): Boolean {
        return (currentMS - lastMS).toDouble() >= milliseconds
    }

    fun reset() {
        lastMS = currentMS
    }

    fun delay(milliSec: Float): Boolean {
        return (time - lastMS).toFloat() >= milliSec
    }

    val time: Long
        get() = System.nanoTime() / 1000000L
}