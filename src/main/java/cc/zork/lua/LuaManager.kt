package cc.zork.lua

import net.minecraft.client.Minecraft
import cc.zork.features.Module
import cc.zork.lua.api.LuaBooleanValue
import cc.zork.lua.api.LuaModeValue
import cc.zork.lua.api.LuaNumberValue
import cc.zork.lua.api.Registration
import cc.zork.manager.ConfigManager.Companion.getLuas
import cc.zork.manager.EventManager
import cc.zork.manager.ModuleManager
import cc.zork.ui.font.FontManager
import cc.zork.utils.client.notify
import cc.zork.utils.os.FileUtils.readFile
import cc.zork.values.Value
import party.iroiro.luajava.Lua
import party.iroiro.luajava.Lua53
import java.io.File
import java.util.function.Consumer

class LuaManager {
    var scripts: ArrayList<LuaScript> = ArrayList()
    fun loadScripts() {
        val luas: Array<out File>? = getLuas()
        for (luaFile in luas!!) {
            try {
                scripts.add(loadLua(readFile(luaFile)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadLua(script: String?): LuaScript {
        val lua: Lua = Lua53()
        lua.run("sys = java.import('java.lang.System')")
        lua.run("module_manager = java.import('${Registration::class.java.name}')")
        lua.run("font_loader = java.import('${FontManager::class.java.name}')")
        lua.run("v_bool = java.import('${LuaBooleanValue::class.java.name}')")
        lua.run("v_mode = java.import('${LuaModeValue::class.java.name}')")
        lua.run("v_number = java.import('${LuaNumberValue::class.java.name}')")
        lua.run ("value = java.import('${Value::class.java.name}')")
        lua.run ("mc = java.import('${Minecraft::class.java.name}'):getMinecraft()")

        lua.push { L: Lua ->
            val type = L.toString(1)
            println(type)
            0
        }
        lua.setGlobal("print")
        lua.pop(0)
        lua.push { L: Lua ->
            val type:String = L.toString(1)!!
            notify(type)
            0
        }
        lua.setGlobal("info")
        lua.pop(0)
        lua.run(script)


        var luaScript = LuaScript(lua["name"].toJavaObject().toString(),
            lua["description"].toJavaObject().toString(),lua["author"].toJavaObject().toString(),lua)

        return luaScript
    }

    fun reload() {
        for (script in scripts) {
            val unload = script.lua["unload"]
            if (unload.type() == Lua.LuaType.FUNCTION) {
                unload.call()
            }
            script.lua.close()
        }
        scripts.clear()
        val remove = ArrayList<Module>()
        ModuleManager.mods.forEach(Consumer { m: Module? ->
            if (m is LuaModule) {
                remove.add(m)
                EventManager.unregisterListener(m)
            }
        })
        ModuleManager.mods.removeAll(remove.toSet())

        val luas: Array<out File>? = getLuas()
        for (luaFile in luas!!) {
            try {
                scripts.add(loadLua(readFile(luaFile)))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}