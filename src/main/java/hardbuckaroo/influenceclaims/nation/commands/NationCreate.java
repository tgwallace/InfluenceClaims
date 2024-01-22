package hardbuckaroo.influenceclaims.nation.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
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

public class NationCreate implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationCreate(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String name = String.join(" ",strings);
        FileConfiguration nationData = plugin.getNationData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();

        String playerUUID = player.getUniqueId().toString();

        if(!playerData.contains(playerUUID+".City") || !Objects.requireNonNull(cityData.getString(playerData.getString(playerUUID + ".City") + ".Leader")).equalsIgnoreCase(playerUUID)) {
            player.sendRawMessage("Only the leader of a city can create a nation!");
            return true;
        }

        String cityUUID = playerData.getString(playerUUID + ".City");

        //Checking for player already in nation, missing arguments, and existing cities with same name:
        if(playerData.contains(cityUUID+".Nation")){
            player.sendRawMessage("You cannot create a nation when you are already a member of another nation!");
            return true;
        }
        else if(strings.length < 1){
            player.sendRawMessage("You must provide a name for your nation!");
            return true;
        }
        else {
            for(String string : nationData.getKeys(false)){
                if(Objects.requireNonNull(nationData.getString(string + ".Name")).equalsIgnoreCase(name)){
                    player.sendRawMessage("A nation with that name already exists.");
                    return true;
                }
            }
        }

        String cityGovernment = cityData.getString(cityUUID+".Government");

        if(Objects.requireNonNull(cityGovernment).equalsIgnoreCase("Monarchy")) {
            //Setting default nationData:
            String nationUUID = UUID.randomUUID().toString();
            nationData.set(nationUUID + ".Name", name);
            nationData.set(nationUUID + ".Leader", playerUUID);
            List<String> nobleList = Collections.singletonList(playerUUID);
            nationData.set(nationUUID + ".Nobles", nobleList);
            List<String> cityList = Collections.singletonList(cityUUID);
            nationData.set(nationUUID + ".Cities",cityList);
            nationData.set(nationUUID + ".Color", "&f");
            nationData.set(nationUUID + ".Motto", " ");
            nationData.set(nationUUID + ".LeaderTitle", "King");
            nationData.set(nationUUID + ".NobilityTitle", "Aristocrat");
            nationData.set(nationUUID + ".CitizenTitle", "Citizen");
            nationData.set(nationUUID + ".Government", cityGovernment);
            nationData.set(nationUUID + ".Legitimacy", 1.00);
            nationData.set(nationUUID+".Tag",strings[0].substring(0,Math.min(strings[0].length(),9)));
            cityData.set(cityUUID+".Nation",nationUUID);
            cityData.set(cityUUID+".NationInvites",null);

            plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".LeaderTitle")+ " " + player.getName() + " has founded a new nation called " + name + "!",true);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            cityData.set(cityUUID+".Elections.NewNation.StartDate",LocalDate.now().format(formatter));
            cityData.set(cityUUID+".Elections.NewNation.VoteCount.Yes",1);
            cityData.set(cityUUID+".Elections.NewNation.VoteCount.No",0);
            cityData.set(cityUUID+".Elections.NewNation.Name",name);
            cityData.set(cityUUID+".Elections.NewNation.Voters."+playerUUID,"Yes");
            plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".LeaderTitle")+ " " + player.getName() + " has called a vote to found a new nation named " + name + "!",true);
        }
        plugin.saveNationData();
        plugin.saveCityData();
        return true;
    }
}
