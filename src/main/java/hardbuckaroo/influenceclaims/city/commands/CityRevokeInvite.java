package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CityRevokeInvite implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityRevokeInvite(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        //Checks if sender is part of a city.
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city. Create a city using /CityCreate [city name] to create a city and start adding members.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        boolean perms = false;
        if(cityData.contains(cityUUID+".Roles")) {
            for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                if (cityData.getStringList(cityUUID + ".Roles."+title+".Players").contains(senderUUID)) {
                    perms = cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions.Invite");
                }
                if(perms) break;
            }
        }

        //Checks whether the sender has permission to add players to their city.
        if(!cityData.getString(cityUUID+".Leader").equalsIgnoreCase(senderUUID) && !perms){
            sender.sendRawMessage("You do not have permission to invite players to the city!");
            return true;
        }

        //Runs through all names listed in arguments.
        for(String name : strings) {
            OfflinePlayer recipient = Bukkit.getOfflinePlayer(name);
            if(!playerData.contains(recipient.getUniqueId().toString())){
                sender.sendRawMessage("Could not locate a player named " + name + ". Players must have logged in to the server at least once before to be added to a city.");
            }
            else if (!playerData.contains(recipient.getUniqueId().toString() + ".Invites."+cityUUID)) {
                sender.sendRawMessage(name + " has not been invited to join our city!");
            } else {
                playerData.set(recipient.getUniqueId().toString() + ".Invites." + cityUUID, null);
                plugin.savePlayerData();
                Player messageRecipient = Bukkit.getPlayer(name);
                //Checks whether player is online to receive a message.
                if(messageRecipient != null) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Your invite to join &l" + cityData.getString(cityUUID + ".Name") + "&r has been revoked.")));
                    messageRecipient.spigot().sendMessage(component);
                } else {
                    //Adds invitation to their messages in playerData.yml.
                    plugin.playerMessage(recipient.getUniqueId().toString(),"Your invite to join &l" + cityData.getString(cityUUID + ".Name") + "&r has been revoked.");
                }
                sender.sendRawMessage(name + " has had their invite to join " + cityData.getString(cityUUID + ".Name") + " revoked!");
            }
        }
        return true;
    }
}
