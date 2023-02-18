package cc.zork.utils.render

import cc.zork.utils.mc
import net.minecraft.client.Minecraft
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.GL11


object StencilUtil {
    fun start() {
        val mc: Minecraft = Minecraft.getMinecraft()
        mc.framebuffer.bindFramebuffer(false)
        if (mc.framebuffer.depthBuffer > -1) {
            setupFBO(mc.framebuffer)
            mc.framebuffer.depthBuffer = -1
        }
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GL11.glColorMask(false, false, false, false)
    }

    fun end() {
        val mc: Minecraft = Minecraft.getMinecraft()
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
        GL11.glColorMask(true, true, true, true)
    }

    fun draw(start: Runnable, end: Runnable) {
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        val mc: Minecraft = Minecraft.getMinecraft()
        mc.framebuffer.bindFramebuffer(false)
        if (mc.framebuffer.depthBuffer > -1) {
            setupFBO(mc.framebuffer)
            mc.framebuffer.depthBuffer = -1
        }
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GL11.glColorMask(false, false, false, false)
        start.run()
        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
        GL11.glColorMask(true, true, true, true)
        end.run()
        GL11.glDisable(GL11.GL_STENCIL_TEST)
    }

    private fun setupFBO(fbo: Framebuffer) {
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        val stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT()
        EXTFramebufferObject.glBindRenderbufferEXT(36161, stencil_depth_buffer_ID)
        EXTFramebufferObject.glRenderbufferStorageEXT(
            36161,
            34041,
            Minecraft.getMinecraft().displayWidth,
            Minecraft.getMinecraft().displayHeight
        )
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencil_depth_buffer_ID)
        EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencil_depth_buffer_ID)
    }

    /*
     * Given to me by igs
     *                    */
    fun checkSetupFBO(framebuffer: Framebuffer?) {
        if (framebuffer != null) {
            if (framebuffer.depthBuffer > -1) {
                setupFBO(framebuffer)
                framebuffer.depthBuffer = -1
            }
        }
    }

    /**
     * @implNote Initializes the Stencil Buffer to write to
     */
    fun initStencilToWrite() {
        //init
        mc.framebuffer.bindFramebuffer(false)
        checkSetupFBO(mc.framebuffer)
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT)
        GL11.glEnable(GL11.GL_STENCIL_TEST)
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1)
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
        GL11.glColorMask(false, false, false, false)
    }

    /**
     * @param ref (usually 1)
     * @implNote Reads the Stencil Buffer and stencils it onto everything until
     * @see StencilUtil.uninitStencilBuffer
     */
    fun readStencilBuffer(ref: Int) {
        GL11.glColorMask(true, true, true, true)
        GL11.glStencilFunc(GL11.GL_EQUAL, ref, 1)
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
    }

    fun uninitStencilBuffer() {
        GL11.glDisable(GL11.GL_STENCIL_TEST)
    }

}