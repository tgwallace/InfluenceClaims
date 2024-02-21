package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CityChunk implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityChunk(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration claimData = plugin.getClaimData();
        String chunkKey = plugin.getChunkKey(player.getLocation().getChunk());

        if(claimData.getConfigurationSection(chunkKey+".Claims") != null) {
            Map<String, Integer> map = new LinkedHashMap<String, Integer>();
            Map<String, Integer> mapSorted = new LinkedHashMap<String, Integer>();
            for (String option : claimData.getConfigurationSection(chunkKey+".Claims").getKeys(false)) {
                map.put(option, claimData.getInt(chunkKey+".Claims."+option+".Temporary") + claimData.getInt(chunkKey+".Claims."+option+".Permanent"));
            }
            map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(x -> mapSorted.put(x.getKey(), x.getValue()));

            TextComponent message = new TextComponent(plugin.color("&m                                                     "));
            String claimant = plugin.getClaimant(chunkKey);
            if(claimant != null) {
                double claimPercentage = ((claimData.getDouble(chunkKey+".Claims."+claimant+".Temporary") + claimData.getDouble(chunkKey+".Claims."+claimant+".Permanent")) / plugin.getConfig().getInt("ClaimMaximum"))*100;
                if (claimPercentage > 100) claimPercentage = 100;

                TextComponent claimantText = new TextComponent(plugin.color("\n&lClaimant: " + cityData.getString(claimant+".Color") + "&l" + cityData.getString(claimant+".Name") + " &f&o(" + String.format("%.0f%%",claimPercentage) + " protection)"));
                claimantText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cityinfo " + cityData.getString(claimant+".Name")));
                message.addExtra(claimantText);
            } else {
                message.addExtra(plugin.color("\n&lNo cities meet the minimum claim threshold!"));
            }

            for (Map.Entry<String, Integer> entry : mapSorted.entrySet()) {
                String claim = entry.getKey();
                String claimName = cityData.getString(claim+".Tag");
                String fullName = cityData.getString(claim + ".Name");
                String claimColor = cityData.getString(claim+".Color");
                int totalClaim = entry.getValue();
                int tempClaim = claimData.getInt(chunkKey+".Claims."+claim+".Temporary");
                int permClaim = claimData.getInt(chunkKey+".Claims."+claim+".Permanent");
                int lastPressure = claimData.getInt(chunkKey+".Claims."+claim+".Pressure");
                int netChange = claimData.getInt(chunkKey+".Claims."+claim+".NetChange");
                String changeString;
                if(netChange > 0) changeString = "+"+netChange;
                else changeString = String.valueOf(netChange);
                double claimPercentage = ((double) totalClaim / plugin.getConfig().getInt("ClaimMaximum"))*100;

                TextComponent claimComponent = new TextComponent(plugin.color("\n"+ claimColor + claimName + "&f: &l" + totalClaim + "&r ("+permClaim+"P + " + tempClaim + "T)" + " Pressure: " + lastPressure + ", Net: " + changeString));
                claimComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cityinfo " + fullName));

                message.addExtra(claimComponent);
                message.addExtra(plugin.color("\n&m                                                     "));
            }
            player.spigot().sendMessage(message);
        } else {
            player.sendRawMessage("There are currently no claims in this chunk.");
        }
        return true;
    }
}
