package cc.zork.commands

import cc.zork.manager.ModuleManager
import cc.zork.utils.client.notify

class ToggleCommand : Command("t", "toggle a module", ".t <name>") {
    override fun execute(args: List<String>): Boolean {
        var toggled = false
        if (args.size == 1) {
            for (mod in ModuleManager.mods) {
                if (mod.name == args[0]) {
                    mod.toggle()
                    notify("toggled ${mod.name}")
                    toggled = true
                }
            }
            if (!toggled) {
                notify("Module not found")
            }
            return true
        }
        return false
    }
}