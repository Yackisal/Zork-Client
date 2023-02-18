package cc.zork.ui.mainmanu

import cc.zork.Zork
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.*
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import cc.zork.api.MicrosoftLogin
import cc.zork.ui.ComponentBase
import cc.zork.ui.GLSLSandboxShader
import cc.zork.ui.account.GuiLogin
import cc.zork.ui.font.FontManager
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.drawImage
import cc.zork.utils.render.isHovered
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL20
import java.awt.Color

class MainScreen() : GuiScreen() {

    var components: ArrayList<ComponentBase> = ArrayList()
    var componentX: Float = 0F;
    var componentY: Float = 0F;
    var componentWidth: Float = 0F;
    var componentHeight: Float = 0F;

    init {
        initComponent()
    }

    fun initComponent() {
        var container: Container = Container("Logo", "Seticon", 223F, 28F)
        container.bindMouseDownEvent {
            Minecraft.getMinecraft().displayGuiScreen(
                GuiOptions(
                    Minecraft.getMinecraft().currentScreen,
                    Minecraft.getMinecraft().gameSettings
                )
            )
        }
        this.components.add(container)

        var button = MainButton("Multi Players", "MultiPlayers", 223F, 50F)
        button.bindMouseDownEvent {
            Minecraft.getMinecraft().displayGuiScreen(GuiMultiplayer(Minecraft.getMinecraft().currentScreen))
        }
        this.components.add(button)

        button = MainButton("Single Players", "SinglePlayers", 107F, 33F)
        button.bindMouseDownEvent {
            Minecraft.getMinecraft().displayGuiScreen(GuiSelectWorld(Minecraft.getMinecraft().currentScreen))
        }
        this.components.add(button)

        button = MainButton("Hypixel", "Hypixel", 107F, 33F)
        button.bindMouseDownEvent {
            mc.displayGuiScreen(GuiConnecting(this, mc, "mc.hypixel.net", 25565))
        }
        this.components.add(button)
    }


    private var shader: GLSLSandboxShader? = null

    init {
        shader = GLSLSandboxShader("wave.frag")
    }

    private val initTime = System.currentTimeMillis()

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)
        if (Zork.username == "") {
            mc.displayGuiScreen(GuiLogin(this))
            return
        }
        drawDefaultBackground()
        GlStateManager.disableCull()
        val sr = ScaledResolution(mc)
        shader!!.useShader(
            width * 2,
            height * 2,
            mouseX.toFloat(),
            mouseY.toFloat(),
            (System.currentTimeMillis() - initTime) / 1000f
        )
        GL11.glBegin(GL11.GL_QUADS)

        GL11.glVertex2f(-1f, -1f)
        GL11.glVertex2f(-1f, 1f)
        GL11.glVertex2f(1f, 1f)
        GL11.glVertex2f(1f, -1f)

        GL11.glEnd()

        GL20.glUseProgram(0)

        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_ALPHA_TEST)

        var scaledResolution = ScaledResolution(Minecraft.getMinecraft())
        var posHeight: Float = 0F;
        var oldHeight: Float = 0F;
        var isWrap: Boolean = true;

        if (isHovered(5f, 5f, 25f + FontManager.normal.getStringWidth(mc.session.username), 15f, mouseX, mouseY)) {
            RoundedUtil.drawRound(
                5f,
                5f,
                25f + FontManager.normal.getStringWidth(mc.session.username),
                15f,
                2f,
                Color(80, 80, 80)
            )
            if (Mouse.isButtonDown(0)) {
                MicrosoftLogin.login()
            }
        } else {
            RoundedUtil.drawRound(
                5f,
                5f,
                25f + FontManager.normal.getStringWidth(mc.session.username),
                15f,
                2f,
                Color(40, 40, 40)
            )
        }

        drawImage(
            ResourceLocation("client/mainmenu/profile.png"),
            6f,
            6.5f,
            12f,
            12f,
            Color(255, 255, 255)
        )
        FontManager.normal.drawFontShadow(mc.session.username, 20f, 7f, Color(255, 255, 255).rgb)

        componentX = scaledResolution.scaledWidth / 2 - componentWidth / 2
        componentY = scaledResolution.scaledHeight / 2 - componentHeight / 2


        components.forEach {


            if (it.location == "Logo") {
                it.draw(componentX, componentY + oldHeight, mouseX, mouseY)
                isWrap = true;
            } else if (it.location == "MultiPlayers") {
                it.draw(componentX, componentY + oldHeight, mouseX, mouseY)
                isWrap = true;
            } else if (it.location == "SinglePlayers") {
                it.draw(componentX, componentY + oldHeight, mouseX, mouseY)
                isWrap = false;
            } else if (it.location == "Hypixel") {
                it.draw(componentX + 116F, componentY + oldHeight, mouseX, mouseY)
                isWrap = true;
            }


            if (isWrap) {
                posHeight += it.height + 8
                oldHeight += it.height + 8
            }
            if (it.width > componentWidth) {
                componentWidth = it.width
            }
        }
        componentHeight = posHeight




        FontManager.normal.drawString(
            "@Zork Team",
            5F,
            height - FontManager.normal.height - 3F,
            Color(255, 255, 255, 200).rgb
        )
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        components.forEach {
            it.mouseClicked(mouseX, mouseY, mouseButton)
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseReleased(mouseX, mouseY, state)
        components.forEach {
            it.mouseReleased(mouseX, mouseY, state)
        }
    }

    override fun initGui() {
        super.initGui()
    }

    override fun onGuiClosed() {
        super.onGuiClosed()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
    }

    companion object {
        @JvmField
        var verify_crash: Boolean = false
    }

}