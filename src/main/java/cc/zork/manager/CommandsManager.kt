package cc.zork.manager

import cc.zork.commands.*
import cc.zork.event.Subscribe
import cc.zork.event.TextEvent
import cc.zork.utils.client.notify
import net.minecraft.client.Minecraft
import kotlin.collections.ArrayList

class CommandsManager {
    companion object {
        val commands = ArrayList<Command>()
        fun registerCommand(command: Command) {
            commands.add(command)
        }
    }

    init {
        EventManager.registerListener(this)
        commands.add(BindCommand())
        commands.add(AuthMeBypassCommand())
        commands.add(RenameCommand())
        commands.add(HelpCommand())
        commands.add(IRCCommand())
        commands.add(ConfigCommand())
        commands.add(ScriptsCommand())
        commands.add(ToggleCommand())
    }

    @Subscribe
    fun chat(chat: TextEvent) {
        if (Minecraft.connected) {
            try {
                if (chat.text!!.startsWith(".")) {
                    var executed = false
                    chat.isCancelled = true
                    for (command in commands) {
                        val cmd = chat.text!!.split(" ")[0].substring(1)
                        if (cmd.equals(command.name, ignoreCase = true)) {
                            executed = true
                            if (!command.execute(chat.text!!.split(" ").drop(1)))
                                notify("Usage: ${command.usage}")
                        }
                    }
                    if (!executed)
                        notify("Command Not Found.")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                e.stackTrace.forEach { it: StackTraceElement? -> notify(it.toString()) }
            }
        }
    }

}