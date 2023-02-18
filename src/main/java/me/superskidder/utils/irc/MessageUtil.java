package me.superskidder.utils.irc;

import com.google.gson.*;
import me.konago.nativeobfuscator.Macros;
import me.konago.nativeobfuscator.Native;
import me.konago.nativeobfuscator.Winlicense;
import me.superskidder.IRCData;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MessageUtil {
    static Gson gson = new GsonBuilder().create();

    public static String getJson(Object object) {
        return gson.toJson(object);
    }
    @Native
    public static String makeMessage(Type type, String... attributes) {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type.toString());
        jsonObject.add("attributes", gson.toJsonTree(attributes));
        String json = getJson(jsonObject);
        //encode to utf-8
        json = new String(json.getBytes(), StandardCharsets.UTF_8);
        String res = SecureUtil.encrypt(json, "你吗四了");
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
        return res;
    }

    @Native
    public static IRCData fromMessage(String message) {
        Macros.define(Winlicense.VM_TIGER_BLACK_START);
        message = SecureUtil.decrypt(message, "你吗四了");
        assert message != null;
        message = new String(message.getBytes(), StandardCharsets.UTF_8);
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        JsonArray attributes = jsonObject.get("attributes").getAsJsonArray();
        // convert attributes to ArrayList<String>
        ArrayList<String> attributesList = new ArrayList<>();
        for (JsonElement attribute : attributes) {
            attributesList.add(attribute.getAsString());
        }
        IRCData type = new IRCData(attributesList, Type.valueOf(jsonObject.get("type").getAsString()));
        Macros.define(Winlicense.VM_TIGER_BLACK_END);
        return type;
    }


}