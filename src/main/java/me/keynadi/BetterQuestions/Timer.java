package me.keynadi.BetterQuestions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class Timer extends BukkitRunnable {

    @Override
    public void run() {
        try {

            FileConfiguration playersConfig = BQMain.plugin.getPlayersConfig();
            FileConfiguration questionsConfig = BQMain.plugin.getQuestionsConfig();
            FileConfiguration configMain = BQMain.plugin.getConfig();

            if (!configMain.getBoolean("active")) {
                this.cancel();
                return;
            }

            Set<String> questionsIds = Objects.requireNonNull(questionsConfig.getConfigurationSection("")).getKeys(false);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("betterquestions.ignore")) {
                    continue;
                }
                if (BQMain.plugin.getConfig().getInt("playersdatasavetype") == 1) {

                    for (String questionID : questionsIds) {

                        if (!questionsConfig.getBoolean(questionID + ".enabled")) {
                            continue;
                        }

                        @SuppressWarnings("unchecked")
                        List<Object> playersList = (List<Object>) playersConfig.getList(questionID + ".players");

                        if (playersList == null || !playersList.contains(p.getUniqueId().toString())) {

                            JsonFormatter.format(p, questionID);
                            break;
                        }
                    }

                } else {
                    for (String questionID : questionsIds) {
                        //I don't really know if it is a efficient way but, i had two choices: 1: use Json 2: use this. I choose 2.
                        File playerUUIDFile = new File(BQMain.plugin.getDataFolder() + File.separator + "players", p.getUniqueId().toString() + ".yml");

                        List<String> answeredQuestions = new ArrayList<>();
                        if (playerUUIDFile.exists()) {
                            Scanner scanner = new Scanner(playerUUIDFile);
                            while (scanner.hasNextLine()) {
                                answeredQuestions.add(scanner.nextLine());
                            }
                        }
                        if (!answeredQuestions.contains(String.valueOf(questionID))) {
                            JsonFormatter.format(p, questionID);
                            break;
                        }

                    }

                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
