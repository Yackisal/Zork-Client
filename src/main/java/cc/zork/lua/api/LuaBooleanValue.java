package cc.zork.lua.api;

import cc.zork.values.BooleanValue;
import org.jetbrains.annotations.NotNull;

public class LuaBooleanValue extends BooleanValue {
    public LuaBooleanValue(@NotNull String name, boolean value) {
        super(name, value);
    }
}
