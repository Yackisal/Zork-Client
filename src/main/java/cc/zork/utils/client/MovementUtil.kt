package cc.zork.utils.client

import net.minecraft.potion.Potion
import cc.zork.utils.mc
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

object MovementUtil {
    fun isMoving(): Boolean {
        return mc.thePlayer.moveForward != 0f || mc.thePlayer.moveStrafing != 0f
    }

    fun calculateBPS(): Double {
        val bps = hypot(
            mc.thePlayer.posX - mc.thePlayer.prevPosX,
            mc.thePlayer.posZ - mc.thePlayer.prevPosZ
        ) * mc.timer.timerSpeed * 20
        return (bps * 100.0).roundToInt() / 100.0
    }
    fun setSpeed(moveSpeed: Double, yaw: Float, strafe: Double, forward: Double) {
        var yaw = yaw
        var strafe = strafe
        var forward = forward
        if (forward != 0.0) {
            if (strafe > 0.0) {
                yaw += (if (forward > 0.0) -45 else 45).toFloat()
            } else if (strafe < 0.0) {
                yaw += (if (forward > 0.0) 45 else -45).toFloat()
            }
            strafe = 0.0
            if (forward > 0.0) {
                forward = 1.0
            } else if (forward < 0.0) {
                forward = -1.0
            }
        }
        if (strafe > 0.0) {
            strafe = 1.0
        } else if (strafe < 0.0) {
            strafe = -1.0
        }
        val mx = cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val mz = sin(Math.toRadians((yaw + 90.0f).toDouble()))
        mc.thePlayer.motionX = forward * moveSpeed * mx + strafe * moveSpeed * mz
        mc.thePlayer.motionZ = forward * moveSpeed * mz - strafe * moveSpeed * mx
    }

    fun setSpeed(moveSpeed: Double) {
        setSpeed(
            moveSpeed, mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveStrafe.toDouble(),
            mc.thePlayer.movementInput.moveForward.toDouble()
        )
    }

    fun isOnGround(height: Double): Boolean {
        return mc.theWorld.getCollidingBoundingBoxes(
            mc.thePlayer,
            mc.thePlayer.entityBoundingBox.offset(0.0, -height, 0.0)
        ).isNotEmpty()
    }


    fun getBaseMoveSpeed(): Double {
        var baseSpeed = mc.thePlayer.capabilities.walkSpeed * 2.873
        if (mc.thePlayer.isPotionActive(Potion.moveSlowdown)) baseSpeed /= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(
            Potion.moveSlowdown
        ).amplifier + 1)
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(
            Potion.moveSpeed
        ).amplifier + 1)
        return baseSpeed
    }

    fun getJumpEffect(): Int {
        return if (mc.thePlayer.isPotionActive(Potion.jump)) mc.thePlayer.getActivePotionEffect(Potion.jump).amplifier + 1 else 0
    }

}