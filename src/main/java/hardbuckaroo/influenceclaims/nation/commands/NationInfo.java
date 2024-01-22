package hardbuckaroo.influenceclaims.nation.commands;

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
import java.util.Objects;
import java.util.UUID;

public class NationInfo implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public NationInfo(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String uuid = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();
        String nationUUID;

        if(strings.length == 0){
            if(playerData.contains(uuid+".City") && cityData.contains(playerData.getString(uuid+".City")+".Nation")) {
                nationUUID = cityData.getString(playerData.getString(uuid+".City")+".Nation");
            }
            else {
                player.sendRawMessage("You are not part of a nation. Please specify which nation you would like information for.");
                return true;
            }
        } else {
            String name = String.join(" ",strings);
            nationUUID = plugin.getNationUUIDFromName(name);

            if(nationUUID == null){
                player.sendRawMessage("Could not find a nation named " + name + ". Please check your spelling and try again.");
                return true;
            }
        }

        //Building NationInfo message box:
        //Opening with solid line in nation's color.
        TextComponent message = new TextComponent(plugin.color(nationData.getString(nationUUID + ".Color")+"&m                                                     "));
        //Name of nation:
        message.addExtra(plugin.color("\n"+nationData.getString(nationUUID + ".Color") + "&l" + nationData.getString(nationUUID + ".Name")));
        //Nation motto:
        message.addExtra(plugin.color("\n&o"+nationData.getString(nationUUID+".Motto")));
        //Nation government:
        message.addExtra(plugin.color("\nGovernment: "+ WordUtils.capitalize(nationData.getString(nationUUID+".Government")) + " (" + String.format("%.0f%%",100*cityData.getDouble(nationUUID+".Legitimacy"))+" Legitimacy)"));
        //Nation leader and title:
        message.addExtra("\n"+nationData.getString(nationUUID+".LeaderTitle")+": "+ Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(nationData.getString(nationUUID + ".Leader")))).getName());
        //Building and adding list of nobles:
        List<String> nobleList = nationData.getStringList(nationUUID+".Nobles");
        String nobleString = "";
        for(String noble : nobleList){
            if(!noble.equals(nationData.getString(nationUUID+".Leader")))
                nobleString = nobleString.concat(Bukkit.getOfflinePlayer(UUID.fromString(noble)).getName()+", ");
        }
        if(nobleString.length() > 2)
            nobleString = nobleString.substring(0,nobleString.length()-2);
        message.addExtra("\n"+nationData.getString(nationUUID+".NobilityTitle")+": "+ nobleString);
        //Building and adding list of regular citizens:
        List<String> memberList = nationData.getStringList(nationUUID+".Cities");
        String memberString = "";
        for(String member : memberList){
            memberString = memberString.concat(cityData.getString(member+".Name")+", ");
        }
        if(memberString.length() > 2)
            memberString = memberString.substring(0,memberString.length()-2);
        message.addExtra("\nCities: "+ memberString);
        //Ending with solid line in nation's color.
        message.addExtra(plugin.color(nationData.getString(nationUUID + ".Color")+"\n&m                                                     "));
        //Sending message to player.
        player.spigot().sendMessage(message);

        return true;
    }
}
