package cc.zork.utils.math

class Animation() {

    var duration: Long = 0
    var startTime: Long = 0
    var start: Double = 0.0
    var value: Double = 0.0
    var end: Double = 0.0
    var type: Type = Type.LINEAR
    var started: Boolean = false

    fun start(start: Double, end: Double, duration: Float, type: Type) {
        if (!started) {
            if (start != this.start || end != this.end || (duration * 1000).toLong() != this.duration || type != this.type) {
                this.duration = (duration * 1000).toLong()
                this.start = start
                this.startTime = System.currentTimeMillis()
                value = start
                this.end = end
                this.type = type
                started = true
            }
        }
    }

    fun update() {

        value = when (type) {
            Type.LINEAR -> linear(startTime, duration, start, end)
            Type.EASE_IN_QUAD -> easeInQuad(startTime, duration, start, end)
            Type.EASE_OUT_QUAD -> easeOutQuad(startTime, duration, start, end)
            Type.EASE_IN_OUT_QUAD -> easeInOutQuad(startTime, duration, start, end)
            Type.EASE_IN_ELASTIC -> easeInElastic(
                (System.currentTimeMillis() - startTime).toDouble(),
                start,
                end - start,
                duration.toDouble()
            )

            Type.EASE_OUT_ELASTIC -> easeOutElastic(
                (System.currentTimeMillis() - startTime).toDouble(),
                start,
                end - start,
                duration.toDouble()
            )

            Type.EASE_IN_OUT_ELASTIC -> easeInOutElastic(
                (System.currentTimeMillis() - startTime).toDouble(),
                start,
                end - start,
                duration.toDouble()
            )

            Type.EASE_IN_BACK -> easeInBack(
                (System.currentTimeMillis() - startTime).toDouble(),
                start,
                end - start,
                duration.toDouble()
            )

            Type.EASE_OUT_BACK -> easeOutBack(
                (System.currentTimeMillis() - startTime).toDouble(),
                start,
                end - start,
                duration.toDouble()
            )
        }
        if ((System.currentTimeMillis() - startTime) > duration) {
            started = false
            value = end
        }

    }

    fun reset() {
        value = 0.0
        start = 0.0
        end = 0.0
        startTime = System.currentTimeMillis()
        started = false
    }

    fun fstart(start: Double, end: Double, duration: Float, type: Type) {
        started = false
        start(start, end, duration, type)
    }
}

public enum class Type {
    LINEAR,
    EASE_IN_QUAD,
    EASE_OUT_QUAD,
    EASE_IN_OUT_QUAD,
    EASE_IN_ELASTIC,
    EASE_OUT_ELASTIC,
    EASE_IN_OUT_ELASTIC,
    EASE_IN_BACK,
    EASE_OUT_BACK,
}