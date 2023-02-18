package cc.zork.lua.api;

import cc.zork.values.NumberValue;
import org.jetbrains.annotations.NotNull;

public class LuaNumberValue extends NumberValue {
    public LuaNumberValue(@NotNull String name, @NotNull Number value, @NotNull Number min, @NotNull Number max, @NotNull Number inc) {
        super(name, value, min, max, inc);
    }
}
