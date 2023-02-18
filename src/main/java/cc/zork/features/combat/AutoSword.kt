package cc.zork.features.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C09PacketHeldItemChange
import cc.zork.event.AttackEvent
import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.BooleanValue

class AutoSword : Module("AutoSword", "Automatically switches to sword in your hotbar", ModuleType.Combat) {
    companion object {
        val switchback = BooleanValue("SwitchBack", true)
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    var origin = -1

    @Subscribe
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.ticksExisted % 3 == 0)
            if (origin != -1) {
                if (switchback.getCurrent())
                    mc.thePlayer.inventory.currentItem = origin
                origin = -1;
            }
    }

    @Subscribe
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            var slot = -1
            for (i in 0..8) {
                if (mc.thePlayer.inventory.getStackInSlot(i) != null) {
                    if (mc.thePlayer.inventory.getStackInSlot(i).item is ItemSword) {
                        slot = i
                        break
                    }
                }
            }
            if (slot != -1) {
                if (switchback.getCurrent())
                    origin = mc.thePlayer.inventory.currentItem
                mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(slot))
                mc.thePlayer.inventory.currentItem = slot
            }
        }
    }

}