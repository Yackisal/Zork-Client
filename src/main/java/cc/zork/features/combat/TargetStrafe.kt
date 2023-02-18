package cc.zork.features.combat

import cc.zork.event.*
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.utils.client.RotationUtils
import cc.zork.utils.player.Rotation
import cc.zork.values.BooleanValue
import cc.zork.values.NumberValue
import net.minecraft.block.BlockAir
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.server.S07PacketRespawn
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class TargetStrafe : Module("TargetStrafe", "strafe to targe", ModuleType.Combat) {
    companion object {
        val hurt = BooleanValue("When Hurt", true)
        val behind = BooleanValue("Behind", false)
        val render = BooleanValue("Render", false)
        val sides = NumberValue("Sides", 30, 3, 360, 1)
        val range = NumberValue("Range", 2, 0, 5, 0.1)
        val onlySpeed = BooleanValue("Only Speed", false)
        val autoThirdPerson = BooleanValue("Auto third person", false)
        val directionKey = BooleanValue("DirectionKey", true)
        val noVoid = BooleanValue("Anti Void", true)
    }

    private var strafe = -1.0


    fun canStrafe(): Boolean {
        val press = !directionKey.getCurrent() || mc.gameSettings.keyBindJump.isKeyDown
        return KillAura.target != null && (!hurt.getCurrent() || mc.thePlayer.hurtTime > 0) && press
    }

    override fun onEnable() {
        super.onEnable()
    }

    @Subscribe
    private fun onChangeWorld(event: PacketEvent) {
        if (event.packet is S07PacketRespawn) strafe = -1.0
    }

    @Subscribe
    private fun onRender3D(event: Render3DEvent) {
        if (canStrafe() && render.getCurrent()) {
            val target = KillAura.target
            esp(target, event.partialTicks, range.get().toDouble())
        }
    }

    fun esp(entity: Entity?, partialTicks: Float, rad: Double) {
        val points = sides.get().toInt()
        GlStateManager.enableDepth()
        var il = 0.0
        GL11.glPushMatrix()
        GL11.glDisable(3553)
        GL11.glEnable(2848)
        GL11.glEnable(2881)
        GL11.glEnable(2832)
        GL11.glEnable(3042)
        GL11.glBlendFunc(770, 771)
        GL11.glHint(3154, 4354)
        GL11.glHint(3155, 4354)
        GL11.glHint(3153, 4354)
        GL11.glDisable(2929)
        GL11.glLineWidth(3.5f)
        GL11.glBegin(3)
        val x =
            entity!!.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
        val y =
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ
        val pix2 = 6.283185307179586
        val speed = 5000f
        var baseHue = (System.currentTimeMillis() % speed.toInt()).toFloat()
        while (baseHue > speed) {
            baseHue -= speed
        }
        baseHue /= speed
        for (i in 0..sides.get().toInt()) {
            val max = (i.toFloat() + (il * 8).toFloat()) / points
            var hue = max + baseHue
            while (hue > 1) {
                hue -= 1f
            }
            val r = 0.003921569f * Color(Color.HSBtoRGB(hue, 0.75f, 1f)).red
            val g = 0.003921569f * Color(Color.HSBtoRGB(hue, 0.75f, 1f)).green
            val b = 0.003921569f * Color(Color.HSBtoRGB(hue, 0.75f, 1f)).blue
            val color = Color.WHITE.rgb
            GL11.glColor3f(r, g, b)
            GL11.glVertex3d(x + rad * Math.cos(i * pix2 / points), y + il, z + rad * Math.sin(i * pix2 / points))
        }
        GL11.glEnd()
        GL11.glDepthMask(true)
        GL11.glEnable(2929)
        GL11.glDisable(2848)
        GL11.glDisable(2881)
        GL11.glEnable(2832)
        GL11.glEnable(3553)
        GL11.glPopMatrix()
        GlStateManager.color(255f, 255f, 255f)
    }

    fun preStrafing(event: MotionEvent?, target: EntityLivingBase?, moveSpeed: Double): Boolean {
        if (event!!.eventState != EventType.PRE) return false
        if (!canStrafe()) return false
        if (!stage || target == null || moveSpeed == 0.0) return true
        if (autoThirdPerson.getCurrent())
            mc.gameSettings.thirdPersonView = 1
        var aroundVoid = false
        for (x in -1..0) for (z in -1..0) if (isVoid(x, z)) aroundVoid = true
        var yaw: Float = getRotationFromEyeHasPrev(target).getYaw()
        val behindTarget: Boolean = RotationUtils.getAngleDifference(
            MathHelper.wrapAngleTo180_float(yaw),
            MathHelper.wrapAngleTo180_float(target.rotationYaw)
        ) <= 10
        if (mc.thePlayer.isCollidedHorizontally || aroundVoid && noVoid.getCurrent() || behindTarget && behind.getCurrent()) strafe *= -1
        var targetStrafe: Float =
            if (directionKey.getCurrent() && mc.thePlayer.moveStrafing != 0f) mc.thePlayer.moveStrafing * strafe.toFloat() else strafe.toFloat()
        if (isAboveVoid()) targetStrafe = 0f
        val rotAssist = 45f / getEnemyDistance(target)
        val moveAssist = (45f / getStrafeDistance(target)).toDouble()
        var mathStrafe = 0f
        if (targetStrafe > 0) {
            if ((target.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY || target.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && getEnemyDistance(
                    target
                ) < range.get().toFloat()
            ) yaw += -rotAssist.toFloat()
            mathStrafe += -moveAssist.toFloat()
        } else if (targetStrafe < 0) {
            if ((target.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY || target.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && getEnemyDistance(
                    target
                ) < range.get().toFloat()
            ) yaw += rotAssist.toFloat()
            mathStrafe += moveAssist.toFloat()
        }
        val doSomeMath = doubleArrayOf(
            cos(Math.toRadians((yaw + 90f + mathStrafe).toDouble())),
            sin(Math.toRadians((yaw + 90f + mathStrafe).toDouble()))
        )
        val asLast = doubleArrayOf(
            moveSpeed * doSomeMath[0],
            moveSpeed * doSomeMath[1]
        )
        event.X = asLast[0].also { mc.thePlayer.motionX = it }
        event.Z = asLast[1].also { mc.thePlayer.motionZ = it }
        return false
    }


    fun getRotationFromEyeHasPrev(target: EntityLivingBase): Rotation {
        val x = target.prevPosX + (target.posX - target.prevPosX)
        val y = target.prevPosY + (target.posY - target.prevPosY)
        val z = target.prevPosZ + (target.posZ - target.prevPosZ)
        return getRotationFromEyeHasPrev(x, y, z)
    }

    fun getRotationFromEyeHasPrev(x: Double, y: Double, z: Double): Rotation {
        val xDiff = x - (mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX))
        val yDiff =
            y - (mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) + (mc.thePlayer.entityBoundingBox.maxY - mc.thePlayer.entityBoundingBox.minY))
        val zDiff = z - (mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ))
        val dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff).toDouble()
        return Rotation(
            (Math.atan2(zDiff, xDiff) * 180.0 / Math.PI).toFloat() - 90f,
            -(Math.atan2(yDiff, dist) * 180.0 / Math.PI).toFloat()
        )
    }

    fun isAboveVoid(): Boolean {
        if (mc.thePlayer.posY < 0) return true
        for (i in (mc.thePlayer.posY - 1).toInt() downTo 1) if (mc.theWorld.getBlockState(
                BlockPos(
                    mc.thePlayer.posX,
                    i.toDouble(),
                    mc.thePlayer.posZ
                )
            ).block !is BlockAir
        ) return false
        return !mc.thePlayer.onGround
    }

    fun isStrafing(event: MoveEvent?, target: EntityLivingBase?, moveSpeed: Double): Boolean {
        if(autoThirdPerson.getCurrent())
            mc.gameSettings.thirdPersonView = 0
        if (!canStrafe()) return false
        if (!stage || target == null || moveSpeed == 0.0) {
            return true
        } else {
            if(autoThirdPerson.getCurrent())
                mc.gameSettings.thirdPersonView = 1
        }
        var aroundVoid = false
        for (x in -1..0) for (z in -1..0) if (isVoid(x, z)) aroundVoid = true
        var yaw: Float = getRotationFromEyeHasPrev(target).getYaw()
        val behindTarget: Boolean = RotationUtils.getAngleDifference(
            MathHelper.wrapAngleTo180_float(yaw),
            MathHelper.wrapAngleTo180_float(target.rotationYaw)
        ) <= 10
        if (mc.thePlayer.isCollidedHorizontally || aroundVoid && noVoid.getCurrent() || behindTarget && behind.getCurrent()) strafe *= -1
        var targetStrafe =
            if (directionKey.getCurrent() && mc.thePlayer.moveStrafing != 0.toFloat()) mc.thePlayer.moveStrafing * strafe else strafe
        if (isAboveVoid()) targetStrafe = 0.0
        val rotAssist = 45f / getEnemyDistance(target)
        val moveAssist = (45f / getStrafeDistance(target)).toDouble()
        var mathStrafe = 0f
        if (targetStrafe > 0) {
            if ((target.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY || target.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && getEnemyDistance(
                    target
                ) < range.get().toFloat()
            ) yaw += -rotAssist.toFloat()
            mathStrafe += -moveAssist.toFloat()
        } else if (targetStrafe < 0) {
            if ((target.entityBoundingBox.minY > mc.thePlayer.entityBoundingBox.maxY || target.entityBoundingBox.maxY < mc.thePlayer.entityBoundingBox.minY) && getEnemyDistance(
                    target
                ) < range.get().toFloat()
            ) yaw += rotAssist.toFloat()
            mathStrafe += moveAssist.toFloat()
        }
        val doSomeMath = doubleArrayOf(
            cos(Math.toRadians((yaw + 90f + mathStrafe).toDouble())),
            sin(Math.toRadians((yaw + 90f + mathStrafe).toDouble()))
        )
        val asLast = doubleArrayOf(
            moveSpeed * doSomeMath[0],
            moveSpeed * doSomeMath[1]
        )
        if (event != null) {
            event.x = asLast[0].also { mc.thePlayer.motionX = it }
            event.z = asLast[1].also { mc.thePlayer.motionZ = it }
        } else {
            mc.thePlayer.motionX = asLast[0]
            mc.thePlayer.motionZ = asLast[1]
        }
        return false
    }

    private fun getEnemyDistance(target: EntityLivingBase): Double {
        return mc.thePlayer.getDistance(target.posX, mc.thePlayer.posY, target.posZ)
    }

    private fun getStrafeDistance(target: EntityLivingBase): Float {
        return (getEnemyDistance(target) - range.get().toDouble()).coerceAtLeast(
            getEnemyDistance(target) - (getEnemyDistance(target) - range.get().toDouble()
                    / (range.get().toDouble() * 2))
        ).toFloat()
    }

    private fun isVoid(x: Int, z: Int): Boolean {
        if (mc.thePlayer.posY < 0) return true
        var off = 0
        while (off < mc.thePlayer.posY + 2) {
            val bb = mc.thePlayer.entityBoundingBox.offset(
                x.toDouble(), (-off).toDouble(),
                z.toDouble()
            )
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                off += 2
                continue
            }
            return false
        }
        return true
    }


}