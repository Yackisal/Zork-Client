package cc.zork.features.ui

import com.mojang.realmsclient.gui.ChatFormatting
import cc.zork.element.elements.bloodbar.ArmorBar
import cc.zork.element.elements.bloodbar.HealthBar
import cc.zork.element.elements.bloodbar.SatBar
import cc.zork.event.Render2DEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.util.ResourceLocation
import cc.zork.utils.render.*
import java.awt.Color

class MCItem() {
    var animation = Animation()
    var current = false

    fun setState(state: Boolean) {
        current = true
        if (state) {
            if (animation.value != 100.0 && !animation.started)
                animation.fstart(animation.value, 100.0, 0.2f, Type.EASE_IN_OUT_QUAD)
        } else {
            if (animation.value != 0.0 && !animation.started)
                animation.fstart(animation.value, 0.0, 0.2f, Type.EASE_IN_OUT_QUAD)
        }
    }
}

class BetterHotbar : Module("BetterHotBar", "New Look of the hotbar", ModuleType.Interface) {
    val health = Animation()
    val armor = Animation()
    var items = ArrayList<MCItem>()
    var healthBar = HealthBar()
    var satBar = SatBar()
    var armorBar = ArmorBar()

    init {
        for (i in 0..8) {
            items.add(MCItem())
        }
    }

    @Subscribe
    fun onRender(e: Render2DEvent) {
        val sr = ScaledResolution(mc)

        if (mc.renderViewEntity is EntityPlayer) {
            //Info
            healthBar.draw((sr.scaledWidth / 2f - 125).toDouble(), (sr.scaledHeight - 78).toDouble(), 0.0, 0.0)
            satBar.draw((sr.scaledWidth / 2f + 15).toDouble(), (sr.scaledHeight - 78).toDouble(), 0.0, 0.0)
            armorBar.draw((sr.scaledWidth / 2f - 125).toDouble(), (sr.scaledHeight - 100).toDouble(), 0.0, 0.0)
            GlStateManager.pushMatrix()
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
            val entityplayer = mc.renderViewEntity as EntityPlayer
            for (j in 0..8) {
                val k = sr.scaledWidth / 2 - 120 + j * 30
                val l: Int = sr.scaledHeight - 30
                val mcItem = items[j]

                drawFullImage(
                    ResourceLocation("client/textures/itemshadow.png"),
                    k.toFloat() - 14f,
                    l.toFloat() + 4 - 20f,
                    38f,
                    38f,
                    0.7f
                )
                RoundedUtil.drawRound(
                    k.toFloat() - 8f + mcItem.animation.value.toFloat() / 20f,
                    l + 24f,
                    14 * (1 + (mcItem.animation.value / 400.0).toFloat()),
                    2f,
                    1f,
                    Color(255, 255, 255, (255 * mcItem.animation.value / 100.0).toInt())
                )
                val stackInSlot = mc.thePlayer.inventory.getStackInSlot(j)
                mcItem.animation.update()
                if (mc.thePlayer.inventory.currentItem == j) {
                    mcItem.setState(true)
                } else {
                    mcItem.setState(false)
                }

                if (stackInSlot != null) {
                    var subTitle = ""
                    var alpha = 100
                    subTitle = "Misc"
                    var item = stackInSlot.item
                    when (item) {
                        is ItemFood -> {
                            subTitle = "${ChatFormatting.AQUA}+ ${(item as ItemFood).getHealAmount(stackInSlot)} Food"
                            alpha = 255
                        }

                        is ItemSword -> {
                            var damage = item.damageVsEntity + 4.0
                            damage += EnchantmentHelper.getEnchantmentLevel(
                                Enchantment.sharpness.effectId,
                                stackInSlot
                            ) * 1.25
                            subTitle = "${ChatFormatting.AQUA}+ ${1.0.coerceAtLeast(damage)} Damage"
                            alpha = 255
                        }

                        is ItemBlock -> {
                            subTitle = "building"
                            alpha = 100
                        }

                        is ItemTool -> {
                            subTitle = "Tool"
                            alpha = 100
                        }
                    }
                    val d = mcItem.animation.value / 400.0
                    val i = FontManager.normal_bold.getStringWidth(stackInSlot.displayName) / 2
                    val subTitleWidth = FontManager.small.getStringWidth(subTitle) / 2
                    GlStateManager.pushMatrix()
                    GlStateManager.enableRescaleNormal()
                    GlStateManager.translate(k.toFloat(), l.toFloat(), 0f)
                    GlStateManager.scale(d.toFloat()*4, d.toFloat()*4, 1f)
                    GlStateManager.translate(-k.toFloat(), -l.toFloat(), 0f)
                    FontManager.normal_bold.drawStringWithShadowA(
                        stackInSlot.displayName,
                        k + (mcItem.animation.value / 20f).toFloat() - i,
                        l.toFloat() - 30,
                        Color(
                            255, 255, 255,
                            (255 * (mcItem.animation.value / 100.0)).toInt()
                        ).rgb
                    )

                    FontManager.small.drawStringWithShadowA(
                        subTitle,
                        k + (mcItem.animation.value / 20f).toFloat() - subTitleWidth,
                        l.toFloat() - 18,
                        Color(
                            255, 255, 255,
                            (alpha * (mcItem.animation.value / 100.0)).toInt()
                        ).rgb
                    )
                    GlStateManager.popMatrix()


                    GlStateManager.pushMatrix()
                    GlStateManager.enableRescaleNormal()
//                    GlStateManager.enableBlend()
//                    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
                    RenderHelper.enableGUIStandardItemLighting()
                    GlStateManager.translate(k.toFloat(), l.toFloat(), 0f)
                    GlStateManager.scale(1 + d.toFloat(), 1 + d.toFloat(), 1f)
                    GlStateManager.translate(-k.toFloat(), -l.toFloat(), 0f)
                    this.renderHotbarItem(j, k - 4, l - 4, e.partialTicks, entityplayer)
                    RenderHelper.disableStandardItemLighting()
                    GlStateManager.popMatrix()
                }
            }
            GlStateManager.popMatrix()
        }


        //health


    }


    private fun renderHotbarItem(index: Int, xPos: Int, yPos: Int, partialTicks: Float, player: EntityPlayer) {
        val itemstack = player.inventory.mainInventory[index]
        if (itemstack != null) {
            val f = itemstack.animationsToGo.toFloat() - partialTicks
            if (f > 0.0f) {
                GlStateManager.pushMatrix()
                val f1 = 1.0f + f / 5.0f
                GlStateManager.translate((xPos + 8).toFloat(), (yPos + 12).toFloat(), 0.0f)
                GlStateManager.scale(1.0f / f1, (f1 + 1.0f) / 2.0f, 1.0f)
                GlStateManager.translate((-(xPos + 8)).toFloat(), (-(yPos + 12)).toFloat(), 0.0f)
            }
            mc.renderItem.renderItemAndEffectIntoGUI(itemstack, xPos, yPos)
            if (f > 0.0f) {
                GlStateManager.popMatrix()
            }
            mc.renderItem.renderItemOverlays(mc.fontRendererObj, itemstack, xPos, yPos)
        }
    }

}