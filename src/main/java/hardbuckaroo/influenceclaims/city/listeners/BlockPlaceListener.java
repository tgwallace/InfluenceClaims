package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.city.CheckProtection;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BlockPlaceListener implements Listener {
    private final InfluenceClaims plugin;
    public BlockPlaceListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player player = event.getPlayer();
        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");

        Block block = event.getBlockPlaced();
        String chunkKey = block.getWorld() + "," + block.getChunk().getX() + "," + block.getChunk().getZ();

        CheckProtection cp = new CheckProtection(plugin);

        if(cp.checkProtection(block, player)){
            event.setCancelled(true);
            return;
        }
        else if (cityUUID == null) return;

        if((block.getWorld().getEnvironment() == World.Environment.THE_END && !plugin.getConfig().getBoolean("EndClaims")) || (block.getWorld().getEnvironment() == World.Environment.NETHER && !plugin.getConfig().getBoolean("NetherClaims"))) {
            return;
        }

        String[] plot = plugin.getPlot(block);
        if(plot != null && !plot[0].equalsIgnoreCase(cityUUID) && (cityData.getStringList(plot[0]+".Plots."+plot[1]+".Whitelist").contains(player.getUniqueId().toString()) || cityData.getString(plot[0]+".Plots."+plot[1]+".Owner").equalsIgnoreCase(player.getUniqueId().toString()))) {
            return;
        }

        int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());
        if (blockValue == 0) blockValue = plugin.getConfig().getInt("DefaultValue");

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addPermClaim(chunkKey, cityUUID, blockValue);
        manageClaims.addTempClaim(chunkKey, cityUUID, blockValue);
    }
}
