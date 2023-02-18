package cc.zork.features.player

import net.minecraft.block.*
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.server.S00PacketKeepAlive
import net.minecraft.stats.StatList
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.MovingObjectPosition
import cc.zork.event.*
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.MovementUtil
import cc.zork.utils.client.RotationUtils
import cc.zork.utils.math.TimerUtil
import cc.zork.utils.player.PlayerUtils
import cc.zork.utils.render.drawSolidBlockESP
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue
import me.konago.nativeobfuscator.Native

@Native
class Scaffold : Module("Scaffold", "Auto Bridge", ModuleType.Player) {
    private var startY: Double = 0.0
    private var slot: Int = 0
    var enumFacing: EnumFacing? = null
    var objectMouseOver: MovingObjectPosition? = null

    //Timer
    var timeHelper: TimerUtil = TimerUtil()
    var istower: Boolean = false
    var items: ItemStack? = null
    var curYaw = 0f
    var curPitch = 0f

    companion object {
        var blackList: List<Block> = listOf(
            Blocks.air,
            Blocks.water,
            Blocks.flowing_water,
            Blocks.lava,
            Blocks.flowing_lava,
            Blocks.enchanting_table,
            Blocks.carpet,
            Blocks.glass_pane,
            Blocks.stained_glass_pane,
            Blocks.iron_bars,
            Blocks.snow_layer,
            Blocks.ice,
            Blocks.packed_ice,
            Blocks.coal_ore,
            Blocks.diamond_ore,
            Blocks.emerald_ore,
            Blocks.chest,
            Blocks.trapped_chest,
            Blocks.torch,
            Blocks.anvil,
            Blocks.trapped_chest,
            Blocks.noteblock,
            Blocks.jukebox,
            Blocks.tnt,
            Blocks.gold_ore,
            Blocks.lapis_ore,
            Blocks.lit_redstone_ore,
            Blocks.quartz_ore,
            Blocks.redstone_ore,
            Blocks.wooden_pressure_plate,
            Blocks.stone_pressure_plate,
            Blocks.light_weighted_pressure_plate,
            Blocks.heavy_weighted_pressure_plate,
            Blocks.stone_button,
            Blocks.wooden_button,
            Blocks.lever,
            Blocks.tallgrass,
            Blocks.tripwire,
            Blocks.tripwire_hook,
            Blocks.rail,
            Blocks.waterlily,
            Blocks.red_flower,
            Blocks.red_mushroom,
            Blocks.brown_mushroom,
            Blocks.vine,
            Blocks.trapdoor,
            Blocks.yellow_flower,
            Blocks.ladder,
            Blocks.furnace,
            Blocks.sand,
            Blocks.gravel,
            Blocks.ender_chest,
            Blocks.cactus,
            Blocks.dispenser,
            Blocks.noteblock,
            Blocks.dropper,
            Blocks.crafting_table,
            Blocks.web,
            Blocks.pumpkin,
            Blocks.sapling,
            Blocks.cobblestone_wall,
            Blocks.oak_fence,
            Blocks.redstone_torch
        )
        val mode: ModeValue = ModeValue("Mode", "Legit", "Legit", "Blatant")
        val rotation: ModeValue = ModeValue("Rotation", "Reverse", "Aim at", "Reverse")
        val delay: NumberValue = NumberValue("Place Delay", 10.0, 0.0, 500.0, 10.0)
        val silent: BooleanValue = BooleanValue("Slient", true)
        val noSwing: BooleanValue = BooleanValue("No Swing", true)


        //MOVEMENT
        val safeWalk: BooleanValue = BooleanValue("Safe Walk", true)
        val onlyGround: BooleanValue = BooleanValue("Only Ground", true)
        val sameY: BooleanValue = BooleanValue("SameY", false)
        val sprint: BooleanValue = BooleanValue("Sprint", false)
        val sprintPacket: BooleanValue = BooleanValue("SprintPacket", false)
        val sneakPacket: BooleanValue = BooleanValue("SneakPacket", false)
        val eagle: BooleanValue = BooleanValue("Eagle", false)
        val jump: BooleanValue = BooleanValue("AutoJump", false)
        val moveSpeed: NumberValue = NumberValue("Move Speed", 1.0, 0.5, 1.4, 0.05)

        //OTHER
        val tower: BooleanValue = BooleanValue("Tower", true)
        val moveTower: BooleanValue = BooleanValue("Move Tower", true)
        val moveTowerSpeed: NumberValue = NumberValue("Move Tower Speed", 0.9, 0.2, 2.0, 0.05)
        val timer: NumberValue = NumberValue("Timer Speed", 1.0, 0.1, 5.0, 0.01)

        val edgePlace: NumberValue = NumberValue("Edge Place", 0.2, 0.0, 0.5, 0.01)


        //ROTATE
        val turnspeed: NumberValue = NumberValue("Rotation Speed", 6.0, 1.0, 6.0, 0.5)
    }


