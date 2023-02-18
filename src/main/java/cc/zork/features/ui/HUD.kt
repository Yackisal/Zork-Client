package cc.zork.features.ui

import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiChat
import cc.zork.Zork
import cc.zork.element.elements.arraylist.ArrayListElement
import cc.zork.event.Render2DEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.ui.font.FontManager
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import cc.zork.event.Shader2DEvent
import cc.zork.features.combat.KillAura
import cc.zork.manager.EventManager
import cc.zork.utils.client.MovementUtil
import cc.zork.utils.math.Animation
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.StencilUtil
import cc.zork.utils.render.drawRect
import cc.zork.utils.render.shader.BloomUtil
import cc.zork.utils.render.shader.GaussianBlur
import cc.zork.utils.render.shader.KawaseBlur
import cc.zork.utils.render.shader.drawGradient
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.SimpleDateFormat
import java.util.Date

class HUD : Module("HUD", "Head Up Display", ModuleType.Interface) {
    companion object {
        @JvmField
        var noPotionInv: BooleanValue = BooleanValue("No Potions Inv", true)
        val noRender: BooleanValue = BooleanValue("No Renders", false)
        val waterMark: ModeValue = ModeValue("Water Mark", "Text", "Text", "None", "OneTap", "NeverLose")
        val blockCounter: BooleanValue = BooleanValue("BlockCounter", true)
        val targetHUD: BooleanValue = BooleanValue("Target HUD", true)
        val blur: ModeValue = ModeValue("Blur", "Gaussian", "Gaussian", "Kawase", "None")
        val radius: NumberValue = NumberValue("Blur Radius", 10f, 1f, 50f, 1f)
        val iterations: NumberValue = NumberValue("Blur Iterations", 4f, 1f, 15f, 1f)
        val offset: NumberValue = NumberValue("Blur Offset", 3f, 1f, 20f, 1f)
        val shadow: BooleanValue = BooleanValue("Shadow", true)
        val shadowRadius: NumberValue = NumberValue("Shadow Radius", 6f, 1f, 50f, 1f)
        val shadowOffset: NumberValue = NumberValue("Shadow Offset", 2f, -10f, 15f, 1f)
        var arraylist = ArrayListElement()

        fun shader() {
            if (blur.getCurrent() != "None") {
                StencilUtil.initStencilToWrite()
                EventManager.callEvent(Shader2DEvent(0))
                StencilUtil.readStencilBuffer(1)
                when (blur.getCurrent()) {
                    "Gaussian" -> GaussianBlur.renderBlur(radius.get().toFloat())
                    "Kawase" -> KawaseBlur.renderBlur(iterations.get().toInt(), offset.get().toInt())
                }
                StencilUtil.uninitStencilBuffer()
            }


            if (shadow.getCurrent()) {
                RoundedUtil.bloomFramebuffer = RoundedUtil.createFrameBuffer(RoundedUtil.bloomFramebuffer)
                RoundedUtil.bloomFramebuffer!!.framebufferClear()
                RoundedUtil.bloomFramebuffer!!.bindFramebuffer(true)
                EventManager.callEvent(Shader2DEvent(1))
                RoundedUtil.bloomFramebuffer!!.unbindFramebuffer()
                BloomUtil.renderBlur(
                    RoundedUtil.bloomFramebuffer!!.framebufferTexture,
                    shadowRadius.get().toInt(),
                    shadowOffset.get().toInt()
                )
            }
        }
    }

    var alphaAnimation: Animation = Animation()
    var scaleAnimation: Animation = Animation()

    init {
        bind = Keyboard.KEY_H
    }

    var hudTarget: EntityLivingBase? = null
    var animation: Animation = Animation()

