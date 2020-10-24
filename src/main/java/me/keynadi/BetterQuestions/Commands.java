package me.keynadi.BetterQuestions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

class Commands implements CommandExecutor, Serializable {
    private BQMain main;

    private String sender;

    public Commands(BQMain main) {
        this.main = main;
    }

    //shenanigans!!!
    public static boolean ifContains(List<String> list, String string) {
        for (String line : list) {
            line = line.replaceAll("&[0-9A-Fa-f]", "");
            if (line.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCommand(CommandSender p, Command command, String s, String[] args) {

        Player player = (Player) p;

        Configuration config = main.getConfig();

        if (args.length == 0) {
            if (!p.hasPermission("betterquestions.admin")) {
                p.sendMessage(config.getString("messages.nopermissions").replace("&", "§"));
                return true;
            }
            return false;
        }

        if (args[0].equalsIgnoreCase("answer")) {
            if (args.length > 1 && args[1] != null && args[2] != null) {

                String UUID = player.getUniqueId().toString();

                String answer = "";

                int argscount = 2;

                while (argscount <= (args.length - 1)) {
                    answer = answer + " " + args[argscount];
                    argscount++;
                }

                answer = answer.substring(1);

                List<String> answers = (List<String>) main.getQuestionsConfig().getList(args[1] + ".answers");

                //Shenanigans!!!
                if (!ifContains(answers, answer)) {
                    p.sendMessage(main.getConfig().getString("messages.noanswer").replace("&", "§"));
                    return true;
                }

                if (config.getInt("playersdatasavetype") == 1) {
                    FileConfiguration playersConfig = main.getPlayersConfig();
                    List<Object> playerslist = (List<Object>) playersConfig.getList(args[1] + ".players");

                    if (playerslist == null) playerslist = new ArrayList<>();

                    if (playerslist != null && playerslist.contains(UUID)) {
                        p.sendMessage(config.getString("messages.alreadyvoted").replace("&", "§"));
                        return true;
                    }

                    playerslist.add(UUID);

                    playersConfig.set(args[1] + ".players", playerslist);
                    try {
                        playersConfig.save(main.getDataFolder() + File.separator + "players.yml");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    //I don't really like this code, but I guess it's works
                    File playerUUIDFile = new File(main.getDataFolder() + File.separator + "players", UUID + ".yml");

                    List<String> questionsList = new ArrayList<>();

                    if (playerUUIDFile.exists()) {
                        Scanner scanner = null;
                        try {
                            scanner = new Scanner(playerUUIDFile);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        while (scanner.hasNextLine()) {
                            questionsList.add(scanner.nextLine());
                        }
                    }
                    if (questionsList.contains(args[1])) {
                        p.sendMessage(config.getString("messages.alreadyvoted").replace("&", "§"));
                        return true;
                    } else {
                        questionsList.add(args[1]);
                        try {
                            FileWriter stream = new FileWriter(playerUUIDFile);
                            BufferedWriter out = new BufferedWriter(stream);

                            for (String answeredQuestion : questionsList) {
                                out.write(answeredQuestion);
                                out.newLine();
                            }
                            out.close();
                            stream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                FileConfiguration questionsConfig = main.getQuestionsConfig();

                questionsConfig.set(args[1] + ".answersresults." + answer, questionsConfig.getInt(args[1] + ".answersresults." + answer) + 1);

                try {
                    questionsConfig.save(main.getDataFolder() + File.separator + "questions.yml");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                p.sendMessage(config.getString("messages.successfulvote").replace("&", "§"));
            }
            return true;
        }

        if (!p.hasPermission("betterquestions.admin")) {
            p.sendMessage(config.getString("messages.nopermissions").replace("&", "§"));
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            p.sendMessage(config.getString("messages.help").replace("&", "§"));
            return true;
        }
        if (args[0].equalsIgnoreCase("create")) {

            sender = p.getName();
            UUID UUID = Bukkit.getPlayer(sender).getUniqueId();

            BQMain.waitingChatMessage.add(UUID);

            Bukkit.getPlayer(UUID).sendTitle(config.getString("messages.title.sendquestionchat").replace("&", "§"), "", 10, 80, 10);

            return true;
        }


        if (args[0].equalsIgnoreCase("on")) {
            if (!config.getBoolean("active")) {
                config.set("active", true);
                main.saveConfig();
                new Timer(main).runTaskTimer(main, 0, config.getInt("delay"));
                p.sendMessage(config.getString("messages.nowbroadcasting").replace("&", "§"));
            } else {
                p.sendMessage(config.getString("messages.alreadybroadcasting").replace("&", "§"));
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("off")) {
            if (config.getBoolean("active")) {
                config.set("active", false);
                main.saveConfig();
                p.sendMessage(config.getString("messages.nolongerbroadcasting").replace("&", "§"));
            } else {
                p.sendMessage(config.getString("messages.alreadystoppedbroadcasting").replace("&", "§"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            main.reloadConfig();
            main.reloadQuestionConfig();
            main.reloadPlayersConfig();
            p.sendMessage(config.getString("messages.reload").replace("&", "§"));
            return true;
        }

        if (args[0].equalsIgnoreCase("view")) {
            if (args.length < 2) {
                p.sendMessage(config.getString("messages.usage.view").replace("&", "§"));
                return true;
            }

            FileConfiguration questionsConfig = main.getQuestionsConfig();

            if (questionsConfig.getString(args[1] + ".question") == null) {
                p.sendMessage(config.getString("messages.questionnotfound").replace("&", "§"));
                return true;
            }

            String allMessage = ("\n\n" + questionsConfig.getString(args[1] + ".question") + "\n\n&f");

            int sum = 0;

            //Pretty bad solution but I haven't find a way to make it more efficient
            //TODO: Make it more efficient

            List<String> alreadyLoopedAnswers = new ArrayList<>(); //Fixes votes doubling when there is two answers with different colors. This answers still counts as one but in /bq view it was a two answers with same number of votes
            for (Object answer : questionsConfig.getList(args[1] + ".answers")) {
                answer = answer.toString().replaceAll("&[0-9A-Fa-f]", "");
                if (alreadyLoopedAnswers == null || !alreadyLoopedAnswers.contains(answer)) {
                    sum += questionsConfig.getInt(args[1] + ".answersresults." + answer);
                    alreadyLoopedAnswers.add(answer.toString());
                }
            }

            for (Object answer : questionsConfig.getList(args[1] + ".answers")) {
                int votes = questionsConfig.getInt(args[1] + ".answersresults." + answer.toString().replaceAll("&[0-9A-Fa-f]", ""));
                double percent = 0;
                if (votes != 0) {
                    percent = (100 * votes) / sum;
                }
                allMessage += answer + " &f[" + votes + "] [" + percent + "%]\n\n";
            }

            p.sendMessage(allMessage.replace("&", "§"));
            return true;
        }

        if (args[0].equalsIgnoreCase("update")) {
            if (args.length < 2) {
                p.sendMessage(config.getString("messages.usage.update").replace("&", "§"));
                return true;
            }

            if (main.getConfig().getInt("playersdatasavetype") == 1) {
                FileConfiguration playersConfig = main.getPlayersConfig();

                playersConfig.set(args[1] + ".players", "");
                try {
                    playersConfig.save(main.getDataFolder() + File.separator + "players.yml");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                p.sendMessage(main.getConfig().getString("messages.updatesuccessful").replace("&", "§"));
                return true;

            } else {
                File dir = new File(main.getDataFolder() + File.separator + "players");
                File[] directoryListing = dir.listFiles();
                if (directoryListing != null) {
                    for (File child : directoryListing) {
                        try {

                            List<String> answeredQuestionsList = new ArrayList<>();
                            Scanner scanner = null;
                            try {
                                scanner = new Scanner(child);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            while (scanner.hasNextLine()) {
                                answeredQuestionsList.add(scanner.nextLine());
                            }

                            Collections.sort(answeredQuestionsList);

                            FileWriter stream = new FileWriter(child);
                            BufferedWriter out = new BufferedWriter(stream);

                            for (String line : answeredQuestionsList) {
                                if (line.equalsIgnoreCase(args[1])) continue;
                                out.write(line);
                                out.newLine();
                            }
                            out.close();
                            stream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                p.sendMessage(main.getConfig().getString("messages.updatesuccessful").replace("&", "§"));
                return true;
            }
        }
        return false;
    }

}