    override fun onDisable() {
        super.onDisable()
        if (last != -1)
            mc.thePlayer.inventory.currentItem = last
        if (mc.thePlayer != null) {
            mc.netHandler.addToSendQueue(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
            mc.gameSettings.keyBindSneak.pressed = false;
            mc.thePlayer.rotationPitchHead = 0f;
        }
    }

    @Subscribe
    fun onPlace(event: BlockClickEvent) {
        if (mode.getCurrent() == "Legit") {
            var placeable = mc.theWorld.getBlockState(
                BlockPos(
                    mc.thePlayer.posX,
                    mc.thePlayer.posY - 1,
                    mc.thePlayer.posZ
                )
            ).block is BlockAir && mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem).item is ItemBlock

            if (objectMouseOver != null && placeable) {
                event.obj = objectMouseOver!!
                event.enumFacing = enumFacing!!

                val blockpos = event.obj.blockPos

                if (mc.theWorld.getBlockState(blockpos).block.material !== Material.air) {
                    if (mc.playerController.onPlayerRightClick(
                            mc.thePlayer,
                            mc.theWorld,
                            mc.thePlayer.currentEquippedItem,
                            blockpos,
                            event.enumFacing,
                            event.obj.hitVec
                        )
                    ) {
                        mc.thePlayer.swingItem()
                    }
                }
            }

            event.cancelEvent()
        }
    }

    @Subscribe
    fun onSafe(e: SafeWalkEvent) {
        if (safeWalk.getCurrent() || mode.getCurrent() == "Legit")
            e.set(mc.thePlayer.onGround || !onlyGround.getCurrent());
    }

    val towerTimer: TimerUtil = TimerUtil()
    val placeTimer: TimerUtil = TimerUtil()


    var blockPos: BlockPos? = null
    var last = -1

    @Subscribe
    fun onRender3D(e: Render3DEvent) {
        if (blockPos != null) {
            var blockPos1 = blockPos!!.add(enumFacing!!.directionVec)
            drawSolidBlockESP(
                blockPos1.x.toDouble() - mc.renderManager.renderPosX,
                blockPos1.y.toDouble() - mc.renderManager.renderPosY,
                blockPos1.z.toDouble() - mc.renderManager.renderPosZ, 25f, 25f, 255f, 0.2f
            )
        }
    }

    var sneak = false

