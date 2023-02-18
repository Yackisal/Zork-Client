package cc.zork.utils.player

import net.minecraft.client.Minecraft
import net.minecraft.init.Items

object PlayerUtils {
    val mc = Minecraft.getMinecraft()

    @JvmStatic
    fun isInLobby(): Boolean {
        // check if the player is in Hypixel lobby
        var item = mc.thePlayer.inventory.getStackInSlot(0) ?: return false
        var item1 = mc.thePlayer.inventory.getStackInSlot(
            8
        )?:return false
        return item.unlocalizedName.equals("compass") && item1.unlocalizedName.equals("nether_star")
    }
}