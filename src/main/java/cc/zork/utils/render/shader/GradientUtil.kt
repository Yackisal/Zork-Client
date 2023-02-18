package cc.zork.utils.render.shader

import cc.zork.utils.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.GlStateManager.resetColor
import cc.zork.utils.render.interpolateColorC
import org.lwjgl.opengl.GL11
import java.awt.Color

private val gradientMaskShader = ShaderUtil("client/shaders/gradientMask.frag")
private val gradientShader = ShaderUtil("client/shaders/gradient.frag")
fun drawGradient(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    alpha: Float,
    bottomLeft: Color,
    topLeft: Color,
    bottomRight: Color,
    topRight: Color
) {
    val sr = ScaledResolution(mc)
    resetColor()
    GlStateManager.enableBlend()
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    gradientShader.init()
    gradientShader.setUniformf(
        "location",
        x * sr.scaleFactor,
        Minecraft.getMinecraft().displayHeight - height * sr.scaleFactor - y * sr.scaleFactor
    )
    gradientShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
    gradientShader.setUniformf("alpha", alpha)
    // Bottom Left
    gradientShader.setUniformf("color1", bottomLeft.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f)
    //Top left
    gradientShader.setUniformf("color2", topLeft.red / 255f, topLeft.green / 255f, topLeft.blue / 255f)
    //Bottom Right
    gradientShader.setUniformf("color3", bottomRight.red / 255f, bottomRight.green / 255f, bottomRight.blue / 255f)
    //Top Right
    gradientShader.setUniformf("color4", topRight.red / 255f, topRight.green / 255f, topRight.blue / 255f)

    //Apply the gradient to whatever is put here
    ShaderUtil.drawQuads(x, y, width, height)
    gradientShader.unload()
    GlStateManager.disableBlend()
}

fun drawGradientLR(x: Float, y: Float, width: Float, height: Float, alpha: Float, left: Color, right: Color) {
    drawGradient(x, y, width, height, alpha, left, left, right, right)
}

fun drawGradientTB(x: Float, y: Float, width: Float, height: Float, alpha: Float, top: Color, bottom: Color) {
    drawGradient(x, y, width, height, alpha, bottom, top, bottom, top)
}

fun applyGradientHorizontal(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    alpha: Float,
    left: Color,
    right: Color,
    content: Runnable
) {
    applyGradient(x, y, width, height, alpha, left, left, right, right, content)
}

fun applyGradientVertical(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    alpha: Float,
    top: Color,
    bottom: Color,
    content: Runnable
) {
    applyGradient(x, y, width, height, alpha, bottom, top, bottom, top, content)
}

fun applyGradientCornerRL(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    alpha: Float,
    bottomLeft: Color,
    topRight: Color,
    content: Runnable
) {
    val mixedColor: Color = interpolateColorC(topRight, bottomLeft, .5f)
    applyGradient(x, y, width, height, alpha, bottomLeft, mixedColor, mixedColor, topRight, content)
}

fun applyGradient(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    alpha: Float,
    bottomLeft: Color,
    topLeft: Color,
    bottomRight: Color,
    topRight: Color,
    content: Runnable
) {
    resetColor()
    GlStateManager.enableBlend()
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    gradientMaskShader.init()
    val sr = ScaledResolution(mc)
    gradientMaskShader.setUniformf(
        "location",
        x * sr.scaleFactor,
        Minecraft.getMinecraft().displayHeight - height * sr.scaleFactor - y * sr.scaleFactor
    )
    gradientMaskShader.setUniformf("rectSize", width * sr.scaleFactor, height * sr.scaleFactor)
    gradientMaskShader.setUniformf("alpha", alpha)
    gradientMaskShader.setUniformi("tex", 0)
    // Bottom Left
    gradientMaskShader.setUniformf("color1", bottomLeft.red / 255f, bottomLeft.green / 255f, bottomLeft.blue / 255f)
    //Top left
    gradientMaskShader.setUniformf("color2", topLeft.red / 255f, topLeft.green / 255f, topLeft.blue / 255f)
    //Bottom Right
    gradientMaskShader.setUniformf(
        "color3",
        bottomRight.red / 255f,
        bottomRight.green / 255f,
        bottomRight.blue / 255f
    )
    //Top Right
    gradientMaskShader.setUniformf("color4", topRight.red / 255f, topRight.green / 255f, topRight.blue / 255f)

    //Apply the gradient to whatever is put here
    content.run()
    gradientMaskShader.unload()
    GlStateManager.disableBlend()
}
