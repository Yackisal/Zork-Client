package cc.zork.features.player

import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C03PacketPlayer
import cc.zork.event.EventType
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType

class Blink : Module("Blink", "Makes you blink", ModuleType.Player) {
    var ep: EntityOtherPlayerMP? = null
    override fun onEnable() {
        super.onEnable()
        if (mc.thePlayer != null) {
            ep = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
            // create a new player
            ep!!.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
            ep!!.renderYawOffset = mc.thePlayer.renderYawOffset
            ep!!.rotationYawHead = mc.thePlayer.rotationYawHead
            ep!!.rotationYaw = mc.thePlayer.rotationYaw
            ep!!.rotationPitch = mc.thePlayer.rotationPitch
            ep!!.inventory = mc.thePlayer.inventory
            ep!!.inventoryContainer = mc.thePlayer.inventoryContainer


            mc.theWorld.addEntityToWorld(-100, ep)
        }
    }

    val packets = ArrayList<C03PacketPlayer>()

    @Subscribe
    fun onPacket(event: PacketEvent) {
        if (event.type == EventType.SEND) {
            if (event.packet is C03PacketPlayer) {
                event.isCancelled = true
                packets.add(event.packet as C03PacketPlayer);
            }
        }
    }

    override fun onDisable() {
        super.onDisable()
        if (mc.thePlayer != null) {
            if (ep != null)
                mc.theWorld.removeEntity(ep)
            packets.forEach(mc.thePlayer.sendQueue::addToSendQueue)
            packets.clear()
        }
    }

}