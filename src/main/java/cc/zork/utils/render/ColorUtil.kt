package cc.zork.utils.render

import java.awt.Color

fun interpolateColorC(color1: Color, color2: Color, amount: Float): Color {
    var amount = amount
    amount = Math.min(1f, Math.max(0f, amount))
    return Color(
        interpolateInt(color1.red, color2.red, amount.toDouble()),
        interpolateInt(color1.green, color2.green, amount.toDouble()),
        interpolateInt(color1.blue, color2.blue, amount.toDouble()),
        interpolateInt(color1.alpha, color2.alpha, amount.toDouble())
    )
}

fun interpolateInt(oldValue: Int, newValue: Int, interpolationValue: Double): Int {
    return interpolate(oldValue.toDouble(), newValue.toDouble(), interpolationValue.toFloat().toDouble())!!.toInt()
}

fun interpolate(oldValue: Double, newValue: Double, interpolationValue: Double): Double? {
    return oldValue + (newValue - oldValue) * interpolationValue
}

fun intToColor(color: Int): Color? {
    val c1 = Color(color)
    return Color(c1.red, c1.green, c1.blue, color shr 24 and 255)
}