package me.keynadi.BetterQuestions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.$Gson$Types;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.UUID;

class Commands implements CommandExecutor, Serializable {
    private BQMain plugin;
    private JsonFormatter formatter;

    private static boolean chat = false;
    private CommandSender p;
    private String sender;
    private JsonObject obj;
    private JsonObject players = new JsonObject();
    private boolean run = false;
    private BukkitTask runnable;
    private JsonArray playersArray = new JsonArray();

    public Commands(BQMain plugin, JsonFormatter formatter)
    {
        this.plugin = plugin;
        this.formatter = formatter;
    }

    @Override
    public boolean onCommand(CommandSender p, Command command, String s, String[] args) {

        if (args.length == 0) {
            if(!p.hasPermission("betterquestions.admin")){
                p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
                return true;
            }
            return false;
        }

        if(args[0].equalsIgnoreCase("answer")){
            if(args.length > 1 && args[1] != null){
                UUID UUID = Bukkit.getPlayer(p.getName()).getUniqueId();
                try {
                    players = (JsonObject) Saver.load(plugin.getDataFolder() + File.separator + "players.json");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                playersArray.add(players.getAsJsonArray("Players"));
                JsonElement JsonUUID = new JsonParser().parse(UUID.toString());
                if(playersArray == null || !playersArray.contains(JsonUUID)){
                    playersArray.add(String.valueOf(UUID));
                    players.add("Players", playersArray);
                    try {
                        Saver.save(players, plugin.getDataFolder() + File.separator + "players.json");;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "You already voted!");
                    return true;
                }

                try {
                    obj = (JsonObject) Saver.load(plugin.getDataFolder() + File.separator + "data.json");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String arg = args[1];
                int i = 2;
                while (args.length > i && args[i] != null){
                    arg = arg + " " + args[i];
                    i++;
                }

                if(obj.has(arg)){
                    int num = Integer.valueOf(String.valueOf(obj.get(arg)));
                    num++;
                    obj.addProperty(arg, num);
                    try {
                        Saver.save(obj, plugin.getDataFolder() + File.separator + "data.json");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Error. Please, contact server Administrator.");
                }
            }
            return true;
        }

        if(!p.hasPermission("betterquestions.admin")){
            p.sendMessage(ChatColor.RED + "You don't have permissions to do that!");
            return true;
        }

        if(args.length == 0){
            return false;
        }

        if(args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?")){
            p.sendMessage(("&f/bq create - &ccreate new question\n" +
                    "&f/bq on|off - &cturn on/off plugin notifies for player\n" +
                    "&f/bq rl - &creload configuration file\n" +
                    "&f/bq view - &cview question statistic").replace("&", "§"));
            return true;
        }
        if(args[0].equalsIgnoreCase("create")){
            chat = true;

            sender = p.getName();
            UUID UUID = Bukkit.getPlayer(sender).getUniqueId();

            Bukkit.getPlayer(UUID).sendTitle(ChatColor.GREEN + "Send your question in chat", "", 10, 80, 10);;

            plugin.getlist().put(UUID, sender);
            return true;
        }
        if(args[0].equalsIgnoreCase("on")){
            if(run == false){
                run = true;
                run();
                p.sendMessage(ChatColor.GREEN + "Questions are now broadcasting.");
            } else {
                p.sendMessage(ChatColor.RED + "Nothing happened. Questions already broadcasting.");
            }
            return true;
        }
        if(args[0].equalsIgnoreCase("off")){
            if(run == true){
                p.sendMessage(ChatColor.GREEN + "Questions no longer broadcasting.");
                run = false;
                runnable.cancel();
            } else {
                p.sendMessage(ChatColor.RED + "Nothing happened. Questions weren't broadcasting.");
            }
            return true;
        }

        if(args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")){
            plugin.reloadConfig();
            p.sendMessage(ChatColor.GREEN + "Config has been reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("view")){
            try {
                obj = (JsonObject) Saver.load(plugin.getDataFolder() + File.separator + "data.json");
            } catch (Exception e) {
                e.printStackTrace();
            }
            JsonArray loop = obj.getAsJsonArray("Answers");
            p.sendMessage("");
            p.sendMessage(obj.get("Question").getAsString().replace("&", "§"));
            int sum = 0;
            for (JsonElement elem : loop){
                sum = sum + obj.get(elem.getAsString()).getAsInt();
            }
            p.sendMessage("");
            double percent = 0;
            for(JsonElement str : loop){
                percent = (100 * obj.get(str.getAsString()).getAsInt()) / sum;
                p.sendMessage((str.getAsString() +  " &f[" +  obj.get(str.getAsString()) + "] " + "[" + percent + "%]").replace("&", "§"));
            }
            p.sendMessage("");
            return true;
        }

        return false;
    }

    public void changeChat(){
        chat = false;
    }

    public boolean chat(){
        return chat;
    }

    private void run(){

        try {
            obj = (JsonObject) Saver.load(plugin.getDataFolder() + File.separator + "data.json");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonArray array = obj.getAsJsonArray("Answers");

        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getOnlinePlayers()){
                    formatter.centrify(String.valueOf(obj.get("Question")), p);
                    formatter.format(array, p);
                }
            }
        }.runTaskTimer(plugin, 0, plugin.getConfig().getInt("delay"));
    }

}
