package cc.zork.commands

import cc.zork.Zork
import cc.zork.utils.client.notify

class ConfigCommand : Command("config", "load and save configs", ".config <load/save> <name>") {
    override fun execute(args: List<String>): Boolean {
        if (args.size == 2) {
            when (args[0]) {
                "load" -> {
                    Zork.configManager.read(args[1])
                    notify("Loaded config ${args[1]}")
                    return true
                }

                "save" -> {
                    Zork.configManager.save(args[1])
                    notify("Saved config ${args[1]}")
                    return true
                }
            }
        }
        return false
    }
}