package hardbuckaroo.influenceclaims.city.commands;
import hardbuckaroo.influenceclaims.InfluenceClaims;

import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

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
        TextComponent message = new TextComponent(plugin.color(cityData.getString(city + ".Color")+"&m                                                     "));
        //Name of city:
        message.addExtra(plugin.color("\n"+cityData.getString(city + ".Color") + "&l" + cityData.getString(city + ".Name")));
        //City motto:
        if(cityData.contains(city+".Motto"))
            message.addExtra(plugin.color("\n&o"+cityData.getString(city+".Motto")));
        //City government:
        message.addExtra(plugin.color("\nGovernment: "+ WordUtils.capitalize(cityData.getString(city+".Government")) + " (" + String.format("%.0f%%",100*cityData.getDouble(city+".Legitimacy"))+" Legitimacy)"));
        //City leader and title:
        message.addExtra("\n"+cityData.getString(city+".LeaderTitle")+": "+ Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(city+".Leader"))).getName());
        //Building and adding list of roles:
        if(cityData.contains(city+".Roles")) {
            for (String role : cityData.getConfigurationSection(city + ".Roles").getKeys(false)) {
                String roleString = "";
                for (String rolePlayer : cityData.getStringList(city + ".Roles." + role)) {
                    roleString = roleString.concat(Bukkit.getOfflinePlayer(UUID.fromString(rolePlayer)).getName() + ", ");
                }
                if (roleString.length() > 2) {
                    roleString = roleString.substring(0, roleString.length() - 2);
                }
                int roleCount = cityData.getStringList(city + ".Roles." + role).size();
                message.addExtra("\n" + role + " ("+roleCount+"): " + roleString);
            }
        }
        //Building and adding list of regular citizens:
        List<String> memberList = cityData.getStringList(city+".Players");
        String memberString = "";
        for(String member : memberList){
            memberString = memberString.concat(Bukkit.getOfflinePlayer(UUID.fromString(member)).getName()+", ");
        }
        if(memberString.length() > 2)
            memberString = memberString.substring(0,memberString.length()-2);
        message.addExtra("\n"+cityData.getString(city+".CitizenTitle")+" ("+memberList.size()+"): "+ memberString);
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
                message.addExtra("\nFriendly Cities: " + friendlyString);
            }
            if(hostileString.length() > 2) {
                hostileString = hostileString.substring(0,hostileString.length()-2);
                message.addExtra("\nHostile Cities: " + hostileString);
            }
        }
        //Ending with solid line in city's color.
        message.addExtra(plugin.color(cityData.getString(city + ".Color")+"\n&m                                                     "));
        //Sending message to player.
        player.spigot().sendMessage(message);

        return true;
    }
}
