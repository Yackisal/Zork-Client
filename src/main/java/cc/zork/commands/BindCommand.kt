package cc.zork.commands

import cc.zork.Zork
import cc.zork.manager.ModuleManager
import cc.zork.utils.client.notify
import org.lwjgl.input.Keyboard
import java.util.*

class BindCommand : Command("Bind", "Bind a module to a key.", ".bind <module> <key>") {
    override fun execute(args: List<String>): Boolean {
        if (args.size < 2)
            return false
        for (mod in ModuleManager.mods) {
            if (mod.name.equals(args[0], ignoreCase = true)) {
                mod.bind = Keyboard.getKeyIndex(args[1].uppercase(Locale.getDefault()))
                notify("Bound ${mod.name} to ${args[1]}")
                Zork.configManager.save("config")
                return true
            }
        }
        notify("Mod ${args[0]} not found")
        return false
    }
}