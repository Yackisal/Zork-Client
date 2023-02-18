package cc.zork.ui.clickguis.azureware.components.assemblys

import cc.zork.features.Module
import cc.zork.ui.ComponentBase
import cc.zork.ui.clickguis.azureware.components.containers.ListBox
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.utils.render.*
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import java.awt.Color

class ListItem(width: Float, height: Float, var parent: ListBox, var module: Module) : ComponentBase(width, height) {

    var isOpen: Boolean = false
    var animationFrame: Animation = Animation()
    var titileHeight: Float = 0f
    var alphaAnimation = Animation()
    var alphaAnimation2 = Animation()
    var components: ArrayList<ComponentBase> = ArrayList()

    init {
        this.titileHeight = this.height
        this.animationFrame.value = 0.0
        this.alphaAnimation.value = 1.0
        this.alphaAnimation2.value = 1.0
        this.initValues()
    }

    fun initValues() {
        module.values.forEach {
            when (it) {
                is BooleanValue -> {
                    components.add(Switch(width, 20f, this, it))
                }

                is NumberValue -> {
                    components.add(Slider(width, 27f, this, it))
                }

                is ModeValue -> {
                    components.add(ModeBox(width, 20f, this, it))
                }
            }
        }
    }


    override fun overrideDraw(mouseX: Int, mouseY: Int) {

        if (this.module.stage) {
            alphaAnimation2.start(alphaAnimation2.value!!, 255.0, 0.2F, Type.EASE_IN_OUT_QUAD)
            alphaAnimation.start(alphaAnimation.value!!, 255.0, 0.2F, Type.EASE_IN_OUT_QUAD)
        } else {
            alphaAnimation2.start(alphaAnimation2.value!!, 165.0, 0.2F, Type.EASE_IN_OUT_QUAD)
            alphaAnimation.start(alphaAnimation.value!!, 0.0, 0.2F, Type.EASE_IN_OUT_QUAD)
        }

        RoundedUtil.drawRound(
            this.x,
            this.y,
            this.width,
            this.titileHeight,
            0F,
            Color(39, 39, 39, alphaAnimation.value!!.toInt())
        )
        FontManager.small.drawString(
            this.module.name,
            this.x + 6F,
            this.y + this.titileHeight / 2 - FontManager.small.height / 2 - 1f,
            Color(255, 255, 255, alphaAnimation2.value!!.toInt()).rgb
        )

        drawImage(
            if (isOpen) ResourceLocation("client/clickguis/zork/Down.png")
            else ResourceLocation("client/clickguis/zork/Up.png"),
            x + width - 14.5f,
            y + this.titileHeight / 2 - 4f,
            9f,
            9f,
            alphaAnimation2.value!!.toFloat() / 255
        )

        //drawValues

        var valueHeight = 0f
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        doScissor(x, y, width, height)

        var posY = this.y + this.titileHeight + 4F
        components.forEach {
            it.draw(x, posY, mouseX, mouseY)
            var getHeight = it.height
            posY += getHeight
            valueHeight += getHeight
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST)
        if (isOpen && components.size > 0) {
            animationFrame.start(
                animationFrame.value!!,
                1.0,
                0.2F,
                Type.EASE_OUT_QUAD
            )
        } else {
            animationFrame.start(
                animationFrame.value!!,
                0.0,
                0.2F,
                Type.EASE_OUT_QUAD
            )
        }


        this.height = this.titileHeight + (valueHeight.toDouble().toFloat() + 8f) * animationFrame.value!!.toFloat()
        this.animationFrame.update()
        this.alphaAnimation.update()
        this.alphaAnimation2.update()
    }

    override fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isHovered(x, y, width, height, mouseX, mouseY)) {
            mouseUp = true
        }
        if (isOpen) {
            components.forEach {
                it.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }
    }

    override fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (isHovered(x, y, width, titileHeight, mouseX, mouseY) && mouseUp) {
            when (state) {
                0 -> {
                    this.module.toggle()
                }

                1 -> {
                    this.isOpen = !this.isOpen
                }
            }
        }
        if (isHovered(x, y, width, height, mouseX, mouseY) && mouseUp) {
            components.forEach {
                it.mouseReleased(mouseX, mouseY, state)
            }
        }
        mouseUp = false
    }
}


