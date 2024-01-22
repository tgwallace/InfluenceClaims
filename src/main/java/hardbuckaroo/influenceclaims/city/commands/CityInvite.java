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

public class CityInvite implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityInvite(InfluenceClaims plugin){
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
                if (cityData.getStringList(cityUUID + ".Roles.Players").contains(senderUUID)) {
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
            else if (playerData.contains(recipient.getUniqueId().toString() + ".City")) {
                sender.sendRawMessage("You cannot invite a player to your city if they are already part of another city!");
            } else {
                playerData.set(recipient.getUniqueId().toString() + ".Invites." + cityUUID, true);
                plugin.savePlayerData();
                Player messageRecipient = Bukkit.getPlayer(name);
                //Checks whether player is online to receive a message.
                if(messageRecipient != null) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "You have been invited to join &l" + cityData.getString(cityUUID + ".Name") + "&r. Click here to accept.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityaccept " + cityData.getString(cityUUID + ".Name")));
                    messageRecipient.spigot().sendMessage(component);
                } else {
                    //Adds invitation to their messages in playerData.yml.
                    plugin.playerMessage(recipient.getUniqueId().toString(),"You have been invited to join &l" + cityData.getString(cityUUID + ".Name") + "&r. Use /CityAccept to review your pending city invitations.");
                }
                sender.sendRawMessage(name + " has been invited to join " + cityData.getString(cityUUID + ".Name"));
            }
        }
        return true;
    }
}
