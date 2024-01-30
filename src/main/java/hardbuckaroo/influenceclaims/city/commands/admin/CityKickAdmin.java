package hardbuckaroo.influenceclaims.city.commands.admin;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageCityLegitimacy;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class CityKickAdmin implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityKickAdmin(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(plugin);

        if(strings.length < 2) {
            commandSender.sendMessage("Must provide the amount to boost and the name of the city.");
            return false;
        }
        String cityName = String.join(" ",strings);
        cityName = cityName.substring(cityName.indexOf(" ")+1);
        String cityUUID = plugin.getCityUUIDFromName(cityName);
        if(cityUUID == null) {
            sender.sendRawMessage("Could not find a city named " + cityName + ". Please check your spelling and try again.");
            return true;
        }

        String name = strings[0];
        OfflinePlayer recipient = Bukkit.getOfflinePlayer(name);
        if (recipient.getUniqueId().equals(sender.getUniqueId())) {
            sender.sendRawMessage("You cannot kick yourself from a city. Use /CityLeave to leave instead.");
        } else if (!cityData.contains(cityUUID + ".Players." + recipient.getUniqueId().toString())) {
            sender.sendRawMessage("Could not locate a player named " + name + " in " + cityData.getString(cityUUID + ".Name") + ".");
        } else {
            //Remove player in cityData:
            cityData.set(cityUUID + ".Players." + recipient.getUniqueId().toString(), null);
            plugin.saveCityData();
            //Remove player in playerData:
            playerData.set(recipient.getUniqueId().toString() + ".City", null);
            playerData.set(recipient.getUniqueId().toString() + ".PlotMode", false);
            playerData.set(recipient.getUniqueId().toString() + ".PlotCorner1", null);
            plugin.savePlayerData();
            //Message removed player if they're online, set a message in playerData if they're not:
            Player messageRecipient = Bukkit.getPlayer(name);
            if (messageRecipient != null) {
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "You have been kicked out of &l" + cityData.getString(cityUUID + ".Name") + "&r!")));
                messageRecipient.spigot().sendMessage(component);
            } else {
                plugin.playerMessage(recipient.getUniqueId().toString(), plugin.color("You have been kicked out of &l" + cityData.getString(cityUUID + ".Name") + "&r!"));
            }
            sender.sendRawMessage(name + " has been kicked out of " + cityData.getString(cityUUID + ".Name") + " by an admin.");
            manageCityLegitimacy.subtractLegitimacy(cityUUID, 0.10);
            plugin.updateScoreboard();
        }
        return true;
    }
}
