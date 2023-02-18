package cc.zork.features.player

import cc.zork.event.Subscribe
import cc.zork.event.TickEvent
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.MovementUtil
import cc.zork.utils.math.TimerUtil
import cc.zork.utils.mc
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import java.util.*

enum class ArmorType {
    BOOTS, LEGGINGS, CHEST_PLATE, HELMET
}

class AutoArmor() : Module("AutoArmor", "Auto armor", ModuleType.Player) {
    private val timer: TimerUtil = TimerUtil()
    private val glitchFixer: TimerUtil = TimerUtil()

    companion object {
        val openInv: BooleanValue = BooleanValue("Sort In Inv", false)
        val noMove: BooleanValue = BooleanValue("No Move", false)
        val delay: NumberValue = NumberValue("Delay", 150.0, 0.0, 1000.0, 50.0)
        val isFull: Boolean
            get() = !listOf<Any?>(mc.thePlayer.inventory.mainInventory).contains(null)
        fun findArmor(armorType: ArmorType, minimum: Float): Int {
            var best = 0f
            var result = -1
            for (i in 0 until mc.thePlayer.inventory.mainInventory.size) {
                val item: ItemStack = mc.thePlayer.inventory.mainInventory[i] ?: continue
                if (getArmorScore(item) < 0 || getArmorScore(item) <= minimum || getArmorScore(item) < best || !isValid(
                        armorType,
                        item
                    )
                ) {
                    continue
                }
                best = getArmorScore(item)
                result = i
            }
            return result
        }

        private fun isValid(type: ArmorType, itemStack: ItemStack): Boolean {
            if (itemStack.item !is ItemArmor) return false
            val armor = itemStack.item as ItemArmor
            if (type == ArmorType.HELMET && armor.armorType == 0) return true
            if (type == ArmorType.CHEST_PLATE && armor.armorType == 1) return true
            return if (type == ArmorType.LEGGINGS && armor.armorType == 2) true else type == ArmorType.BOOTS && armor.armorType == 3
        }

        private fun warmArmor(slot: Int) {
            if (slot in 0..8) { // 0-8 is hotbar
                clickSlot(slot + 36, 0, true)
            } else {
                clickSlot(slot, 0, true)
            }
        }

        private fun clickSlot(slot: Int, mouseButton: Int, shiftClick: Boolean) {
            mc.playerController.windowClick(
                mc.thePlayer.inventoryContainer.windowId, slot, mouseButton,
                if (shiftClick) 1 else 0, mc.thePlayer
            )
        }

        private fun dropArmor(armorSlot: Int) {
            val slot = armorSlotToNormalSlot(armorSlot)
            if (!isFull) {
                clickSlot(slot, 0, true)
            } else {
                mc.playerController.windowClick(
                    mc.thePlayer.inventoryContainer.windowId, slot, 1, 4,
                    mc.thePlayer
                )
            }
        }

        var isDone = false
        fun getArmorScore(itemStack: ItemStack?): Float {
            if (itemStack == null || itemStack.item !is ItemArmor) return (-1).toFloat()
            val itemArmor = itemStack.item as ItemArmor
            var score = 0f

            //basic reduce amount
            score += itemArmor.damageReduceAmount.toFloat()
            if (EnchantmentHelper.getEnchantments(itemStack).isEmpty()) score -= 0.1.toFloat()
            val protection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, itemStack)
            score += (protection * 0.2).toFloat()
            return score
        }

        fun armorSlotToNormalSlot(armorSlot: Int): Int {
            return 8 - armorSlot
        }

    }

    @Subscribe
    fun onTick(event: TickEvent?) {
        displayName = delay.get().toInt().toString()
        if (mc.thePlayer == null) return
        if (noMove.getCurrent() && MovementUtil.isMoving()) return
        if (openInv.getCurrent()) {
            if (mc.currentScreen !is GuiInventory) return
        } else {
            // Glitch Fix
            if (mc.currentScreen != null) glitchFixer.reset()
        }

        var slot: Int
        for (armorType in ArmorType.values()) {
            if (findArmor(
                    armorType,
                    getArmorScore(mc.thePlayer.inventory.armorItemInSlot(armorType.ordinal))
                ).also { slot=it } != -1
            ) {
                // set
                isDone = false
                if (mc.thePlayer.inventory.armorItemInSlot(armorType.ordinal) != null) {
                    dropArmor(armorType.ordinal)
                    timer.reset()
                    return
                }
                warmArmor(slot)
                timer.reset()
                return
            } else {
                isDone = true
            }
        }
    }

}