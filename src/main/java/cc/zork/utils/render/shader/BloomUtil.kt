package cc.zork.utils.render.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import java.nio.FloatBuffer

object BloomUtil {
    var gaussianBloom = ShaderUtil("client/shaders/bloom.frag")
    var framebuffer = Framebuffer(1, 1, false)
    private val mc = Minecraft.getMinecraft()
    private fun setAlphaLimit(limit: Float) {
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(GL11.GL_GREATER, (limit * .01).toFloat())
    }
    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }

    fun calculateGaussianValue(x: Float, sigma: Float): Float {
        val PI = 3.141592653
        val output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma))
        return (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma)))).toFloat()
    }

    fun bindTexture(texture: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
    }
    fun renderBlur(sourceTexture: Int, radius: Int, offset: Int) {
        framebuffer = createFrameBuffer(framebuffer)
        GlStateManager.enableAlpha()
        GlStateManager.alphaFunc(516, 0.0f)
        GlStateManager.enableBlend()
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        val weightBuffer = BufferUtils.createFloatBuffer(256)
        for (i in 0..radius) {
            weightBuffer.put(calculateGaussianValue(i.toFloat(), radius.toFloat()))
        }
        weightBuffer.rewind()
        setAlphaLimit(0.0f)
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        gaussianBloom.init()
        setupUniforms(radius, offset, 0, weightBuffer)
        bindTexture(sourceTexture)
        ShaderUtil.drawQuads()
        gaussianBloom.unload()
        framebuffer.unbindFramebuffer()
        mc.framebuffer.bindFramebuffer(true)
        gaussianBloom.init()
        setupUniforms(radius, 0, offset, weightBuffer)
        GL13.glActiveTexture(GL13.GL_TEXTURE16)
        bindTexture(sourceTexture)
        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        bindTexture(framebuffer.framebufferTexture)
        ShaderUtil.drawQuads()
        gaussianBloom.unload()
        GlStateManager.alphaFunc(516, 0.1f)
        GlStateManager.enableAlpha()
        GlStateManager.bindTexture(0)
    }

    fun setupUniforms(radius: Int, directionX: Int, directionY: Int, weights: FloatBuffer?) {
        gaussianBloom.setUniformi("inTexture", 0)
        gaussianBloom.setUniformi("textureToCheck", 16)
        gaussianBloom.setUniformf("radius", radius.toFloat())
        gaussianBloom.setUniformf("texelSize", 1.0f / mc.displayWidth.toFloat(), 1.0f / mc.displayHeight.toFloat())
        gaussianBloom.setUniformf("direction", directionX.toFloat(), directionY.toFloat())
        GL20.glUniform1(gaussianBloom.getUniform("weights"), weights)
    }
}