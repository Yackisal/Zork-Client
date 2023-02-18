package cc.zork.lua.api;

import cc.zork.lua.LuaModule;
import cc.zork.manager.ModuleManager;
import cc.zork.values.Value;
import party.iroiro.luajava.value.LuaValue;

import java.util.ArrayList;
import java.util.Map;

public class Registration {
    public static LuaModule register(String name, String desc, Map<String, LuaValue> events) {
        events.get("on_load").call();
        System.out.println(ModuleManager.INSTANCE.getMods().size());
        LuaModule e = new LuaModule(name, desc, events,new ArrayList());

        ModuleManager.add(e);
        System.out.println(ModuleManager.INSTANCE.getMods().size());
        return e;
    }
}
