package me.keynadi.BetterQuestions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> commands = new ArrayList<>();
        if (strings.length == 1) {

            if (commandSender.hasPermission("betterquestions.admin")) {
                commands.add("help");
                commands.add("on");
                commands.add("off");
                commands.add("reload");
                commands.add("view");
                commands.add("create");
                commands.add("update");
                return commands;
            }

        }

        if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("view") || strings[0].equalsIgnoreCase("update")) {
                commands.add("[questionNumber]");
                return commands;
            }
        }
        return null;
    }
}
