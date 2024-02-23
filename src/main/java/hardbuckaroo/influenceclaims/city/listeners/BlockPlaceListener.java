package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.city.CheckProtection;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import org.bukkit.Bukkit;
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
import java.util.HashMap;

public class BlockPlaceListener implements Listener {
    private final InfluenceClaims plugin;
    public BlockPlaceListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public HashMap<Player,String> cooldownMap = new HashMap<>();

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlaceEvent(BlockPlaceEvent event){
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player player = event.getPlayer();
        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");

        Block block = event.getBlockPlaced();
        String chunkKey = plugin.getChunkKey(block.getChunk());

        CheckProtection cp = new CheckProtection(plugin);
        String claimant = plugin.getClaimant(plugin.getChunkKey(block.getChunk()));
        if(cooldownMap.containsKey(player) && cooldownMap.get(player).equalsIgnoreCase(claimant)) {
            event.setCancelled(true);
            return;
        } else if(cp.checkProtection(block, player)){
            event.setCancelled(true);
            cooldownMap.put(player,claimant);
            Bukkit.getScheduler().runTaskLater(plugin, () -> cooldownMap.remove(player),(long) (1/block.getBreakSpeed(player)));
            player.sendRawMessage("You have been given a " + (1/block.getBreakSpeed(player))/20 + " second cooldown on attempting to place blocks in "+cityData.getString(claimant+".Name")+"!");
            return;
        }
        else if (cityUUID == null) return;

        String[] plot = plugin.getPlot(block);
        if(plot != null && !plot[0].equalsIgnoreCase(cityUUID)
                && (cityData.getStringList(plot[0]+".Plots."+plot[1]+".Whitelist").contains(player.getUniqueId().toString()) || cityData.getString(plot[0]+".Plots."+plot[1]+".Owner").equalsIgnoreCase(player.getUniqueId().toString()))
                && !(cityData.contains(cityUUID+".Nation") && (cityData.contains(cityUUID+".Nation") && cityData.getString(cityUUID+".Nation").equalsIgnoreCase(cityData.getString(plot[0]+".Nation"))))) {
            return;
        }

        int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());
        if (blockValue == 0) blockValue = plugin.getConfig().getInt("DefaultValue");

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addPermClaim(chunkKey, cityUUID, blockValue);
        manageClaims.addTempClaim(chunkKey, cityUUID, blockValue);
    }
}
