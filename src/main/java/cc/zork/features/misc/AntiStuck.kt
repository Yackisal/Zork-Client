package cc.zork.features.misc

import net.minecraft.block.BlockAir
import cc.zork.event.PushOutEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.notify

class AntiStuck : Module("AntiStuck", "Prevent you from getting stuck in blocks.", ModuleType.Misc) {
    @Subscribe
    fun pushOut(e: PushOutEvent) {
        if (mc.theWorld.getBlockState(mc.thePlayer.position).block !is BlockAir)
            notify("Pushed out")
            mc.thePlayer.posY += 0.5
    }
}