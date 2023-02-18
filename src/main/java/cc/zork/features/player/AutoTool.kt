package cc.zork.features.player

import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType

class AutoTool:Module("AutoTool","Automatically switches to the best tool", ModuleType.Player) {
    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    @Subscribe
    fun onPaclet(e:PacketEvent){
        if (e.packet is C07PacketPlayerDigging) {
            val packetPlayerDigging = e.packet as C07PacketPlayerDigging
            if (packetPlayerDigging.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                if (!mc.thePlayer.capabilities.isCreativeMode) {
                    val blockPosHit =
                        packetPlayerDigging.position
                    if (blockPosHit != null) {
                        mc.thePlayer.inventory.currentItem = getBestTool(blockPosHit)
                        mc.playerController.updateController()
                    }
                }
            }
        }
    }

    private fun getBestTool(pos: BlockPos): Int {
        val block = mc.theWorld.getBlockState(pos).block
        var slot = mc.thePlayer.inventory.currentItem
        var dmg = 0.1f
        for (index in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer
                .getSlot(index).stack
            if (itemStack != null && block != null && itemStack.item.getStrVsBlock(itemStack, block) > dmg) {
                slot = index - 36
                dmg = itemStack.item.getStrVsBlock(itemStack, block)
            }
        }
        return if (dmg > 0.1f) {
            slot
        } else mc.thePlayer.inventory.currentItem
    }

}