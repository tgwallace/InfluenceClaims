package hardbuckaroo.influenceclaims.city.commands.admin;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public class CityChunkBoostAdmin implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityChunkBoostAdmin(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration playerData = plugin.getPlayerData();
        String chunkKey = plugin.getChunkKey(player.getLocation().getChunk());

        if(strings.length < 2) {
            commandSender.sendMessage("Must provide the amount to boost and the name of the city.");
            return false;
        }

        double boost = Double.parseDouble(strings[0]);
        String cityName = String.join(" ",strings);
        cityName = cityName.substring(cityName.indexOf(" ")+1);
        String cityUUID = plugin.getCityUUIDFromName(cityName);

        if(cityUUID == null) {
            player.sendRawMessage("Could not find a city named " + cityName + ". Please check your spelling and try again.");
            return true;
        }

        int tempAdd = (int) (plugin.getConfig().getDouble("ChunkBoostTemporaryRate")*boost);
        int permAdd = (int) (plugin.getConfig().getDouble("ChunkBoostPermanentRate")*boost);

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addTempClaim(chunkKey,cityUUID,tempAdd);
        manageClaims.addPermClaim(chunkKey,cityUUID,permAdd);
        player.sendRawMessage("Successfully spent " + boost + " to boost "+cityName+"'s claim on this chunk!");
        return true;
    }
}
