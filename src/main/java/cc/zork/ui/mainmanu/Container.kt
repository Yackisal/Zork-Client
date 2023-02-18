package cc.zork.ui.mainmanu

import cc.zork.ui.ComponentBase
import cc.zork.utils.render.drawFullImage
import cc.zork.utils.render.isHovered
import net.minecraft.util.ResourceLocation

class Container : ComponentBase {

    constructor(location: String, content: String, width: Float, height: Float) : super(width, height) {
        this.content = content
        this.location = location
    }

    override fun overrideDraw(mouseX: Int, mouseY: Int) {
        drawFullImage(ResourceLocation("client/mainmenu/$location.png"), x, y, 77F, 44F, 1F)
        drawFullImage(ResourceLocation("client/mainmenu/$content.png"), x + width - 18F, y + height / 2, 18F, 18F, 1F)
    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.overrideMouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        super.overrideMouseReleased(mouseX, mouseY, state)
        if(isHovered(x + width - 18F, y + height / 2, 18F, 18F, mouseX, mouseY)){
            this.mouseDownEvent.run()
        }

    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        super.keyTyped(typedChar, keyCode)
    }
}