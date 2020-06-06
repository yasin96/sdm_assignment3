package csv;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map;

public class JsonLoader {
    private Gson gson;
    private Type mapType;

    public JsonLoader() {
        gson = new GsonBuilder()
                .create();
        mapType = new TypeToken<Map<String, Map<String, Double>>>() {
        }.getType();
    }

    public Map<String, Map<String, Double>> loadJson(String filepath) {
        try (Reader reader = new FileReader(filepath)) {
            return gson.fromJson(reader, mapType);
        } catch (IOException | JsonParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String root = "C:\\Users\\pavao\\Documents\\erasmus\\Courses\\SDM\\assignment3\\fma_metadata\\split\\";
        JsonLoader jsonLoader = new JsonLoader();
        Map<String, Map<String, Double>> validate = jsonLoader.loadJson(root + "validate.json");
        validate.forEach((id, features) -> {
            System.out.println(id);
            features.forEach((name, value) -> {
                System.out.printf("\"%s\": %f ", name, value);
            });
            System.out.println();
        });
    }
}
