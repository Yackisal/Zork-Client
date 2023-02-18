package cc.zork.ui.mainmanu

import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.ui.ComponentBase
import cc.zork.ui.font.FontManager
import cc.zork.utils.render.RoundedUtil
import cc.zork.utils.render.drawImage
import net.minecraft.util.ResourceLocation
import java.awt.Color

class MainButton : ComponentBase {

    var animAlpha: Animation = Animation()

    constructor(content: String, location: String, width: Float, height: Float) : super(width, height) {
        this.content = content
        this.location = location
        animAlpha.value = 0.0
    }

    init {

    }

    override fun overrideDraw(mouseX: Int, mouseY: Int) {

        if (hovered) {
            if (!mouseDown) {
                animAlpha.start(animAlpha.value!!, 1.0, 0.1F, Type.LINEAR)
            } else {
                animAlpha.start(animAlpha.value!!, 0.2, 0.05F, Type.LINEAR)
            }

        } else {
            animAlpha.start(animAlpha.value!!, 0.0, 0.3F, Type.LINEAR)
        }
        animAlpha.update()

        RoundedUtil.drawRound(x + 2, y + 2, width - 4, height - 4, 3f,Color(0,0,0))
        drawImage(ResourceLocation("client/mainmenu/$location.png"), x, y, width, height, 1F)
        drawImage(
            ResourceLocation("client/mainmenu/$location" + "Hover.png"),
            x,
            y,
            width,
            height,
            animAlpha.value!!.toFloat()
        )

        FontManager.button.drawString(
            content,
            this.x + width / 2 - FontManager.button.getStringWidth(content) / 2,
            this.y + height / 2 - FontManager.button.height / 2 - 5,
            -1
        )

        drawImage(
            ResourceLocation("client/mainmenu/goto.png"),
            this.x.toInt() + width / 2 - 5,
            (this.y.toInt() + height / 2 - 5) + 8f,
            10F,
            10F,
            1F
        )


    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (this.hovered) {
            mouseDown = true
        }
    }

    override fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (this.hovered && mouseDown) {
            mouseDown = false
            this.mouseDownEvent.run()
        }
    }


}