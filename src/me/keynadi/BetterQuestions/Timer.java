package me.keynadi.BetterQuestions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Timer extends BukkitRunnable {

    private BQMain main;
    private int questionNumber = 1;

    public Timer(BQMain instance) {
        this.main = instance;
    }

    @Override
    public void run() {
        try {

            //Code below will hunt me in my nightmares. Causing server crush and I don't know know why
            //TODO: Make it more efficient

            Configuration playersConfig = main.getPlayersConfig();
            Configuration questionConfig = main.getQuestionsConfig();
            Configuration configMain = main.getConfig();
            int questionsAmount = 0;

            if (!configMain.getBoolean("active")) {
                this.cancel();
                return;
            }

            while (questionConfig.get((questionsAmount + 1) + ".question") != null) {
                questionsAmount++;
            }

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.getConfig().getInt("playersdatasavetype") == 1) {

                    for (int i = 0; i <= questionsAmount; i++) {
                        //I hate this part
                        List<Object> playersList = (List<Object>) playersConfig.getList(questionNumber + ".players");

                        if (playersList == null || !playersList.contains(p.getUniqueId().toString())) {
                            JsonFormatter.format(p, questionNumber);
                            questionNumber++;
                            break;
                        }
                        questionNumber++;

                        if (questionConfig.get(questionNumber + ".question") == null) {
                            questionNumber = 1;
                            continue;
                        }
                    }


                } else {
                    for (int i = 0; i <= questionsAmount; i++) {
                        //I don't really know if it is a efficient way but, i had two choices: 1: use Json 2: use this. I choose 2.
                        File playerUUIDFile = new File(main.getDataFolder() + File.separator + "players", p.getUniqueId().toString() + ".yml");

                        List<String> answeredQuestions = new ArrayList<>();
                        if (playerUUIDFile.exists()) {
                            Scanner scanner = new Scanner(playerUUIDFile);
                            while (scanner.hasNextLine()) {
                                answeredQuestions.add(scanner.nextLine());
                            }

                        }

                        if (!answeredQuestions.contains(String.valueOf(questionNumber))) {
                            JsonFormatter.format(p, questionNumber);
                            questionNumber++;
                            break;
                        }
                        questionNumber++;

                        if (questionConfig.get(questionNumber + ".question") == null) {
                            questionNumber = 1;
                            continue;
                        }
                    }

                }

            }
            if (questionConfig.getString(questionNumber + ".question") == null) {
                questionNumber = 1;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
