package cc.zork.commands

import cc.zork.manager.CommandsManager

class HelpCommand : Command("help", "show the help information", ".help") {
    override fun execute(args: List<String>): Boolean {
        CommandsManager.commands.forEach { command ->
            cc.zork.utils.client.notify(command.name + " - " + command.description + " (" + command.usage + ")")
        }
        return true
    }
}