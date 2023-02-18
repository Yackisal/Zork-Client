package cc.zork.features.render

import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.entity.Render
import net.minecraft.client.renderer.entity.RendererLivingEntity
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import cc.zork.event.EventType
import cc.zork.event.RenderEntityLivingEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import org.lwjgl.opengl.GL11
import java.awt.Color

class Chams : Module("Chams", "Render entities through walls", ModuleType.Render) {
    companion object {
        val rainbow: BooleanValue = BooleanValue("Color", false)
        val flat: BooleanValue = BooleanValue("Flat", false)
        val alpha: NumberValue = NumberValue("Alpha", 0.8, 0, 1, 0.1)
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    @Subscribe
    fun onRenderEntity(event: RenderEntityLivingEvent) {
        if (event.entity is EntityPlayer && event.entity !== mc.thePlayer && event.type == EventType.PRE) {
            if (rainbow.getCurrent()) {
                event.cancelEvent()
                try {
                    val renderer: Render<EntityPlayer> = mc.renderManager.getEntityRenderObject(event.entity)
                    if (mc.renderManager.renderEngine != null && renderer is RendererLivingEntity<*>) {
                        GL11.glPushMatrix()
                        GL11.glDisable(2929)
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                        GL11.glDisable(GL11.GL_TEXTURE_2D)
                        GL11.glEnable(3042)

                        if (flat.getCurrent()) {
                            GlStateManager.disableLighting()
                        }

                        glColor(alpha.get().toFloat(), 255, 255, 255)
                        renderer.doRenderModel(
                            event.entity,
                            event.limbSwing,
                            event.limbSwingAmount,
                            event.ageInTicks,
                            event.rotationYawHead,
                            event.rotationPitch,
                            event.offset
                        )
                        GL11.glEnable(2929)
                        this.glColor(alpha.get().toFloat(), 255, 255, 255)
                        renderer.doRenderModel(
                            event.entity,
                            event.limbSwing,
                            event.limbSwingAmount,
                            event.ageInTicks,
                            event.rotationYawHead,
                            event.rotationPitch,
                            event.offset
                        )
                        GL11.glEnable(3553)
                        GL11.glDisable(3042)
                        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                        if (flat.getCurrent()) {
                            GlStateManager.enableLighting()
                        }
                        GL11.glPopMatrix()
                        renderer.doRenderLayers(
                            event.entity,
                            event.limbSwing,
                            event.limbSwingAmount,
                            mc.timer.renderPartialTicks,
                            event.ageInTicks,
                            event.rotationYawHead,
                            event.rotationPitch,
                            event.offset
                        )
                        GL11.glPopMatrix()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else {
                GL11.glEnable(32823)
                GL11.glPolygonOffset(1.0f, -1100000.0f)
            }
        } else if (!rainbow.getCurrent() && event.entity is EntityPlayer && event.type == EventType.POST) {
            GL11.glDisable(32823)
            GL11.glPolygonOffset(1.0f, 1100000.0f)
        }
    }

    fun glColor(alpha: Float, redRGB: Int, greenRGB: Int, blueRGB: Int) {
        val red = 0.003921569f * redRGB.toFloat()
        val green = 0.003921569f * greenRGB.toFloat()
        val blue = 0.003921569f * blueRGB.toFloat()
        GL11.glColor4f(red, green, blue, alpha)
    }
}