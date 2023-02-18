package cc.zork.features.render

import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockEnderChest
import net.minecraft.block.BlockLiquid
import net.minecraft.block.BlockOre
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import cc.zork.event.*
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.math.TimerUtil
import cc.zork.utils.render.drawSolidBlockESP
import cc.zork.utils.render.drawTracer
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import net.minecraft.block.BlockStone
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.concurrent.thread

class Xray : Module("Xray", "See ores cross walls", ModuleType.Render) {

    companion object {
        var tracers = BooleanValue("Tracers", false)
        var esp = BooleanValue("ESP", true)
        var diamond = BooleanValue("Diamond", true)
        var gold = BooleanValue("Gold", true)
        var iron = BooleanValue("Iron", false)
        var lapis = BooleanValue("Lapis", false)
        var emerald = BooleanValue("Emerald", false)
        var onlyCave = BooleanValue("OnlyCave", true)
        var blatant = BooleanValue("Blatant", false)
        var range = NumberValue("Range", 4, 0, 8, 1)
        var delay = NumberValue("Delay", 150, 0, 1000, 10)
        var list = ArrayList<BlockPos>()
    }

    var ores = mutableListOf<BlockPos>()
    var testedblocks = mutableListOf<BlockPos>()


    override fun onEnable() {
//        Minecraft.getMinecraft()?.renderGlobal?.loadRenderers()
    }

    override fun onDisable() {
//        Minecraft.getMinecraft()?.renderGlobal?.loadRenderers()
        ores.clear()
    }

    val timer = TimerUtil()
    var direction = 0

    @Subscribe
    fun onMotion(e: MotionEvent) {
        if (blatant.getCurrent()) {
            val value = range.value.toInt()
            for (i in -value..value) {
                for (j in -value..value) {
                    for (k in -value..value) {
                        val blockPos = BlockPos(mc.thePlayer.posX + i, mc.thePlayer.posY + j, mc.thePlayer.posZ + k)
                        val block = mc.theWorld.getBlockState(
                            blockPos
                        ).block
                        // auto interact the block
                        if (timer.hasReached(delay.get().toDouble())) {
                            if ((block is BlockStone || block is BlockOre) && !testedblocks.contains(blockPos)) {
                                mc.netHandler.addToSendQueue(
                                    C07PacketPlayerDigging(
                                        C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                                        blockPos,
                                        EnumFacing.UP
                                    )
                                )
                                mc.netHandler.addToSendQueue(
                                    C07PacketPlayerDigging(
                                        C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                        blockPos,
                                        EnumFacing.UP
                                    )
                                )
                                testedblocks.add(blockPos)
                                timer.reset()
                            }
                        }
                    }
                }
            }
        }
    }

    //check if there's any air or liquid around this block
    fun isExpose(pos: BlockPos): Boolean {
        if (!onlyCave.value) return true
        return mc.theWorld.getBlockState(pos.up()).block is BlockAir || mc.theWorld.getBlockState(pos.up()).block is BlockLiquid ||
                mc.theWorld.getBlockState(pos.down()).block is BlockAir || mc.theWorld.getBlockState(pos.down()).block is BlockLiquid ||
                mc.theWorld.getBlockState(pos.north()).block is BlockAir || mc.theWorld.getBlockState(pos.north()).block is BlockLiquid ||
                mc.theWorld.getBlockState(pos.south()).block is BlockAir || mc.theWorld.getBlockState(pos.south()).block is BlockLiquid ||
                mc.theWorld.getBlockState(pos.east()).block is BlockAir || mc.theWorld.getBlockState(pos.east()).block is BlockLiquid ||
                mc.theWorld.getBlockState(pos.west()).block is BlockAir || mc.theWorld.getBlockState(pos.west()).block is BlockLiquid
    }

    fun need(block: Block): Boolean {
        if (diamond.getCurrent() && block == Blocks.diamond_ore) return true
        if (gold.getCurrent() && block == Blocks.gold_ore) return true
        if (iron.getCurrent() && block == Blocks.iron_ore) return true
        if (lapis.getCurrent() && block == Blocks.lapis_ore) return true
        if (emerald.getCurrent() && block == Blocks.emerald_ore) return true
        return false
    }

    @Subscribe
    fun onPacket(e: PacketEvent) {
        if (e.type == EventType.RECEIVE) {
            val packet = e.packet
            if (packet is S23PacketBlockChange) {
                val block = packet.blockState.block
                if (block is BlockOre && need(block)) {
                    if (packet.blockPosition.distanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) < 30) {
                        if (!ores.contains(packet.blockPosition))
                            ores.add(packet.blockPosition)
                    }
                }
            }
            if (packet is S22PacketMultiBlockChange) {
                val block = packet.changedBlocks
                for (blockUpdateData in block) {
                    val block1 = blockUpdateData.blockState
                    if (block1.block is BlockOre && need(block1.block)) {
                        if (blockUpdateData.pos.distanceSq(
                                mc.thePlayer.posX,
                                mc.thePlayer.posY,
                                mc.thePlayer.posZ
                            ) < 30
                        ) {
                            if (!ores.contains(blockUpdateData.pos))
                                ores.add(blockUpdateData.pos)
                        }
                    }
                }
            }
        }
    }

    @Subscribe
    fun onRender3D(e: Render3DEvent) {
        val remove = mutableListOf<BlockPos>()
        ores.forEach { ore ->
            if (mc.theWorld.getBlockState(ore).block is BlockOre) {
                var color = Color(0, 0, 0)
                when {
                    mc.theWorld.getBlockState(ore).block.equals(Blocks.coal_ore) -> {
                        color = Color(80, 80, 80)
                    }

                    mc.theWorld.getBlockState(ore).block.equals(Blocks.diamond_ore) -> {
                        color = Color(20, 100, 255)
                    }

                    mc.theWorld.getBlockState(ore).block.equals(Blocks.emerald_ore) -> {
                        color = Color(50, 255, 50)
                    }

                    mc.theWorld.getBlockState(ore).block.equals(Blocks.gold_ore) -> {
                        color = Color(255, 190, 0)
                    }

                    mc.theWorld.getBlockState(ore).block.equals(Blocks.iron_ore) -> {
                        color = Color(255, 150, 255)
                    }
                }
                if (tracers.getCurrent())
                    drawTracer(Vec3(ore).addVector(0.5, 0.5, 0.5), color.rgb)
                if (esp.getCurrent())
                    drawSolidBlockESP(
                        ore.x - mc.renderManager.renderPosX,
                        ore.y - mc.renderManager.renderPosY,
                        ore.z - mc.renderManager.renderPosZ,
                        color.red / 255f,
                        color.green / 255f,
                        color.blue / 255f,
                        0.2f
                    )
            } else {
                remove.add(ore)
            }
        }

//        testedblocks.forEach { blc ->
//                drawSolidBlockESP(
//                    blc.x - mc.renderManager.renderPosX,
//                    blc.y - mc.renderManager.renderPosY,
//                    blc.z - mc.renderManager.renderPosZ,
//                    1f,
//                    1f,
//                    1f,
//                    0.2f
//                )
//        }

        if(testedblocks.size>400){
            testedblocks.removeAt(0)
        }


        if (remove.size > 0) {
            ores.removeAll(remove)
            remove.clear()
        }
    }

}