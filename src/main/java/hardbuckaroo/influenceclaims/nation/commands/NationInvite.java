package hardbuckaroo.influenceclaims.nation.commands;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class NationInvite implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationInvite(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        //Checks if sender is part of a city.
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city or a nation. Only nation leaders can invite cities to nations.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        if(!cityData.contains(cityUUID+".Nation")) {
            sender.sendRawMessage("You are not part of a nation. Only nation leaders can invite cities to nations.");
            return true;
        }
        String nationUUID = cityData.getString(cityUUID+".Nation");

        //Checks whether the sender has permission to add cities to their nation.
        if(!Objects.requireNonNull(nationData.getString(nationUUID + ".Leader")).equalsIgnoreCase(senderUUID)){
            sender.sendRawMessage("Only the " + nationData.getString(cityUUID+".LeaderTitle") + " can invite cities to the nation.");
            return true;
        }

        String inviteeName;
        String inviteeUUID;

        if (strings.length == 0) {
            sender.sendRawMessage("Please name the city you would like to invite!");
            return true;
        }
        else if(!cityData.contains(strings[0])) {
            inviteeName = String.join(" ", strings);
            inviteeUUID = plugin.getCityUUIDFromName(inviteeName);
        } else {
            inviteeUUID = strings[0];
            inviteeName = cityData.getString(inviteeUUID+".Name");
        }

        if(inviteeUUID == null){
            sender.sendRawMessage("Could not locate a city named " + inviteeName + ". Please check your spelling and try again.");
        }
        else if (cityData.contains(inviteeUUID + ".Nation")) {
            sender.sendRawMessage("You cannot invite a city to your nation if they are already part of another nation!");
        } else {
            if(Objects.requireNonNull(nationData.getString(nationUUID + ".Government")).equalsIgnoreCase("Monarchy")) {
                List<String> inviteList = cityData.getStringList(inviteeUUID+".NationInvites");
                inviteList.add(nationUUID);
                cityData.set(inviteeUUID+".NationInvites",inviteList);

                List<String> invitees = nationData.getStringList(nationUUID+".Invitees");
                invitees.add(inviteeUUID);
                nationData.set(nationUUID+".Invitees",invitees);
                plugin.playerMessage(cityData.getString(inviteeUUID+".Leader"),inviteeName + " has been invited to join the nation of " + nationData.getString(nationUUID+".Name") + ". Use /NationAccept to review all outstanding nation invites.");
                plugin.nationMessage(nationUUID, "The city of " + inviteeName + " has been invited to join our nation!", false);
            } else {
                if(strings.length == 2 && strings[1].equalsIgnoreCase("confirm")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    nationData.set(nationUUID + ".Elections.AddCity.StartDate", LocalDate.now().format(formatter));
                    nationData.set(nationUUID + ".Elections.AddCity.VoteCount.Yes", 0);
                    nationData.set(nationUUID + ".Elections.AddCity.VoteCount.No", 0);
                    nationData.set(nationUUID + ".Elections.AddCity.City", inviteeUUID);
                    plugin.nationMessage(nationUUID,nationData.getString(nationUUID+".LeaderTitle") + " " + sender.getName() + " has called a vote to add the city of " + inviteeName + " to the nation! If you are eligible, use /NationVote to vote.",true);
                } else {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call a vote to invite the city of " + inviteeName + " to the nation? Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationinvite "+inviteeUUID+" confirm"));
                    sender.spigot().sendMessage(component);
                }
            }
        }
        plugin.saveCityData();
        plugin.saveNationData();
        return true;
    }
}
