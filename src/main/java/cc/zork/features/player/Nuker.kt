package cc.zork.features.player

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.RotationUtils
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3

class Nuker : Module("Nuker", "Auto break some blocks", ModuleType.Player) {
    companion object {
        val range = NumberValue("range", 5, 0, 10, 1)
        val lock = BooleanValue("Lock-View", false)
    }

    var yaw = 0f
    var pitch = 0f


    private var currentDamage = 0f
    val blocks = mutableListOf<BlockPos>()
    var target: BlockPos? = null

    @Subscribe
    fun onUpdate(e: MotionEvent) {
        blocks.clear()
        for (x in 0..range.get().toInt()) {
            for (y in 0..range.get().toInt()) {
                for (z in 0..range.get().toInt()) {
                    var pos = mutableListOf<BlockPos>()
                    pos.add(BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z))
                    pos.add(BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z))
                    pos.add(BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z))
                    pos.add(BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z))
//                    pos.add(BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY - y, mc.thePlayer.posZ + z))
//                    pos.add(BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY - y, mc.thePlayer.posZ + z))
//                    pos.add(BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY - y, mc.thePlayer.posZ - z))
//                    pos.add(BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY - y, mc.thePlayer.posZ - z))


                    pos.forEach { it ->
                        if (mc.theWorld.getBlockState(it).block.material.isSolid) {
                            blocks.add(it)
                        }
                    }
                }
            }
        }
        if (target == null && blocks.isNotEmpty()) {
            target = blocks[0]
        } else if (target != null) {
            var rotations =
                RotationUtils.getRotations(Vec3(target!!.x.toDouble()+0.5, target!!.y.toDouble()+0.5, target!!.z.toDouble()+0.5))
            e.Yaw = rotations[0]
            e.Pitch = rotations[1]
            if (lock.getCurrent()) {
                mc.thePlayer.rotationYaw = rotations[0]
                mc.thePlayer.rotationPitch = rotations[1]
            }
            mc.thePlayer.renderYawOffset = rotations[0]
            mc.thePlayer.rotationYawHead = rotations[0]
            mc.thePlayer.rotationPitchHead = rotations[1]
//            val ray = mc.theWorld.rayTraceBlocks(
//                Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ),
//                Vec3(target!!.x.toDouble()+0.5, target!!.y.toDouble()+0.5, target!!.z.toDouble()+0.5)
//            ).blockPos?:return
//            val block = mc.theWorld.getBlockState(ray).block
//            target = ray
            val block = mc.theWorld.getBlockState(target!!).block
            // Start block breaking
            if (currentDamage == 0F) {
                mc.netHandler.addToSendQueue(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                        target, EnumFacing.DOWN
                    )
                )

                // End block break if able to break instant
                if (block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld!!, target) >= 1F) {
                    currentDamage = 0F
                    mc.thePlayer.swingItem()
                    mc.playerController.onPlayerDestroyBlock(target, EnumFacing.DOWN)
                    target = null
                }
            }

            // Break block
            mc.thePlayer.swingItem()
            currentDamage += block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld!!, target)
            mc.theWorld!!.sendBlockBreakProgress(mc.thePlayer.entityId, target, (currentDamage * 10F).toInt() - 1)

            // End of breaking block
            if (currentDamage >= 1F) {
                mc.netHandler.addToSendQueue(
                    C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                        target,
                        EnumFacing.DOWN
                    )
                )
                mc.playerController.onPlayerDestroyBlock(target, EnumFacing.DOWN)
                currentDamage = 0F
                target = null
            }
        }
    }
}