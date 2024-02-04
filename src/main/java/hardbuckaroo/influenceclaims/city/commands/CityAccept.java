package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CityAccept implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityAccept(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player player = (Player) commandSender;
        String playerID = player.getUniqueId().toString();

        //Checks if the player has invites to a city.
        if(!playerData.contains(playerID+".Invites")) {
            player.sendRawMessage("You do not have any city invites right now. Start your own city using /citycreate or ask around for a city to join!");
            return true;
        } else if (strings.length == 0){
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&l&n Click the option below to choose which invite to accept:")));
            player.spigot().sendMessage(component);

            //Sends the player a list of clickable invites.
            for(String invite : playerData.getConfigurationSection(playerID+".Invites").getKeys(false)) {
                component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "You have been invited to join &l" + cityData.getString(invite + ".Name") + "&r. Click here to accept.")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityaccept " + invite));
                player.spigot().sendMessage(component);
            }
            return true;
        } else {
            //Should only get to this branch if player clicks invite to execute command sent from above branch.
            String invite = strings[0];
            Set<String> inviteList = playerData.getConfigurationSection(playerID+".Invites").getKeys(false);

            try{
                UUID uuid = UUID.fromString(invite);
            } catch (IllegalArgumentException exception){
                invite = plugin.getCityUUIDFromName(invite);
            }

            if(!inviteList.contains(invite)) {
                player.sendRawMessage("You do not have an invite from that city.");
            } else {
                //Set city in player data.
                playerData.set(playerID + ".City", invite);
                playerData.set(playerID + ".Invites", null);
                plugin.savePlayerData();

                //Add player to city data.
                List<String> players = cityData.getStringList(invite+".Players");
                players.add(playerID);
                cityData.set(invite + ".Players",players);
                plugin.saveCityData();

                //Lets city members know that a new member has joined.
                plugin.cityMessage(invite,cityData.getString(invite+".Color")+player.getName()+" has joined " + cityData.getString(invite+".Name") + "!");

                plugin.updateScoreboard();
            }
            return true;
        }
    }
}