    fun drawTargetHUD() {
        var guiOpen = mc.currentScreen is GuiChat
        if (!guiOpen) {
            if (KillAura.target != null) {
                hudTarget = KillAura.target
                alphaAnimation.start(alphaAnimation.value, 1.0, 0.35f, cc.zork.utils.math.Type.EASE_IN_OUT_QUAD)
                if (hudTarget!!.hurtTime == 0) {
                    scaleAnimation.start(scaleAnimation.value, 1.0, 0.15f, cc.zork.utils.math.Type.EASE_OUT_BACK)
                } else {
                    scaleAnimation.start(
                        scaleAnimation.value,
                        1.0 + hudTarget!!.hurtTime / 100f,
                        0.15f,
                        cc.zork.utils.math.Type.EASE_IN_OUT_QUAD
                    )
                }
            } else {
                alphaAnimation.start(alphaAnimation.value, 0.0, 0.35f, cc.zork.utils.math.Type.EASE_IN_OUT_QUAD)
                scaleAnimation.start(scaleAnimation.value, 0.6, 0.35f, cc.zork.utils.math.Type.EASE_IN_OUT_QUAD)
            }
        }

        val sr = ScaledResolution(mc)

        if (guiOpen) {

            hudTarget = mc.thePlayer
        }
        if (hudTarget != null) {
            GL11.glPushMatrix()
            GlStateManager.translate(sr.scaledWidth / 2.0 + 10, sr.scaledHeight / 2.0 + 10, 0.0)
            GL11.glScaled(scaleAnimation.value, scaleAnimation.value, 1.0)
            GlStateManager.translate(-sr.scaledWidth / 2.0 + 10, -sr.scaledHeight / 2.0 + 10, 0.0)
            drawTargetHud(hudTarget as EntityLivingBase)
            GL11.glPopMatrix()
        }


        scaleAnimation.update()
        alphaAnimation.update()
    }

    fun drawTargetHud(entity: EntityLivingBase) {
        when {
            KillAura.targetHUD.getCurrent() -> {
                val sr = ScaledResolution(mc)
                val posX = sr.scaledWidth / 2 + 20f
                val posY = sr.scaledHeight / 2 + 20f

                RoundedUtil.drawRound(
                    posX,
                    posY,
                    120f,
                    40f,
                    5f,
                    Color(21, 23, 23, (255 * alphaAnimation.value.toFloat()).toInt())
                )

                RoundedUtil.drawRound(
                    posX + 9.5f,
                    posY + 9.5f,
                    21f,
                    21f,
                    2f,
                    Color(80, 80, 80, (255 * alphaAnimation.value.toFloat()).toInt())
                )
                if (entity is EntityPlayer) {
                    StencilUtil.initStencilToWrite()
                    RoundedUtil.drawRound(
                        posX + 10,
                        posY + 10,
                        20f,
                        20f,
                        5f,
                        Color(50, 50, 50, (255 * alphaAnimation.value.toFloat()).toInt())
                    )
                    StencilUtil.readStencilBuffer(1)
                    GlStateManager.enableBlend()
                    GlStateManager.resetColor()
                    mc.textureManager.bindTexture((entity as AbstractClientPlayer?)!!.locationSkin)
                    GlStateManager.color(1f, 1f, 1f, alphaAnimation.value.toFloat())
                    Gui.drawScaledCustomSizeModalRect(
                        posX.toInt() + 10,
                        posY.toInt() + 10,
                        8.0f, 8.0f, 8, 8, 20, 20, 64f, 64f
                    )
                    GlStateManager.disableBlend()
                    StencilUtil.uninitStencilBuffer()
                }
                FontManager.small.drawString(
                    entity.name,
                    posX + 40,
                    posY + 10,
                    Color(255, 255, 255, (255 * alphaAnimation.value.toFloat()).toInt()).rgb
                )


                val maxHealth = entity.maxHealth
                animation.start(animation.value, entity.health.toDouble(), 0.2f, cc.zork.utils.math.Type.LINEAR)
                val health = animation.value.toFloat()
                var healthPercent = health / maxHealth
                if (healthPercent > 1.0) {
                    healthPercent = 1.0f
                }

                RoundedUtil.drawRound(
                    posX + 40f,
                    posY + 22f,
                    70f,
                    2f,
                    1f,
                    Color(0, 0, 0, (100 * alphaAnimation.value.toFloat()).toInt())
                )

                RoundedUtil.drawRound(
                    posX + 40f,
                    posY + 22f,
                    70f * healthPercent,
                    2f,
                    1f,
                    Color(83, 121, 255, (255 * alphaAnimation.value.toFloat()).toInt())
                )

                RoundedUtil.drawRound(
                    posX + 40f,
                    posY + 28f,
                    70f,
                    2f,
                    1f,
                    Color(0, 0, 0, (100 * alphaAnimation.value.toFloat()).toInt())
                )

                RoundedUtil.drawRound(
                    posX + 40f,
                    posY + 28f,
                    70f * (entity.totalArmorValue / 20f),
                    2f,
                    1f,
                    Color(255, 255, 255, (255 * alphaAnimation.value.toFloat()).toInt())
                )

                animation.update()
            }
        }
    }

