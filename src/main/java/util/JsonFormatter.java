package util;

import com.google.gson.*;

public class JsonFormatter {
    public static String format(String json) {
        try {
            JsonElement je = JsonParser.parseString(json);
            return new GsonBuilder().setPrettyPrinting().create().toJson(je);
        } catch (Exception e) {
            return json;
        }
    }
}