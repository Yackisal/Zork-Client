package cc.zork.utils.render.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20

object GaussianBlur {
    private val mc = Minecraft.getMinecraft()
    var blurShader = ShaderUtil("client/shaders/gaussian.frag")
    var framebuffer = Framebuffer(1, 1, false)
    fun setupUniforms(dir1: Float, dir2: Float, radius: Float) {
        blurShader.setUniformi("textureIn", 0)
        blurShader.setUniformf("texelSize", 1.0f / mc.displayWidth.toFloat(), 1.0f / mc.displayHeight.toFloat())
        blurShader.setUniformf("direction", dir1, dir2)
        blurShader.setUniformf("radius", radius)
        val weightBuffer = BufferUtils.createFloatBuffer(256)
        var i = 0
        while (i <= radius) {
            weightBuffer.put(calculateGaussianValue(i.toFloat(), radius / 2))
            i++
        }
        weightBuffer.rewind()
        GL20.glUniform1(blurShader.getUniform("weights"), weightBuffer)
    }

    fun calculateGaussianValue(x: Float, sigma: Float): Float {
        val PI = 3.141592653
        val output = 1.0 / Math.sqrt(2.0 * PI * (sigma * sigma))
        return (output * Math.exp(-(x * x) / (2.0 * (sigma * sigma)))).toFloat()
    }

    @JvmStatic
    fun createFrameBuffer(framebuffer: Framebuffer?): Framebuffer {
        if (framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, true)
        }
        return framebuffer
    }

    fun bindTexture(texture: Int) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture)
    }

    fun renderBlur(radius: Float) {
        GlStateManager.enableBlend()
        GlStateManager.color(1f, 1f, 1f, 1f)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        framebuffer = createFrameBuffer(framebuffer)
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(1f, 0f, radius)
        bindTexture(mc.framebuffer.framebufferTexture)
        ShaderUtil.drawQuads()
        framebuffer.unbindFramebuffer()
        blurShader.unload()
        mc.framebuffer.bindFramebuffer(true)
        blurShader.init()
        setupUniforms(0f, 1f, radius)
        bindTexture(framebuffer.framebufferTexture)
        ShaderUtil.drawQuads()
        blurShader.unload()
        GlStateManager.resetColor()
        GlStateManager.bindTexture(0)
    }
}