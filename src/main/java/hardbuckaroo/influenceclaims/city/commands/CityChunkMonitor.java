package hardbuckaroo.influenceclaims.city.commands;

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

import java.util.ArrayList;

public class CityChunkMonitor implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityChunkMonitor(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration claimData = plugin.getClaimData();
        String chunkKey = plugin.getChunkKey(player.getLocation().getChunk());
        String playerUUID = player.getUniqueId().toString();

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");

        if(plugin.getClaimant(chunkKey) == null || !plugin.getClaimant(chunkKey).equalsIgnoreCase(cityUUID)) {
            player.sendRawMessage("Cannot use ChunkMonitor outside chunks claimed by your city.");
            return true;
        }

        if(!claimData.contains(chunkKey+".Claims."+cityUUID+".Monitor") || !claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor").contains(playerUUID)) {
            ArrayList<String> playerList = new ArrayList<>();
            if(claimData.contains(chunkKey+".Claims."+cityUUID+".Monitor")) {
                playerList = (ArrayList<String>) claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor");
            }
            playerList.add(playerUUID);
            claimData.set(chunkKey+".Claims."+cityUUID+".Monitor",playerList);
            player.sendRawMessage("ChunkMonitor set for this chunk!");
        } else {
            ArrayList<String> playerList = (ArrayList<String>) claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor");
            playerList.remove(playerUUID);
            claimData.set(chunkKey+".Claims."+cityUUID+".Monitor",playerList);
            player.sendRawMessage("ChunkMonitor removed for this chunk!");
        }
        plugin.saveClaimData();

        return true;
    }
}
