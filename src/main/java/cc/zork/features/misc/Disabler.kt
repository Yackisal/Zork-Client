package cc.zork.features.misc

import cc.zork.Zork
import cc.zork.event.*
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.ui.notification.Type
import cc.zork.utils.math.TimerUtil
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList


class Disabler : Module("Disabler", "Disable some anticheats", ModuleType.Misc) {
    private val confirmTransactionQueue: Queue<C0FPacketConfirmTransaction> = ConcurrentLinkedQueue()

    private val keepAliveQueue: Queue<C00PacketKeepAlive> = ConcurrentLinkedQueue()

    private val clickWindowPackets = CopyOnWriteArrayList<C0EPacketClickWindow>()

    private var disabled = false

    private var lastuid = 0

    var isCraftingItem = false

    private val lastRelease = TimerUtil()

    private var cancelledPackets = 0

    private val timer2 = TimerUtil()


    @Subscribe
    private fun LoadWorld(e: WorldEvent) {
        timer2.reset()
        confirmTransactionQueue.clear()
        disabled = false
        isCraftingItem = false
        clickWindowPackets.clear()
        lastuid = 0
        cancelledPackets = 0
    }

    @Subscribe
    private fun onPacket(e: PacketEvent) {
        val packet = e.packet
        if (disabled) {
            if (packet is C03PacketPlayer && !(packet is C04PacketPlayerPosition || packet is C05PacketPlayerLook || packet is C06PacketPlayerPosLook)) {
                cancelledPackets++
                e.cancelEvent()
            }
        }
        if (packet is C0FPacketConfirmTransaction) {
            if ((packet as C0FPacketConfirmTransaction).windowId == 0 && (packet as C0FPacketConfirmTransaction).uid < 0 && (packet as C0FPacketConfirmTransaction).uid.toInt() != -1) {
                if (disabled) {
                    cancelledPackets++
                    e.cancelEvent()
                }
            }
        }
        if (packet is C0FPacketConfirmTransaction) {
            processConfirmTransactionPacket(e)
        } else if (packet is C00PacketKeepAlive) {
            processKeepAlivePacket(e)
        }
    }

    @Subscribe
    private fun onUpdate(e: MotionEvent) {
        displayName = if (disabled) "Active" else "Progressing"

        if (disabled) {
            if (confirmTransactionQueue.isEmpty()) {
                lastRelease.reset()
            } else {
                if (confirmTransactionQueue.size >= 6) {
                    while (!keepAliveQueue.isEmpty()) mc.netHandler.addToSendQueueNoEvent(keepAliveQueue.poll())
                    while (!confirmTransactionQueue.isEmpty()) {
                        mc.netHandler.addToSendQueueNoEvent(confirmTransactionQueue.poll())
                    }
                }
            }
        }
    }

    private fun processConfirmTransactionPacket(e: PacketEvent) {
        val packet = e.packet
        val preuid = lastuid - 1
        if (packet is C0FPacketConfirmTransaction) {
            if (packet.windowId == 0 || packet.uid < 0) {
                if (packet.uid.toInt() == preuid) {
                    if (!disabled) {
                        Zork.notificationManager.add("Watchdog disabled", "Disabler", 1f, Type.Info)
                        disabled = true
                    }
                    confirmTransactionQueue.offer(packet)
                    e.cancelEvent()
                }
                lastuid = packet.uid.toInt()
            }
        }
    }


    private fun processKeepAlivePacket(e: PacketEvent) {
        val packet = e.packet
        if (packet is C00PacketKeepAlive) {
            if (disabled) {
                keepAliveQueue.offer(packet)
                e.cancelEvent()
            }
        }
    }
}