package hardbuckaroo.influenceclaims.city.pressurebeacons;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;
import java.util.List;


public class PressureBeaconBreakListener implements Listener {
    private final InfluenceClaims plugin;
    private final PressureBeaconManager manager;

    public PressureBeaconBreakListener(InfluenceClaims plugin, PressureBeaconManager manager){
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPressureBeaconBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.BEACON) {
            FileConfiguration cityData = plugin.getCityData();
            String blockKey = plugin.getBlockKey(event.getBlock());
            for (String cityUUID : cityData.getKeys(false)) {
                List<String> beaconList = cityData.getStringList(cityUUID + ".PressureBeacons");
                if (beaconList.contains(blockKey)) {
                    beaconList.remove(blockKey);
                    cityData.set(cityUUID + ".PressureBeacons", beaconList);
                    plugin.saveCityData();

                    event.setDropItems(false);
                    World world = block.getWorld();
                    ItemStack item = manager.tagItem(new ItemStack(Material.BEACON));
                    world.dropItemNaturally(block.getLocation(),item);
                    return;
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPressureBeaconExplode(EntityExplodeEvent event) {
        List<Block> blockList = event.blockList();
        List<Block> removeBlocks = new java.util.ArrayList<>();
        for (Block block : blockList) {
            if (block.getType() == Material.BEACON) {
                FileConfiguration cityData = plugin.getCityData();
                String blockKey = plugin.getBlockKey(block);
                for (String cityUUID : cityData.getKeys(false)) {
                    List<String> beaconList = cityData.getStringList(cityUUID + ".PressureBeacons");
                    if (beaconList.contains(blockKey)) {
                        beaconList.remove(blockKey);
                        cityData.set(cityUUID + ".PressureBeacons", beaconList);
                        plugin.saveCityData();

                        removeBlocks.add(block);
                    }
                }
            }
        }
        for(Block remove : removeBlocks) {
            blockList.remove(remove);
        }
    }
}
