package me.superskidder;


import cc.zork.Zork;
import cc.zork.manager.ModuleManager;
import cc.zork.ui.mainmanu.MainScreen;
import cc.zork.utils.auth.Auth;
import me.konago.nativeobfuscator.Macros;
import me.konago.nativeobfuscator.Native;
import me.konago.nativeobfuscator.Winlicense;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.net.URISyntaxException;

@Native
public class IRCLoader {
    private static IRCClient client;

    public static void login(String username, String password, String hwid) {
        try {
//            client = new IRCClient(new URI("ws://zork.cc:8181/irc?type=login&username=" + username + "&password=" + password + "&hwid=" + hwid));
            client = new IRCClient(new URI("wss://skidder.life/irc?type=login&username=" + username + "&password=" + password + "&hwid=" + hwid));
            Zork.Companion.setVerify_step(49);
        } catch (URISyntaxException e) {
            Macros.define(Winlicense.VM_TIGER_BLACK_START);
            System.exit(0);
            Auth.doCrash();
            if(!MainScreen.verify_crash) {
                Minecraft.getMinecraft().thePlayer = null;
                Minecraft.getMinecraft().thePlayer.jump();
            }
            Minecraft.getMinecraft().thePlayer =  null;
            Minecraft.getMinecraft().thePlayer.jump();
            Macros.define(Winlicense.VM_TIGER_BLACK_END);
            throw new RuntimeException(e);
        }
        client.connect();
    }

    public static void register(String username, String password, String key) {
        try {
//            client = new IRCClient(new URI("ws://zork.cc:8181/irc?type=register&username=" + username + "&password=" + password + "&key=" + key));
            client = new IRCClient(new URI("wss://skidder.life/irc?type=register&username=" + username + "&password=" + password + "&key=" + key));

        } catch (URISyntaxException e) {
            System.out.println("Failed to connect to server");
            System.exit(0);
            Auth.doCrash();
            if(!MainScreen.verify_crash) {
                Minecraft.getMinecraft().thePlayer = null;
                Minecraft.getMinecraft().thePlayer.jump();
            }
            Minecraft.getMinecraft().thePlayer =  null;
            Minecraft.getMinecraft().thePlayer.jump();
            throw new RuntimeException(e);
        }
        client.connect();
    }

    public static IRCClient getClient() {
        return client;
    }
}