package hardbuckaroo.influenceclaims.city.commands.admin;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CityUnclaimAdmin implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityUnclaimAdmin(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration claimData = plugin.getClaimData();
        Player sender = (Player) commandSender;

        if(strings.length < 1) {
            commandSender.sendMessage("Must provide the name of the city.");
            return false;
        }

        String cityName = String.join(" ",strings);
        String cityUUID = plugin.getCityUUIDFromName(cityName);

        if(cityUUID == null) {
            sender.sendRawMessage("Could not find a city named " + cityName + ". Please check your spelling and try again.");
            return true;
        }

        String chunkKey = plugin.getChunkKey(sender.getLocation().getChunk());
        if(claimData.contains(chunkKey + ".Claims." + cityUUID)) {
            claimData.set(chunkKey + ".Claims." + cityUUID, null);
            sender.sendRawMessage("Claims for " + cityName +" have been removed in this chunk.");
        }
        plugin.saveClaimData();
        return true;
    }
}
