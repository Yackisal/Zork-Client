package cc.zork.features.movement

import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C07PacketPlayerDigging
import cc.zork.event.EventType
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import cc.zork.event.MotionEvent

class Velocity : Module("Velocity", "Prevent your knockbacks", ModuleType.Movement) {
    companion object {
        val mode = ModeValue("Mode", "Hypixel", "Hypixel", "Cancel", "Modify")
        val x: NumberValue = NumberValue("X", 0F, -100F, 100F, 1F)
        val y: NumberValue = NumberValue("Y", 0F, -100F, 100F, 1F)
        val z: NumberValue = NumberValue("Z", 0F, -100F, 100F, 1F)
    }


    @Subscribe
    fun onPacket(e: PacketEvent) {
        if (e.type == EventType.RECEIVE) {
            if (e.packet is S12PacketEntityVelocity) {
                var s12PacketEntityVelocity = e.packet as S12PacketEntityVelocity
                if (s12PacketEntityVelocity.entityID == mc.thePlayer.entityId) {
                    when (mode.getCurrent()) {
                        "Hypixel" -> {
                            if (mc.thePlayer.ticksExisted % 3 == 0) {
                                s12PacketEntityVelocity.motionX = 0
                                s12PacketEntityVelocity.motionY = 0
                                s12PacketEntityVelocity.motionZ = 0
                            } else {
                                s12PacketEntityVelocity.motionX = 0
                                s12PacketEntityVelocity.motionY = (s12PacketEntityVelocity.motionY * 0.99905).toInt()
                                s12PacketEntityVelocity.motionZ = 0
                            }
                        }

                        "Cancel" -> {
                            e.isCancelled = true
                        }

                        "Modify" -> {
                            s12PacketEntityVelocity.motionX =
                                (s12PacketEntityVelocity.motionX * x.value.toFloat() / 100f).toInt()
                            s12PacketEntityVelocity.motionY =
                                (s12PacketEntityVelocity.motionY * y.value.toFloat() / 100).toInt()
                            s12PacketEntityVelocity.motionZ =
                                (s12PacketEntityVelocity.motionZ * z.value.toFloat() / 100).toInt()
                        }
                    }
                }
            }
        }
    }

}