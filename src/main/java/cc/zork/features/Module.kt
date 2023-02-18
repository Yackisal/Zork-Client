package cc.zork.features

import cc.zork.Zork
import cc.zork.manager.EventManager
import cc.zork.ui.notification.Type
import cc.zork.utils.math.Animation
import cc.zork.values.Value
import net.minecraft.client.Minecraft

open class Module(var name: String, var description: String, var category: ModuleType) {
    var displayName: String = ""
    var stage: Boolean = false
    var bind: Int = -1
    val mc: Minecraft = Minecraft.getMinecraft()
    var xAnimation = Animation()
    var yAnimation = Animation()
    var oAnimation = Animation()

    open var values: List<Value<*>> = ArrayList()

    init {
        values = javaClass.declaredFields.map { valueField ->
            valueField.isAccessible = true
            valueField[this]
        }.filterIsInstance<Value<*>>()
    }

    fun set(stage: Boolean) {
        this.stage = stage
        if (stage) {
            EventManager.registerListener(this)
            if (mc.thePlayer != null)
                mc.thePlayer.playSound("random.click", 1f, 1f)
            Zork.notificationManager.add(this.name, "Enabled", 1f, Type.Enable)
            onEnable()
        } else {
            EventManager.unregisterListener(this)
            if (mc.thePlayer != null)
                mc.thePlayer.playSound("random.click", 1f, 0.4f)
            Zork.notificationManager.add(this.name, "Disabled", 1f, Type.Disable)
            onDisable()
        }

    }

    fun toggle() {
        set(!stage)
    }

    open fun onEnable() {

    }

    open fun onDisable() {

    }
}