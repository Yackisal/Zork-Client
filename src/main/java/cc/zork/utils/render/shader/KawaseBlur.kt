package cc.zork.utils.render.shader

import cc.zork.utils.render.shader.GaussianBlur.createFrameBuffer
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer

object KawaseBlur {
    private val mc = Minecraft.getMinecraft()
    var kawaseDown = ShaderUtil("client/shaders/kawaseDown.frag")
    var kawaseUp = ShaderUtil("client/shaders/kawaseUp.frag")
    var framebuffer = Framebuffer(1, 1, false)
    fun setupUniforms(offset: Float) {
        kawaseDown.setUniformf("offset", offset, offset)
        kawaseUp.setUniformf("offset", offset, offset)
    }

    private var currentIterations = 0
    private val framebufferList: MutableList<Framebuffer> = ArrayList()
    private fun initFramebuffers(iterations: Float) {
        for (framebuffer in framebufferList) {
            framebuffer.deleteFramebuffer()
        }
        framebufferList.clear()
        framebufferList.add(createFrameBuffer(framebuffer))
        var i = 1
        while (i <= iterations) {
            val framebuffer = Framebuffer(mc.displayWidth, mc.displayHeight, false)
            //  framebuffer.setFramebufferFilter(GL11.GL_LINEAR);
            framebufferList.add(createFrameBuffer(framebuffer))
            i++
        }
    }

    fun renderBlur(iterations: Int, offset: Int) {
        if (currentIterations != iterations) {
            initFramebuffers(iterations.toFloat())
            currentIterations = iterations
        }
        renderFBO(framebufferList[1], mc.framebuffer.framebufferTexture, kawaseDown, offset.toFloat())

        //Downsample
        for (i in 1 until iterations) {
            renderFBO(framebufferList[i + 1], framebufferList[i].framebufferTexture, kawaseDown, offset.toFloat())
        }

        //Upsample
        for (i in iterations downTo 2) {
            renderFBO(framebufferList[i - 1], framebufferList[i].framebufferTexture, kawaseUp, offset.toFloat())
        }
        mc.framebuffer.bindFramebuffer(true)
        GlStateManager.bindTexture(framebufferList[1].framebufferTexture)
        kawaseUp.init()
        kawaseUp.setUniformf("offset", offset.toFloat(), offset.toFloat())
        kawaseUp.setUniformf("halfpixel", 0.5f / mc.displayWidth, 0.5f / mc.displayHeight)
        kawaseUp.setUniformi("inTexture", 0)
        ShaderUtil.drawQuads()
        kawaseUp.unload()
    }

    private fun renderFBO(framebuffer: Framebuffer, framebufferTexture: Int, shader: ShaderUtil, offset: Float) {
        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(true)
        shader.init()
        GlStateManager.bindTexture(framebufferTexture)
        shader.setUniformf("offset", offset, offset)
        shader.setUniformi("inTexture", 0)
        shader.setUniformf("halfpixel", 0.5f / mc.displayWidth, 0.5f / mc.displayHeight)
        ShaderUtil.drawQuads()
        shader.unload()
        framebuffer.unbindFramebuffer()
    }
}