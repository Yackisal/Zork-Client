package cc.zork.features.misc

import cc.zork.event.EventType
import cc.zork.event.MotionEvent
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.notify
import cc.zork.utils.math.TimerUtil
import cc.zork.values.NumberValue
import net.minecraft.block.BlockAir
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos

class AntiFall : Module("AntiFall", "prevent you from falling", ModuleType.Misc) {
    companion object {
        var falldistance: NumberValue =
            NumberValue("FallDistance", 10.0, 0.0, 30.0, 0.1)
        var delay: NumberValue = NumberValue("Delay", 800.0, 200.0, 3000.0, 100.0)
    }


    fun isAboveVoid(): Boolean {
        if (mc.thePlayer == null) {
            return false
        }
        if (mc.thePlayer.posY < 0) return true
        for (i in (mc.thePlayer.posY - 1).toInt() downTo 1) if (mc.theWorld.getBlockState(
                BlockPos(
                    mc.thePlayer.posX,
                    i.toDouble(),
                    mc.thePlayer.posZ
                )
            ).block !is BlockAir
        ) return false
        return !mc.thePlayer.onGround
    }

    fun isAboveVoid(x: Float, y: Float, z: Float): Boolean {
        if (mc.thePlayer == null) {
            return false
        }
        if (y < 0) return true
        for (i in (y - 1).toInt() downTo 1) if (mc.theWorld.getBlockState(
                BlockPos(
                    x.toDouble(),
                    i.toDouble(),
                    z.toDouble()
                )
            ).block !is BlockAir
        ) return false
        return !mc.thePlayer.onGround
    }

    var last = floatArrayOf(0f, 0f, 0f)
    var timerUtil: TimerUtil = TimerUtil()
    var packets = ArrayList<Packet<*>>()
    var falling = false

    @Subscribe
    fun onMotion(e: MotionEvent) {
        if (!isAboveVoid()) {
            last = floatArrayOf(
                mc.thePlayer.posX.toFloat(), mc.thePlayer.posY.toFloat(),
                mc.thePlayer.posZ.toFloat()
            )
        }
    }

    @Subscribe
    fun onSend(e: PacketEvent) {
        if (e.type == EventType.SEND) {
            //clear
            if (mc.thePlayer != null && mc.thePlayer.ticksExisted < 100) {
                packets.clear()
                return
            }
            if (e.packet is C03PacketPlayer && !mc.thePlayer.capabilities.isFlying) {
                if (isAboveVoid() && !mc.thePlayer.onGround) {
                    e.cancelEvent()
                    packets.add(e.packet)
                    if (timerUtil.hasReached(delay.get().toDouble())) {
                        mc.thePlayer.setPosition(last[0].toDouble(), last[1].toDouble() + 0.1, last[2].toDouble())
                        mc.thePlayer.sendQueue.addToSendQueueNoEvent(
                            C04PacketPlayerPosition(
                                last[0].toDouble(), (last[1]).toDouble(), last[2].toDouble(), true
                            )
                        )
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionX = -mc.thePlayer.motionX / 2
                        mc.thePlayer.motionZ = -mc.thePlayer.motionZ / 2
                        packets.clear()
                        timerUtil.reset()
                    }
                } else {
                    if (packets.size > 0) {
                        for (packet in packets) {
                            mc.netHandler.addToSendQueueNoEvent(packet)
                        }
                        packets.clear()
                    }
                    timerUtil.reset()
                }
            }
        }
    }

    @Subscribe
    fun onReceive(e: PacketEvent) {
        if (e.packet is S08PacketPlayerPosLook) {
            packets.clear()
            var packet = e.packet as S08PacketPlayerPosLook
            val x = packet.x.toFloat()
            val y = packet.y.toFloat()
            val z = packet.z.toFloat()


            if (!isAboveVoid(x, y, z)) {
                last[0] = x
                last[1] = y
                last[2] = z
            } else if (mc.thePlayer.ticksExisted > 200) {
                notify("服务器正在将你拉回到虚空中，请退出到大厅解决")
            }
        }
    }
}