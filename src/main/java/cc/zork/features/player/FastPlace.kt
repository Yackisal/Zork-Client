package cc.zork.features.player

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.math.TimerUtil
import cc.zork.values.NumberValue

class FastPlace : Module("FastPlace", "palce blocks fast", ModuleType.Player) {
    companion object {
        val delay = NumberValue("Delay", 50, 0, 1000, 10)
    }

    val timer = TimerUtil()

    @Subscribe
    fun onUpdate(e: MotionEvent) {
        if (timer.delay(delay.get().toFloat())) {
            mc.rightClickDelayTimer = 0
        } else {
            mc.rightClickDelayTimer = 1
        }
    }

}