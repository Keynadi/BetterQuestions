package me.keynadi.BetterQuestions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {

    @SuppressWarnings("NullableProblems")
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
                commands.add("update");
                commands.add("delete");
                commands.add("list");
                commands.add("toggle");
                return commands;
            }

        }

        if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("view")
                    || strings[0].equalsIgnoreCase("update")
                    || strings[0].equalsIgnoreCase("delete")
                    || strings[0].equalsIgnoreCase("toggle")) {
                commands.add("[questionID]");
                return commands;
            }
        }
        return null;
    }
}
