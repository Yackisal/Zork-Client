package cc.zork.ui

import cc.zork.utils.render.isHovered

open class ComponentBase {

    var x = 0F
    var y = 0F
    var width = 0F
    var height = 0F
    var mouseX = 0
    var mouseY = 0
    var visible = true
    var hovered = false
    var content = ""
    var location = ""
    var mouseDown = false
    var mouseUp = false
    var mouseDownEvent: Runnable = Runnable { }


    constructor(x: Float, y: Float, width: Float, height: Float) {
        this.x = x
        this.y = y
        this.width = width
        this.height = height
    }

    fun bindMouseDownEvent(event: Runnable) {
        this.mouseDownEvent = event
    }


    constructor(width: Float, height: Float) {
        this.width = width
        this.height = height
    }

    fun draw(mouseX: Int, mouseY: Int) {
        if (this.visible) {
            this.mouseX = mouseX
            this.mouseY = mouseY
            this.hovered =
                isHovered(this.x, this.y, this.width, this.height, this.mouseX, this.mouseY)
            this.overrideDraw(this.mouseX, this.mouseY)
        }
    }

    fun draw(x: Float, y: Float, mouseX: Int, mouseY: Int) {
        if (this.visible) {
            this.x = x
            this.y = y
            this.mouseX = mouseX
            this.mouseY = mouseY
            this.hovered =
                isHovered(this.x, this.y, this.width, this.height, this.mouseX, this.mouseY)
            this.overrideDraw(this.mouseX, this.mouseY)
        }
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (this.visible) {
            this.mouseX = mouseX
            this.mouseY = mouseY
            this.hovered =
                isHovered(this.x, this.y, this.width, this.height, this.mouseX, this.mouseY)
            this.overrideMouseClicked(this.mouseX, this.mouseY, mouseButton)
        }
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        if (visible) {
            this.mouseX = mouseX
            this.mouseY = mouseY
            this.hovered =
                isHovered(this.x, this.y, this.width, this.height, this.mouseX, this.mouseY)
            overrideMouseReleased(this.mouseX, this.mouseY, state)
        }
    }

    open fun mouseDWheel(mouseX: Int, mouseY: Int, dWheel: Int) {
    }

    open fun keyTyped(typedChar: Char, keyCode: Int) {
    }

    open fun overrideDraw(mouseX: Int, mouseY: Int) {
    }

    open fun overrideMouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
    }

    open fun overrideMouseReleased(mouseX: Int, mouseY: Int, state: Int) {
    }


}