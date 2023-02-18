package cc.zork.ui.clickguis.azureware.components.assemblys

import cc.zork.ui.ComponentBase
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.isHovered
import cc.zork.values.NumberValue
import org.lwjgl.input.Mouse
import java.awt.Color

class Slider(width: Float, height: Float, var parent: ListItem, var value: NumberValue) :
    ComponentBase(width, height) {
    var drag: Boolean = false
    var sliderWidth = 0f
    var sliderHeight = 0f
    var sliderX = 0f
    var sliderY = 0f
    var sliderValue = 0f
    var sliderMin = 0f
    var sliderMax = 0f
    var sliderRange = 0f
    var sliderValueRange = 0f
    var sliderValuePercent = 0f
    var sliderValueX = 0f
    var sliderValueY = 0f
    var sliderAnimation: Animation = Animation()

    init {
        sliderAnimation.value = 0.0
    }

    override fun overrideDraw(mouseX: Int, mouseY: Int) {
        RoundedUtil.drawRound(
            x + 4f,
            y + 2f,
            width - 8f,
            height - 4f,
            1.5F,
            Color(8, 8, 8, (200 * parent.animationFrame.value.toFloat()).toInt())
        )
        FontManager.tiny.drawString(
            this.value.name,
            x + 8f,
            y + 6f,
            Color(255, 255, 255, (255 * parent.animationFrame.value.toFloat()).toInt()).rgb
        )
        FontManager.tiny.drawString(
            this.value.get().toString(),
            x + width - 8f - FontManager.tiny.getStringWidth(this.value.get().toString()),
            y + 6f,
            Color(255, 255, 255, (255 * parent.animationFrame.value.toFloat()).toInt()).rgb
        )

        sliderWidth = width - 16f
        sliderHeight = 2f
        sliderX = x + 8f
        sliderY = y + height - 10f
        sliderValue = value.get().toFloat()
        sliderMin = value.min.toFloat()
        sliderMax = value.max.toFloat()
        sliderRange = sliderMax - sliderMin
        sliderValueRange = sliderValue - sliderMin
        sliderValuePercent = sliderValueRange / sliderRange
        sliderValueX = sliderX + sliderWidth * sliderValuePercent
        sliderValueY = sliderY + sliderHeight / 2

        RoundedUtil.drawRound(
            sliderX,
            sliderY,
            sliderWidth,
            sliderHeight,
            1F,
            Color(255, 255, 255, (100 * parent.animationFrame.value.toFloat()).toInt())
        )

        if (parent.parent.parent.onMoving) {
            sliderAnimation.value = sliderValueX.toDouble()
            sliderAnimation.end = sliderAnimation.value
        } else {
            sliderAnimation.start(
                sliderAnimation.value, sliderValueX.toDouble(), 0.1f, Type.LINEAR
            )
        }


        RoundedUtil.drawGradientCornerLR(
            sliderX,
            sliderY,
            sliderAnimation.value.toFloat() - sliderX,
            sliderHeight,
            1F,
            Color(22, 107, 234, (255 * parent.animationFrame.value.toFloat()).toInt()),
            Color(34, 172, 232, (255 * parent.animationFrame.value.toFloat()).toInt())
        )
        RoundedUtil.drawRound(
            sliderAnimation.value.toFloat() - 3f,
            sliderValueY - 2.5f,
            5f,
            5f,
            2F,
            Color(34, 172, 232, (255 * parent.animationFrame.value.toFloat()).toInt())
        )

        when {
            this.mouseUp && (isHovered(
                sliderX - 8f,
                sliderY - 8f,
                sliderWidth + 16f,
                sliderHeight + 16f,
                mouseX,
                mouseY
            ) || drag) -> {

                val valAbs = mouseX.toDouble() - ((sliderX).toDouble())
                var perc = valAbs / sliderWidth
                perc = Math.min(Math.max(0.0, perc), 1.0)
                val valRel: Double = (value.max.toDouble() - value.min.toDouble()) * perc
                val predict: Double = value.min.toDouble() + valRel
                val sliderValue2: Double =
                    Math.round(predict * (1.0 / this.value.inc.toDouble())) / (1.0 / this.value.inc.toDouble())

                when {
                    sliderValue2 > this.value.max.toDouble() -> this.value.set(this.value.max)
                    sliderValue2 < this.value.min.toDouble() -> this.value.set(this.value.min)
                    else -> this.value.set(sliderValue2)
                }
                drag = true
            }
        }
        if (!Mouse.isButtonDown(0)) {
            this.mouseUp = false
            drag = false
        }
        sliderAnimation.update()
    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isHovered(
                sliderX,
                sliderY - 10,
                sliderWidth,
                sliderHeight + 20,
                mouseX,
                mouseY
            ) && mouseButton == 0
        ) {
            this.mouseUp = true
        }

    }

    override fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        this.mouseUp = false
    }
}