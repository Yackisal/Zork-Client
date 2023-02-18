package cc.zork.manager

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import cc.zork.Zork
import cc.zork.utils.os.FileUtils
import cc.zork.values.BooleanValue
import cc.zork.values.ModeValue
import cc.zork.values.NumberValue
import net.minecraft.client.Minecraft
import java.io.File

class ConfigManager {

    var otherConfig: Map<String, String> = HashMap()
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    init {
        if (File(dir).exists()) {
            File(dir).mkdirs()
        }
        if (!File("${dir}/config.json").exists()) {
            Zork.logger.info("Auto created config")
            save("config")
        }
    }

    companion object{
        var dir: String = File(Minecraft.getMinecraft().mcDataDir, Zork.CLIENT_NAME).absolutePath.replace("\\.\\", "\\")
        fun getLuas(): Array<out File>? {
            var lua = File("$dir/scripts")
            if (!lua.exists()) {
                lua.mkdirs()
                return emptyArray()
            }
            return lua.listFiles()
        }
    }

    private fun getConfig(): String {
        var json = JsonObject()
        for (mod in ModuleManager.mods) {
            var jo = JsonObject()
            jo.addProperty("stage", mod.stage)
            jo.addProperty("bind", mod.bind)
            mod.values.forEach {
                when (it.value) {
                    is Double -> {
                        jo.addProperty(it.name, it.value as Double)
                    }

                    is Int -> {
                        jo.addProperty(it.name, it.value as Int)
                    }

                    is Float -> {
                        jo.addProperty(it.name, it.value as Float)
                    }

                    is Long -> {
                        jo.addProperty(it.name, it.value as Long)
                    }

                    is String -> {
                        jo.addProperty(it.name, it.value as String)
                    }

                    is Boolean -> {
                        jo.addProperty(it.name, it.value as Boolean)
                    }
                }
            }
            json.add(mod.name, jo)
        }
        return gson.toJson(json)
    }

    fun save(name: String) {
        Zork.logger.info("auto saved config to $dir/$name.json")
        FileUtils.saveFile("$dir/$name.json", getConfig())
    }

    fun read(name: String) {
        var cfg = FileUtils.readFile("$dir/$name.json")
        val fromJson = gson.fromJson(cfg, JsonObject::class.java)
        for (mod in ModuleManager.mods) {
            try {

                val config = fromJson.get(mod.name).asJsonObject
                mod.set(config.get("stage").asBoolean)
                mod.bind = config.get("bind").asInt

                for (v in mod.values) {
                    try {
                        if (v is BooleanValue) {
                            v.set(config.get(v.name).asBoolean)
                        } else if (v is NumberValue) {
                            when (v.value) {
                                is Double -> {
                                    v.set(config.get(v.name).asDouble)
                                }

                                is Int -> {
                                    v.set(config.get(v.name).asInt)
                                }

                                is Float -> {
                                    v.set(config.get(v.name).asFloat)
                                }

                                is Long -> {
                                    v.set(config.get(v.name).asLong)
                                }
                            }
                        } else if (v is ModeValue) {
                            v.set(config.get(v.name).asString)
                        }
                    } catch (e: Exception) {
                        System.out.println("missing value " + v.name + " in " + mod.name)
                    }
                }
            } catch (e: Exception) {
                System.out.println("missing mod " + mod.name)
                e.printStackTrace()
            }

        }
    }
}