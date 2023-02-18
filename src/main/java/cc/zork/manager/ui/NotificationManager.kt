package cc.zork.manager.ui

import cc.zork.ui.notification.Notification
import cc.zork.ui.notification.Type
import cc.zork.utils.mc
import net.minecraft.client.gui.ScaledResolution
import kotlin.math.absoluteValue

class NotificationManager {

    var notifications: ArrayList<Notification> = ArrayList()

    fun draw() {
        val sr: ScaledResolution = ScaledResolution(mc)
        var posY: Float = (sr.scaledHeight - 45).toFloat()
        var remove: Notification? = null
        notifications.forEach {
            if (it.animation.end.equals(-10.0) && (it.animation.value - it.animation.end).absoluteValue < 0.5) {
                remove = it
            }
            it.draw(sr.scaledWidth.toFloat() - 10, posY)
            posY -= it.height + 10
        }
        if (remove != null)
            notifications.remove(remove)


    }

    fun add(title: String, subTitle: String, duration: Float, type: Type) {
        notifications.add(Notification(title, subTitle, type, duration))
    }
}