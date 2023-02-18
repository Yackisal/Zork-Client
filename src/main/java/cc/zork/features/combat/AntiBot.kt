package cc.zork.features.combat

import cc.zork.Zork
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.ModeValue
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import cc.zork.manager.ModuleManager


class AntiBot : Module("AntiBot", "Prevent hitting the server bot", ModuleType.Combat) {
    companion object {
        var mod = ModeValue("Mode", "Hypixel", "Hypixel", "Basic")
        fun isBot(entity: EntityLivingBase): Boolean {
            if (!ModuleManager.getMod(AntiBot::class.java)!!.stage)
                return false
            when (mod.getCurrent()) {
                "Hypixel" -> {
                    if (entity.displayName.formattedText.equals("§r§8[NPC]") || !inTab(entity))
                        return true
                }
            }
            return false
        }

        private fun inTab(entity: EntityLivingBase): Boolean {
            for (info in Minecraft.getMinecraft().netHandler.playerInfoMap) {
                if (info != null && info.gameProfile != null && info.gameProfile.name.contains(
                        entity.name
                    )
                )
                    return true
            }
            return false
        }
    }
}