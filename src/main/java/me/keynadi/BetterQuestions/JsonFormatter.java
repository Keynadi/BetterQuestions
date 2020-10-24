package me.keynadi.BetterQuestions;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class JsonFormatter {
    private static BQMain main;

    public JsonFormatter(BQMain main) {
        JsonFormatter.main = main;
    }

    public static void format(Player p, int questionCount) {

        Configuration questionConfig = main.getQuestionsConfig();

        TextComponent mainstr = new TextComponent();
        TextComponent answersText = new TextComponent();

        int delimitercount = 1; //Used to count until last needed delimiter. Fixing "Yes|No|" issue.

        if (questionConfig.get(questionCount + ".answers") == null) {
            return;
        }

        for (Object answer : questionConfig.getList(questionCount + ".answers")) {

            TextComponent text = new TextComponent(answer.toString().replace("&", "§"));
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bq answer " + questionCount + " " + answer.toString()));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(main.getConfig().getString("messages.hoveranswertext").replace("%answer%", answer.toString()).replace("&", "§")).create()));

            answersText.addExtra(text);

            if (delimitercount != questionConfig.getList(questionCount + ".answers").size())
                answersText.addExtra(main.getConfig().getString("delimiter").replace("&", "§"));
            delimitercount++;
        }

        String layout = (main.getConfig().getString("layout").replace("%question%", "§f" + questionConfig.getString(questionCount + ".question")).replace("&", "§"));
        String[] layoutCutArray = layout.split("%answers%");

        mainstr.addExtra(layoutCutArray[0]);
        mainstr.addExtra(answersText);
        mainstr.addExtra(layoutCutArray[1]);

        p.spigot().sendMessage(mainstr);

    }

}
