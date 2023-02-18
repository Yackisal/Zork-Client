package cc.zork.features.render

import cc.zork.event.Render3DEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.AntiBot
import cc.zork.features.combat.Teams
import cc.zork.utils.WorldToScreen
import cc.zork.utils.render.color
import cc.zork.utils.render.drawRect
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.AxisAlignedBB
import cc.zork.features.combat.KillAura
import cc.zork.utils.render.drawBoundingBox
import cc.zork.utils.render.renderBox
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector3f
import java.awt.Color

class ESP : Module("ESP", "See other players cross walls", ModuleType.Render) {
    companion object {
        val mode: ModeValue = ModeValue("ESP", "Frame", "Frame","Box", "2D")
        val invisible = BooleanValue("Invisible", false)
    }

    @Subscribe
    fun onRender(event: Render3DEvent?) {
        if (mode.getCurrent() == "Box") {
            displayName = "Box"
            for (o in mc.theWorld.loadedEntityList) {
                if (o is EntityLivingBase) {
                    if (AntiBot.isBot(o) || (KillAura.targets.contains(o) && KillAura.esp.getCurrent())) continue
                    if (o is EntityPlayer) {
                        val ent = o
                        if (ent != mc.thePlayer && !ent.isDead) {
                            renderBox(ent, 1f, 1f, 1f)
                        }
                    }
                }
            }
        } else if (mode.getCurrent() == "2D") {
            displayName = "2D"
            doOther2DESP()
        } else if (mode.getCurrent() == "Frame") {
            displayName = "frame"
            doWireFrame()
        }
    }

    fun isValid(entity: EntityLivingBase): Boolean {
        return entity !== mc.thePlayer && entity.health > 0.0f && entity is EntityPlayer && !AntiBot.isBot(
            entity
        )
    }

    fun doWireFrame() {
        val mvMatrix = WorldToScreen.getMatrix(GL11.GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL11.GL_PROJECTION_MATRIX)
        for (entity in mc.theWorld.playerEntities) {
            if (entity.isInvisible && !invisible.value) {
                return
            }
            if (isValid(entity)) {
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT)
                GL11.glEnable(GL11.GL_BLEND)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glMatrixMode(GL11.GL_PROJECTION)
                GL11.glPushMatrix()
                GL11.glLoadIdentity()
                GL11.glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
                GL11.glMatrixMode(GL11.GL_MODELVIEW)
                GL11.glPushMatrix()
                GL11.glLoadIdentity()
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GlStateManager.enableTexture2D()
                GL11.glDepthMask(true)
                GL11.glLineWidth(2.0f)

                val renderManager = mc.renderManager
                val timer = mc.timer
                val bb = entity.entityBoundingBox
                    .offset(-entity.posX, -entity.posY, -entity.posZ)
                    .offset(
                        entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks,
                        entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks,
                        entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                    )
                    .offset(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ)
                val boxVertices = arrayOf(
                    doubleArrayOf(bb.minX, bb.minY, bb.minZ),
                    doubleArrayOf(bb.minX, bb.maxY, bb.minZ),
                    doubleArrayOf(bb.maxX, bb.maxY, bb.minZ),
                    doubleArrayOf(bb.maxX, bb.minY, bb.minZ),
                    doubleArrayOf(bb.minX, bb.minY, bb.maxZ),
                    doubleArrayOf(bb.minX, bb.maxY, bb.maxZ),
                    doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ),
                    doubleArrayOf(bb.maxX, bb.minY, bb.maxZ)
                )
                var minX = Float.MAX_VALUE
                var minY = Float.MAX_VALUE
                var maxX = -1f
                var maxY = -1f
                for (boxVertex in boxVertices) {
                    val screenPos = WorldToScreen.worldToScreen(
                        Vector3f(
                            boxVertex[0].toFloat(),
                            boxVertex[1].toFloat(),
                            boxVertex[2].toFloat()
                        ), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight
                    )
                        ?: continue
                    minX = Math.min(screenPos.x, minX)
                    minY = Math.min(screenPos.y, minY)
                    maxX = Math.max(screenPos.x, maxX)
                    maxY = Math.max(screenPos.y, maxY)
                }
                if (minX > 0 || minY > 0 || maxX <= mc.displayWidth || maxY <= mc.displayWidth) {
                    var espColor:Color = if(entity.totalArmorValue * 1 + entity.health * 2 >mc.thePlayer.totalArmorValue + mc.thePlayer.health*2) Color(255,0,0)else Color(255,255,255)
                    if(Teams.isInSameTeam(entity))
                        espColor = Color(50,255,50)
                    color(if (entity.hurtTime == 0) espColor.rgb else Color(255,0,0).rgb)
                    GL11.glBegin(GL11.GL_LINE_LOOP)
                    GL11.glVertex2f(minX, minY)
                    GL11.glVertex2f(minX, maxY)
                    GL11.glVertex2f(maxX, maxY)
                    GL11.glVertex2f(maxX, minY)
                    GL11.glEnd()
                }
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glMatrixMode(GL11.GL_PROJECTION)
                GL11.glPopMatrix()
                GL11.glMatrixMode(GL11.GL_MODELVIEW)
                GL11.glPopMatrix()
                GL11.glPopAttrib()
            }
        }
    }

    private fun doOther2DESP() {
        for (entity in mc.theWorld.playerEntities) {
            if (entity.isInvisible && !invisible.value) {
                return
            }
            if (isValid(entity)) {
                GL11.glPushMatrix()
                GL11.glEnable(3042)
                GL11.glDisable(2929)
                GL11.glNormal3f(0.0f, 1.0f, 0.0f)
                GlStateManager.enableBlend()
                GL11.glBlendFunc(770, 771)
                GL11.glDisable(3553)
                val partialTicks = mc.timer.renderPartialTicks
                val x =
                    entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
                val y =
                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
                val z =
                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ
                var SCALE = 0.035f
                SCALE /= 2.0f
                val xMid = x.toFloat()
                val yMid = y.toFloat() + entity.height + 0.5f - if (entity.isChild) entity.height / 2.0f else 0.0f
                val zMid = z.toFloat()
                GlStateManager.translate(
                    x.toFloat(),
                    y.toFloat() + entity.height + 0.5f - if (entity.isChild) entity.height / 2.0f else 0.0f,
                    z.toFloat()
                )
                GL11.glNormal3f(0.0f, 1.0f, 0.0f)
                GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
                GL11.glScalef(-SCALE, -SCALE, -SCALE)
                val tesselator = Tessellator.getInstance()
                val worldRenderer = tesselator.worldRenderer
                val xLeft = -30.0
                val xRight = 60.0
                val yUp = 15.0
                val yDown = 140.0
                drawRect(xLeft.toFloat(), yUp.toFloat(), xRight.toFloat(), yDown.toFloat(), Color(255,255,255,100).rgb)
                GL11.glEnable(3553)
                GL11.glEnable(2929)
                GlStateManager.disableBlend()
                GL11.glDisable(3042)
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
                GL11.glNormal3f(1.0f, 1.0f, 1.0f)
                GL11.glPopMatrix()
            }
        }
    }


}