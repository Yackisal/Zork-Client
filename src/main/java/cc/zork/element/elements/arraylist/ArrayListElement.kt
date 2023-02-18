package cc.zork.element.elements.arraylist

import cc.zork.Zork
import cc.zork.element.Element
import cc.zork.features.ModuleType
import cc.zork.features.ui.HUD
import cc.zork.manager.ModuleManager
import cc.zork.ui.font.FontManager
import kotlin.collections.ArrayList

class ArrayListElement : Element() {
    var elements = ArrayList<ArrayListMod>()

    override fun draw(x: Double, y: Double, mouseX: Double, mouseY: Double) {
        super.draw(x, y, mouseX, mouseY)

        if (elements.size == 0) {
            ModuleManager.mods.forEach { it ->
                elements.add(ArrayListMod(it))
            }
        }
        var y1 = y

        elements.sortByDescending { it -> FontManager.normal.getStringWidth("${it.mod.name} ${if (it.mod.displayName == "") "" else " " + it.mod.displayName}") }


        elements.forEach { it ->
            if ((!HUD.noRender.getCurrent() || it.mod.category != ModuleType.Render))

            if (it.mod.stage) {
                it.draw(x, y1, mouseX, mouseY)
                y1 += it.height + 4
            } else {
                it.draw(x, y1 - it.height, mouseX, mouseY)
            }
        }
        this.height = y1 - y;
    }
}