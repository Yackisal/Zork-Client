package cc.zork.features.combat

import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import cc.zork.event.AttackEvent
import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.addChatMessage
import cc.zork.values.ModeValue
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.EnumFacing

class Criticals : Module("Criticals", "Make crits", ModuleType.Combat) {
    companion object {
        val mode = ModeValue("Mode", "Legit", "Legit", "HVH");
    }

    @Subscribe
    fun onMotion(e: MotionEvent) {
        displayName = mode.getCurrent()
        if (mode.getCurrent() == "Legit") {
            var block = mc.theWorld.getBlockState(
                BlockPos(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY + 2,
                    mc.thePlayer.posZ
                )
            ).block
            if (block !is BlockAir && block.isFullBlock && mc.thePlayer.onGround && KillAura.target != null
            ) {
                mc.thePlayer.jump()
            }
        }
    }

    @Subscribe
    fun onAttack(e: AttackEvent) {
        if (mode.getCurrent() == "HVH") {
            addChatMessage(e.targetEntity!!.hurtResistantTime.toString())
            if (e.targetEntity!!.hurtResistantTime < 2 && mc.thePlayer.onGround) {
                mc.thePlayer.sendQueue.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.0004632238+Math.random()*0.000123,
                        mc.thePlayer.posZ,
                        false
                    )
                )
                mc.thePlayer.sendQueue.addToSendQueue(
                    C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 0.0004232238+Math.random()*0.000123,
                        mc.thePlayer.posZ,
                        false
                    )
                )
            }
        } else if (mode.getCurrent() == "Legit") {
            if (mc.theWorld.getBlockState(
                    BlockPos(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + 2,
                        mc.thePlayer.posZ
                    )
                ).block !is BlockAir && mc.thePlayer.onGround && KillAura.target == null
            ) {
                mc.thePlayer.jump()
            }
        }
    }
}