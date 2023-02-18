package cc.zork.features.misc

import cc.zork.Zork
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.manager.ui.NotificationManager
import cc.zork.ui.notification.Type
import net.minecraft.event.ClickEvent
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S02PacketChat

class AutoPlay : Module("AutoPlay", "Auto play the next game", ModuleType.Misc) {
    @Subscribe
    fun onPacket(event: PacketEvent) {
        var packet = event.packet
        if (packet is S02PacketChat) {
            for (cc in packet.chatComponent.siblings) {
                val ce = cc.chatStyle.chatClickEvent
                if (ce != null) {
                    if (ce.action == ClickEvent.Action.RUN_COMMAND && ce.value.contains("/play")) {
                        Zork.notificationManager.add("AutoPlay","Next game will start in 3 seconds.",3f, Type.Info)
                        Thread {
                            try {
                                Thread.sleep(3000L)
                            } catch (a: InterruptedException) {
                                a.printStackTrace()
                            }
                            mc.thePlayer.sendQueue.addToSendQueue(C01PacketChatMessage(ce.value))
                        }.start()
                    }
                }
            }
        }
    }
}