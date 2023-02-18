package cc.zork.utils.render

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import cc.zork.features.render.ESP
import cc.zork.utils.mc
import org.lwjgl.opengl.GL11
import java.awt.Color


fun reAlpha(color: Int, alpha: Float): Int {
    val a = (color shr 24 and 255).toFloat()
    val r = (color shr 16 and 255).toFloat()
    val g = (color shr 8 and 255).toFloat()
    val b = (color and 255).toFloat()
    return (a * alpha).toInt() shl 24 or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
}

fun color(color: Int) {
    val a = (color shr 24 and 255).toFloat() / 255.0f
    val r = (color shr 16 and 255).toFloat() / 255.0f
    val g = (color shr 8 and 255).toFloat() / 255.0f
    val b = (color and 255).toFloat() / 255.0f
    GlStateManager.color(r, g, b, a)
}

fun scaleStart(x: Float, y: Float, scale: Float) {
    GlStateManager.pushMatrix()
    GlStateManager.translate(x, y, 0f)
    GL11.glScalef(scale, scale, 1f)
    GlStateManager.translate(-x, -y, 0f)
}

fun scaleEnd() {
    GlStateManager.popMatrix()
}


fun drawRect(x: Double, y: Double, width: Double, height: Double, color: Int) {
    resetColor()
    setup2DRendering {
        render(GL11.GL_QUADS) {
            color(color)
            GL11.glVertex2d(x, y)
            GL11.glVertex2d(x, y + height)
            GL11.glVertex2d(x + width, y + height)
            GL11.glVertex2d(x + width, y)
        }
    }
}

fun drawRect(x: Float, y: Float, width: Float, height: Float, color: Int) {
    resetColor()
    setup2DRendering {
        render(GL11.GL_QUADS) {
            color(color)
            GL11.glVertex2d(x.toDouble(), y.toDouble())
            GL11.glVertex2d(x.toDouble(), (y + height).toDouble())
            GL11.glVertex2d((x + width).toDouble(), (y + height).toDouble())
            GL11.glVertex2d((x + width).toDouble(), y.toDouble())
        }
    }
}

fun drawImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, alpha: Float) {
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    Minecraft.getMinecraft().textureManager.bindTexture(location)
    GlStateManager.color(1F, 1F, 1F, alpha)
    drawModalRectWithCustomSizedTexture(
        x,
        y,
        0F,
        0F,
        width,
        height,
        width,
        height
    )
    GlStateManager.disableBlend()
}

fun drawImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, color: Color) {
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    Minecraft.getMinecraft().textureManager.bindTexture(location)
    color(color.rgb)
    drawModalRectWithCustomSizedTexture(
        x,
        y,
        0F,
        0F,
        width,
        height,
        width,
        height
    )
    GlStateManager.disableBlend()
}

fun drawImage(location: ResourceLocation, x: Float, y: Float, width: Float, height: Float, color: Int) {
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    Minecraft.getMinecraft().textureManager.bindTexture(location)
    color(color)
    drawModalRectWithCustomSizedTexture(
        x,
        y,
        0F,
        0F,
        width,
        height,
        width,
        height
    )
    GlStateManager.disableBlend()
}

fun drawModalRectWithCustomSizedTexture(
    x: Float,
    y: Float,
    u: Float,
    v: Float,
    width: Float,
    height: Float,
    textureWidth: Float,
    textureHeight: Float
) {
    val f = 1.0f / textureWidth
    val f1 = 1.0f / textureHeight
    val tessellator = Tessellator.getInstance()
    val worldrenderer = tessellator.worldRenderer
    worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
    worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
        .tex((u * f).toDouble(), ((v + height.toFloat()) * f1).toDouble()).endVertex()
    worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
        .tex(((u + width.toFloat()) * f).toDouble(), ((v + height.toFloat()) * f1).toDouble()).endVertex()
    worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
        .tex(((u + width.toFloat()) * f).toDouble(), (v * f1).toDouble()).endVertex()
    worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
    tessellator.draw()
}

fun drawFullImage(image: ResourceLocation?, x: Float, y: Float, width: Float, height: Float, alpha: Float) {
    GlStateManager.disableAlpha()
    GlStateManager.enableAlpha()
    GlStateManager.disableBlend()
    GL11.glPushMatrix()
    GlStateManager.enableBlend()
    GlStateManager.disableAlpha()
    GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)
    Minecraft.getMinecraft().textureManager.bindTexture(image)
    GlStateManager.color(1.0f, 1.0f, 1.0f, alpha)
    Gui.drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width, height)
    GlStateManager.disableBlend()
    GlStateManager.enableAlpha()
    GL11.glPopMatrix()
}

fun drawFullImage(image: ResourceLocation?, x: Float, y: Float, width: Float, height: Float, color: Int) {
    GlStateManager.disableAlpha()
    GlStateManager.enableAlpha()
    GlStateManager.disableBlend()
    GL11.glPushMatrix()
    GlStateManager.enableBlend()
    GlStateManager.disableAlpha()
    color(color)
    Minecraft.getMinecraft().textureManager.bindTexture(image)
    color(color)
    Gui.drawModalRectWithCustomSizedTexture(x.toInt(), y.toInt(), 0f, 0f, width.toInt(), height.toInt(), width, height)
    GlStateManager.disableBlend()
    GlStateManager.enableAlpha()
    GL11.glPopMatrix()
}

fun isHovered(x: Float, y: Float, width: Float, height: Float, mouseX: Int, mouseY: Int): Boolean {
    return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height
}

