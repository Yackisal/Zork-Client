package cc.zork.ui.clickguis.azureware.components.containers

import cc.zork.features.ModuleType
import cc.zork.ui.clickguis.azureware.ClickUI
import cc.zork.ui.font.FontManager
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.drawImage
import cc.zork.utils.render.isHovered
import net.minecraft.util.ResourceLocation
import java.awt.Color

class Frame {

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var titileHeight: Float = 25f
    var listbox: ListBox
    var parent: ClickUI
    var moduletype: ModuleType
    var onMoving = false
    var moveX = 0f
    var moveY = 0f
    var mouseDown = false

    constructor(x: Float, y: Float, width: Float, parent: ClickUI, moduletype: ModuleType) {
        this.x = x
        this.y = y
        this.width = width
        this.parent = parent
        this.moduletype = moduletype
        this.listbox = ListBox(this.width, this, moduletype)
    }

    fun drawScreen(mouseX: Int, mouseY: Int) {


        if (mouseDown) {
            if (onMoving) {
                if (moveX == 0f && moveY == 0f) {
                    moveX = mouseX - this.x - 250
                    moveY = mouseY - this.y - 250
                } else {
                    this.x = mouseX - moveX - 250
                    this.y = mouseY - moveY - 250
                }
            }
        } else if (moveX != 0f || moveY != 0f) {
            moveX = 0f
            moveY = 0f
        }


        RoundedUtil.drawRound(x, y, width, height, 1F, Color(21, 23, 23, 255))
        listbox.drawComponents(this.x, this.y + titileHeight + 2f, mouseX, mouseY)
        this.height = titileHeight + listbox.height
        RoundedUtil.drawGradientCornerLR(
            x,
            y,
            width,
            titileHeight,
            1F,
            Color(34, 172, 232, 255),
            Color(23, 107, 235, 255)
        )
        RoundedUtil.drawRound(x, y, titileHeight, titileHeight, 1F, Color(21, 23, 23, 60))

        drawImage(
            ResourceLocation("client/clickguis/zork/" + moduletype.name + ".png"),
            x + titileHeight / 2 - 9f,
            y + titileHeight / 2 - 9f,
            18f,
            18f,
            255F
        )

        FontManager.title.drawString(
            moduletype.name,
            x + 35F,
            y + titileHeight / 2 - FontManager.title.height / 2,
            Color(255, 255, 255, 255).rgb
        )

    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (isHovered(x, y, width, titileHeight, mouseX, mouseY) && !parent.isSelected) {
            parent.isSelected = true
            onMoving = true
        }
        mouseDown = true
        listbox.mouseClicked(mouseX, mouseY, mouseButton)
        return isHovered(x, y, width, height, mouseX, mouseY)
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int): Boolean {
        onMoving = false
        mouseDown = false
        parent.isSelected = false
        listbox.mouseReleased(mouseX, mouseY, state)
        return isHovered(x, y, width, height, mouseX, mouseY)
    }

    fun mouseDWheel(mouseX: Int, mouseY: Int, dWheel: Int) {
        listbox.mouseDWheel(mouseX, mouseY, dWheel)
    }


}