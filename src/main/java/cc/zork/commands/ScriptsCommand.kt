package cc.zork.commands

import cc.zork.Zork
import cc.zork.features.render.ClickGui
import cc.zork.manager.ModuleManager
import cc.zork.utils.client.notify

class ScriptsCommand : Command("scripts", "Lua api", ".scripts <reload/list>") {
    override fun execute(args: List<String>): Boolean {
        if (args.size == 1) {
            when (args[0]) {
                "reload" -> {
                    Zork.luaManager.reload()
                    (ModuleManager.getMod(ClickGui::class.java) as ClickGui).gui = null
                    notify("reloaded!")
                }

                "list" -> {
                    for (script in Zork.luaManager.scripts) {
                        notify("[Scripts] name: ${script.name}  desc: ${script.description} by ${script.author}")
                    }
                }
            }
            return true
        } else
            return false
    }
}