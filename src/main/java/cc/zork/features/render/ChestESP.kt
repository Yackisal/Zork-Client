package cc.zork.features.render

import net.minecraft.block.BlockChest
import net.minecraft.block.BlockEnderChest
import net.minecraft.util.BlockPos
import cc.zork.event.Render3DEvent
import cc.zork.event.RenderBlockEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.render.drawSolidBlockESP

class ChestESP : Module("ChestESP", "Shows you where chests are", ModuleType.Render) {
    companion object {
        var list = ArrayList<BlockPos>()
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    @Subscribe
    fun onBlock(e: RenderBlockEvent) {
        val pos = BlockPos(e.x, e.y, e.z)
        if (!list.contains(pos) && (e.block is BlockChest || e.block is BlockEnderChest)) {
            list.add(pos)
        }
    }

    @Subscribe
    fun onRender(e: Render3DEvent) {
        var iterator = list.iterator()
        try {
            while (iterator.hasNext()) {
                val pos = iterator.next()
                if (mc.theWorld.getBlockState(pos).block !is BlockChest && mc.theWorld.getBlockState(pos).block !is BlockEnderChest) list.remove(
                    pos
                )
                drawSolidBlockESP(
                    pos.x - mc.renderManager.renderPosX,
                    pos.y - mc.renderManager.renderPosY,
                    pos.z - mc.renderManager.renderPosZ, 1f, 1f, 1f, 0.2f
                )
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}