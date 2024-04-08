package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.city.CheckProtection;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class BlockBreakListener implements Listener {
    private final InfluenceClaims plugin;
    public BlockBreakListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreakEvent(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        CheckProtection cp = new CheckProtection(plugin);
        if(cp.checkProtection(block, player)){
            event.setCancelled(true);
            if(player.getInventory().getItemInMainHand() instanceof Damageable) {
                Damageable item = (Damageable) player.getInventory().getItemInMainHand();
                item.setHealth(item.getHealth()-1);
            }
            return;
        }

        FileConfiguration playerData = plugin.getPlayerData();
        ManageClaims manageClaims = new ManageClaims(plugin);

        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");
        String chunkKey = plugin.getChunkKey(block.getChunk());

        if(Arrays.asList(Material.SWEET_BERRY_BUSH, Material.POTATOES, Material.BEETROOTS, Material.WHEAT,
                Material.CARROTS, Material.COCOA).contains(block.getType()) && cityUUID != null) {

            BlockData blockData = block.getBlockData();

            if(blockData instanceof Ageable cropData) {
                int age = cropData.getAge();

                if (age > cropData.getMaximumAge()-1) {
                    int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());
                    if (blockValue == 0) blockValue = plugin.getConfig().getInt("DefaultValue");

                    manageClaims.addTempClaim(chunkKey, cityUUID, blockValue);
                }
            }
        }
        else if(Arrays.asList(Material.BROWN_MUSHROOM, Material.MELON, Material.KELP_PLANT, Material.SUGAR_CANE,
                Material.CACTUS, Material.RED_MUSHROOM, Material.PUMPKIN,
                Material.ACACIA_LOG, Material.BIRCH_LOG, Material.CHERRY_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.MANGROVE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG).contains(block.getType()) && cityUUID != null) {

            String mostRecentPlacer = null;
            String mostRecentPlacerClaim = null;

            List<String[]> blocklog = CoreProtect.getInstance().getAPI().blockLookup(block, (int)(System.currentTimeMillis() / 1000L));

            if(!blocklog.isEmpty()) {
                for(String[] action : blocklog) {
                    CoreProtectAPI CoreProtect = plugin.getCoreProtect();
                    CoreProtectAPI.ParseResult parseResult = CoreProtect.parseResult(action);
                    if(parseResult.getActionId() == 1) {
                        Material material = parseResult.getBlockData().getMaterial();
                        if(material == block.getType()) {
                            mostRecentPlacer = parseResult.getPlayer();
                            mostRecentPlacerClaim = playerData.getString(Bukkit.getOfflinePlayer(mostRecentPlacer).getUniqueId().toString() + ".City");
                            break;
                        }
                    }
                }
            }

            int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());


            if(mostRecentPlacerClaim == null || !mostRecentPlacerClaim.equals(cityUUID)) {
                if (blockValue == 0 || block.getType().toString().contains("LOG")) blockValue = plugin.getConfig().getInt("DefaultValue");
                manageClaims.addTempClaim(chunkKey,cityUUID,blockValue);
            } else {
                manageClaims.subtractTempClaim(chunkKey,mostRecentPlacerClaim,blockValue);
                manageClaims.subtractPermClaim(chunkKey,mostRecentPlacerClaim,blockValue);
            }
        }
        else if(!CoreProtect.getInstance().getAPI().blockLookup(block, (int) (System.currentTimeMillis() / 1000L)).isEmpty()) {
            List<String[]> blocklog = CoreProtect.getInstance().getAPI().blockLookup(block, (int)(System.currentTimeMillis() / 1000L));
            String mostRecentPlacer;
            String claimToDecrease = null;

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
                int blockValue = plugin.getConfig().getInt("BlockValues." + block.getType().name());
                if (blockValue == 0) blockValue = plugin.getConfig().getInt("DefaultValue");
                manageClaims.subtractTempClaim(chunkKey,claimToDecrease,blockValue);
                manageClaims.subtractPermClaim(chunkKey,claimToDecrease,blockValue);
            }
        }
    }
}
