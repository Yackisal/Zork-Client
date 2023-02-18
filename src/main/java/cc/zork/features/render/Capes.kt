package cc.zork.features.render

import net.minecraft.util.ResourceLocation
import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.math.TimerUtil
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import java.util.*

class Capes : Module("Capes", "set your own cape", ModuleType.Render) {
    companion object {
        val theme = ModeValue("Theme", "Dark", "Light", "Dynamic")
    }

    override fun onEnable() {
        super.onEnable()
        if (mc.thePlayer != null)
            if (theme.getCurrent() != "Dynamic") {
                mc.thePlayer.locationOfCape =
                    ResourceLocation("client/capes/${theme.getCurrent().lowercase(Locale.getDefault())}.png")
            } else {
                mc.thePlayer.locationOfCape =
                    ResourceLocation("client/capes/${theme.getCurrent().lowercase(Locale.getDefault())}0.png")
            }
    }

    var i = 1;
    val timer = TimerUtil()

    @Subscribe
    fun onMotion(e: MotionEvent) {
        if (timer.delay(350f)) {
            if (i < 7)
                i++
            else
                i = 1
            timer.reset()
        }
        if (theme.getCurrent() != "Dynamic") {
            mc.thePlayer.locationOfCape =
                ResourceLocation("client/capes/${theme.getCurrent().lowercase(Locale.getDefault())}.png")
        } else {
            mc.thePlayer.locationOfCape =
                ResourceLocation("client/capes/${theme.getCurrent().lowercase(Locale.getDefault())}$i.png")
        }
    }

    override fun onDisable() {
        super.onDisable()
        if (mc.thePlayer != null)
            mc.thePlayer.locationOfCape = null
    }
}