package me.keynadi.BetterQuestions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JsonFormatter {
    private BQMain plugin;

    public JsonFormatter(BQMain plugin)
    {
        this.plugin = plugin;
    }


    private final static int CENTER_PX = 154;

    /*

    Code of sendCenteredMessage provided by SirSpoodles
    https://www.spigotmc.org/members/sirspoodles.109063/

     */

    public static void sendCenteredMessage(Player player, String message, TextComponent comp) {
        message = ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        int charIndex = 0;
        int lastSpaceIndex = 0;
        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            } else if (previousCode == true) {
                previousCode = false;
                if (c == 'l' || c == 'L') {
                    isBold = true;
                    continue;
                } else isBold = false;
            } else if (c == ' ') lastSpaceIndex = charIndex;
            else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
            charIndex++;
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        TextComponent sb = new TextComponent();
        while (compensated < toCompensate) {
            sb.addExtra(" ");
            compensated += spaceLength;
        }
        sb.addExtra(comp);
        player.spigot().sendMessage(sb);
    }

    public void format(JsonArray array, Player p) {
        TextComponent str = new TextComponent();

        String cal = "";
        for (JsonElement string : array) {
            cal = cal + string + " | ";
        }

        cal = cal.replace("\"", "");
        cal = cal.substring(0, cal.length() - 3);

        int extra = 0;

        for (JsonElement Json : array) {
            String confirmed = Json.toString().replace("\"", "").replace("&", "§");
            String answer = Json.toString().replace("\"", "");
            TextComponent text = new TextComponent(confirmed);
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bq answer " + answer));
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Send " + ChatColor.AQUA + confirmed + ChatColor.WHITE + " as an answer.").create()));
            if(extra != 0)str.addExtra(ChatColor.WHITE + " | ");
            str.addExtra(text);
            extra++;
        }

        if(plugin.getConfig().getBoolean("centering") == true){
            sendCenteredMessage(p, cal, str);
        } else {
            p.spigot().sendMessage(str);
        }
        p.sendMessage("");

    }

    public void centrify(String string, Player p) {
        TextComponent str = new TextComponent();
        String cal = "";
        cal = string.replace("\"", "");

        string = string.replace("&", "§").replace("\"", "");
        str.addExtra(string);

        p.sendMessage("");
        if(plugin.getConfig().getBoolean("centering") == true){
            sendCenteredMessage(p, cal, str);
        } else {
            p.spigot().sendMessage(str);
        }
        p.sendMessage("");
    }
}
