package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.network.protocol.status.ServerStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class CityChat implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityChat(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You cannot enable city chat if you are not part of a city.");
            return true;
        }

        if(!Objects.requireNonNull(playerData.getString(playerUUID + ".ChatChannel")).equalsIgnoreCase("City")) {
            playerData.set(playerUUID+".ChatChannel","City");
            plugin.savePlayerData();
            player.sendRawMessage("Chat channel has been set to City!");
        } else if(Objects.requireNonNull(playerData.getString(playerUUID + ".ChatChannel")).equalsIgnoreCase("City")) {
            playerData.set(playerUUID+".ChatChannel","World");
            plugin.savePlayerData();
            player.sendRawMessage("Chat channel has been set to World!");
        }
        return true;
    }
}
