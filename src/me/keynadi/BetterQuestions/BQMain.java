package me.keynadi.BetterQuestions;

import com.google.gson.JsonObject;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class BQMain extends JavaPlugin {

    private static BQMain instance;
    private File config;
    private File players;

    private ArrayList<Object> voted = new ArrayList<Object>();
    private HashMap<UUID, String> list = new HashMap<UUID, String>();
    private JsonObject obj = new JsonObject();

    @Override
    public void onEnable() {
        config = new File(getDataFolder() + File.separator + "config.yml");
        players = new File(getDataFolder(), "players.json");

        if (!players.exists()) {
            players.getParentFile().mkdirs();
            saveResource("players.json", false);
        }

        if(!config.exists()){
            this.getConfig().options().copyDefaults(true);
            this.saveDefaultConfig();
        }

        getCommand("betterquestions").setExecutor(new Commands(this, new JsonFormatter(this)));
        getServer().getPluginManager().registerEvents(new Listener(this, new Commands(this, new JsonFormatter(this))), this);
    }

    public void onDisable()
    {
        getLogger().info("Plugin BetterQuestions by Keynadi turned off");
    }

    public static BQMain getInstance() {
        return instance;
    }

    public HashMap<UUID, String> getlist() {
        return list;
    }

    public JsonObject Json() {
        return obj;
    }
}
