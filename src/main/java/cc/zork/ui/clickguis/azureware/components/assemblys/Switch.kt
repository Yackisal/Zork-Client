package cc.zork.ui.clickguis.azureware.components.assemblys

import cc.zork.ui.ComponentBase
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.utils.mc
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.isHovered
import cc.zork.values.BooleanValue
import java.awt.Color

class Switch(width: Float, height: Float, var parent: ListItem, var value: BooleanValue) :
    ComponentBase(width, height) {

    var xAnimation: Animation = Animation()
    lateinit var backgroundColor: Color

    init {
        if (xAnimation.value == null) {
            xAnimation.value = 0.0
        }
    }

    override fun overrideDraw(mouseX: Int, mouseY: Int) {
        RoundedUtil.drawRound(
            x + 4f,
            y + 2f,
            width - 8f,
            height - 4f,
            1.5F,
            Color(8, 8, 8, (200 * parent.animationFrame.value!!.toFloat()).toInt())
        )
        FontManager.tiny.drawString(
            value.name,
            x + 8f,
            y + 6f,
            Color(255, 255, 255, (255 * parent.animationFrame.value!!.toFloat()).toInt()).rgb
        )

        when {
            this.value.getCurrent() -> backgroundColor =
                Color(22, 107, 234, (200 * parent.animationFrame.value!!.toFloat()).toInt())

            else -> backgroundColor = Color(255, 255, 255, (80 * parent.animationFrame.value!!.toFloat()).toInt())
        }
        when {
            this.value.getCurrent() -> xAnimation.start(
                xAnimation.value!!,
                (width - 16f).toDouble(),
                0.1F,
                Type.LINEAR
            )

            else -> xAnimation.start(xAnimation.value!!, (width - 26f).toDouble(), 0.1F, Type.LINEAR)
        }

        RoundedUtil.drawRound(
            x + width - 28f,
            y + 6f,
            19f,
            8f,
            4F,
            backgroundColor
        )
        RoundedUtil.drawRound(
            x + xAnimation.value!!.toFloat(),
            y + 7.2f,
            5f,
            5f,
            2.35F,
            Color(255, 255, 255, (200 * parent.animationFrame.value!!.toFloat()).toInt())
        )

        xAnimation.update()
    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {

    }

    override fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isHovered(x, y, width, height, mouseX, mouseY) && state == 0) {
            this.value.setBoolean(!this.value.getCurrent())
            if (mc.thePlayer != null)
                mc.thePlayer.playSound(
                    "random.click", 1f, if (this.value.getCurrent()) {
                        1.0f
                    } else {
                        0.5f
                    }
                )
        }
    }

}