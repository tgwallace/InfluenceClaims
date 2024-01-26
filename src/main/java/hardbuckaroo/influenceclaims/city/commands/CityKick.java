package hardbuckaroo.influenceclaims.city.commands;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CityKick implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityKick(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(plugin);

        //Check whether player is part of a city:
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");
        List<String> nobles = cityData.getStringList(cityUUID+".Nobles");

        boolean perms = false;
        if(cityData.contains(cityUUID+".Roles")) {
            for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                if (cityData.getStringList(cityUUID + ".Roles.Players").contains(senderUUID)) {
                    perms = cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions.Kick");
                }
                if(perms) break;
            }
        }

        //Check whether player has permission to kick members out of the city:
        if(!Objects.requireNonNull(cityData.getString(cityUUID + ".Leader")).equalsIgnoreCase(senderUUID) && !nobles.contains(senderUUID)){
            sender.sendRawMessage("You do not have permission to kick players out of the city.");
            return true;
        }

        //Run through all names listed to be kicked:
        for(String name : strings) {
            OfflinePlayer recipient = Bukkit.getOfflinePlayer(name);
            if (recipient.getUniqueId().equals(sender.getUniqueId())){
                sender.sendRawMessage("You cannot kick yourself from a city. Use /CityLeave to leave instead.");
            } else if(!cityData.contains(cityUUID + ".Players."+recipient.getUniqueId().toString())){
                sender.sendRawMessage("Could not locate a player named " + name + " in " + cityData.getString(cityUUID+".Name") + ".");
            } else {
                //Make sure other player doesn't have a role:
                if(cityData.contains(cityUUID+".Roles")) {
                    for(String role : cityData.getConfigurationSection(cityUUID+".Roles").getKeys(false)) {
                        if(cityData.getStringList(cityUUID+".Roles."+role+".Players").contains(recipient.getUniqueId().toString())) {
                            sender.sendRawMessage("Only the " + cityData.getString(cityUUID + ".Leader") + " can kick players who have roles!");
                            return true;
                        }
                    }
                }

                //Remove player in cityData:
                cityData.set(cityUUID + ".Players."+recipient.getUniqueId().toString(),null);
                plugin.saveCityData();
                //Remove player in playerData:
                playerData.set(recipient.getUniqueId().toString()+".City",null);
                playerData.set(recipient.getUniqueId().toString()+".PlotMode",false);
                playerData.set(recipient.getUniqueId().toString()+".PlotCorner1",null);
                plugin.savePlayerData();

                //Message removed player if they're online, set a message in playerData if they're not:
                Player messageRecipient = Bukkit.getPlayer(name);
                if(messageRecipient != null) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "You have been kicked out of &l" + cityData.getString(cityUUID + ".Name") + "&r!")));
                    messageRecipient.spigot().sendMessage(component);
                } else {
                    plugin.playerMessage(recipient.getUniqueId().toString(),plugin.color("You have been kicked out of &l" + cityData.getString(cityUUID + ".Name") + "&r!"));
                }
                sender.sendRawMessage(name + " has been kicked out of " + cityData.getString(cityUUID + ".Name")+".");
                manageCityLegitimacy.subtractLegitimacy(cityUUID,0.10);
                plugin.updateScoreboard();
            }
        }
        return true;
    }
}
