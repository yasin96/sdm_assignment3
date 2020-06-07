package initialization;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import structures.Vector;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.stream.Collectors;


public class Initializer {

    public Initializer() {}

    public List<Vector> initializeSplitDataSets(String filename) {
        JsonReader reader;
        JsonObject data = null;
        try {
            reader = new JsonReader(new FileReader(filename));
            data = new Gson().fromJson(reader, JsonObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert data != null;
        return data.entrySet()
                .stream()
                .map(v -> new Vector(
                        v.getKey(),
                        v.getValue()
                                .getAsJsonObject()
                                .entrySet()
                                .stream()
                                .map(j -> Double.parseDouble(String.valueOf(j.getValue()))).collect(Collectors.toList())))
                .collect(Collectors.toList());
    }
}
