package cc.zork.utils.client

import com.mojang.realmsclient.gui.ChatFormatting
import cc.zork.Zork
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText

fun notify(msg: String) {
    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(ChatComponentText("${ChatFormatting.DARK_RED}[${Zork.CLIENT_NAME}]${ChatFormatting.RESET} $msg"))
}

fun addChatMessage(msg: String) {
    Minecraft.getMinecraft().thePlayer.addChatComponentMessage(ChatComponentText(msg))
}