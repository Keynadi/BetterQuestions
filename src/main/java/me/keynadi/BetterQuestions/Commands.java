package me.keynadi.BetterQuestions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

class Commands implements CommandExecutor, Serializable {
    private BQMain main;

    Commands(BQMain main) {
        this.main = main;
    }

    //shenanigans!!!
    private static boolean ifContains(List<String> list, String string) {
        for (String line : list) {
            line = line.replaceAll("&[0-9A-Fa-f]", "");
            if (line.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {

        if (args.length == 0) {
            if (!commandSender.hasPermission("betterquestions.admin")) {
                sendMessage(commandSender, "messages.nopermissions");
                return true;
            }
            return false;
        }

        Configuration config = main.getConfig();

        if (args[0].equalsIgnoreCase("answer")) {
            if (commandSender instanceof ConsoleCommandSender) {
                sendMessage(commandSender, "messages.onlyPlayerCanAnswer");
                return true;
            }
            if (args.length > 1 && args[1] != null && args[2] != null) {
                Player player = (Player) commandSender;

                String UUID = player.getUniqueId().toString();

                StringBuilder answer = new StringBuilder();

                int argscount = 2;

                while (argscount <= (args.length - 1)) {
                    answer.append(" ").append(args[argscount]);
                    argscount++;
                }

                answer = new StringBuilder(answer.substring(1));

                @SuppressWarnings("unchecked")
                List<String> answers = (List<String>) main.getQuestionsConfig().getList(args[1] + ".answers");

                FileConfiguration questionsConfig = main.getQuestionsConfig();

                //Shenanigans!!!
                assert answers != null;
                if (!ifContains(answers, answer.toString()) || !questionsConfig.getBoolean(args[1] + ".enabled")) {
                    sendMessage(player, "messages.noanswer");
                    return true;
                }

                if (config.getInt("playersdatasavetype") == 1) {
                    FileConfiguration playersConfig = main.getPlayersConfig();
                    @SuppressWarnings("unchecked")
                    List<Object> playerslist = (List<Object>) playersConfig.getList(args[1] + ".players");

                    if (playerslist == null) playerslist = new ArrayList<>();

                    if (playerslist.contains(UUID)) {
                        sendMessage(player, "messages.alreadyvoted");
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

                        assert scanner != null;
                        while (scanner.hasNextLine()) {
                            questionsList.add(scanner.nextLine());
                        }
                    }
                    if (questionsList.contains(args[1])) {
                        sendMessage(player, "messages.alreadyvoted");
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

                questionsConfig.set(args[1] + ".answersresults." + answer, questionsConfig.getInt(args[1] + ".answersresults." + answer) + 1);

                try {
                    questionsConfig.save(main.getDataFolder() + File.separator + "questions.yml");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                sendMessage(player, "messages.successfulvote");
            }
            return true;
        }

        if (!commandSender.hasPermission("betterquestions.admin")) {
            sendMessage(commandSender, "messages.nopermissions");
            return true;
        }

        if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")) {
            sendMessage(commandSender, "messages.help");
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (!config.getBoolean("active")) {
                config.set("active", true);
                main.saveConfig();
                new Timer().runTaskTimer(main, 0, config.getInt("delay"));
                sendMessage(commandSender, "messages.nowbroadcasting");
            } else {
                sendMessage(commandSender, "messages.alreadybroadcasting");
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("off")) {
            if (config.getBoolean("active")) {
                config.set("active", false);
                main.saveConfig();
                sendMessage(commandSender, "messages.nolongerbroadcasting");
            } else {
                sendMessage(commandSender, "messages.alreadystoppedbroadcasting");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            main.reloadConfig();
            main.reloadQuestionConfig();
            main.reloadPlayersConfig();
            sendMessage(commandSender, "messages.reload");
            return true;
        }

        if (args[0].equalsIgnoreCase("view")) {
            if (args.length < 2) {
                sendMessage(commandSender, "messages.usage.view");
                return true;
            }

            FileConfiguration questionsConfig = main.getQuestionsConfig();

            if (questionsConfig.getString(args[1] + ".question") == null) {
                sendMessage(commandSender, "messages.questionnotfound");
                return true;
            }

            StringBuilder allMessage = new StringBuilder(("\n\n" + questionsConfig.getString(args[1] + ".question") + "\n\n&f"));

            int sum = 0;

            //Pretty bad solution but I haven't find a way to make it more efficient
            //TODO: Make it more efficient

            List<String> alreadyLoopedAnswers = new ArrayList<>(); //Fixes votes doubling when there is two answers with different colors. This answers still counts as one but in /bq view it was a two answers with same number of votes
            for (Object answer : Objects.requireNonNull(questionsConfig.getList(args[1] + ".answers"))) {
                answer = answer.toString().replaceAll("&[0-9A-FK-ORa-fk-or]", "");
                if (!alreadyLoopedAnswers.contains(answer)) {
                    sum += questionsConfig.getInt(args[1] + ".answersresults." + answer);
                    alreadyLoopedAnswers.add(answer.toString());
                }
            }

            for (Object answer : Objects.requireNonNull(questionsConfig.getList(args[1] + ".answers"))) {
                double votes = questionsConfig.getInt(args[1] + ".answersresults." + answer.toString().replaceAll("&[0-9A-FK-ORa-fk-or]", ""));
                double percent = 0;
                if (votes != 0) {
                    percent = (100 * votes) / sum;
                }
                allMessage.append(answer).append(" &f[").append(votes).append("] [").append(percent).append("%]\n\n");
            }

            commandSender.sendMessage(allMessage.toString().replace("&", "§"));
            return true;
        }

        if (args[0].equalsIgnoreCase("update")) {
            if (args.length < 2) {
                sendMessage(commandSender, "messages.usage.update");
                return true;
            }

            removeQuestionAnswers(args, commandSender);

            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length < 2) {
                sendMessage(commandSender, "messages.usage.delete");
                return true;
            }

            FileConfiguration questionConfig = main.getQuestionsConfig();

            questionConfig.set(args[1], null);
            try {
                questionConfig.save(main.getDataFolder() + File.separator + "questions.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }

            removeQuestionAnswers(args, commandSender);


            return true;
        }

        if (args[0].equalsIgnoreCase("toggle")) {
            if (args.length < 2) {
                sendMessage(commandSender, "messages.usage.toggle");
                return true;
            }

            FileConfiguration questionConfig = main.getQuestionsConfig();

            if (questionConfig.getBoolean(args[1] + ".enabled")) {
                sendMessage(commandSender, "messages.questionNowDisabled");
                questionConfig.set(args[1] + ".enabled", false);
            } else {
                sendMessage(commandSender, "messages.questionNowEnabled");
                questionConfig.set(args[1] + ".enabled", true);
            }
            try {
                questionConfig.save(main.getDataFolder() + File.separator + "questions.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("list")) {
            Configuration questionConfig = main.getQuestionsConfig();

            for (String question : Objects.requireNonNull(questionConfig.getConfigurationSection("")).getKeys(false)) {
                String status;
                if (questionConfig.getBoolean(question + ".enabled")) {
                    status = "&aEnabled";
                } else {
                    status = "&cDisabled";
                }
                commandSender.sendMessage(("&b(" + question + ") &f(" + status + "&f) " + questionConfig.getString(question + ".question")).replace("&", "§"));
            }
            return true;
        }

        return false;
    }

    private void removeQuestionAnswers(String[] args, CommandSender p) {
        if (main.getConfig().getInt("playersdatasavetype") == 1) {
            FileConfiguration playersConfig = main.getPlayersConfig();

            playersConfig.set(args[1] + ".players", "");
            try {
                playersConfig.save(main.getDataFolder() + File.separator + "players.yml");
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMessage(p, "messages.updatesuccessful");
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

                        assert scanner != null;
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
            sendMessage(p, "messages.updatesuccessful");
        }
    }


    private void sendMessage(CommandSender sender, String path) {
        sender.sendMessage(Objects.requireNonNull(main.getConfig().getString(path)).replace("&", "§"));
    }
}
