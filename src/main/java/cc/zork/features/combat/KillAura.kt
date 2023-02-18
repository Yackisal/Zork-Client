package cc.zork.features.combat

import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.boss.EntityWither
import net.minecraft.entity.monster.EntityMob
import net.minecraft.entity.monster.EntitySlime
import net.minecraft.entity.passive.EntityAnimal
import net.minecraft.entity.passive.EntitySquid
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import cc.zork.event.*
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.player.Scaffold
import cc.zork.manager.ModuleManager
import cc.zork.utils.client.RotationUtils
import cc.zork.utils.client.RotationUtils.getRotations
import cc.zork.utils.client.notify
import cc.zork.utils.math.TimerUtil
import cc.zork.utils.render.drawEntityBoxESP
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import java.awt.Color

class KillAura : Module("KillAura", "Auto attack all enemies near you", ModuleType.Combat) {
    companion object {
        val mode = ModeValue("Mode", "Switch", "Switch", "Single");
        val order = ModeValue("Order", "Range", "Health", "Armor", "Fov", "Advanced");
        val blockmode = ModeValue("BlockMode", "Advanced", "Advanced", "100%");
        val autoblock = BooleanValue("AutoBlock", true)
        val cps = NumberValue("CPS", 12, 0, 40, 1)
        val range = NumberValue("Range", 4.1, 3.0, 8.0, 0.1)
        val blockRange = NumberValue("BlockRange", 3.0, 1.0, 8.0, 0.1)
        val maxTargets = NumberValue("MaxTargets", 3, 0, 10, 1)
        val switchTime = NumberValue("SwitchTime", 1000, 0, 10000, 100)
        val turnSpeed = NumberValue("TurnSpeed", 70, 0, 100, 1)
        val fov = NumberValue("FOV", 360, 0, 360, 1)
        val player = BooleanValue("Player", true)
        val antiBehindAttack = BooleanValue("AntiBehindAttack", true)
        val mob = BooleanValue("Mob", true)
        val animals = BooleanValue("Animals", false)
        val winterFirst = BooleanValue("WinterFirst", false)
        val targetHUD = BooleanValue("TargetHUD", true)
        val hittable = BooleanValue("HittableCheck", true)
        val lobby = BooleanValue("LobbyCheck", true)
        val esp = BooleanValue("ESP", true)
        var targets = ArrayList<EntityLivingBase>()
        var target: EntityLivingBase? = null
        var cpsTimer = TimerUtil()
    }

    var isBlocking = false
    val behindTimer = TimerUtil()


    @Subscribe
    fun onRender3D(e: Render3DEvent) {
        if (esp.getCurrent() || targets.size > 0) {
            for (entity in targets) {
                val x = (entity.lastTickPosX
                        + (entity.posX - entity.lastTickPosX) * cc.zork.utils.mc.timer.renderPartialTicks
                        - cc.zork.utils.mc.renderManager.viewerPosX)
                val y = (entity.lastTickPosY
                        + (entity.posY - entity.lastTickPosY) * cc.zork.utils.mc.timer.renderPartialTicks
                        - cc.zork.utils.mc.renderManager.viewerPosY)
                val z = (entity.lastTickPosZ
                        + (entity.posZ - entity.lastTickPosZ) * cc.zork.utils.mc.timer.renderPartialTicks
                        - cc.zork.utils.mc.renderManager.viewerPosZ)
                val width = entity.entityBoundingBox.maxX - entity.entityBoundingBox.minX - 0.1
                val height = (entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY
                        + 0.25)
                var color = Color(155, 155, 200, 50)
                if (entity == target) {
                    if (entity.hurtTime != 0) {
                        color = Color(255, 0, 0, 50)
                    } else {
                        color = Color(60, 100, 255, 50)
                    }
                }
                drawEntityBoxESP(x, y, z, width * 0.7, height, color.rgb, 60 / 255f, 100 / 255f, 1f, 0.0f, 1f)
            }
        }
    }


    val switchTimer = TimerUtil()


