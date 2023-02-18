package cc.zork.ui.notification

import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.TimerUtil
import cc.zork.utils.math.base
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.drawFullImage
import cc.zork.utils.render.drawImage
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.*
import kotlin.math.max

class Notification(var title: String, var subtitle: String, var type: Type, var duration: Float) {
    var height: Float = 35f
    var width: Float = 0f
    val font = FontManager.normal
    val font2 = FontManager.small
    var animation = Animation()
    var animation2 = 0.0
    var timer: TimerUtil = TimerUtil()
    var backward = false

    init {
        width = 80 + max(font.getStringWidth(title).toFloat(),font.getStringWidth(subtitle).toFloat())
//        if (type == Type.Error) {
//            animation.start(0.0, width.toDouble(), 0.3f, net.azureware.utils.math.Type.EASE_OUT_BACK)
//        } else {
        animation.start(0.0, width.toDouble(), 0.5f, cc.zork.utils.math.Type.EASE_OUT_BACK)
//        }
        timer.reset()
    }

    fun draw(x: Float, y: Float) {
        animation.update()
        if (animation2 == 0.0)
            animation2 = y.toDouble()
        animation2 = base(animation2, y.toDouble(), 0.05)
        if (!animation.started) {
            if (!backward)
                timer.reset()
            backward = true
            if (timer.delay(duration * 1000)) {
                animation.start(animation.value, -10.0, 0.3f, cc.zork.utils.math.Type.EASE_IN_QUAD)
            }
        }
        RoundedUtil.drawRound(
            x - animation.value.toFloat(),
            animation2.toFloat(),
            width,
            height,
            3f,
            Color(29, 29, 31)
        )

        drawImage(
            ResourceLocation("client/textures/notification/" + type.name.lowercase(Locale.getDefault()) + ".png"),
            x + 10 - animation.value.toFloat(),
            animation2.toFloat() + height / 2 - 7.5f,
            15f,
            15f,
            -1
        )
        drawFullImage(
            ResourceLocation("client/textures/notification/cir.png"),
            x + width - 20 - animation.value.toFloat(),
            animation2.toFloat() + height / 2 - 6.5f,
            13f,
            13f,
            type.color
        )
        font.drawString(title, x + 35 - animation.value.toFloat(), animation2.toFloat() + 6, -1)
        font2.drawString(
            subtitle,
            x + 35 - animation.value.toFloat(),
            animation2.toFloat() + 18,
            Color(96, 96, 98).rgb
        )
    }


}

enum class Type(var color: Int) {
    Error(Color(255, 74, 34).rgb),
    Success(Color(69, 215, 0).rgb),
    Info(Color(34, 122, 255).rgb),
    Enable(Color(69, 215, 0).rgb),
    Disable(Color(255, 74, 34).rgb)

}