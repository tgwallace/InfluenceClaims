package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.elections.CityStartElection;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.List;

public class CityLeave implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityLeave(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String uuid = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        String currentCity = playerData.getString(uuid+".City");
        String cityName = cityData.getString(currentCity+".Name");
        String government = cityData.getString(currentCity+".Government");

        if(!playerData.contains(uuid+".City")){
            player.sendRawMessage("You are not part of a city.");
            return true;
        }

        if(strings.length < 1 || !strings[0].equalsIgnoreCase("confirm")){
            String message;
            List<String> remainingPlayers = cityData.getStringList(currentCity+".Players");
            remainingPlayers.remove(uuid);
            if(remainingPlayers.isEmpty()) message = "&cAre you sure? You are the last member of this city and leaving will cause the city to be disbanded. Click here to confirm or ignore this message to stay in the city.";
            else if (cityData.getString(currentCity + ".Leader").equalsIgnoreCase(uuid)) message = "&cAre you sure? You are currently the leader of this city and leaving will trigger a special election and you will not be able to rejoin without being added back. Click here to confirm or ignore this message to stay in the city.";
            else message = "&cAre you sure? If you leave, you will not be able to rejoin without being added back. Click here to confirm or ignore this message to stay in the city.";
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',message)));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cityleave confirm"));
            player.spigot().sendMessage(component);
            return true;
        } else {
            List<String> remainingPlayers = cityData.getStringList(currentCity + ".Players");
            remainingPlayers.remove(uuid);
            if (remainingPlayers.isEmpty()) {
                cityData.set(currentCity, null);
                playerData.set(uuid + ".City", null);
                playerData.set(uuid+".PlotMode",false);
                playerData.set(uuid+".ChatChannel","World");
                player.sendRawMessage("You have left " + cityName + " and the city has been disbanded.");
                FileConfiguration claimData = plugin.getClaimData();
                for(String chunkKey : claimData.getKeys(false)) {
                    if(claimData.contains(chunkKey+".Claims."+currentCity)) {
                        claimData.set(chunkKey+".Claims."+currentCity,null);
                    }
                }
                plugin.saveClaimData();
            } else {
                cityData.set(currentCity + ".Players", remainingPlayers);
                playerData.set(uuid+".City",null);
                playerData.set(uuid+".ChatChannel","World");
                if (cityData.getString(currentCity + ".Leader").equalsIgnoreCase(uuid)) {
                    List<String> heirList = cityData.getStringList(currentCity + ".Nobles");
                    if (!heirList.isEmpty()) {
                        cityData.set(currentCity+".Leader",heirList.get(0));
                    } else {
                        cityData.set(currentCity+".Leader",remainingPlayers.get(0));
                        heirList.add(remainingPlayers.get(0));
                        cityData.set(currentCity+".Nobles",heirList);
                    }
                    CityStartElection cityStartElection = new CityStartElection(plugin);
                    cityStartElection.startElection(currentCity);
                }
                player.sendRawMessage("You have left " + cityName + ".");
            }
            plugin.saveCityData();
            plugin.savePlayerData();
            plugin.updateScoreboard();
            return true;
        }
    }
}
