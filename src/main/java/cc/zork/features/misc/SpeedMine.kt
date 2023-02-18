package cc.zork.features.misc

import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.event.UpdateEvent
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.NumberValue

class SpeedMine : Module("SpeedMine", "make your mining faster.", ModuleType.Misc) {
    companion object {
        val speed = NumberValue("Speed", 1.6, 0.8, 3.0, 0.1)
    }

    private var bzs = false
    private var bzx = 0.0f
    var blockPos: BlockPos? = null
    var facing: EnumFacing? = null

    @Subscribe
    fun update(e: UpdateEvent) {
        if (mc.playerController.extendedReach()) {
            mc.playerController.blockHitDelay = 0
        } else if (bzs) {
            val block = mc.theWorld.getBlockState(this.blockPos).block
            bzx += block.getPlayerRelativeBlockHardness(
                mc.thePlayer,
                mc.theWorld,
                this.blockPos
            ) * speed.get().toFloat()
            if (this.bzx >= 1.0f) {
                mc.theWorld.setBlockState(this.blockPos, Blocks.air.defaultState, 11)
                mc.thePlayer.sendQueue.networkManager.sendPacket(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        this.blockPos,
                        this.facing
                    )
                )
                this.bzx = 0.0f
                this.bzs = false
            }
        }
    }

    @Subscribe
    fun onPacket(e: PacketEvent) {
        if (e.packet is C07PacketPlayerDigging && mc.playerController != null) {
            val c07PacketPlayerDigging = e.packet as C07PacketPlayerDigging
            if (c07PacketPlayerDigging.status == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                bzs = true
                blockPos = c07PacketPlayerDigging.position
                facing = c07PacketPlayerDigging.facing
                bzx = 0.0f
            } else if (c07PacketPlayerDigging.status == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK || c07PacketPlayerDigging.status == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                bzs = false
                blockPos = null
                facing = null
            }
        }
    }
}