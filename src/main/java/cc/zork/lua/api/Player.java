package cc.zork.lua.api;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class Player {
    static Minecraft mc = Minecraft.getMinecraft();
    public static void setMouseOver(MovingObjectPosition mop){
        mc.objectMouseOver = mop;
    }

}
