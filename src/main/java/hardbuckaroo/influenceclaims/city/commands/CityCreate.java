package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CityCreate implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityCreate(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String name = String.join(" ",strings);
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();

        //Checking for player already in city, missing arguments, and existing cities with same name:
        if(playerData.contains(player.getUniqueId().toString()+".City")){
            player.sendRawMessage("You cannot create a city when you are already a member of another city!");
            return true;
        }
        else if(strings.length < 1){
            player.sendRawMessage("You must provide a name for your city!");
            return true;
        }
        else {
            for(String string : cityData.getConfigurationSection("").getKeys(false)){
                if(cityData.getString(string+".Name").equalsIgnoreCase(name)){
                    player.sendRawMessage("A city with that name already exists.");
                    return true;
                }
            }
        }

        //Setting default cityData:
        UUID cityUUID = UUID.randomUUID();
        cityData.set(cityUUID+".Name",name);
        cityData.set(cityUUID+".Leader",player.getUniqueId().toString());
        List<String> playerList = Collections.singletonList(player.getUniqueId().toString());
        cityData.set(cityUUID+".Players",playerList);
        String color = "&f";
        cityData.set(cityUUID+".Color",color);
        cityData.set(cityUUID+".Motto"," ");
        cityData.set(cityUUID+".LeaderTitle","Duke");
        cityData.set(cityUUID+".CitizenTitle","Citizen");
        cityData.set(cityUUID+".Government","Monarchy");
        cityData.set(cityUUID+".Legitimacy",1.00);
        String cityTag = strings[0].substring(0,Math.min(strings[0].length(),9));
        cityData.set(cityUUID+".Tag",cityTag);
        plugin.saveCityData();

        //Adding player to city in playerData:
        playerData.set(player.getUniqueId().toString()+".City",cityUUID.toString());
        plugin.savePlayerData();

        plugin.updateScoreboard();

        player.sendRawMessage("You are now the leader of a new city called " + name + "!");

        return true;
    }
}
