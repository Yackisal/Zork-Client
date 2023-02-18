package me.superskidder;

import cc.zork.Zork;
import cc.zork.ui.account.GuiLogin;
import cc.zork.ui.account.GuiRegister;
import cc.zork.ui.mainmanu.MainScreen;
import cc.zork.utils.auth.Auth;
import me.konago.nativeobfuscator.Macros;
import me.konago.nativeobfuscator.Native;
import me.konago.nativeobfuscator.Winlicense;
import me.superskidder.utils.irc.MessageUtil;
import cc.zork.utils.client.NotifyUtilKt;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

@Native
public class IRCClient extends WebSocketClient {
    public IRCClient(URI serverUri) {
        super(serverUri);
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Minecraft.connected = true;
    }

    @Override
    public void onMessage(String message) {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        IRCData ircData = MessageUtil.fromMessage(message);
        Zork.Companion.setVerify_step(100);
        switch (ircData.type) {
            case ERROR:
                System.out.println("Error: " + ircData.attributes.get("msg"));
                if (Minecraft.getMinecraft().currentScreen instanceof GuiLogin)
                    GuiLogin.status = ircData.attributes.get("msg");
                if (Minecraft.getMinecraft().currentScreen instanceof GuiRegister)
                    GuiRegister.status = ircData.attributes.get("msg");
                break;
            case MESSAGE:
                String text = ircData.attributes.get("msg");
                NotifyUtilKt.addChatMessage(text);
                break;
            case Login_Rep:
                BlockAir.verify_step += 4;
                String rank = ircData.attributes.get("rank");
                BlockAir.verify_step += 5;
                String name = ircData.attributes.get("name");
                Zork.startClient(Zork.Companion.getVerify_step()+1+ BlockAir.verify_step+100);
                Zork.rank = rank;
                Zork.Companion.setUsername(name);
                GuiLogin.status="Logged in.";
                break;
        }
        Macros.define(Winlicense.VM_TIGER_BLACK_END);

    }
    int times = 0;
    @Override
    public void onClose(int code, String reason, boolean remote) {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        System.out.println("reason " + reason);
//        Auth.doCrash();
        times++;
//        System.out.println("reconnecting");
//        IRCLoader.login(Zork.name);
        if(times>5){
            Auth.doCrash();
            if(!MainScreen.verify_crash) {
                Minecraft.getMinecraft().thePlayer = null;
                Minecraft.getMinecraft().thePlayer.jump();
            }
        }
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
