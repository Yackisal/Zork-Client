package cc.zork.features.player

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.player.AutoArmor.Companion.findArmor
import cc.zork.features.player.AutoArmor.Companion.getArmorScore
import cc.zork.utils.math.TimerUtil
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import net.minecraft.block.Block
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.inventory.Slot
import net.minecraft.item.*
import net.minecraft.potion.Potion
import net.minecraft.util.EnumChatFormatting
import cc.zork.utils.player.PlayerUtils
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class InvManager() : Module("InvManager", "Manage your inventory", ModuleType.Player) {
    var timer: TimerUtil = TimerUtil()

    @Subscribe
    fun onUpdate(event: MotionEvent?) {
        displayName = delayValue.get().toString()
        if (!startTimerUtil.hasReached(400.0))
            return
        if (lobby.getCurrent() && PlayerUtils.isInLobby())
            this.set(false)
        if (mc.currentScreen is GuiChest || invValue.getCurrent() && mc.currentScreen !is GuiContainer) return
        val realdelay: Double = delayValue.get().toDouble()
        val delay = 20.0.coerceAtLeast(realdelay + ThreadLocalRandom.current().nextDouble(-40.0, 40.0))
        if (timer.hasReached(delay)) {
            invManager(delay)
            timer.reset()
        }
    }


    private fun invManager(delay: Double) {
        var bestSword = -1
        var bestDamage = 1f
        for (k in 0 until mc.thePlayer.inventory.mainInventory.size) {
            val item: ItemStack = mc.thePlayer.inventory.mainInventory[k] ?: continue
            if (item.item !is ItemSword) continue
            val `is` = item.item as ItemSword
            var damage = `is`.damageVsEntity
            damage += (EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, item) * 1.26f
                    + EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, item) * 0.01f)
            if (damage > bestDamage) {
                bestDamage = damage
                bestSword = k
            }
        }
        val swordSlot = 1
        if (bestSword != -1 && bestSword != 0) {
            for (i in 0 until mc.thePlayer.inventoryContainer.inventorySlots.size) {
                val s: Slot = mc.thePlayer.inventoryContainer.inventorySlots.get(i)
                if (!(s.hasStack && s.stack == mc.thePlayer.inventory.mainInventory.get(bestSword))) continue
                val slot = 0
                mc.playerController.windowClick(
                    mc.thePlayer.inventoryContainer.windowId,
                    s.slotNumber,
                    slot,
                    2,
                    mc.thePlayer
                )
                timer.reset()
                return
            }
        }
        moveGapToHotBar()
        moveBlocks()
        for (i in 9..44) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).hasStack) continue
            val `is`: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack()
            if (shouldDrop(`is`, i) && timer.hasReached(delay)) {
                this.drop(i)
                timer.reset()
                break
            }
        }
    }

    fun drop(slot: Int) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 4, mc.thePlayer)
    }

    fun shouldDrop(`is`: ItemStack, k: Int): Boolean {
        val bestSword = swordSlot
        if (`is`.item is ItemSword) {
            if (bestSword != -1 && bestSword != k) {
                return true
            }else if(bestSword == k)
                return false
        }
        val bestPick = pickaxeSlot
        if (`is`.item is ItemPickaxe) {
            if (!toolsValue.getCurrent()) return true
            if (bestPick != -1 && bestPick != k) return true
        }
        val bestAxe = axeSlot
        if (`is`.item is ItemAxe) {
            if (!toolsValue.getCurrent()) return true
            if (bestAxe != -1 && bestAxe != k) return true
        }
        if (`is`.item is ItemArmor) {
            for (armorType in ArmorType.values()) {
                val slot = findArmor(
                    armorType,
                    getArmorScore(mc.thePlayer.inventory.armorItemInSlot(armorType.ordinal))
                )
                return slot != k
            }
        }
        val bestShovel = shovelSlot
        if (isShovel(`is`.item)) {
            if (!toolsValue.getCurrent()) return true
            if (bestShovel != -1 && bestShovel != k) return true
        }
        if (bowValue.getCurrent() && `is`.item is ItemBow)
            return false
        return isBad(`is`)
    }

    fun isBad(item: ItemStack): Boolean {
        return (!(item.item is ItemBlock || item.item is ItemSkull || item.item is ItemSnowball || item.item is ItemEgg
                || item.item is ItemEnderPearl || item.item is ItemFood || item.item is ItemPotion && !isBadPotion(item))
                && !item.displayName.lowercase(Locale.getDefault())
            .contains(EnumChatFormatting.GRAY.toString() + "(right click)"))
    }

    fun isBadPotion(stack: ItemStack?): Boolean {
        if (!(stack != null && stack.item is ItemPotion)) return false
        val potion = stack.item as ItemPotion
        if (!ItemPotion.isSplash(stack.itemDamage)) return false
        for (o in potion.getEffects(stack)) {
            if (o.potionID == Potion.poison.getId() || o.potionID == Potion.harm.getId() || o.potionID == Potion.moveSlowdown.getId() || o.potionID == Potion.weakness.getId()) {
                return true
            }
        }
        return false
    }

    fun moveGapToHotBar() {
        var added = false
        if (emptySlot != -1) {
            for (k in 0 until mc.thePlayer.inventory.mainInventory.size) {
                if (k > 8 && !added) {
                    val itemStack: ItemStack = mc.thePlayer.inventory.mainInventory[k] ?: continue
                    if (!(mc.thePlayer.inventory.getStackInSlot(1) != null && mc.thePlayer.inventory.getStackInSlot(
                            1
                        ).item is ItemAppleGold)
                    ) {
                        if (itemStack.item is ItemAppleGold) {
                            mc.playerController.windowClick(
                                mc.thePlayer.inventoryContainer.windowId,
                                k,
                                1,
                                2,
                                mc.thePlayer
                            )
                            added = true
                        }
                    }
                }
            }
        }
    }

    fun moveBlocks() {
        var added = false
        if (emptySlot == -1) return
        for (k in 0 until mc.thePlayer.inventory.mainInventory.size) {
            if (!(k > 8 && !added)) continue
            val itemStack: ItemStack = mc.thePlayer.inventory.mainInventory.get(k) ?: continue
            if (itemStack.item !is ItemBlock) continue
            shiftClickSlot(k)
            added = true
        }
    }

    val emptySlot: Int
        get() {
            for (k in 0..8) {
                if (mc.thePlayer.inventory.mainInventory.get(k) == null) {
                    return k
                }
            }
            return -1
        }

    private fun getValue(`is`: ItemStack, itemArmor: ItemArmor): Float {
        val types = intArrayOf(
            0, 3, 2, 1
        )
        val type = types[itemArmor.armorType]
        val renderIndexes = intArrayOf(
            0, 1, 3, 4, 2
        )
        val render = renderIndexes[itemArmor.renderIndex]
        var value = ((type + 1) * (render + 1)).toFloat()
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, `is`) * 2.5f
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, `is`) * 1.25f
        value += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, `is`) * 1f
        return value
    }

    val swordSlot: Int
        get() {
            if (mc.thePlayer == null) {
                return -1
            }
            var bestSword = -1
            var bestDamage = 1f
            for (i in 9..44) {
                if (!mc.thePlayer.inventoryContainer.getSlot(i).hasStack) continue
                val item: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack ?: continue
                if (item.item !is ItemSword) continue
                val `is` = item.item as ItemSword
                var damage = `is`.damageVsEntity
                damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, item) * 1.26f
                damage += EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, item) * 0.01f
                if (damage > bestDamage) {
                    bestDamage = damage
                    bestSword = i
                }
            }
            return bestSword
        }
    val pickaxeSlot: Int
        get() {
            if (mc.thePlayer == null) {
                return -1
            }
            var bestSword = -1
            var bestDamage = 1f
            for (i in 9..44) {
                if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue
                val item: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack() ?: continue
                if (item.item !is ItemPickaxe) continue
                val `is` = item.item as ItemPickaxe
                val damage = `is`.getStrVsBlock(item, Block.getBlockById(4))
                if (damage > bestDamage) {
                    bestDamage = damage
                    bestSword = i
                }
            }
            return bestSword
        }
    val axeSlot: Int
        get() {
            if (mc.thePlayer == null) {
                return -1
            }
            var bestSword = -1
            var bestDamage = 1f
            for (i in 9..44) {
                if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue
                val item: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack() ?: continue
                if (item.item !is ItemAxe) continue
                val `is` = item.item as ItemAxe
                val damage = `is`.getStrVsBlock(item, Block.getBlockById(17))
                if (damage > bestDamage) {
                    bestDamage = damage
                    bestSword = i
                }
            }
            return bestSword
        }
    val shovelSlot: Int
        get() {
            if (mc.thePlayer == null) {
                return -1
            }
            var bestSword = -1
            var bestDamage = 1f
            for (i in 9..44) {
                if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) continue
                val item: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack() ?: continue
                if (item.item !is ItemTool) continue
                val itemTool = item.item as ItemTool
                if (!isShovel(itemTool)) continue
                val damage = itemTool.getStrVsBlock(item, Block.getBlockById(3))
                if (damage > bestDamage) {
                    bestDamage = damage
                    bestSword = i
                }
            }
            return bestSword
        }

    fun shiftClickSlot(slotId: Int) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slotId, 0, 1, mc.thePlayer)
    }

    val startTimerUtil = TimerUtil()
    override fun onEnable() {
        timer.reset()
        startTimerUtil.reset()
        super.onEnable()
    }

    override fun onDisable() {
        timer.reset()
        super.onDisable()
    }

    companion object {
        private val toolsValue: BooleanValue = BooleanValue("Keep Tools", true)
        private val bowValue: BooleanValue = BooleanValue("Bows", true)
        private val invValue: BooleanValue = BooleanValue("Inventory Only", false)
        private val lobby: BooleanValue = BooleanValue("Lobby check", false)
        private val delayValue: NumberValue = NumberValue("Delay", 80.0, 10.0, 1000.0, 100.0)
        fun isShovel(item: Item): Boolean {
            val ids = intArrayOf(256, 269, 273, 277, 284)
            for (id in ids) {
                if (Item.getItemById(id) !== item) continue
                return true
            }
            return false
        }
    }
}