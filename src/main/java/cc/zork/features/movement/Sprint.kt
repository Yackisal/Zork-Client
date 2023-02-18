package cc.zork.features.movement

import cc.zork.event.MotionEvent
import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.features.player.Scaffold
import cc.zork.manager.ModuleManager
import cc.zork.values.BooleanValue

class Sprint : Module("Sprint", "Make you always sprint.", ModuleType.Movement) {
    companion object{
        var ommi: BooleanValue = BooleanValue("Always", false)
    }

    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }

    @Subscribe
    fun onMotion(e: MotionEvent) {
        val canSprint = (mc.thePlayer.foodStats.foodLevel > 6.0f || mc.thePlayer.capabilities.allowFlying) && !mc.thePlayer.isSneaking && !ModuleManager.getMod(Scaffold::class.java).stage
        mc.thePlayer.isSprinting = if (ommi.value) (movementInput() && !mc.thePlayer.isSneaking) else (mc.gameSettings.keyBindForward.isKeyDown && canSprint)
    }

    private fun movementInput(): Boolean {
        return mc.gameSettings.keyBindForward.isKeyDown
                || mc.gameSettings.keyBindLeft.isKeyDown
                || mc.gameSettings.keyBindRight.isKeyDown
                || mc.gameSettings.keyBindBack.isKeyDown
    }
}