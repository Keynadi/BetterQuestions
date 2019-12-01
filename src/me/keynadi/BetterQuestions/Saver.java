package me.keynadi.BetterQuestions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Saver {
    private BQMain plugin;

    public Saver(BQMain plugin) {
        this.plugin = plugin;
    }

    public static void save(JsonObject obj, String path) throws Exception {
        try (FileWriter file = new FileWriter(path)) {
            file.write(obj.toString());
            file.flush();
        } catch (IOException except) {
            except.printStackTrace();
        }
    }

    public static Object load(String path) throws Exception {
        JsonParser parser = new JsonParser();
        Object obj = parser.parse(new FileReader(path));
        JsonObject jsonObject = (JsonObject) obj;
        return jsonObject;
    }
}

