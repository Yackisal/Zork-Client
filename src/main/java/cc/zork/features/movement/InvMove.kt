package cc.zork.features.movement

import cc.zork.event.EventType
import cc.zork.event.MotionEvent
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.ui.clickguis.azureware.ClickUI
import cc.zork.utils.math.TimerUtil
import cc.zork.values.NumberValue
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.settings.KeyBinding
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import org.lwjgl.input.Keyboard
import java.util.*

class InvMove : Module("InvMove", "Move with inventory open", ModuleType.Misc) {
    var packets = LinkedList<Packet<*>>()

    companion object {
        val delay: NumberValue = NumberValue("delay", 20f, 1F, 50F, 1F)
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        packets.clear()
        super.onDisable()
    }

    val timer: TimerUtil = TimerUtil()
    var packetThread = Thread(Runnable {
        var done = false;
        while (!done) {
            if (timer.delay(16F)) {
                if (packets.size > 0) {
                    mc.netHandler.addToSendQueue(packets.poll())
                } else {
                    done = true
                }
                timer.reset()
            }
        }
    })

    var started = false
    var noC0F = 0

    @Subscribe
    fun onPacket(e: PacketEvent) {
        displayName = if (packets.size != 0) "1" else "0"
        if (e.type == EventType.SEND) {
            if (mc.currentScreen != null && mc.currentScreen !is ClickUI && mc.currentScreen !is GuiChat) {
                if (e.packet is C03PacketPlayer) {
                    if (!((e.packet as C03PacketPlayer).positionX == 0.0 && (e.packet as C03PacketPlayer).positionY == 0.0 && (e.packet as C03PacketPlayer).positionZ == 0.0)) {
                        packets.add(e.packet)
                        e.isCancelled = true
                    }
                }
            } else {
                if (e.packet is C03PacketPlayer && packets.size > 0) {
                    if (!((e.packet as C03PacketPlayer).positionX == 0.0 && (e.packet as C03PacketPlayer).positionY == 0.0 && (e.packet as C03PacketPlayer).positionZ == 0.0)) {
                        packets.add(e.packet)
                        e.isCancelled = true
                    }
                    if (packets.size > 0) {
                        synchronized(packetThread) {
                            if (!packetThread.isAlive) {
                                packetThread = Thread(Runnable {
                                    var done = false;
                                    while (!done) {
                                        if (timer.delay(delay.get().toFloat())) {
                                            if (packets.size > 0) {
                                                mc.netHandler.addToSendQueueNoEvent(packets.poll())
                                            } else {
                                                done = true
                                            }
                                            timer.reset()
                                        }
                                    }
                                })
                                packetThread.start()
                            }
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    fun setKeyStat(e: MotionEvent) {
        if (e.eventState == EventType.PRE) {
            if (mc.currentScreen != null && mc.currentScreen !is GuiChat) {
                val key = arrayOf(
                    mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight,
                    mc.gameSettings.keyBindSprint, mc.gameSettings.keyBindJump
                )
                var array: Array<KeyBinding>
                val length = key.also { array = it }.size
                var i = 0
                while (i < length) {
                    val b = array[i]
                    KeyBinding.setKeyBindState(b.keyCode, Keyboard.isKeyDown(b.keyCode))
                    ++i
                }
            }
        }
    }
}