    @Subscribe
    fun onShader(e: Shader2DEvent) {
        if (e.type == 0) {

        } else {
            Zork.notificationManager.draw()
            if (targetHUD.getCurrent())
                drawTargetHUD()
        }
    }


    var rot = 0.0
    @Subscribe
    fun onRender(e: Render2DEvent) {
        val sr = ScaledResolution(mc)

        //Logo
//        drawFullImage(ResourceLocation("client/logo.png"), 5f, 5f, 45f, 50f, 1f)
        if (targetHUD.getCurrent())
            drawTargetHUD()
        val sdf = SimpleDateFormat("hh:mm")
        val normal = FontManager.normal
        when (waterMark.getCurrent()) {
            "Text" -> {
                normal.drawStringWithShadowA(
                    "${Zork.CLIENT_NAME} ${ChatFormatting.WHITE} ${Zork.CLIENT_VERSION}",
                    10f,
                    10f,
                    Color(30, 116, 254).rgb
                )
            }

            "OneTap" -> {
                var text = "OneZork | ${Zork.username} | ${sdf.format(Date())}"
                val width =
                    10 + FontManager.small.getStringWidth(text)
                        .toDouble()
                RoundedUtil.drawRound(5f, 5f, width.toFloat(), 14f, 1f, Color(0, 0, 0, 200))
                drawGradient(
                    5f,
                    5f,
                    width.toFloat(),
                    2f,
                    1f,
                    Color(50, 116, 255),
                    Color(50, 116, 255),
                    Color(50, 200, 200),
                    Color(50, 200, 200)
                )
                FontManager.small.drawString(
                    text,
                    10,
                    8,
                    -1
                )
            }

            "NeverLose" -> {
                var text = "${Zork.username} ${Minecraft.debugFPS}fps ${sdf.format(Date())}"
                val width =
                    10 + normal.getStringWidth(text)
                        .toDouble()
                RoundedUtil.drawRound(5f, 5f, width.toFloat(), 13f, 2f, Color(0, 0, 0, 200))
                FontManager.normal_bold.drawString("ZK", 8.5f, 6.5f, Color(50, 116, 255).rgb)
                FontManager.normal_bold.drawString("ZK", 7.5f, 5.5f, Color(50, 116, 255).rgb)
                FontManager.normal_bold.drawString("ZK", 8, 6, -1)
                FontManager.small.drawString(
                    text,
                    28,
                    6,
                    -1
                )
            }
        }

        val chatY = if (mc.currentScreen is GuiChat) 10f else 0f

        normal.drawStringWithShadowA(
            "X: ${mc.thePlayer.posX.toInt()} Y: ${mc.thePlayer.posY.toInt()} Z: ${mc.thePlayer.posZ.toInt()} Speed: ${MovementUtil.calculateBPS()}",
            5f,
            sr.scaledHeight - normal.height - 5f - chatY,
            -1
        )

        normal.drawStringWithShadowA(
            "Yaw: ${mc.thePlayer.rotationYaw.toInt() % 360} Pitch:${mc.thePlayer.rotationPitch.toInt() % 360}",
            5f,
            sr.scaledHeight - normal.height * 2 - 10f - chatY,
            -1
        )

        val text = "${ChatFormatting.WHITE}${Zork.rank}${ChatFormatting.AQUA} - ${Zork.username}"
        FontManager.normal_bold.drawStringWithShadowA(
            text,
            sr.scaledWidth - FontManager.normal_bold.getStringWidth(text) - 5f,
            sr.scaledHeight - FontManager.normal_bold.height - 5f - chatY,
            Color(255, 255, 255).rgb
        )

        //ArrayLst
        arraylist.draw(sr.scaledWidth.toDouble() - 5.0, 5.0, 0.0, 0.0)

        //notification
        Zork.notificationManager.draw()
    }
}