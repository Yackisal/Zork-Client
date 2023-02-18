package cc.zork.element

open class Element {
    var x = 0.0
    var y = 0.0
    var width = 0.0
    var height = 0.0
    open fun draw(x: Double, y: Double, mouseX: Double, mouseY: Double) {
        this.x = x
        this.y = y
    }
}