package cc.zork.features.render

import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.EnumRarity
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper
import cc.zork.event.Render2DEvent
import cc.zork.event.Render3DEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.AntiBot
import cc.zork.features.combat.Teams
import cc.zork.ui.font.FontManager
import cc.zork.utils.render.Colors
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.drawRect
import cc.zork.utils.render.reAlpha
import cc.zork.values.BooleanValue
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import java.awt.Color
import java.util.*
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt

class NameTags : Module("NameTags", "See other players' name tag cross walls", ModuleType.Render) {

    companion object {
        val invisible = BooleanValue("invisible", false)
        val armor = BooleanValue("Armor", false)

    }

    var entityPositions: Map<EntityLivingBase, DoubleArray> = HashMap()

    @Subscribe
    fun onRender3D(event: Render3DEvent) {
        updatePositions()

    }

    @Subscribe
    fun onRender2D(event: Render2DEvent?) {
        val scaledRes = ScaledResolution(mc)
        try {
            for (ent in entityPositions.keys) {
                if (ent !== mc.thePlayer && (invisible.getCurrent() || !ent.isInvisible)) {
                    GlStateManager.pushMatrix()
                    if (ent is EntityPlayer) {
                        val renderPositions: DoubleArray =
                            entityPositions[ent]!!
                        if (renderPositions[3] < 0.0 || renderPositions[3] >= 1.0) {
                            GlStateManager.popMatrix()
                            continue
                        }
                        val font: FontRenderer = FontManager.small
                        GlStateManager.translate(
                            renderPositions[0] / scaledRes.scaleFactor,
                            renderPositions[1] / scaledRes.scaleFactor,
                            0.0
                        )
                        GlStateManager.scale(1f, 1f, 1f)
                        GlStateManager.translate(0.0, -2.5, 0.0)
                        val nowhealth = Math.ceil((ent.getHealth() + ent.getAbsorptionAmount()).toDouble()).toFloat()
                        val str = ent.getName() + "  "+ChatFormatting.GRAY + nowhealth



                        var color: Int = Colors.RED.c
                        var text = ent.getDisplayName().formattedText
                        if(Teams.isInSameTeam(ent)){
                            text = "[Team]"+ent.getDisplayName().formattedText
                        }
                        text = text.replace(
                            (if (text.contains("[") && text.contains("]")) "\u00a77" else "").toRegex(),
                            ""
                        )

                        val allWidth: Float = font.getStringWidth(text) + 14f
                        val maxHealth = ent.getMaxHealth() + ent.getAbsorptionAmount()
                        val healthP = nowhealth / maxHealth
                        for (i in text.indices) {
                            if (text[i] == '\u00a7' && i + 1 < text.length) {
                                val oneMore = text[i + 1].lowercaseChar()
                                val colorCode = "0123456789abcdefklmnorg".indexOf(oneMore)
                                if (colorCode < 16) {
                                    try {
                                        color = reAlpha(mc.fontRendererObj.getColorCode(oneMore), 1f)
                                    } catch (ignored: ArrayIndexOutOfBoundsException) {
                                    }
                                }
                            }
                        }
                        RoundedUtil.drawRound(-allWidth / 2f, -16.0f, allWidth, 16f,2f, Color(0, 0, 0, 140))
                        RoundedUtil.drawRound(-allWidth / 2f, -1.0f, allWidth * min(1f,healthP), 1f,2f, Color(color))
                        font.drawString(
                            text,
                            (-allWidth / 2 + 5.5f).roundToInt(),
                            (-12f).roundToInt(),
                            -1
                        )


                        val armors: Boolean = armor.getCurrent()
                        if (armors) {
                            val itemsToRender: MutableList<ItemStack> = ArrayList()
                            for (i in 0..4) {
                                val stack = ent.getEquipmentInSlot(i)
                                if (stack != null) {
                                    itemsToRender.add(stack)
                                }
                            }
                            var x = -(itemsToRender.size * 9) - 8f
                            for (stack in itemsToRender) {
                                GlStateManager.pushMatrix()
                                RenderHelper.enableGUIStandardItemLighting()
                                GlStateManager.disableAlpha()
                                GlStateManager.clear(256)
                                mc.renderItem.zLevel = -150.0f
                                GlStateManager.disableDepth()
                                RoundedUtil.drawRound(x + 5, -39f, 18f, 18f, 5f, Color(0, 0, 0, 128))
                                fixGlintShit()
                                mc.renderItem.renderItemIntoGUI(stack, (x + 6).toInt(), -38)
                                mc.renderItem.renderItemOverlays(mc.fontRendererObj, stack, (x + 6).toInt(), -38)
                                mc.renderItem.zLevel = 0.0f
                                GlStateManager.enableDepth()
                                GlStateManager.enableAlpha()
                                RenderHelper.disableStandardItemLighting()
                                GlStateManager.popMatrix()
                                x += 20f
                            }
                        }
                    }
                    GlStateManager.popMatrix()
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun fixGlintShit() {
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
        GlStateManager.disableLighting()
        GlStateManager.disableDepth()
        GlStateManager.disableTexture2D()
        GlStateManager.disableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.enableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.enableDepth()
    }

    private fun getColor(level: Int): String {
        if (level == 2) {
            return "\u00a7a"
        } else if (level == 3) {
            return "\u00a73"
        } else if (level == 4) {
            return "\u00a74"
        } else if (level >= 5) {
            return "\u00a76"
        }
        return "\u00a7f"
    }

    private fun drawEnchantTag(text: String, x: Float, y: Float) {
        var x = x
        var y = y
        GlStateManager.pushMatrix()
        GlStateManager.disableDepth()
        x = (x * 1.05).toInt().toFloat()
        y -= 6f
        FontManager.small.drawString(text, x, -44 - y, Colors.WHITE.c)
        GlStateManager.enableDepth()
        GlStateManager.popMatrix()
    }

    private fun updatePositions() {
        entityPositions = HashMap<EntityLivingBase, DoubleArray>()
        val pTicks: Float = mc.timer.renderPartialTicks
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !== mc.thePlayer && entity is EntityPlayer && (!entity.isInvisible() || invisible.getCurrent()) && !AntiBot.isBot(entity)) {
                val x: Double =
                    entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * pTicks - mc.renderManager.renderPosX
                var y: Double =
                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * pTicks - mc.renderManager.renderPosY
                val z: Double =
                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * pTicks - mc.renderManager.renderPosZ
                y += entity.height + 0.25
                if (Objects.requireNonNull(convertTo2D(x, y, z))!![2] >= 0.0 && Objects.requireNonNull(
                        convertTo2D(
                            x,
                            y,
                            z
                        )
                    )!![2] < 1.0
                ) {
                    (entityPositions as HashMap<EntityLivingBase, DoubleArray>)[entity] = doubleArrayOf(
                        Objects.requireNonNull(convertTo2D(x, y, z))!![0],
                        Objects.requireNonNull(convertTo2D(x, y, z))!![1], Math.abs(
                            convertTo2D(x, y + 1.0, z, entity)!![1] - convertTo2D(x, y, z, entity)!![1]
                        ), Objects.requireNonNull(convertTo2D(x, y, z))!![2]
                    )
                }
            }
        }
    }

    private fun convertTo2D(x: Double, y: Double, z: Double, ent: Entity): DoubleArray? {
        val pTicks: Float = mc.timer.renderPartialTicks
        val prevYaw = mc.thePlayer.rotationYaw
        val prevPrevYaw = mc.thePlayer.prevRotationYaw
        val rotations: FloatArray = getRotationFromPosition(
            ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * pTicks,
            ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * pTicks,
            ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * pTicks - 1.6
        )
        mc.renderViewEntity.rotationYaw = rotations[0].also { mc.renderViewEntity.prevRotationYaw = it }
        Minecraft.getMinecraft().entityRenderer.setupCameraTransform(pTicks, 0)
        val convertedPoints = convertTo2D(x, y, z)
        mc.renderViewEntity.rotationYaw = prevYaw
        mc.renderViewEntity.prevRotationYaw = prevPrevYaw
        Minecraft.getMinecraft().entityRenderer.setupCameraTransform(pTicks, 0)
        return convertedPoints
    }

    fun getRotationFromPosition(x: Double, z: Double, y: Double): FloatArray {
        val xDiff = x - Minecraft.getMinecraft().thePlayer.posX
        val zDiff = z - Minecraft.getMinecraft().thePlayer.posZ
        val yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 1.2
        val dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff).toDouble()
        val yaw = (atan2(zDiff, xDiff) * 180.0 / Math.PI).toFloat() - 90.0f
        val pitch = (-atan2(yDiff, dist) * 180.0 / Math.PI).toFloat()
        return floatArrayOf(yaw, pitch)
    }

    private fun convertTo2D(x: Double, y: Double, z: Double): DoubleArray? {
        val screenCoords = BufferUtils.createFloatBuffer(3)
        val viewport = BufferUtils.createIntBuffer(16)
        val modelView = BufferUtils.createFloatBuffer(16)
        val projection = BufferUtils.createFloatBuffer(16)
        GL11.glGetFloat(2982, modelView)
        GL11.glGetFloat(2983, projection)
        GL11.glGetInteger(2978, viewport)
        val result =
            GLU.gluProject(x.toFloat(), y.toFloat(), z.toFloat(), modelView, projection, viewport, screenCoords)
        return if (result) {
            doubleArrayOf(
                screenCoords[0].toDouble(),
                (Display.getHeight() - screenCoords[1]).toDouble(),
                screenCoords[2].toDouble()
            )
        } else null
    }
}