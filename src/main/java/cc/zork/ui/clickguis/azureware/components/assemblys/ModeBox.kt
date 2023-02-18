package cc.zork.ui.clickguis.azureware.components.assemblys

import cc.zork.ui.ComponentBase
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.isHovered
import cc.zork.values.ModeValue
import java.awt.Color

class ModeBox(width: Float, height: Float, var parent: ListItem, value: ModeValue) : ComponentBase(width, height) {

    var isOpen: Boolean = false
    var components: ArrayList<ComponentBase> = ArrayList()
    var modeValue: ModeValue = value
    var animationFrame: Animation = Animation()
    var titleHeight: Float = 0f

    init {
        titleHeight = height
        animationFrame.value = 0.0
        this.initComponents()
    }

    fun initComponents() {
        components.clear()
        for (mode in modeValue.getModes()) {
            components.add(ModeItem(this.width, 10f, this, mode))
        }
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
            this.modeValue.name,
            x + 8f,
            y + 6f,
            Color(255, 255, 255, (255 * parent.animationFrame.value.toFloat()).toInt()).rgb
        )
        FontManager.tiny.drawString(
            this.modeValue.getCurrent(),
            x + width - 8f - FontManager.tiny.getStringWidth(this.modeValue.getCurrent()),
            y + 6f,
            Color(255, 255, 255, (255 * parent.animationFrame.value.toFloat()).toInt()).rgb
        )

        var posY = y + titleHeight
        var posHieght = 0f
        for (component in components) {
            component.draw(x, posY, mouseX, mouseY)
            val getHeight = component.height
            posY += getHeight + 1f
            posHieght += getHeight + 2f
        }
        if (isOpen) {
            animationFrame.start(animationFrame.value, 1.0, 0.2F, Type.EASE_IN_OUT_QUAD)
        } else {
            animationFrame.start(animationFrame.value, 0.0, 0.2F, Type.EASE_IN_OUT_QUAD)
        }
        this.height = this.titleHeight + posHieght * this.animationFrame.value.toFloat()
        animationFrame.update()
    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.overrideMouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isHovered(x, y, width, titleHeight, mouseX, mouseY)) {
            when (state) {
                0 -> {
                    isOpen = !isOpen
                }
            }
        }
        if (isHovered(x, y, width, height, mouseX, mouseY) && isOpen) {
            when (state) {
                0 -> {
                    for (component in components) {
                        component.mouseClicked(mouseX, mouseY, state)
                    }
                }
            }
        }
    }


}