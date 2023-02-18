package cc.zork.features.misc

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.NumberValue

class VLReset : Module("ResetVL", "Rest the VL you have", ModuleType.Misc) {
    companion object {
        var times = NumberValue("Times", 30, 0, 100, 1)
    }

    var time = 0

    override fun onEnable() {
        super.onEnable()
        time = 0
    }

    @Subscribe
    fun onUpdate(e: MotionEvent) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.2
            time++
        }

        if (time >= times.get().toInt()) {
            this.set(false)
        }
    }
}