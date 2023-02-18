package cc.zork.utils.client

import cc.zork.utils.mc
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3

object RotationUtils {
    fun rayCastedBlock(yaw: Float, pitch: Float): MovingObjectPosition? {
        val range: Float = mc.playerController.blockReachDistance
        val vec31: Vec3 = getVectorForRotation(pitch, yaw)
        val vec3: Vec3 = mc.thePlayer.getPositionEyes(1.0f)
        val vec32 = vec3.addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range)
        val ray: MovingObjectPosition = mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, false) ?: return null
        return if (ray != null && ray.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) ray else null
    }

    private fun getVectorForRotation(pitch: Float, yaw: Float): Vec3 {
        val f = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
        val f1 = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
        val f2 = -MathHelper.cos(-pitch * 0.017453292f)
        val f3 = MathHelper.sin(-pitch * 0.017453292f)
        return Vec3((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }

    fun getRotations(entityIn: EntityLivingBase): FloatArray {
        val d0 = entityIn.posX - mc.thePlayer.posX
        val d1 = entityIn.posZ - mc.thePlayer.posZ
        val d2 = entityIn.posY + entityIn.eyeHeight - (mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight)
        val d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1).toDouble()
        var f = (MathHelper.atan2(d1, d0) * 180.0 / Math.PI).toFloat() - 90.0f
        val f1 = (-(MathHelper.atan2(d2, d3) * 180.0 / Math.PI)).toFloat()
        return floatArrayOf(f, f1)
    }


    fun getRotations(vec: Vec3): FloatArray {
        val d0 = vec.xCoord - mc.thePlayer.posX
        val d1 = vec.zCoord - mc.thePlayer.posZ
        val d2 = vec.yCoord - (mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.eyeHeight)
        val d3 = MathHelper.sqrt_double(d0 * d0 + d1 * d1).toDouble()
        var f = (MathHelper.atan2(d1, d0) * 180.0 / Math.PI).toFloat() - 90.0f
        val f1 = (-(MathHelper.atan2(d2, d3) * 180.0 / Math.PI)).toFloat()
        return floatArrayOf(f, f1)
    }

    fun faceBlock(
        pos: BlockPos,
        yTranslation: Float,
        currentYaw: Float,
        currentPitch: Float,
        speed: Float
    ): FloatArray {
        val x = pos.x + 0.5f - mc.thePlayer.posX - mc.thePlayer.motionX
        val y = pos.y - yTranslation - (mc.thePlayer.posY + mc.thePlayer.eyeHeight)
        val z = pos.z + 0.5f - mc.thePlayer.posZ - mc.thePlayer.motionZ
        val calculate = MathHelper.sqrt_double(x * x + z * z).toDouble()
        val calcYaw = (MathHelper.atan2(z, x) * 180.0 / Math.PI).toFloat() - 90.0f
        val calcPitch = -(MathHelper.atan2(y, calculate) * 180.0 / Math.PI).toFloat()
        var yaw: Float = updateRotation(currentYaw, calcYaw, speed)
        var pitch: Float = updateRotation(currentPitch, calcPitch, speed)
        val sense = mc.gameSettings.mouseSensitivity * 0.8f + 0.2f
        val fix = (Math.pow(sense.toDouble(), 3.0) * 1.5).toFloat()
        yaw -= yaw % fix
        pitch -= pitch % fix
        return floatArrayOf(yaw, if (pitch >= 90f) 90f else if (pitch <= -90f) -90f else pitch)
    }

    fun updateRotation(curRot: Float, destination: Float, speed: Float): Float {
        var f = MathHelper.wrapAngleTo180_float(destination - curRot)
        if (f > speed) {
            f = speed
        }
        if (f < -speed) {
            f = -speed
        }
        return curRot + f
    }

    fun getAngleDifference(a: Float, b: Float): Float {
        var abs = MathHelper.abs(a % 360 - (b % 360))
        if (abs > 180)
            abs = 360 - abs
        return MathHelper.abs(abs * 2)
    }
}