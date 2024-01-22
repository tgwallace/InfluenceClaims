package hardbuckaroo.influenceclaims.nation.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class NationLeave implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationLeave(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration nationData = plugin.getNationData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not a member of a city.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");
        String government = cityData.getString(cityUUID+".Government");
        String cityName = cityData.getString(cityUUID+".Name");

        if(!cityData.contains(cityUUID+".Nation")) {
            player.sendRawMessage("Your city is not part of a nation.");
            return true;
        }
        String nationUUID = cityData.getString(cityUUID+".Nation");
        String nationName = nationData.getString(nationUUID+".Name");

        if(!Objects.requireNonNull(cityData.getString(cityUUID + ".Leader")).equalsIgnoreCase(playerUUID)) {
            player.sendRawMessage("Only city leaders can call to leave a nation.");
            return true;
        }

        if(strings.length > 0 && strings[0].equalsIgnoreCase("confirm")) {
            if(Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
                cityData.set(cityUUID+".Nation",null);
                plugin.saveCityData();

                List<String> cityList = nationData.getStringList(nationUUID+".Cities");
                cityList.remove(cityUUID);
                nationData.set(nationUUID+".Cities",cityList);
                plugin.saveNationData();

                plugin.nationMessage(nationUUID,"The city of " + cityName + " has seceded from our nation!",true);
                plugin.cityMessage(cityUUID,"Our city has seceded from the nation of " + nationName + "!",true);
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                cityData.set(cityUUID+".Elections.LeaveNation.StartDate",LocalDate.now().format(formatter));
                cityData.set(cityUUID+".Elections.LeaveNation.VoteCount.Yes",0);
                cityData.set(cityUUID+".Elections.LeaveNation.VoteCount.No",0);
                plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".LeaderTitle")+ " " + player.getName() + " has called a vote to secede from the nation of " + nationName + "!",true);
            }
        } else {
            if(Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to secede from the nation? Click here to confirm.")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationleave confirm"));
                player.spigot().sendMessage(component);
            } else {
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call a vote to secede from the nation? Click here to confirm.")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationleave confirm"));
                player.spigot().sendMessage(component);
            }
        }
        plugin.saveNationData();
        plugin.saveCityData();
        return true;
    }
}
