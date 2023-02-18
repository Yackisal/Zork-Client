package cc.zork.features.movement

import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.ModeValue

class NoFall : Module("NoFall", "Prevent you from damaging by falling", ModuleType.Player) {
    companion object {
        val mode = ModeValue("Mode", "Hypixel", "Hypixel", "Vanilla")
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    @Subscribe
    fun onMove(event: MotionEvent) {
        if (mode.getCurrent() == "Vanilla") {
            if (mc.thePlayer.fallDistance > 3.0f) {
                mc.thePlayer.onGround = true
            }
        } else if (mode.getCurrent() == "Hypixel") {
            if (mc.thePlayer.fallDistance > 3.0f && mc.thePlayer.ticksExisted > 120 && mc.thePlayer.ticksExisted % 3 == 0)
                mc.thePlayer.sendQueue.addToSendQueueNoEvent(C03PacketPlayer(true))
        }
    }
}