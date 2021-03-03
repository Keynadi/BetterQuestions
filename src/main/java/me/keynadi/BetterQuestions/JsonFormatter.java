package me.keynadi.BetterQuestions;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.Objects;

class JsonFormatter {
    private static BQMain main;

    JsonFormatter(BQMain main) {
        JsonFormatter.main = main;
    }

    static void format(Player p, String questionID) {

        Configuration questionConfig = main.getQuestionsConfig();

        if (questionConfig.get(questionID + ".answers") == null) {
            return;
        }

        TextComponent mainstr = new TextComponent();
        TextComponent answersText = new TextComponent();

        int delimitercount = 1; //Used to count until last needed delimiter. Fixing "Yes|No|" issue.

        for (Object answer : Objects.requireNonNull(questionConfig.getList(questionID + ".answers"))) {

            TextComponent text = new TextComponent(answer.toString().replace("&", "§"));
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bq answer " + questionID + " " + answer.toString()));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Objects.requireNonNull(main.getConfig().getString("messages.hoveranswertext")).replace("%answer%", answer.toString()).replace("&", "§")).create()));

            answersText.addExtra(text);

            if (delimitercount != Objects.requireNonNull(questionConfig.getList(questionID + ".answers")).size())
                answersText.addExtra(Objects.requireNonNull(main.getConfig().getString("delimiter")).replace("&", "§"));
            delimitercount++;
        }

        String layout = (Objects.requireNonNull(main.getConfig().getString("layout")).replace("%question%", "§f" + questionConfig.getString(questionID + ".question")).replace("&", "§"));
        String[] layoutCutArray = layout.split("%answers%");

        mainstr.addExtra(layoutCutArray[0]);
        mainstr.addExtra(answersText);
        mainstr.addExtra(layoutCutArray[1]);

        p.spigot().sendMessage(mainstr);

    }

}
