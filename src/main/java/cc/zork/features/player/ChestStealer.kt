package cc.zork.features.player

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.math.TimerUtil
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.*
import net.minecraft.potion.Potion
import net.minecraft.potion.PotionEffect
import cc.zork.features.combat.KillAura
import cc.zork.utils.player.PlayerUtils
import org.apache.commons.lang3.ArrayUtils
import java.util.*

class ChestStealer : Module("ChestStealer", "Auto steal items in container", ModuleType.Player) {
    companion object {

        val trashValue: BooleanValue = BooleanValue("Trash", false)
        val delayValue: NumberValue = NumberValue("Delay", 80.0, 10.0, 1000.0, 10.0)
        var itemHelmet: IntArray = intArrayOf(298, 302, 306, 310, 314)
        var itemChestplate: IntArray= intArrayOf(299, 303, 307, 311, 315)
        var itemLeggings: IntArray= intArrayOf(300, 304, 308, 312, 316)
        var itemBoots: IntArray= intArrayOf(301, 305, 309, 313, 317)
        var timer: TimerUtil = TimerUtil()
        var noGui: BooleanValue = BooleanValue("NoGui", false)
        var lobby: BooleanValue = BooleanValue("LobbyCheck", true)
    }

    fun doesContain(arr: IntArray?, targetValue: Int): Boolean {
        return ArrayUtils.contains(arr, targetValue)
    }

    @Subscribe
    fun onUpdate(event: MotionEvent?) {
        if(KillAura.lobby.getCurrent() && PlayerUtils.isInLobby())
            this.set(false)
        displayName = delayValue.get().toInt().toString()
        if (mc.thePlayer.openContainer is ContainerChest) {
            val c = mc.thePlayer.openContainer as ContainerChest
            if (c.lowerChestInventory.displayName.unformattedTextForChat.lowercase(Locale.getDefault())
                    .contains("menu")
            ) return
            if (isChestEmpty(c)) mc.thePlayer.closeScreen()
            for (i in 0 until c.lowerChestInventory.sizeInventory) {
                if (c.lowerChestInventory.getStackInSlot(i) != null) {
                    if (timer.delay(
                            delayValue.get().toFloat() + Random().nextInt(100)
                        ) && (itemIsUseful(c, i) || trashValue.getCurrent())
                    ) {
                        if (Random().nextInt(100) > 80) continue
                        mc.playerController.windowClick(c.windowId, i, 0, 1, mc.thePlayer)
                        timer.reset()
                    }
                }
            }
        }
    }

    private fun isChestEmpty(c: ContainerChest): Boolean {
        for (i in 0 until c.lowerChestInventory.sizeInventory) {
            if (c.lowerChestInventory.getStackInSlot(i) != null) {
                if (itemIsUseful(c, i) || trashValue.getCurrent()) return false
            }
        }
        return true
    }

    fun isPotionNegative(itemStack: ItemStack): Boolean {
        val potion = itemStack.item as ItemPotion
        val potionEffectList = potion.getEffects(itemStack)
        return potionEffectList.stream().map { potionEffect: PotionEffect ->
            Potion.potionTypes[potionEffect.potionID]
        }
            .anyMatch { obj: Potion -> obj.isBadEffect }
    }

    private fun itemIsUseful(c: ContainerChest, i: Int): Boolean {
        val itemStack = c.lowerChestInventory.getStackInSlot(i)
        val item = itemStack.item
        if (item is ItemAxe || item is ItemPickaxe) {
            return true
        }
        if (item is ItemFood) return true
        if (item is ItemEnderPearl) return true
        if (item is ItemPotion && !isPotionNegative(itemStack)) return true
        if (item is ItemSword) return true
        return if (item is ItemArmor && isBestArmor(c, itemStack)) true else item is ItemBlock
    }

    private fun isBestArmor(c: ContainerChest, item: ItemStack): Boolean {
        val itempro1 = (item.item as ItemArmor).damageReduceAmount.toFloat()
        var itempro2 = 0f
        if (doesContain(
                itemHelmet,
                Item.getIdFromItem(item.item)
            )
        ) { // 头盔
            for (i in 0..44) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).hasStack && doesContain(
                        itemHelmet,
                        Item.getIdFromItem(mc.thePlayer.inventoryContainer.getSlot(i).stack.item)
                    )
                ) {
                    val temppro = (mc.thePlayer.inventoryContainer.getSlot(i).stack
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
            for (i in 0 until c.lowerChestInventory.sizeInventory) {
                if (c.lowerChestInventory.getStackInSlot(i) != null && doesContain(
                        itemHelmet,
                        Item.getIdFromItem(c.lowerChestInventory.getStackInSlot(i).item)
                    )
                ) {
                    val temppro = (c.lowerChestInventory.getStackInSlot(i)
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
        }
        if (doesContain(
                itemChestplate,
                Item.getIdFromItem(item.item)
            )
        ) { // 胸甲
            for (i in 0..44) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).hasStack && doesContain(
                        itemChestplate,
                        Item.getIdFromItem(mc.thePlayer.inventoryContainer.getSlot(i).stack.item)
                    )
                ) {
                    val temppro = (mc.thePlayer.inventoryContainer.getSlot(i).stack
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
            for (i in 0 until c.lowerChestInventory.sizeInventory) {
                if (c.lowerChestInventory.getStackInSlot(i) != null && doesContain(
                        itemChestplate,
                        Item.getIdFromItem(c.lowerChestInventory.getStackInSlot(i).item)
                    )
                ) {
                    val temppro = (c.lowerChestInventory.getStackInSlot(i)
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
        }
        if (doesContain(
                itemLeggings,
                Item.getIdFromItem(item.item)
            )
        ) { // 腿子
            for (i in 0..44) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).hasStack && doesContain(
                        itemLeggings,
                        Item.getIdFromItem(mc.thePlayer.inventoryContainer.getSlot(i).stack.item)
                    )
                ) {
                    val temppro = (mc.thePlayer.inventoryContainer.getSlot(i).stack
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
            for (i in 0 until c.lowerChestInventory.sizeInventory) {
                if (c.lowerChestInventory.getStackInSlot(i) != null && doesContain(
                        itemLeggings,
                        Item.getIdFromItem(c.lowerChestInventory.getStackInSlot(i).item)
                    )
                ) {
                    val temppro = (c.lowerChestInventory.getStackInSlot(i)
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
        }
        if (doesContain(
                itemBoots,
                Item.getIdFromItem(item.item)
            )
        ) { // 鞋子
            for (i in 0..44) {
                if (mc.thePlayer.inventoryContainer.getSlot(i).hasStack && doesContain(
                        itemBoots,
                        Item.getIdFromItem(mc.thePlayer.inventoryContainer.getSlot(i).stack.item)
                    )
                ) {
                    val temppro = (mc.thePlayer.inventoryContainer.getSlot(i).stack
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
            for (i in 0 until c.lowerChestInventory.sizeInventory) {
                if (c.lowerChestInventory.getStackInSlot(i) != null && doesContain(
                        itemBoots,
                        Item.getIdFromItem(c.lowerChestInventory.getStackInSlot(i).item)
                    )
                ) {
                    val temppro = (c.lowerChestInventory.getStackInSlot(i)
                        .item as ItemArmor).damageReduceAmount.toFloat()
                    if (temppro > itempro2) itempro2 = temppro
                }
            }
        }
        return itempro1 == itempro2
    }

}