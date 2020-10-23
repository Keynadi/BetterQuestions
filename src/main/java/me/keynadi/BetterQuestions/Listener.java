package me.keynadi.BetterQuestions;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Listener implements org.bukkit.event.Listener {

    private BQMain main;
    private int times = 1;

    public Listener(BQMain main) {
        this.main = main;
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        //Ok here is the tricky part. I have a list in Main class that contains players that used /bq create and waiting for chat message. //If two players  at one will try to create a question it will probably create a problem
        if (BQMain.waitingChatMessage.contains(sender.getUniqueId())) {
            int questionsCount = 1;
            UUID UUID = sender.getUniqueId();
            String message = e.getMessage();
            FileConfiguration questionsConfig = main.getQuestionsConfig();
            while (questionsConfig.getString(questionsCount + ".question") != null) {
                questionsCount++;
            }
            if (times == 1) {
                main.getQuestionsConfig().set(questionsCount + ".question", message);

                times++;
                e.setCancelled(true);
                sender.sendTitle(main.getConfig().getString("messages.title.sendanswerschat").replace("&", "§"), main.getConfig().getString("messages.title.answerssecondline").replace("&", "§"), 10, 60, 10);
            } else {
                questionsCount--;
                String[] str = message.split("\\s*,\\s*");
                List<String> answers = new ArrayList<>();
                for (String part : str) {
                    answers.add(part);
                }

                times = 1;
                BQMain.waitingChatMessage.remove(UUID);
                main.getQuestionsConfig().set(questionsCount + ".answers", answers);
                sender.sendTitle(main.getConfig().getString("messages.title.questioncreated").replace("&", "§"), main.getConfig().getString("messages.title.questioncreatedsecondline").replace("&", "§"), 10, 60, 10);
                try {
                    main.getQuestionsConfig().save(main.getDataFolder() + File.separator + "questions.yml");
                } catch (IOException questionConfigException) {
                    questionConfigException.printStackTrace();
                }
                e.setCancelled(true);
            }
        }
    }
}
