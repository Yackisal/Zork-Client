package cc.zork.ui.clickguis.azureware.components.assemblys

import cc.zork.ui.ComponentBase
import cc.zork.ui.font.FontManager
import cc.zork.utils.render.isHovered
import java.awt.Color

class ModeItem(width: Float, height: Float, var parent: ModeBox, var modeValue: String) : ComponentBase(width, height) {

    override fun overrideDraw(mouseX: Int, mouseY: Int) {

        FontManager.tiny.drawString(
            this.modeValue,
            x + width - 8f - FontManager.tiny.getStringWidth(this.modeValue),
            y - 2f,
            Color(255, 255, 255, (255 * parent.animationFrame.value).toInt()).rgb
        )

    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isHovered(x, y, width, height, mouseX, mouseY)) {
            parent.modeValue.setMode(modeValue)
            parent.isOpen = false
        }
    }

}