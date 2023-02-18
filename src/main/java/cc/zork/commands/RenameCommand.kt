package cc.zork.commands

import net.minecraft.client.Minecraft
import net.minecraft.util.Session

class RenameCommand:Command("Rename","rename your session",".rename <name>") {
    override fun execute(args: List<String>): Boolean {
        if(args.isEmpty())
            return false
        Minecraft.getMinecraft().session = Session( args[0],"0","0","0");
        return super.execute(args)
    }
}