package cc.zork.features.movement

import cc.zork.event.EventType
import cc.zork.event.MotionEvent
import cc.zork.event.MoveEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.combat.KillAura
import cc.zork.features.combat.TargetStrafe
import cc.zork.manager.ModuleManager
import cc.zork.utils.client.MovementUtil
import cc.zork.utils.math.TimerUtil
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue

class Speed : Module("Speed", "Boost your movement speed", ModuleType.Movement) {
    companion object {
        val mode = ModeValue("Mode", "Hypixel", "BunnyHop", "Hypixel")
        val liquidJump = BooleanValue("LiquidJump", true)
        val liquidDelay = NumberValue("LiquidDelay", 2000.0, 0.0, 10000.0, 0.1)
        val strafeSpeed = NumberValue("StrafeSpeed", 1.2, 0.0, 2.0, 0.1)
    }

    fun isMoving(): Boolean {
        return mc.thePlayer.moveForward != 0f || mc.thePlayer.moveStrafing != 0f
    }

    override fun onDisable() {
//        mc.thePlayer.motionX = 0.0
//        mc.thePlayer.motionZ = 0.0
    }

    val liquidTimer = TimerUtil()


    @Subscribe
    fun onMove(e:MoveEvent){
        if (mode.getCurrent() == "Hypixel") {
            if (mc.thePlayer.onGround || mc.thePlayer.hurtTime != 0) {
                (ModuleManager.getMod(TargetStrafe::class.java) as TargetStrafe).isStrafing(
                    e,
                    KillAura.target,
                    MovementUtil.getBaseMoveSpeed()
                )
            }
        } else if (mode.getCurrent() == "BunnyHop") {
            (ModuleManager.getMod(TargetStrafe::class.java) as TargetStrafe).isStrafing(
                e,
                KillAura.target,
                MovementUtil.getBaseMoveSpeed() * strafeSpeed.get().toFloat()
            )
        }
    }
    @Subscribe
    fun onMotion(e: MotionEvent) {
        displayName = mode.getCurrent()
        var isinLiquid = mc.thePlayer.isInWater || mc.thePlayer.isInLava
        if ((isinLiquid && !liquidTimer.delay(
                liquidDelay.value.toFloat()
            )) || !liquidJump.getCurrent()
        ) {
            return
        }
        if (isinLiquid && mc.thePlayer.onGround)
            liquidTimer.reset()
        if (mode.getCurrent() == "Hypixel") {
            if (isMoving()) {
                var speed = 1f
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.419999999
                    speed = 1 * strafeSpeed.get().toFloat()
                }

                if (mc.thePlayer.onGround || mc.thePlayer.hurtTime != 0) {
                    MovementUtil.setSpeed(MovementUtil.getBaseMoveSpeed() * speed)
                }
            }
        } else if (mode.getCurrent() == "BunnyHop") {
            if (isMoving()) {
                var speed = 1f
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.419999999
                    speed = 1.6233f
                }

                if (KillAura.target != null) {
                    mc.timer.timerSpeed = 1f
                }

                MovementUtil.setSpeed(MovementUtil.getBaseMoveSpeed() * speed)
            }
        }
    }


}