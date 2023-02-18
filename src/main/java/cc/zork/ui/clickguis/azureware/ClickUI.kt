package cc.zork.ui.clickguis.azureware

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import cc.zork.features.ModuleType
import cc.zork.ui.clickguis.azureware.components.containers.Frame
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

class ClickUI : GuiScreen() {

    var frames: ArrayList<Frame> = ArrayList()
    var isSelected: Boolean = false

    init {
        var posX: Float = 20f
        ModuleType.values().forEach {
            frames.add(Frame(posX, 20f, 108.5f, this, it))
            posX += 128.5F
        }
    }

    val scaleAnimation = Animation()
    override fun initGui() {
        val mc = Minecraft.getMinecraft()
        scaleAnimation.value = 0.6
        close = false
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for ((i, frame) in frames.withIndex()) {
            if (frame.mouseClicked(mouseX, mouseY, mouseButton)) {
                frames.add(0, frames.removeAt(i))
                return
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (frame in frames) {
            if (frame.mouseReleased(mouseX, mouseY, state)) {
                return
            }
        }

    }

    var close = false
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == 1) {
            close = true
            scaleAnimation.fstart(scaleAnimation.value, 0.1, 0.2f, Type.EASE_IN_OUT_QUAD)
            return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        if (close) {
            if (scaleAnimation.value <= 0.5) {
                mc.displayGuiScreen(null as GuiScreen?)
                if (mc.currentScreen == null) {
                    mc.setIngameFocus()
                }
            }
        } else {
            scaleAnimation.start(scaleAnimation.value, 1.0, 0.3f, Type.EASE_OUT_BACK)
        }
        scaleAnimation.update()

        GL11.glPushMatrix()
        val sr = ScaledResolution(mc)
        GlStateManager.translate(sr.scaledWidth / 2.0, sr.scaledHeight / 2.0, 0.0)
        GL11.glScaled(scaleAnimation.value, scaleAnimation.value, 1.0)
        GlStateManager.translate(-sr.scaledWidth / 2.0, -sr.scaledHeight / 2.0, 0.0)
        var mouseDheel = Mouse.getDWheel();
        frames.reverse()
        frames.forEach {
            it.mouseDWheel(mouseX, mouseY, mouseDheel)
            it.drawScreen(mouseX, mouseY)
        }
        frames.reverse()
        GL11.glPopMatrix()
    }

}