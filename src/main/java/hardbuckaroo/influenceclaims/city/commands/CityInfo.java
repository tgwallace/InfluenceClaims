package hardbuckaroo.influenceclaims.city.commands;
import hardbuckaroo.influenceclaims.InfluenceClaims;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.NumberFormat;
import java.util.List;
import java.util.UUID;

public class CityInfo implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityInfo(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String uuid = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();
        String city;

        if(strings.length == 0){
            if(playerData.contains(uuid+".City")) {
                city = playerData.getString(uuid+".City");
            }
            else {
                player.sendRawMessage("You are not part of a city. Please specify which city you would like information for.");
                return true;
            }
        } else {
            String name = String.join(" ",strings);
            city = plugin.getCityUUIDFromName(name);

            if(city == null){
                player.sendRawMessage("Could not find a city named " + name + ". Please check your spelling and try again.");
                return true;
            }
        }

        //Building CityInfo message box:
        //Opening with solid line in city's color.
        TextComponent topLine = new TextComponent(plugin.color(cityData.getString(city + ".Color")+"&m                                                     "));
        player.spigot().sendMessage(topLine);
        //Name of city:
        TextComponent cityName = new TextComponent(plugin.color(cityData.getString(city + ".Color") + "&l" + cityData.getString(city + ".Name")));
        player.spigot().sendMessage(cityName);
        //City motto:
        if(cityData.contains(city+".Motto") && !cityData.getString(city+".Motto").equalsIgnoreCase(" ")) {
            TextComponent motto = new TextComponent(plugin.color("&o" + cityData.getString(city + ".Motto")));
            player.spigot().sendMessage(motto);
        }
        //Total Influence:
        TextComponent influence = new TextComponent("Total Influence: " + NumberFormat.getInstance().format(cityData.getLong(city+".TotalInfluence")));
        player.spigot().sendMessage(influence);
        //Nation
        if(cityData.contains(city+".Nation")) {
            String nationUUID = cityData.getString(city+".Nation");
            String nationName =  nationData.getString(nationUUID+".Name");
            String nationColor = nationData.getString(nationUUID+".Color");
            TextComponent nation = new TextComponent("Nation: " + plugin.color(nationColor + nationName));
            nation.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/nationinfo " + nationName));
            player.spigot().sendMessage(nation);
        }
        //City government:
        TextComponent government = new TextComponent(plugin.color("Government: "+ WordUtils.capitalize(cityData.getString(city+".Government")) + " (" + String.format("%.0f%%",100*cityData.getDouble(city+".Legitimacy"))+" Legitimacy)"));
        player.spigot().sendMessage(government);
        //City leader and title:
        TextComponent leader = new TextComponent(cityData.getString(city+".LeaderTitle")+": "+ Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(city+".Leader"))).getName());
        player.spigot().sendMessage(leader);
        //Building and adding list of roles:
        if(cityData.contains(city+".Roles")) {
            for (String role : cityData.getConfigurationSection(city + ".Roles").getKeys(false)) {
                String roleString = "";
                for (String rolePlayer : cityData.getStringList(city + ".Roles." + role + ".Players")) {
                    roleString = roleString.concat(Bukkit.getOfflinePlayer(UUID.fromString(rolePlayer)).getName() + ", ");
                }
                if (roleString.length() > 2) {
                    roleString = roleString.substring(0, roleString.length() - 2);
                }
                int roleCount = cityData.getStringList(city + ".Roles." + role + ".Players").size();
                TextComponent roleMessage = new TextComponent(role + " ("+roleCount+"): " + roleString);
                player.spigot().sendMessage(roleMessage);
            }
        }
        //Building and adding list of regular citizens:
        List<String> memberList = cityData.getStringList(city+".Players");
        TextComponent citizens = new TextComponent(cityData.getString(city+".CitizenTitle")+" ("+memberList.size()+") "+plugin.color("&oClick for list!"));
        citizens.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/citypopulationlist "+cityData.getString(city+".Name")));
        player.spigot().sendMessage(citizens);
        //Building and adding list of friendly/hostile cities
        if(cityData.contains(city+".Stances")) {
            String friendlyString = "";
            String hostileString = "";
            for (String stanceCity : cityData.getConfigurationSection(city + ".Stances").getKeys(false)) {
                String stance = cityData.getString(city+".Stances."+stanceCity);
                if(stance != null && stance.equalsIgnoreCase("Friendly")) {
                    friendlyString = friendlyString.concat(cityData.getString(stanceCity+".Name") + ", ");
                } else if (stance != null && stance.equalsIgnoreCase("Hostile")) {
                    hostileString = hostileString.concat(cityData.getString(stanceCity+".Name") + ", ");
                }
            }
            if(friendlyString.length() > 2) {
                friendlyString = friendlyString.substring(0,friendlyString.length()-2);
                TextComponent friendly = new TextComponent("Friendly Cities: " + friendlyString);
                player.spigot().sendMessage(friendly);
            }
            if(hostileString.length() > 2) {
                hostileString = hostileString.substring(0,hostileString.length()-2);
                TextComponent hostile = new TextComponent("Hostile Cities: " + hostileString);
                player.spigot().sendMessage(hostile);
            }
        }
        //Ending with solid line in city's color.
        TextComponent endLine = new TextComponent(plugin.color(cityData.getString(city + ".Color")+"&m                                                     "));
        player.spigot().sendMessage(endLine);

        return true;
    }
}
