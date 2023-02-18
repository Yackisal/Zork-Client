package cc.zork.features.misc

import cc.zork.Zork
import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.movement.Speed
import cc.zork.features.player.Scaffold
import cc.zork.manager.ModuleManager
import cc.zork.values.NumberValue

class Timer : Module("Timer", "Change your game speed", ModuleType.Misc) {
    companion object{
        val timer=NumberValue("Timer",1,0,10,0.1)
    }

    @Subscribe
    fun onMotion(e:MotionEvent){
        if (ModuleManager.getMod(Scaffold::class.java)!!.stage || ModuleManager.getMod(Speed::class.java)!!.stage) {
        } else {
            mc.timer.timerSpeed = timer.get().toFloat()
        }
    }
}