package me.keynadi.BetterQuestions;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class BQMain extends JavaPlugin {

    private File config;

    public static ArrayList<UUID> waitingChatMessage = new ArrayList<>();
    private File questionsConfigFile;
    private FileConfiguration questionsConfig;
    private File playersConfigFile;
    private FileConfiguration playersConfig;

    @Override
    public void onEnable() {
        new JsonFormatter(this);

        config = new File(getDataFolder() + File.separator + "config.yml");

        if (!config.exists()) {
            this.getConfig().options().copyDefaults(true);
            this.saveDefaultConfig();
        }

        createCustomConfigs();

        if (getConfig().getBoolean("active")) {
            new Timer(this).runTaskTimer(this, 0, this.getConfig().getInt("delay"));
        }

        getCommand("betterquestions").setExecutor(new Commands(this));
        getServer().getPluginManager().registerEvents(new Listener(this), this);
    }

    public void onDisable() {
        getLogger().info("Plugin BetterQuestions by Keynadi turned off");
    }

    public void createCustomConfigs() {
        questionsConfigFile = new File(getDataFolder(), "questions.yml");
        if (!questionsConfigFile.exists()) {
            questionsConfigFile.getParentFile().mkdirs();
            saveResource("questions.yml", false);
        }

        questionsConfig = new YamlConfiguration();
        try {
            questionsConfig.load(questionsConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        playersConfigFile = new File(getDataFolder(), "players.yml");
        if (!playersConfigFile.exists()) {
            playersConfigFile.getParentFile().mkdirs();
            saveResource("players.yml", false);
        }

        File playerFolder = new File(getDataFolder(), "players");
        if (!playerFolder.exists()) {
            playerFolder.mkdir();
        }

        playersConfig = new YamlConfiguration();
        try {
            playersConfig.load(playersConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getQuestionsConfig() {
        return this.questionsConfig;
    }

    public FileConfiguration getPlayersConfig() {
        return this.playersConfig;
    }

    public void reloadQuestionConfig() {
        try {
            questionsConfig = new YamlConfiguration();
            questionsConfig.load(questionsConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reloadPlayersConfig() {
        try {
            playersConfig = new YamlConfiguration();
            playersConfig.load(playersConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
