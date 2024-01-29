package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import net.coreprotect.CoreProtect;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BrewListener implements Listener {
    private final InfluenceClaims plugin;
    public BrewListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBrew(BrewEvent event){
        FileConfiguration playerData = plugin.getPlayerData();

        Block block = event.getBlock();

        List<String[]> blocklog = CoreProtect.getInstance().getAPI().blockLookup(block, (int)(System.currentTimeMillis() / 1000L));
        String[] mostRecentAction = blocklog.get(blocklog.size()-1);
        String mostRecentPlacer = mostRecentAction[1];

        Player player = Bukkit.getPlayer(mostRecentPlacer);
        if(player == null) return;
        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");
        if(cityUUID == null) return;
        String chunkKey = plugin.getChunkKey(block.getChunk());

        int potionMultiplier = 0;
        if(!event.getResults().get(0).getType().equals(Material.AIR)) potionMultiplier++;
        if(!event.getResults().get(1).getType().equals(Material.AIR)) potionMultiplier++;
        if(!event.getResults().get(2).getType().equals(Material.AIR)) potionMultiplier++;

        int blockValue = 20*potionMultiplier;

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addTempClaim(chunkKey,cityUUID,blockValue);

        plugin.saveClaimData();
    }
}
