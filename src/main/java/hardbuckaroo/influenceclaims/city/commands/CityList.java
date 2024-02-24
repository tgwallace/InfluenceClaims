package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.text.NumberFormat;
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
        int page;

        if(strings.length == 0) page = 0;
        else if(strings[0].matches("\\d+")) page = Integer.parseInt(strings[0]);
        else {
            player.sendRawMessage("Invalid input. Use /CityList [page #] or just /CityList.");
            return true;
        }

        if(cityData.getKeys(false).isEmpty()){
            player.sendRawMessage("There are no cities here yet. Wait a little while or start your own city using /CityCreate!");
        } else {
            TextComponent message = new TextComponent(plugin.color("&lClick a city to see its info tab:"));

            Map<String, Long> map = new LinkedHashMap<>();
            Map<String, Long> mapSorted = new LinkedHashMap<>();
            for(String cityUUID : cityData.getKeys(false)) {
                    map.put(cityUUID, cityData.getLong(cityUUID+".TotalInfluence"));
            }
            map.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).forEachOrdered(x -> mapSorted.put(x.getKey(), x.getValue()));

            int count = 0;
            for(Map.Entry<String, Long> entry : mapSorted.entrySet()) {
                if(count>=(page+1)*5) {
                    TextComponent nextLine = new TextComponent(plugin.color("\n&oNext Page &r->"));
                    nextLine.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/citylist " + (page + 1)));
                    message.addExtra(nextLine);
                    break;
                }
                if(count>=page*5 && count<(page+1)*5) {
                    String cityUUID = entry.getKey();
                    long influence = entry.getValue();
                    TextComponent subComponent = new TextComponent(plugin.color("\n&l" + cityData.getString(cityUUID + ".Color") + cityData.getString(cityUUID + ".Name") + " &r(" + NumberFormat.getInstance().format(influence) + "): &o" + cityData.getString(cityUUID + ".Motto")));
                    subComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityinfo " + cityData.getString(cityUUID + ".Name")));
                    message.addExtra(subComponent);
                }
                count++;
            }
            player.spigot().sendMessage(message);
        }
        return true;
    }
}
