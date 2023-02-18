package cc.zork.utils.math

object MathUtils {
    fun getRandomInRange(min: Int, max: Int): Int {
        return (Math.random() * (max - min + 1) + min).toInt()
    }
}