package cc.zork.ui.clickguis.azureware.components.containers;

import cc.zork.Zork
import cc.zork.features.ModuleType
import cc.zork.ui.ComponentBase
import cc.zork.ui.clickguis.azureware.components.assemblys.ListItem
import cc.zork.utils.math.Animation
import cc.zork.utils.math.Type
import cc.zork.utils.mc
import cc.zork.utils.render.StencilUtil
import cc.zork.utils.render.drawRect
import cc.zork.utils.render.isHovered
import net.minecraft.client.gui.ScaledResolution
import cc.zork.manager.ModuleManager

public class ListBox {

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f
    var scrollTarget: Float = 0f
    var valueHeight: Float = 0f
    var maxHeight = 0f
    var scrollAnimation: Animation = Animation()
    var components: ArrayList<ComponentBase> = ArrayList()
    var parent: Frame
    var moduletype: ModuleType

    constructor(width: Float, parent: Frame, moduletype: ModuleType) {
        this.width = width
        this.parent = parent
        this.moduletype = moduletype
        this.scrollAnimation.value = 0.0
        this.initComponents()
    }

    fun initComponents() {
        for (module in ModuleManager.getModWithType(moduletype)) {
            components.add(ListItem(width, 20F, this, module))
        }
    }

    fun drawComponents(x: Float, y: Float, mouseX: Int, mouseY: Int) {
        this.x = x
        this.y = y

        if (scrollTarget > 0) {
            scrollTarget = 0f
        }
        if (scrollTarget < -(valueHeight - maxHeight) && valueHeight > maxHeight) {
            scrollTarget = -(valueHeight - maxHeight)
        }
        if (valueHeight < maxHeight && valueHeight + y != 0f) {
            scrollTarget = 0f
        }

        scrollAnimation.fstart(scrollAnimation.value, scrollTarget.toDouble(), 0.25F, Type.EASE_OUT_QUAD)
        this.scrollAnimation.update()


        var scaledResolution = ScaledResolution(mc)
        this.maxHeight = scaledResolution.scaledHeight - (this.parent.titileHeight + 40f)

        StencilUtil.initStencilToWrite()
        drawRect(x.toDouble(), y.toDouble(), width.toDouble(), height.toDouble() - 2f, -1)
        StencilUtil.readStencilBuffer(1)

        scrollAnimation.fstart(scrollAnimation.value, scrollTarget.toDouble(), 0.25F, Type.EASE_OUT_QUAD)
        this.scrollAnimation.update()

        var posY = y + scrollAnimation.value.toFloat()
        var posHeight = 2f
        components.forEach {
            it.draw(this.x, posY, mouseX, mouseY)
            var getheight = it.height
            posY += getheight
            posHeight += getheight
        }
        valueHeight = posHeight
        if (posHeight > maxHeight) {
            posHeight = maxHeight
        }
        this.height = posHeight


        StencilUtil.uninitStencilBuffer()

    }


    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var mouseY = mouseY
        if (isHovered(x, y, width, height, mouseX, mouseY)) {
            components.forEach {
                it.mouseClicked(mouseX, mouseY, mouseButton)
            }
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        var mouseY = mouseY
        if (isHovered(x, y, width, height, mouseX, mouseY)) {
            components.forEach {
                it.mouseReleased(mouseX, mouseY, state)
            }
        }
    }

    fun mouseDWheel(mouseX: Int, mouseY: Int, dWheel: Int) {
        var mouseY = mouseY

        if (isHovered(x, y, width, height, mouseX, mouseY)) {

            if (valueHeight > maxHeight) when {
                dWheel > 0 -> if (scrollTarget < 0) {
                    scrollTarget += 31.41592f
                }

                dWheel < 0 -> if (scrollTarget > -(valueHeight - maxHeight)) {
                    scrollTarget -= 31.41592f
                }
            }

            components.forEach {
                it.mouseDWheel(mouseX, mouseY, dWheel)
            }
        }

    }


}





