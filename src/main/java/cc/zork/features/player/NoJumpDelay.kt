package cc.zork.features.player

import cc.zork.event.Subscribe
import cc.zork.features.Module
import cc.zork.features.ModuleType

class NoJumpDelay:Module("NoJumpDelay","No jump delay", ModuleType.Player) {
    @Subscribe
    fun onUpdate() {
        mc.thePlayer.jumpTicks = 0
    }
}