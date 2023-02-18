package message;

import java.util.ArrayList;
import java.util.HashMap;

public class IRCData {
    public HashMap<String,String> attributes = new HashMap<>();
    public Type type = Type.UNKNOWN;
    public IRCData(ArrayList<String> attributes, Type type) {
        for (String attribute : attributes) {
            String name = attribute.substring(0, attribute.indexOf("="));
            String value = attribute.substring(attribute.indexOf("=") + 1);
            this.attributes.put(name,value);
        }
        this.type = type;
    }
}