    @Subscribe
    fun onMotion(e: MotionEvent) {
        displayName = mode.getCurrent()
        // do rotation
        if (e.eventState == EventType.PRE) {
            // get all entities
            targets.clear()
            mc.theWorld.loadedEntityList.forEach {
                if (it is EntityLivingBase)
                    if ((it !is EntityPlayer || !AntiBot.isBot(it)) && (it !is EntityPlayer || !Teams.isInSameTeam(it)) && it.isEntityAlive && it != mc.thePlayer && isValid(
                            it
                        )
                    ) {
                        if (it.getDistanceToEntity(mc.thePlayer) < range.value.toFloat() && targets.size < maxTargets.value.toInt()) {
                            if (RotationUtils.getAngleDifference(
                                    getRotations(it)[0],
                                    mc.thePlayer.rotationYaw
                                ) < fov.value.toFloat()
                            )
                                targets.add(it)
                        }
                    }
            }

            if (order.getCurrent() == "Health") {
                targets.sortedBy { it.health }
            }
            if (order.getCurrent() == "Range") {
                targets.sortedBy { it.getDistanceToEntity(mc.thePlayer) }
            }
            if (order.getCurrent() == "Armor") {
                targets.sortedBy { mc.thePlayer.totalArmorValue }
            }
            if (order.getCurrent() == "Advanced") {
                targets.sortedBy { it.health * 3 + it.totalArmorValue * 2 }
            }
            if (order.getCurrent() == "Fov") {
                targets.sortedBy { getRotations(it)[0] - mc.thePlayer.rotationYaw }
            }
            if (targets.size > 0) {
                target = targets[0]
                if (mode.getCurrent() == "Switch") {
                    var current = targets.indexOf(target)
                    for ((time, t) in targets.withIndex()) {
                        if (hittable.getCurrent() && t.hurtTime != 0) continue
                        if (switchTimer.delay(switchTime.value.toFloat())) {
                            if (time > current) {
                                target = t
                                switchTimer.reset()
                            }
                        }
                        if (t is EntityWither && winterFirst.getCurrent()) {
                            target = t
                        }
                    }
                } else if (mode.getCurrent() == "Single") {
                    for (target1 in targets) {
                        target = target1
                    }
                }
                if (target != null) {
                    if (antiBehindAttack.value) {
                        var behindTargets = 0
                        for (behind in targets) {
                            val rotations = getRotations(target!!)
                            if (RotationUtils.getAngleDifference(rotations[0], mc.thePlayer.rotationYaw % 360) > 190) {
                                behindTargets++
                                if (target!!.getDistanceToEntity(mc.thePlayer) >= 3 && target != behind) {
                                    if (behindTimer.delay(10000f)) {
                                        notify("请留意，你的背后有 $behindTargets 个敌人，已优先攻击")
                                        behindTimer.reset()
                                    }
                                    target = behind
                                }
                            }
                        }
                    }

                    val rotations = getRotations(target!!)

                    if (ModuleManager.getMod(Scaffold::class.java).stage)
                        return
                    e.Yaw = rotations[0]
                    mc.thePlayer.renderYawOffset = rotations[0]
                    e.Pitch = rotations[1]
                    mc.thePlayer.rotationPitchHead = rotations[1]
                    val delay = 1000f / cps.value.toFloat()
                    if (cpsTimer.delay(delay)) {
                        unBlock()
//                    mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))

                        // do attack
                        mc.thePlayer.swingItem()
                        mc.playerController.attackEntity(mc.thePlayer, target)
                        cpsTimer.reset()

                    }
                }
            } else {
                target = null
                if (isBlocking)
                    unBlock()
            }


        } else if (e.eventState == EventType.POST) {
            if (target != null) {
                //do Block
                if (target!!.getDistanceToEntity(mc.thePlayer) < blockRange.get().toFloat()) {
                    doBlock()
                }
            }
        }
    }

    @Subscribe
    fun onLag(e: PacketEvent) {
        if (e.type == EventType.RECEIVE)
            if (e.packet is S08PacketPlayerPosLook) {
                lagTimer.reset()
            }
    }

    override fun onDisable() {
        super.onDisable()
        target = null
        targets.clear()
    }

    private fun isValid(entity: EntityLivingBase): Boolean {
        if (entity is EntityPlayer && player.getCurrent()) {
            return true
        } else if ((entity is EntityMob || entity is EntitySlime) && mob.getCurrent()) {
            return true
        } else if ((entity is EntityAnimal || entity is EntityVillager || entity is EntitySquid) && animals.getCurrent()) {
            return true
        }
        return false
    }

    private fun unBlock() {
        if (isBlocking && mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword && (autoblock.getCurrent() || mc.gameSettings.keyBindUseItem.pressed)) {
            mc.thePlayer.sendQueue.addToSendQueue(
                C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                    BlockPos.ORIGIN,
                    EnumFacing.DOWN
                )
            )
            isBlocking = false
        }
    }

    val lagTimer: TimerUtil = TimerUtil()
    private fun doBlock() {
        var advanced =
            mc.thePlayer.hurtTime < 2 && (target!!.health > mc.thePlayer.health || target!!.totalArmorValue > mc.thePlayer.totalArmorValue) && lagTimer.delay(
                3000f
            )
        if (blockmode.getCurrent() == "100%" || (blockmode.getCurrent() == "Advanced" && advanced)) {
            if (mc.thePlayer.heldItem != null && mc.thePlayer.heldItem.item is ItemSword && (autoblock.getCurrent() || mc.gameSettings.keyBindUseItem.pressed)) {
                mc.netHandler.addToSendQueue(
                    C08PacketPlayerBlockPlacement(
                        BlockPos(-1, -1, -1),
                        255,
                        mc.thePlayer.inventory.getCurrentItem(),
                        0f,
                        0f,
                        0f
                    )
                )
                isBlocking = true
            }
        }
    }
}