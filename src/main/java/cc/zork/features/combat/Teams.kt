package cc.zork.features.combat

import cc.zork.Zork
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.ui.MCItem
import net.minecraft.client.Minecraft
import net.minecraft.entity.EntityLivingBase
import cc.zork.manager.ModuleManager
import cc.zork.values.BooleanValue

class Teams : Module("Teams", "Make you avoid to attack teammates", ModuleType.Combat) {
    companion object {

        @JvmField
        var noTeamHit = BooleanValue("No Team hits", true)

        fun isInSameTeam(entity: EntityLivingBase): Boolean {
            if (ModuleManager.getMod(Teams::class.java)!!.stage) {
                if (Minecraft.getMinecraft().thePlayer.displayName.unformattedText.startsWith("\u00a7")) {
                    return if (Minecraft.getMinecraft().thePlayer.displayName.unformattedText.length <= 2
                        || entity.displayName.unformattedText.length <= 2
                    ) {
                        false
                    } else (Minecraft.getMinecraft().thePlayer.displayName.unformattedText.substring(0, 2)
                            == entity.displayName.unformattedText.substring(0, 2))
                }
                return false
            } else {
                return false
            }
        }
    }
}