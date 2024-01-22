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

public class NationKick implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationKick(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration nationData = plugin.getNationData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        //Check whether player is part of a city:
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        if(!cityData.contains(cityUUID+".Nation")) {
            sender.sendRawMessage("You are not part of a nation.");
            return true;
        }
        String nationUUID = cityData.getString(cityUUID+".Nation");

        //Check whether player has permission to kick members out of the city:
        if(!Objects.requireNonNull(nationData.getString(nationUUID + ".Leader")).equalsIgnoreCase(senderUUID)){
            sender.sendRawMessage("Only the " + nationData.getString(nationUUID+".LeaderTitle") + " can kick cities out of the nation.");
            return true;
        }

        String kickName;
        String kickUUID;
        if(!cityData.contains(strings[0])) {
            kickName = String.join(" ", strings);
            kickUUID = plugin.getCityUUIDFromName(kickName);
        } else {
            kickUUID = strings[0];
            kickName = cityData.getString(kickUUID+".Name");
        }

        if(kickUUID == null || !nationData.getStringList(nationUUID+".Cities").contains(kickUUID)){
            sender.sendRawMessage("Could not locate a city named " + kickName + " in the nation. Please check your spelling and try again.");
        }

        if(Objects.requireNonNull(nationData.getString(nationUUID + ".Government")).equalsIgnoreCase("Monarchy")) {
            if(strings.length == 2 && strings[1].equalsIgnoreCase("confirm")) {
                cityData.set(kickUUID+".Nation",null);
                plugin.saveCityData();

                List<String> cityList = nationData.getStringList(nationUUID+".Cities");
                cityList.remove(kickUUID);
                nationData.set(nationUUID+".Cities",cityList);
                plugin.saveNationData();
                plugin.nationMessage(nationUUID, nationData.getString(nationUUID+".LeaderTitle") + " " + sender.getName() + " has kicked the city of " + kickName + " out of the nation!",true);
                plugin.cityMessage(kickUUID, nationData.getString(nationUUID+".LeaderTitle") + " " + sender.getName() + " has kicked the city of " + kickName + " out of the nation of " + nationData.getString(nationUUID+".Name")+"!",true);
            } else {
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call a vote to kick the city of " + kickName + " out of the nation? Click here to confirm.")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationkick "+kickUUID+" confirm"));
                sender.spigot().sendMessage(component);
            }
        } else {
            if(strings.length == 2 && strings[1].equalsIgnoreCase("confirm")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                nationData.set(nationUUID + ".Elections.KickCity.StartDate", LocalDate.now().format(formatter));
                nationData.set(nationUUID + ".Elections.KickCity.VoteCount.Yes", 0);
                nationData.set(nationUUID + ".Elections.KickCity.VoteCount.No", 0);
                nationData.set(nationUUID + ".Elections.KickCity.City", kickUUID);
                plugin.nationMessage(nationUUID,nationData.getString(nationUUID+".LeaderTitle") + " " + sender.getName() + " has called a vote to kick the city of " + kickName + " out of the nation! If you are eligible, use /NationVote to vote.",true);
            } else {
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call a vote to kick the city of " + kickName + " out of the nation? Click here to confirm.")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationkick "+kickUUID+" confirm"));
                sender.spigot().sendMessage(component);
            }
        }

        return true;
    }
}
