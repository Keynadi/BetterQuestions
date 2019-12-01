package me.keynadi.BetterQuestions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class Listener implements org.bukkit.event.Listener {

    private Commands commands;
    private BQMain plugin;
    private JsonObject obj = new JsonObject();
    private int times = 1;

    public Listener(BQMain plugin, Commands commands )
    {
        this.plugin = plugin;
        this.commands = commands;
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e){
        Player sender = e.getPlayer();
        String sendername = sender.getName();
        if(commands.chat() == true){
            UUID UUID = Bukkit.getPlayer(sendername).getUniqueId();
            if (sendername.equalsIgnoreCase(plugin.getlist().get(UUID))){
                String message = e.getMessage();
                if(times == 1) {
                    obj = new JsonObject();
                    obj.addProperty("Question", message);
                    times++;
                    e.setCancelled(true);
                    sender.sendTitle(ChatColor.GREEN + "Send your asnwers in chat", "Example: Yes, No, Maybe", 10, 60, 10);
                } else {
                    String[] str = message.split("\\s*,\\s*");
                    JsonArray array = new JsonArray();
                    for(String part : str) {
                        obj.addProperty(part, 0);
                        array.add(part);
                    }

                    times = 1;
                    commands.changeChat();
                    obj.add("Answers", array);
                    e.setCancelled(true);
                }

                try {
                    Saver.save(obj, plugin.getDataFolder() + File.separator + "data.json");
                } catch (Exception except){
                    except.printStackTrace();
                }

            }
        } else {
            times = 1;
        }
    }

}
