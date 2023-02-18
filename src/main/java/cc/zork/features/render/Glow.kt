package cc.zork.features.render

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.optifine.shaders.config.ShaderParser.getColorIndex
import cc.zork.Zork
import cc.zork.event.Render2DEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.AntiBot
import cc.zork.utils.render.shader.GlowShader
import cc.zork.values.BooleanValue
import org.lwjgl.opengl.GL11
import java.awt.Color

class Glow : Module("Glow", "Render entities through walls", ModuleType.Render) {
    val hexColors = IntArray(16)

    init {
        repeat(16) { i ->
            val baseColor = (i shr 3 and 1) * 85

            val red = (i shr 2 and 1) * 170 + baseColor + if (i == 6) 85 else 0
            val green = (i shr 1 and 1) * 170 + baseColor
            val blue = (i and 1) * 170 + baseColor

            hexColors[i] = red and 255 shl 16 or (green and 255 shl 8) or (blue and 255)
        }
    }

    companion object {
        val teamColor = BooleanValue("TeamColor", true);
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    @Subscribe
    fun onRender2D(event: Render2DEvent) {
        var partialTicks = event.partialTicks;
        val shader = GlowShader.GLOW_SHADER
        GL11.glPushMatrix()
        try {
            //search entities
            val entityMap = HashMap<Color, ArrayList<Entity>>()
            for (entity in mc.theWorld!!.loadedEntityList) {
                if (entity !is EntityLivingBase) continue
                if (AntiBot.isBot(entity)) continue
                //can draw
                val color = getColor(entity)
                if (!entityMap.containsKey(color)) {
                    entityMap[color] = ArrayList()
                }
                entityMap[color]!!.add(entity)
            }
            //then draw it
            for ((color, arr) in entityMap) {
                shader.startDraw(partialTicks)
                for (entity in arr) {
                    if (entity is EntityPlayer) {
                        if (entity == mc.thePlayer && mc.gameSettings.thirdPersonView == 0) continue
                        mc.renderManager.renderEntityStatic(entity, partialTicks, false)
                    }
                }
                shader.stopDraw(color, 3f, 0.7f)
            }
        } catch (ex: Exception) {
            Zork.logger.error("An error occurred while rendering all entities for shader esp", ex)
        }
//        shader.stopDraw(Color(255,255,255), 2.3f, 1f)

        GL11.glPopMatrix()
    }

    fun getColor(entity: Entity?): Color {
        if (entity != null && entity is EntityLivingBase) {
            if (entity.hurtTime > 0)
                return Color.RED

            if (teamColor.getCurrent()) {
                val chars: CharArray = entity.displayName.formattedText.toCharArray()
                var color = Int.MAX_VALUE
                for (i in chars.indices) {
                    if (chars[i] != '§' || i + 1 >= chars.size) continue
                    val index = getColorIndex(chars[i + 1].toString())
                    if (index < 0 || index > 15) continue
                    color = hexColors[index]
                    break
                }
                return Color(color)
            } else {
                if (entity is EntityPlayer)
                    return Color.WHITE
            }
        }

        return Color(255, 255, 255)
    }
}