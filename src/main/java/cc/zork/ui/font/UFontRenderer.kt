package cc.zork.ui.font

import cc.zork.features.ui.HUD
import cc.zork.ui.font.ttfr.StringCache
import cc.zork.utils.mc
import cc.zork.utils.render.shader.BloomUtil
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.intToColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.awt.Font

class UFontRenderer(font: Font?, size: Int, antiAlias: Boolean) : FontRenderer(
    Minecraft.getMinecraft().gameSettings,
    ResourceLocation("textures/font/ascii.png"),
    Minecraft.getMinecraft().renderEngine,
    false
) {
    var FONT_HEIGHT = 8
    var stringCache: StringCache? = null
    var colorCode = IntArray(32)

    init {
        val res = ResourceLocation("textures/font/ascii.png")
        for (i in 0..31) {
            val j = (i shr 3 and 1) * 85
            var k = (i shr 2 and 1) * 170 + j
            var l = (i shr 1 and 1) * 170 + j
            var i1 = (i and 1) * 170 + j
            if (i == 6) {
                k += 85
            }
            if (Minecraft.getMinecraft().gameSettings.anaglyph) {
                val j1 = (k * 30 + l * 59 + i1 * 11) / 100
                val k1 = (k * 30 + l * 70) / 100
                val l1 = (k * 30 + i1 * 70) / 100
                k = j1
                l = k1
                i1 = l1
            }
            if (i >= 16) {
                k /= 4
                l /= 4
                i1 /= 4
            }
            this.colorCode[i] = k and 255 shl 16 or (l and 255 shl 8) or (i1 and 255)
        }
        if (res.resourcePath.equals("textures/font/ascii.png", ignoreCase = true) && stringCache == null) {
            stringCache = StringCache(colorCode)
            stringCache!!.setDefaultFont(font, size, antiAlias)
        }
    }

    /**
     * Draws the specified string with a shadow.
     */
    override fun drawStringWithShadow(text: String, x: Float, y: Float, color: Int): Int {
        val color1 = intToColor(color)
        return this.drawString(
            text,
            x,
            y,
            Color(color1!!.red, color1.green, color1.blue, 255).rgb,
            true
        )
    }

    fun drawStringWithShadowA(text: String, x: Float, y: Float, color: Int): Int {
        val color1 = intToColor(color)
        return this.drawString(text, x, y, Color(color1!!.red, color1.green, color1.blue, color1.alpha).rgb, true)
    }

    /**
     * Draws the specified string.
     */
    fun drawString(text: String, x: Float, y: Int, color: Int): Int {
        val color1 = Color(color)
        return this.drawString(text, x, y.toFloat(), Color(color1.red, color1.green, color1.blue).rgb, false)
    }

    fun drawString(text: String, x: Float, y: Float, color: Int): Int {
        this.drawString(text, x, y, color, false)
        return getStringWidth(text)
    }

    fun drawFontShadow(text: String, x: Float, y: Float, color: Int) {
        if (!mc.gameSettings.ofFastRender && HUD.shadow.value && intToColor(color)!!.alpha > 0) {
            RoundedUtil.bloomFramebuffer = RoundedUtil.createFrameBuffer(RoundedUtil.bloomFramebuffer)
            RoundedUtil.bloomFramebuffer!!.framebufferClear()
            RoundedUtil.bloomFramebuffer!!.bindFramebuffer(true)

            this.drawString(text, x, y, -1)
            RoundedUtil.bloomFramebuffer!!.unbindFramebuffer()
            BloomUtil.renderBlur(
                RoundedUtil.bloomFramebuffer!!.framebufferTexture,
                HUD.shadowRadius.get().toInt(),
                HUD.shadowOffset.get().toInt()
            )
        }
        this.drawString(text, x, y, color)
    }

    fun drawFontShadow(text: String, x: Float, y: Float, color: Int, radius: Int, offset: Int) {
        if (!mc.gameSettings.ofFastRender && HUD.shadow.value) {
            RoundedUtil.bloomFramebuffer = RoundedUtil.createFrameBuffer(RoundedUtil.bloomFramebuffer)
            RoundedUtil.bloomFramebuffer!!.framebufferClear()
            RoundedUtil.bloomFramebuffer!!.bindFramebuffer(true)

            this.drawString(text, x, y, -1)
            RoundedUtil.bloomFramebuffer!!.unbindFramebuffer()
            BloomUtil.renderBlur(
                RoundedUtil.bloomFramebuffer!!.framebufferTexture,
                radius,
                offset
            )
        }
        this.drawString(text, x, y, color)
    }

    /**
     * Draws the specified string.
     */
    override fun drawString(text: String, x: Float, y: Float, color: Int, dropShadow: Boolean): Int {
        val i: Int
        GlStateManager.enableBlend()
        i = if (dropShadow) {
            if(intToColor(color)!!.alpha > 50) {
                stringCache!!.renderString(
                    text,
                    x + 1.0f,
                    y + 1.0f,
                    Color(50, 50, 50, Color(color).alpha / 2).rgb,
                    true
                )
            }
            Math.max(0, stringCache!!.renderString(text, x, y, color, false))
        } else {
            stringCache!!.renderString(text, x, y, color, false)
        }
        GlStateManager.disableBlend()
        return i
    }

    override fun getStringWidth(text: String): Int {
        return stringCache!!.getStringWidth(text)
    }

    fun drawCenteredString(text: String?, x: Float, y: Float, color: Int) {
        stringCache!!.renderString(text, x - stringCache!!.getStringWidth(text) / 2f, y, color, false)
    }

    val height: Int
        get() = stringCache!!.height / 2

    override fun trimStringToWidth(text: String, width: Int): String {
        return stringCache!!.trimStringToWidth(text, width, false)
    }

    override fun trimStringToWidth(text: String, width: Int, reverse: Boolean): String {
        return stringCache!!.trimStringToWidth(text, width, reverse)
    }
}