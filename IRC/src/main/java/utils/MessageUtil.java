package utils;

import com.google.gson.*;
import message.IRCData;
import message.Type;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MessageUtil {
    static Gson gson = new GsonBuilder().create();

    public static String getJson(Object object) {
        return gson.toJson(object);
    }


    public static String makeMessage(Type type, String... attributes) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", type.toString());
        jsonObject.add("attributes", gson.toJsonTree(attributes));
        String json = getJson(jsonObject);
        //encode to utf-8
        json = new String(json.getBytes(), StandardCharsets.UTF_8);
        return SecureUtil.encrypt(json, "你吗四了");
    }


    public static IRCData fromMessage(String message) {
        message = SecureUtil.decrypt(message, "你吗四了");
        message = new String(message.getBytes(), StandardCharsets.UTF_8);
        JsonObject jsonObject = gson.fromJson(message, JsonObject.class);
        JsonArray attributes = jsonObject.get("attributes").getAsJsonArray();
        // convert attributes to ArrayList<String>
        ArrayList<String> attributesList = new ArrayList<>();
        for (JsonElement attribute : attributes) {
            attributesList.add(attribute.getAsString());
        }
        return new IRCData(attributesList, Type.valueOf(jsonObject.get("type").getAsString()));
    }
}