    @Subscribe
    private fun onMotion(event: MotionEvent) {
        if (event.eventState == EventType.PRE) {
            mc.thePlayer.isSprinting = sprint.getCurrent()

            var rot = curYaw
            event.Yaw = rot + 0.217422f
            event.Pitch = 87.82312f
//            event.Pitch = curPitch
            mc.thePlayer.rotationYawHead = rot
            mc.thePlayer.renderYawOffset = rot
            mc.thePlayer.rotationPitchHead = curPitch
            if (istower)
                event.Pitch = 89.283f
        } else {
            val pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ)
            mc.thePlayer.isSprinting = sprint.getCurrent()

            if (eagle.getCurrent())
                if (mc.theWorld.getBlockState(pos).block is BlockAir) {
                    if (!sneak && sprint.getCurrent()) {
                        if (sprintPacket.getCurrent())
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    C0BPacketEntityAction.Action.STOP_SPRINTING
                                )
                            )
                        if (sneakPacket.getCurrent())
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    C0BPacketEntityAction.Action.START_SNEAKING
                                )
                            )
                        sneak = true
                    }

                    mc.gameSettings.keyBindSneak.pressed = true
                } else {
                    if (sneak && sprint.getCurrent()) {
                        if (sneakPacket.getCurrent())
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    C0BPacketEntityAction.Action.STOP_SNEAKING
                                )
                            )

                        if (sprintPacket.getCurrent())
                            mc.thePlayer.sendQueue.addToSendQueue(
                                C0BPacketEntityAction(
                                    mc.thePlayer,
                                    C0BPacketEntityAction.Action.START_SPRINTING
                                )
                            )
                        sneak = false
                    }
                    mc.gameSettings.keyBindSneak.pressed = false

                }

            getBlockData(
                BlockPos(
                    mc.thePlayer.posX, mc.thePlayer.posY - 1.0,
                    mc.thePlayer.posZ
                )
            )

            if (sameY.getCurrent() && !mc.gameSettings.keyBindJump.isKeyDown) {
                getBlockData(BlockPos(mc.thePlayer.posX, startY - 1, mc.thePlayer.posZ))
            } else {
                startY = mc.thePlayer.posY
            }
            mc.timer.timerSpeed = ((timer.get().toInt() + Math.random() / 100).toFloat())
            slot = getBlockSlot()
            if (silent.getCurrent() && mode.getCurrent() != "Legit") {
                if (mc.thePlayer.inventory.currentItem != getBlockSlot()) {
                    if (slot == -1) return
                    mc.thePlayer.sendQueue.addToSendQueue(C09PacketHeldItemChange(getBlockSlot()))
                }
            } else {
                if (slot != -1) {
                    if (last == -1)
                        last = mc.thePlayer.inventory.currentItem
                    mc.thePlayer.inventory.currentItem = getBlockSlot()
                }
            }
            val itemStack = mc.thePlayer.inventory.getStackInSlot(slot)
            if (itemStack != null && itemStack.item is ItemBlock) {
                items = itemStack
                mc.thePlayer.motionX *= moveSpeed.get().toFloat()
                mc.thePlayer.motionZ *= moveSpeed.get().toFloat()

                var clickpos = blockPos!!.add(
                    enumFacing!!.directionVec.x / 2,
                    enumFacing!!.directionVec.y / 2,
                    enumFacing!!.directionVec.z / 2
                )
                val rots: FloatArray = RotationUtils.faceBlock(
                    clickpos,
                    (mc.theWorld.getBlockState(blockPos).block.blockBoundsMaxY - mc.theWorld.getBlockState(blockPos).block.blockBoundsMinY).toFloat() + 0.5f,
                    curYaw,
                    curPitch,
                    turnspeed.get().toInt() * 30f
                )
                curYaw = rots[0]
                curPitch = rots[1]

                if (rotation.getCurrent() == "Reverse") {
                    var rot = 0f
                    if (mc.thePlayer.movementInput.moveForward > 0.0f) {
                        rot = 175.0f
                        if (mc.thePlayer.movementInput.moveStrafe > 0.0f) {
                            rot = -120.0f
                        } else if (mc.thePlayer.movementInput.moveStrafe < 0.0f) {
                            rot = 120.0f
                        }
                    } else if (mc.thePlayer.movementInput.moveForward == 0.0f) {
                        rot = 180.0f
                        if (mc.thePlayer.movementInput.moveStrafe > 0.0f) {
                            rot = -90.0f
                        } else if (mc.thePlayer.movementInput.moveStrafe < 0.0f) {
                            rot = 90.0f
                        }
                    } else if (mc.thePlayer.movementInput.moveForward < 0.0f) {
                        if (mc.thePlayer.movementInput.moveStrafe > 0.0f) {
                            rot = -45.0f
                        } else if (mc.thePlayer.movementInput.moveStrafe < 0.0f) {
                            rot = 45.0f
                        }
                    }
                    rot = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - rot
                    if (RotationUtils.getAngleDifference(event.Yaw, curYaw) < 60) {
                        rot = event.Yaw
                    }
                    curYaw = rot
                }

                val ray =
                    RotationUtils.rayCastedBlock(curYaw, curPitch) ?: return
                if (mode.getCurrent() == "Legit") {
                    objectMouseOver = ray
                }
                if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    if (jump.getCurrent()) {
                        mc.thePlayer.motionY = 0.419999
                    }
                }
                val hitVec = ray.hitVec?:return
                var placeable = false
                var d1 = edgePlace.get().toFloat()
                if (enumFacing!!.directionVec.x != 0) {
                    var d = mc.thePlayer.posX - clickpos.x
                    if (enumFacing!!.directionVec.x > 0 && d > d1) {
                        placeable = true
                    } else if (enumFacing!!.directionVec.x < 0 && d < -d1) {
                        placeable = true
                    }
                } else if (enumFacing!!.directionVec.y != 0) {
                    var d = mc.thePlayer.posY - clickpos.y
                    if (enumFacing!!.directionVec.y > 0 && d > d1) {
                        placeable = true
                    } else if (enumFacing!!.directionVec.y < 0 && d < -d1) {
                        placeable = true
                    }

                } else if (enumFacing!!.directionVec.z != 0) {
                    var d = mc.thePlayer.posZ - clickpos.z
                    if (enumFacing!!.directionVec.z > 0 && d > d1) {
                        placeable = true
                    } else if (enumFacing!!.directionVec.z < 0 && d < -d1) {
                        placeable = true
                    }
                }

                if (placeTimer.delay(delay.get().toFloat()) && placeable && mode.getCurrent() != "Legit") {
                    if (mc.playerController.onPlayerRightClick(
                            mc.thePlayer,
                            mc.theWorld,
                            itemStack,
                            blockPos,
                            enumFacing,
                            hitVec
                        )
                    ) {
                        if (!noSwing.getCurrent()) mc.thePlayer.swingItem() else mc.thePlayer.sendQueue.addToSendQueue(
                            C0APacketAnimation()
                        )
                        timeHelper.reset()
                    }
                    placeTimer.reset()
                }
                // tower
                if (MovementUtil.getJumpEffect() == 0 && tower.getCurrent() && mode.getCurrent() != "Legit") {
                    if (mc.thePlayer.movementInput.jump) { // if Scaffolded to UP
                        if (MovementUtil.isOnGround(0.15) && (moveTower.getCurrent() || !MovementUtil.isMoving())) {
                            if (mc.gameSettings.keyBindJump.isKeyDown) {
                                // different tower mode
                                istower = true
                                mc.thePlayer.motionX *= moveTowerSpeed.get().toFloat()
                                mc.thePlayer.motionZ *= moveTowerSpeed.get().toFloat()
//                                mc.thePlayer!!.isAirBorne = true
//                                mc.thePlayer!!.triggerAchievement(StatList.jumpStat)
                                mc.thePlayer.motionY = 0.419899999
                                towerTimer.reset();
                            }
                        }
                    } else {
                        istower = false
                    }
                } else {
                    istower = false
                }
            }
        }
    }


    fun getBlockSlot(): Int {
        var slot = -1
        for (i in 0..8) {
            val itemStack = mc.thePlayer.inventory.mainInventory[i]
            if (itemStack == null || itemStack.item !is ItemBlock || itemStack.stackSize < 1) continue
            val block = itemStack.item as ItemBlock
            if (blackList.contains(block.block)) continue
            slot = i
            break
        }
        return slot
    }

    private fun isPosSolid(pos: BlockPos): Boolean {
        val block = mc.theWorld.getBlockState(pos).block
        return ((block.material.isSolid || !block.isTranslucent || block.isVisuallyOpaque
                || block is BlockLadder || block is BlockCarpet || block is BlockSnow
                || block is BlockSkull) && !block.material.isLiquid
                && block !is BlockContainer)
    }

    private fun setData(pos: BlockPos, facing: EnumFacing) {
        this.blockPos = pos
        this.enumFacing = facing
    }

    private fun getBlockData(pos: BlockPos) {
        if (this.isPosSolid(pos.add(0, -1, 0))) {
            return setData(pos.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos.add(-1, 0, 0))) {
            return setData(pos.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos.add(1, 0, 0))) {
            return setData(pos.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos.add(0, 0, 1))) {
            return setData(pos.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos.add(0, 0, -1))) {
            return setData(pos.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos1 = pos.add(-1, 0, 0)
        if (this.isPosSolid(pos1.add(0, -1, 0))) {
            return setData(pos1.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos1.add(-1, 0, 0))) {
            return setData(pos1.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos1.add(1, 0, 0))) {
            return setData(pos1.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos1.add(0, 0, 1))) {
            return setData(pos1.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos1.add(0, 0, -1))) {
            return setData(pos1.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos2 = pos.add(1, 0, 0)
        if (this.isPosSolid(pos2.add(0, -1, 0))) {
            return setData(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos2.add(-1, 0, 0))) {
            return setData(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos2.add(1, 0, 0))) {
            return setData(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos2.add(0, 0, 1))) {
            return setData(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos2.add(0, 0, -1))) {
            return setData(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos3 = pos.add(0, 0, 1)
        if (this.isPosSolid(pos3.add(0, -1, 0))) {
            return setData(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos3.add(-1, 0, 0))) {
            return setData(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos3.add(1, 0, 0))) {
            return setData(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos3.add(0, 0, 1))) {
            return setData(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos3.add(0, 0, -1))) {
            return setData(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos4 = pos.add(0, 0, -1)
        if (this.isPosSolid(pos4.add(0, -1, 0))) {
            return setData(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos4.add(-1, 0, 0))) {
            return setData(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos4.add(1, 0, 0))) {
            return setData(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos4.add(0, 0, 1))) {
            return setData(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos4.add(0, 0, -1))) {
            return setData(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos19 = pos.add(-2, 0, 0)
        if (this.isPosSolid(pos1.add(0, -1, 0))) {
            return setData(pos1.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos1.add(-1, 0, 0))) {
            return setData(pos1.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos1.add(1, 0, 0))) {
            return setData(pos1.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos1.add(0, 0, 1))) {
            return setData(pos1.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos1.add(0, 0, -1))) {
            return setData(pos1.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos29 = pos.add(2, 0, 0)
        if (this.isPosSolid(pos2.add(0, -1, 0))) {
            return setData(pos2.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos2.add(-1, 0, 0))) {
            return setData(pos2.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos2.add(1, 0, 0))) {
            return setData(pos2.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos2.add(0, 0, 1))) {
            return setData(pos2.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos2.add(0, 0, -1))) {
            return setData(pos2.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos39 = pos.add(0, 0, 2)
        if (this.isPosSolid(pos3.add(0, -1, 0))) {
            return setData(pos3.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos3.add(-1, 0, 0))) {
            return setData(pos3.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos3.add(1, 0, 0))) {
            return setData(pos3.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos3.add(0, 0, 1))) {
            return setData(pos3.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos3.add(0, 0, -1))) {
            return setData(pos3.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos49 = pos.add(0, 0, -2)
        if (this.isPosSolid(pos4.add(0, -1, 0))) {
            return setData(pos4.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos4.add(-1, 0, 0))) {
            return setData(pos4.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos4.add(1, 0, 0))) {
            return setData(pos4.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos4.add(0, 0, 1))) {
            return setData(pos4.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos4.add(0, 0, -1))) {
            return setData(pos4.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos5 = pos.add(0, -1, 0)
        if (this.isPosSolid(pos5.add(0, -1, 0))) {
            return setData(pos5.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos5.add(-1, 0, 0))) {
            return setData(pos5.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos5.add(1, 0, 0))) {
            return setData(pos5.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos5.add(0, 0, 1))) {
            return setData(pos5.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos5.add(0, 0, -1))) {
            return setData(pos5.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos6 = pos5.add(1, 0, 0)
        if (this.isPosSolid(pos6.add(0, -1, 0))) {
            return setData(pos6.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos6.add(-1, 0, 0))) {
            return setData(pos6.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos6.add(1, 0, 0))) {
            return setData(pos6.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos6.add(0, 0, 1))) {
            return setData(pos6.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos6.add(0, 0, -1))) {
            return setData(pos6.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos7 = pos5.add(-1, 0, 0)
        if (this.isPosSolid(pos7.add(0, -1, 0))) {
            return setData(pos7.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos7.add(-1, 0, 0))) {
            return setData(pos7.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos7.add(1, 0, 0))) {
            return setData(pos7.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos7.add(0, 0, 1))) {
            return setData(pos7.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos7.add(0, 0, -1))) {
            return setData(pos7.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos8 = pos5.add(0, 0, 1)
        if (this.isPosSolid(pos8.add(0, -1, 0))) {
            return setData(pos8.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos8.add(-1, 0, 0))) {
            return setData(pos8.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos8.add(1, 0, 0))) {
            return setData(pos8.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos8.add(0, 0, 1))) {
            return setData(pos8.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos8.add(0, 0, -1))) {
            return setData(pos8.add(0, 0, -1), EnumFacing.SOUTH)
        }
        val pos9 = pos5.add(0, 0, -1)
        if (this.isPosSolid(pos9.add(0, -1, 0))) {
            return setData(pos9.add(0, -1, 0), EnumFacing.UP)
        }
        if (this.isPosSolid(pos9.add(-1, 0, 0))) {
            return setData(pos9.add(-1, 0, 0), EnumFacing.EAST)
        }
        if (this.isPosSolid(pos9.add(1, 0, 0))) {
            return setData(pos9.add(1, 0, 0), EnumFacing.WEST)
        }
        if (this.isPosSolid(pos9.add(0, 0, 1))) {
            return setData(pos9.add(0, 0, 1), EnumFacing.NORTH)
        }
        if (this.isPosSolid(pos9.add(0, 0, -1))) {
            return setData(pos9.add(0, 0, -1), EnumFacing.SOUTH)
        }
    }


    override fun onEnable() {
        super.onEnable()
        this.startY = mc.thePlayer.posY
        if (sprint.getCurrent())
            mc.thePlayer.sendQueue.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.START_SPRINTING
                )
            )
        else
            mc.thePlayer.sendQueue.addToSendQueue(
                C0BPacketEntityAction(
                    mc.thePlayer,
                    C0BPacketEntityAction.Action.STOP_SNEAKING
                )
            )
        last = -1
    }

    @Subscribe
    private fun onPacket(e: PacketEvent) {
        if (e.type == EventType.SEND) {
            if (e.packet is C09PacketHeldItemChange) {
                val C09 = e.packet as C09PacketHeldItemChange
                if (slot != C09.slotId) slot = C09.slotId
            }
        }
    }
}