package cc.zork.features.movement

import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.KillAura
import cc.zork.utils.client.MovementUtil.isMoving
import cc.zork.values.ModeValue
import me.konago.nativeobfuscator.Native

@Native
class NoSlow : Module("NoSlow", "There won't a slowdown when you using items", ModuleType.Movement) {
    companion object {
        var mode: ModeValue = ModeValue("Mode", "Hypixel", "Hypixel", "Vanilla")
    }

    var released = true

    @Subscribe
    fun onMotion(e: MotionEvent) {
        displayName = mode.getCurrent()
        if (mode.getCurrent() == "Hypixel") {
            if (mc.thePlayer.isUsingItem && mc.thePlayer.heldItem != null && isMoving()) {
                var st = 8
                for (i in 0..8) {
                    if (mc.thePlayer.inventory.getStackInSlot(i) == null) {
                        st = i
                    }
                }
                if (mc.thePlayer.hurtTime == 0 && KillAura.targets.size == 0 && mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword) {
                    if (!released) {
                        mc.thePlayer.sendQueue.addToSendQueue(
                            C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                                BlockPos.ORIGIN,
                                EnumFacing.DOWN
                            )
                        )
                        released = true
                    }
                    return
                } else {
                    released = false
                }

                mc.netHandler.addToSendQueueNoEvent(C09PacketHeldItemChange(st))

//                for (i in 0..3) {
                mc.netHandler.addToSendQueueNoEvent(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                        BlockPos.ORIGIN,
                        EnumFacing.UP
                    )
                )
//                }
                mc.netHandler.addToSendQueueNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            }else{
                released = false
            }
        }
    }
}