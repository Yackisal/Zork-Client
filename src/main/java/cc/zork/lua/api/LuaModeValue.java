package cc.zork.lua.api;

import cc.zork.values.ModeValue;
import org.jetbrains.annotations.NotNull;

public class LuaModeValue extends ModeValue {
    public LuaModeValue(@NotNull String name, @NotNull String value, @NotNull String[] modes) {
        super(name, value, modes);
    }
}
