

import net.minecraft.client.main.Main;

import static net.minecraft.client.main.Main.concat;

public class Start
{
    public static void main(String[] args)
    {
        Main.main(concat(new String[]{"--version", "mcp", "--accessToken", "0", "--assetsDir", "assets", "--assetIndex", "1.8", "--userProperties", "{}"}, args));
    }

}
