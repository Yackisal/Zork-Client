package cc.zork.utils.math

import java.awt.Color

class ColorAnimation {
    var r: Animation = Animation()
    var g: Animation = Animation()
    var b: Animation = Animation()
    var a: Animation = Animation()

    fun start(start: Color, end: Color, duration: Float, type: Type) {
        r.start(start.red.toDouble(), end.red.toDouble(), duration, type)
        g.start(start.green.toDouble(), end.green.toDouble(), duration, type)
        b.start(start.blue.toDouble(), end.blue.toDouble(), duration, type)
        a.start(start.alpha.toDouble(), end.alpha.toDouble(), duration, type)
    }

    fun update() {
        r.update()
        g.update()
        b.update()
        a.update()
    }

    fun reset() {
        r.reset()
        g.reset()
        b.reset()
        a.reset()
    }

    fun getColor(): Color {
        return Color(r.value.toInt(), g.value.toInt(), b.value.toInt(), a.value.toInt())
    }
}