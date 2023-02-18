package cc.zork.utils.math

import net.minecraft.client.Minecraft

fun base(current: Double, target: Double, speed: Double): Double {
    return current + (target - current) * speed / (Minecraft.getDebugFPS() / 60)
}


fun linear(startTime: Long, duration: Long, start: Double, end: Double): Double {
    return (end - start) * ((System.currentTimeMillis() - startTime).toFloat() / duration) + start
}

fun easeInQuad(startTime: Long, duration: Long, start: Double, end: Double): Double {
    return (end - start) * Math.pow(
        (System.currentTimeMillis() - startTime).toFloat() / duration.toDouble(),
        2.0
    ) + start
}

fun easeOutQuad(startTime: Long, duration: Long, start: Double, end: Double): Double {
    val x = (System.currentTimeMillis() - startTime).toFloat() / duration
    val y = -x * x + 2 * x
    return start + (end - start) * y
}

fun easeInOutQuad(startTime: Long, duration: Long, start: Double, end: Double): Double {
    var t = (System.currentTimeMillis() - startTime).toFloat() / duration
    t *= 2
    if (t < 1) return (end - start) / 2 * t * t + start
    t--
    return -(end - start) / 2 * (t * (t - 2) - 1) + start
}

fun easeInElastic(t: Double, b: Double, c: Double, d: Double): Double {
    var t = t
    var b = b
    var c = c
    var d = d
    var s = 1.70158
    var p = 0.0
    var a = c
    if (t == 0.0) return b
    t /= d
    if (t == 1.0) return b + c
    p = d * 0.3
    if (a < Math.abs(c)) {
        a = c
        s = p / 4.0
    } else {
        s = p / (2 * Math.PI) * Math.asin(c / a)
    }
    t--
    return -(a * Math.pow(2.0, (10 * t).toDouble()) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b
}

fun easeOutElastic(t: Double, b: Double, c: Double, d: Double): Double {
    var t = t
    var b = b
    var c = c
    var d = d
    var s = 1.70158
    var p = 0.0
    var a = c
    if (t == 0.0) return b
    t /= d
    if (t == 1.0) return b + c
    p = d * 0.3
    if (a < Math.abs(c)) {
        a = c
        s = p / 4.0
    } else {
        s = p / (2 * Math.PI) * Math.asin(c / a)
    }
    return a * Math.pow(2.0, (-10 * t).toDouble()) * Math.sin((t * d - s) * (2 * Math.PI) / p) + c + b
}

fun easeInOutElastic(t: Double, b: Double, c: Double, d: Double): Double {
    var t = t
    var b = b
    var c = c
    var d = d
    var s = 1.70158
    var p = 0.0
    var a = c
    if (t == 0.0) return b
    t /= d / 2
    if (t == 2.0) return b + c
    p = d * (0.3 * 1.5)
    if (a < Math.abs(c)) {
        a = c
        s = p / 4.0
    } else {
        s = p / (2 * Math.PI) * Math.asin(c / a)
    }
    if (t < 1) {
        t--
        return -0.5 * (a * Math.pow(2.0, (10 * t).toDouble()) * Math.sin((t * d - s) * (2 * Math.PI) / p)) + b
    }
    t--
    return a * Math.pow(2.0, (-10 * t).toDouble()) * Math.sin((t * d - s) * (2 * Math.PI) / p) * 0.5 + c + b
}

fun easeInBack(t: Double, b: Double, c: Double, d: Double): Double {
    var t = t
    var b = b
    var c = c
    var d = d
    val s = 1.70158
    t /= d
    return c * t * t * ((s + 1) * t - s) + b
}

fun easeOutBack(t: Double, b: Double, c: Double, d: Double): Double {
    var t = t
    var b = b
    var c = c
    var d = d
    val s = 1.70158
    t = t / d - 1
    return c * (t * t * ((s + 1) * t + s) + 1) + b
}