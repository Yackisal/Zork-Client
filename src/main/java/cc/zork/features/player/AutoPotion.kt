package cc.zork.features.player

import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.potion.Potion
import cc.zork.event.MotionEvent
import cc.zork.event.PacketEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.KillAura
import cc.zork.utils.client.MovementUtil
import cc.zork.utils.math.TimerUtil
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue

class AutoPotion : Module("AutoPotion", "AutoPotion", ModuleType.Combat) {
    var isPotting = false
    private var slot = 0
    private var last = 0
    var healthValue: BooleanValue = BooleanValue("Healthl", true)
    var speedValue: BooleanValue = BooleanValue("Speed", true)
    var noMove: BooleanValue = BooleanValue("NoMove", true)
    var potHealthValue: NumberValue = NumberValue("MinHealth", 15.0, 3.0, 20.0, 1.0)
    var delay: NumberValue = NumberValue("Delay", 1000.0, 0.0, 20000.0, 100.0)
    private val timer: TimerUtil = TimerUtil()
    fun onEnabled() {
        super.onEnable()
        slot = -1
        last = -1
        timer.reset()
        isPotting = false
    }

    fun onDisabled() {
        super.onDisable()
        isPotting = false
    }

    @Subscribe
    fun onPacket(e: PacketEvent) {
        if (e.packet is S08PacketPlayerPosLook) timer.reset()
    }

    @Subscribe
    fun onUpdate(event: MotionEvent) {
        slot = getSlot()
        if (timer.delay(delay.get().toFloat()) && KillAura.target == null && (!noMove.getCurrent() || !MovementUtil.isMoving())) {
            val regenId = Potion.regeneration.getId()
            if (!mc.thePlayer.isPotionActive(regenId) && !isPotting && mc.thePlayer.onGround && healthValue.getCurrent() && mc.thePlayer.getHealth() <= potHealthValue.get()
                    .toInt() && hasPot(
                    regenId
                )
            ) {
                val cum = hasPot(regenId, slot)
                if (cum != -1) swap(cum, slot)
                isPotting = true
                timer.reset()
            }
            val speedId = Potion.moveSpeed.getId()
            if (!mc.thePlayer.isPotionActive(speedId) && !isPotting && mc.thePlayer.onGround && speedValue.getCurrent() && hasPot(
                    speedId
                )
            ) {
                val cum = hasPot(speedId, slot)
                if (cum != -1) swap(cum, slot)
                timer.reset()
                isPotting = true
            }
            if (isPotting) {
                last = mc.thePlayer.inventory.currentItem
                mc.thePlayer.inventory.currentItem = slot
                event.Pitch = 90f
            }
        }
    }

    @Subscribe
    fun onPost(event: MotionEvent?) {
        if (isPotting) {
            if (mc.thePlayer.onGround && mc.thePlayer.inventory.getCurrentItem() != null && mc.playerController.sendUseItem(
                    mc.thePlayer,
                    mc.theWorld,
                    mc.thePlayer.inventory.getCurrentItem()
                )
            ) {
                mc.entityRenderer.itemRenderer.resetEquippedProgress2()
            }
            if (last != -1) mc.thePlayer.inventory.currentItem = last
            isPotting = false
            last = -1
        }
    }

    private fun hasPot(id: Int, targetSlot: Int): Int {
        for (i in 9..44) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                val `is`: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack()
                if (`is`.item is ItemPotion) {
                    val pot = `is`.item as ItemPotion
                    if (pot.getEffects(`is`).isEmpty()) continue
                    val effect = pot.getEffects(`is`)[0]
                    if (effect.potionID == id) {
                        if (ItemPotion.isSplash(`is`.itemDamage) && isBestPot(pot, `is`)) {
                            if (36 + targetSlot != i) return i
                        }
                    }
                }
            }
        }
        return -1
    }

    private fun hasPot(id: Int): Boolean {
        for (i in 9..44) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                val `is`: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack()
                if (`is`.item is ItemPotion) {
                    val pot = `is`.item as ItemPotion
                    if (pot.getEffects(`is`).isEmpty()) continue
                    val effect = pot.getEffects(`is`)[0]
                    if (effect.potionID == id) {
                        if (ItemPotion.isSplash(`is`.itemDamage) && isBestPot(pot, `is`)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    private fun isBestPot(potion: ItemPotion, stack: ItemStack): Boolean {
        if (potion.getEffects(stack) == null || potion.getEffects(stack).size != 1) return false
        val effect = potion.getEffects(stack)[0]
        val potionID = effect.potionID
        val amplifier = effect.amplifier
        val duration = effect.duration
        for (i in 9..44) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                val `is`: ItemStack = mc.thePlayer.inventoryContainer.getSlot(i).getStack()
                if (`is`.item is ItemPotion) {
                    val pot = `is`.item as ItemPotion
                    if (pot.getEffects(`is`) != null) {
                        for (o in pot.getEffects(`is`)) {
                            val id = o.potionID
                            val ampl = o.amplifier
                            val dur = o.duration
                            if (id == potionID && ItemPotion.isSplash(`is`.itemDamage)) {
                                if (ampl > amplifier) {
                                    return false
                                } else if (ampl == amplifier && dur > duration) {
                                    return false
                                }
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    private fun getSlot(): Int {
        var spoofSlot = 8
        for (i in 36..44) {
            if (!mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                spoofSlot = i - 36
                break
            } else if (mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem() is ItemPotion) {
                spoofSlot = i - 36
                break
            }
        }
        return spoofSlot
    }

    private fun swap(slot1: Int, hotbarSlot: Int) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot1, hotbarSlot, 2, mc.thePlayer)
    }
}