package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public class CityList implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityList(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();

        if(cityData.getKeys(false).isEmpty()){
            player.sendRawMessage("There are no cities here yet. Wait a little while or start your own city using /CityCreate!");
        } else {
            TextComponent message = new TextComponent(plugin.color("&lClick a city to see its info tab:"));

            Map<String, Integer> map = new LinkedHashMap<String, Integer>();
            Map<String, Integer> mapSorted = new LinkedHashMap<String, Integer>();
            for(String cityUUID : cityData.getKeys(false)) {
                    map.put(cityUUID, cityData.getStringList(cityUUID+".Players").size());
            }
            map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(x -> mapSorted.put(x.getKey(), x.getValue()));

            for(Map.Entry<String, Integer> entry : mapSorted.entrySet()) {
                String cityUUID = entry.getKey();
                int pop = entry.getValue();
                TextComponent subComponent = new TextComponent(plugin.color("\n&l"+cityData.getString(cityUUID+".Color")+cityData.getString(cityUUID+".Name")+" &r("+pop+"): &o" + cityData.getString(cityUUID+".Motto")));
                subComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cityinfo "+cityData.getString(cityUUID+".Name")));
                message.addExtra(subComponent);
            }
            player.spigot().sendMessage(message);
        }
        return true;
    }
}
