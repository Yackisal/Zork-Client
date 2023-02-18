package cc.zork.features.render

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue


class BlockAnimation : Module("BlockAnimation", "Change your  block animation", ModuleType.Render) {
    companion object {
        var swingSpeed: NumberValue = NumberValue("SwingSpeed", 1.0, 0.5, 5.0, 0.1)
        val posX: NumberValue = NumberValue("PosX", 0.0, -2.0, 2.0, 0.1)
        val posY: NumberValue = NumberValue("PosY", 0.0, -2.0, 2.0, 0.1)
        val posZ: NumberValue = NumberValue("PosZ", 0.0, -2.0, 2.0, 0.1)
        val Mode: ModeValue = ModeValue("Mode", "1.7", "1.7", "Swang", "Swank", "Swong", "Jello", "Sigma", "Hanabi")
    }

    @Subscribe
    fun onMotion(e:MotionEvent){
        displayName = Mode.getCurrent()
    }
}