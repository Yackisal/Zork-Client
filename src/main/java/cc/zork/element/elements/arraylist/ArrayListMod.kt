package cc.zork.element.elements.arraylist

import com.mojang.realmsclient.gui.ChatFormatting
import cc.zork.element.Element
import cc.zork.features.Module
import cc.zork.ui.font.FontManager
import cc.zork.utils.math.Type
import cc.zork.utils.mc
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color

class ArrayListMod(var mod: Module) : Element() {

    override fun draw(x: Double, y: Double, mouseX: Double, mouseY: Double) {
        super.draw(x, y, mouseX, mouseY)

        //自适应位置左右变化
        val sr = ScaledResolution(mc)
        var x = x
        this.height = FontManager.normal.height.toDouble()
        this.width = FontManager.normal.getStringWidth("${mod.name} ${if (mod.displayName == "") "" else (" " + ChatFormatting.GRAY + mod.displayName)}",).toDouble()
        x -= (if (x < sr.scaledWidth / 2) (if (mod.stage) 0 else width) else (if (mod.stage) width else -width)).toDouble()

        mod.xAnimation.start(mod.xAnimation.value, x, 0.2f, Type.EASE_IN_OUT_QUAD)
        mod.yAnimation.start(mod.yAnimation.value, y, 0.2f, Type.EASE_IN_OUT_QUAD)
        mod.oAnimation.start(
            mod.oAnimation.value!!.toDouble(),
            if (mod.stage) 255.0 else 1.0,
            0.2f,
            Type.EASE_IN_OUT_QUAD
        )
        mod.oAnimation.update()
        mod.xAnimation.update()
        mod.yAnimation.update()

        if (mod.oAnimation.value > 40) {
            FontManager.normal.drawStringWithShadowA(
                "${mod.name} ${if (mod.displayName == "") "" else (" " + ChatFormatting.GRAY + mod.displayName)}",
                mod.xAnimation.value!!.toFloat(),
                mod.yAnimation.value!!.toFloat(),
                Color(255, 255, 255, mod.oAnimation.value.toInt()).rgb
            )
        }

    }
}