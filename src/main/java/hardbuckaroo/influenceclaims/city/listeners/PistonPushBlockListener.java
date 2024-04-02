package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.CheckProtection;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.Arrays;
import java.util.List;

public class PistonPushBlockListener implements Listener {
    private final InfluenceClaims plugin;
    public PistonPushBlockListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPistonExtendEvent(BlockPistonExtendEvent event){
        FileConfiguration playerData = plugin.getPlayerData();
        List<Block> blockList = event.getBlocks();
        BlockFace direction = event.getDirection();

        for(Block block : blockList) {
            List<String[]> blocklog = CoreProtect.getInstance().getAPI().blockLookup(block, (int)(System.currentTimeMillis() / 1000L));
            String mostRecentPlacer;
            String claimToDecrease = null;
            String chunkKey = plugin.getChunkKey(block.getChunk());

            if(!blocklog.isEmpty()) {
                for(String[] action : blocklog) {
                    CoreProtectAPI CoreProtect = plugin.getCoreProtect();
                    CoreProtectAPI.ParseResult parseResult = CoreProtect.parseResult(action);
                    if (parseResult.getActionId() == 1) {
                        Material material = parseResult.getBlockData().getMaterial();
                        if(material == block.getType()) {
                            mostRecentPlacer = parseResult.getPlayer();
                            claimToDecrease = playerData.getString(Bukkit.getOfflinePlayer(mostRecentPlacer).getUniqueId().toString() + ".City");
                            break;
                        }
                    }
                }
            }

            if(claimToDecrease != null) {
                ManageClaims manageClaims = new ManageClaims(plugin);
                int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());
                if (blockValue == 0) blockValue = plugin.getConfig().getInt("DefaultValue");
                manageClaims.subtractTempClaim(chunkKey,claimToDecrease,blockValue);
                manageClaims.subtractPermClaim(chunkKey,claimToDecrease,blockValue);
            }
        }
    }

    @EventHandler
    public void onPistonRetractEvent(BlockPistonRetractEvent event){
        FileConfiguration playerData = plugin.getPlayerData();
        List<Block> blockList = event.getBlocks();
        BlockFace direction = event.getDirection();

        for(Block block : blockList) {
            List<String[]> blocklog = CoreProtect.getInstance().getAPI().blockLookup(block, (int)(System.currentTimeMillis() / 1000L));
            String mostRecentPlacer;
            String claimToDecrease = null;
            String chunkKey = plugin.getChunkKey(block.getChunk());

            if(!blocklog.isEmpty()) {
                for(String[] action : blocklog) {
                    CoreProtectAPI CoreProtect = plugin.getCoreProtect();
                    CoreProtectAPI.ParseResult parseResult = CoreProtect.parseResult(action);
                    if (parseResult.getActionId() == 1) {
                        Material material = parseResult.getBlockData().getMaterial();
                        if(material == block.getType()) {
                            mostRecentPlacer = parseResult.getPlayer();
                            claimToDecrease = playerData.getString(Bukkit.getOfflinePlayer(mostRecentPlacer).getUniqueId().toString() + ".City");
                            break;
                        }
                    }
                }
            }

            if(claimToDecrease != null) {
                ManageClaims manageClaims = new ManageClaims(plugin);
                int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());
                if (blockValue == 0) blockValue = plugin.getConfig().getInt("DefaultValue");
                manageClaims.subtractTempClaim(chunkKey,claimToDecrease,blockValue);
                manageClaims.subtractPermClaim(chunkKey,claimToDecrease,blockValue);
            }
        }
    }
}