fun doScissor(x: Float, y: Float, width: Float, height: Float) {
    val sr = ScaledResolution(mc)
    val scale = sr.scaleFactor.toDouble()
    val finalHeight = height * scale
    val finalY = (sr.scaledHeight - y) * scale
    val finalX = x * scale
    val finalWidth = width * scale
    GL11.glScissor(finalX.toInt(), (finalY - finalHeight).toInt(), finalWidth.toInt(), finalHeight.toInt())
}


fun drawSolidBlockESP(x: Double, y: Double, z: Double, red: Float, green: Float, blue: Float, alpha: Float) {
    GL11.glPushMatrix()
    GL11.glEnable(3042)
    GL11.glBlendFunc(770, 771)
    GL11.glDisable(3553)
    GL11.glEnable(2848)
    GL11.glDisable(2929)
    GL11.glDepthMask(false)
    GL11.glColor4f(red, green, blue, alpha)
    drawBoundingBox(AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0))
    GL11.glDisable(2848)
    GL11.glEnable(3553)
    GL11.glEnable(2929)
    GL11.glDepthMask(true)
    GL11.glDisable(3042)
    GL11.glPopMatrix()
}

fun drawBoundingBox(aa: AxisAlignedBB) {
    val tessellator = Tessellator.getInstance()
    val worldRenderer = tessellator.worldRenderer
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(7, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    tessellator.draw()
}

fun renderBox(entity: Entity, r: Float, g: Float, b: Float) {
    if (entity.isInvisible && !ESP.invisible.value) {
        return
    }
    val x = (entity.lastTickPosX
            + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
            - mc.renderManager.viewerPosX)
    val y = (entity.lastTickPosY
            + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
            - mc.renderManager.viewerPosY)
    val z = (entity.lastTickPosZ
            + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
            - mc.renderManager.viewerPosZ)
    val width = entity.entityBoundingBox.maxX - entity.entityBoundingBox.minX - 0.1
    val height = (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY
            + 0.25)
    drawEntityBoxESP(
        x, y, z, width, height, Color(255, 255, 255, 50).rgb, r,
        g, b, 0f, 1f
    )
}

fun drawOutlinedBoundingBox(aa: AxisAlignedBB) {
    val tessellator = Tessellator.getInstance()
    val worldRenderer = tessellator.worldRenderer
    worldRenderer.begin(3, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(3, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    tessellator.draw()
    worldRenderer.begin(1, DefaultVertexFormats.POSITION)
    worldRenderer.pos(aa.minX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.minZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.maxX, aa.maxY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.minY, aa.maxZ).endVertex()
    worldRenderer.pos(aa.minX, aa.maxY, aa.maxZ).endVertex()
    tessellator.draw()
}

fun drawEntityBoxESP(
    x: Double,
    y: Double,
    z: Double,
    width: Double,
    height: Double,
    color: Int,
    lineRed: Float,
    lineGreen: Float,
    lineBlue: Float,
    lineAlpha: Float,
    lineWdith: Float
) {
    drawRect(0f, 0f, 0f, 0f, -1)
    GL11.glPushMatrix()
    GL11.glEnable(3042)
    GL11.glBlendFunc(770, 771)
    GL11.glDisable(3553)
    GL11.glEnable(2848)
    GL11.glDisable(2929)
    GL11.glDepthMask(false)
    color(color)
    drawBoundingBox(
        AxisAlignedBB(
            x - width,
            y,
            z - width,
            x + width,
            y + height,
            z + width
        )
    )
    GL11.glLineWidth(lineWdith)
    GL11.glColor4f(lineRed, lineGreen, lineBlue, lineAlpha)
    drawOutlinedBoundingBox(
        AxisAlignedBB(
            x - width,
            y,
            z - width,
            x + width,
            y + height,
            z + width
        )
    )
    GL11.glDisable(2848)
    GL11.glEnable(3553)
    GL11.glEnable(2929)
    GL11.glDepthMask(true)
    GL11.glDisable(3042)
    GL11.glPopMatrix()
}

fun drawTracer(position: Vec3, color: Int) {
    val posX =
        position.xCoord - mc.renderManager.renderPosX
    val posY =
        position.yCoord - mc.renderManager.renderPosX
    val posZ =
        position.zCoord - mc.renderManager.renderPosX

    val mc = Minecraft.getMinecraft()
    val old = mc.gameSettings.viewBobbing
    startDrawing()
    mc.gameSettings.viewBobbing = false
    mc.entityRenderer.setupCameraTransform(mc.timer.renderPartialTicks, 2)
    mc.gameSettings.viewBobbing = old
    drawLine(color, posX, posY, posZ)
    stopDrawing()
}

private fun drawLine(color: Int, x: Double, y: Double, z: Double) {
    val mc = Minecraft.getMinecraft()
    GL11.glEnable(2848)
    GL11.glColor3f(
        (color shr 16 and 255).toFloat() / 255f,
        (color shr 8 and 255).toFloat() / 255f,
        (color and 255).toFloat() / 255f
    )
    GL11.glLineWidth(1.0f)
    GL11.glBegin(1)
    GL11.glVertex3d(0.0, mc.thePlayer.eyeHeight.toDouble(), 0.0)
    GL11.glVertex3d(x, y, z)
    GL11.glEnd()
    GL11.glDisable(2848)
}

fun startDrawing() {
    GL11.glEnable(3042)
    GL11.glEnable(3042)
    GL11.glBlendFunc(770, 771)
    GL11.glEnable(2848)
    GL11.glDisable(3553)
    GL11.glDisable(2929)
    Minecraft.getMinecraft().entityRenderer.setupCameraTransform(
        Minecraft.getMinecraft().timer.renderPartialTicks,
        0
    )
}

fun stopDrawing() {
    GL11.glDisable(3042)
    GL11.glEnable(3553)
    GL11.glDisable(2848)
    GL11.glDisable(3042)
    GL11.glEnable(2929)
}