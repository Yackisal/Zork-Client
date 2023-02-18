package cc.zork.element.elements.bloodbar

import cc.zork.element.Element
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.utils.mc
import cc.zork.utils.render.RoundedUtil
import java.awt.Color

class SatBar : Element() {

    var animation: Animation = Animation()

    init {
        this.width = 125.0
        this.height = 12.0
    }

    override fun draw(x: Double, y: Double, mouseX: Double, mouseY: Double) {
        super.draw(x, y, mouseX, mouseY)


        val maxHealth = mc.thePlayer.maxHealth
        animation.start(animation.value.toDouble(),mc.thePlayer.foodStats.foodLevel.toDouble() , 0.2f, Type.EASE_IN_OUT_QUAD)
        val health = animation.value
        val healthPercentage = health / 20

        RoundedUtil.drawRound(
            x.toFloat(),
            y.toFloat(),
            width.toFloat(),
            height.toFloat(),
            0f,
            Color(21, 23, 23, 255)
        )
        RoundedUtil.drawRound(
            x.toFloat(),
            y.toFloat(),
            (width * healthPercentage).toFloat(),
            height.toFloat(),
            0f,
            Color(255, 164, 34, 255)
        )
        var text = "${healthPercentage * 100}%"
        FontManager.small.drawFontShadow(
            "${(healthPercentage * 100).toInt()}%",
            (x + 2).toFloat(),
            (y + height / 2 - FontManager.small.height / 2).toFloat(),
            Color(255, 255, 255, 255).rgb
        )

        animation.update()


    }


}