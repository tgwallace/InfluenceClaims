package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PopulationList implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PopulationList(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration cityData = plugin.getCityData();

        if(strings.length == 0) {
            player.sendRawMessage("Please name the city you would like the population list for.");
            return true;
        }
        String cityName = String.join(" ",strings);
        String cityUUID = plugin.getCityUUIDFromName(cityName);
        if(cityUUID == null) {
            player.sendRawMessage("Could not find a city named " + cityName + ". Please check your spelling and try again.");
        } else {
            List<String> memberList = cityData.getStringList(cityUUID+".Players");
            String memberString = "";
            for(String member : memberList){
                memberString = memberString.concat(Bukkit.getOfflinePlayer(UUID.fromString(member)).getName()+", ");
            }
            if(memberString.length() > 2)
                memberString = memberString.substring(0,memberString.length()-2);
            player.sendRawMessage("\n"+cityData.getString(cityUUID+".CitizenTitle")+" ("+memberList.size()+"): "+ memberString);
        }
        return true;
    }
}
