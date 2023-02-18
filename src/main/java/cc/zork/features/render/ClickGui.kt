package cc.zork.features.render

import cc.zork.Zork
import cc.zork.features.Module
import cc.zork.features.ModuleType
import cc.zork.ui.clickguis.azureware.ClickUI
import org.lwjgl.input.Keyboard

class ClickGui : Module("ClickGui", "ClickGui", ModuleType.Render) {

    var gui: ClickUI? = null

    init {
        this.bind = Keyboard.KEY_RSHIFT
    }

    override fun onEnable() {
        if (gui == null)
            gui = ClickUI()
        mc.displayGuiScreen(gui)
        this.toggle()
    }

    override fun onDisable() {
        super.onDisable()
        Zork.configManager.save("config")
    }

}