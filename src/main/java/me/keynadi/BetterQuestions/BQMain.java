package me.keynadi.BetterQuestions;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class BQMain extends JavaPlugin {

    private File questionsConfigFile;
    private FileConfiguration questionsConfig;
    private File playersConfigFile;
    private FileConfiguration playersConfig;

    static BQMain plugin;

    @Override
    public void onEnable() {
        new JsonFormatter(this);

        File config = new File(getDataFolder() + File.separator + "config.yml");

        if (!config.exists()) {
            this.getConfig().options().copyDefaults(true);
            this.saveDefaultConfig();
        }

        try {
            createCustomConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }

        plugin = this;

        Objects.requireNonNull(getCommand("betterquestions")).setExecutor(new Commands(this));
        Objects.requireNonNull(getCommand("betterquestions")).setTabCompleter(new TabCompletion());

        if (getConfig().getBoolean("active")) {
            new Timer().runTaskTimer(this, 0, this.getConfig().getInt("delay"));
        }

    }

    public void onDisable() {
        getLogger().info("Plugin BetterQuestions by Keynadi turned off");
    }

    private void createCustomConfigs() throws IOException {
        questionsConfigFile = new File(getDataFolder(), "questions.yml");
        if (!questionsConfigFile.exists()) {
            boolean mkdir = questionsConfigFile.getParentFile().mkdirs();
            if (!mkdir) {
                throw new IOException("Cant make a folder");
            }
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
            boolean mkdir = playersConfigFile.getParentFile().mkdirs();
            if (!mkdir) {
                throw new IOException("Cant make a folder");
            }
            saveResource("players.yml", false);
        }

        File playerFolder = new File(getDataFolder(), "players");
        if (!playerFolder.exists()) {
            boolean mkdir = playerFolder.mkdir();
            if (!mkdir) {
                throw new IOException("Cant make a folder");
            }
        }

        playersConfig = new YamlConfiguration();
        try {
            playersConfig.load(playersConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    FileConfiguration getQuestionsConfig() {
        return questionsConfig;
    }

    FileConfiguration getPlayersConfig() {
        return playersConfig;
    }

    void reloadQuestionConfig() {
        try {
            questionsConfig = new YamlConfiguration();
            questionsConfig.load(questionsConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void reloadPlayersConfig() {
        try {
            playersConfig = new YamlConfiguration();
            playersConfig.load(playersConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
