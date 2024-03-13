package hardbuckaroo.influenceclaims.city.pressurebeacons;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;


public class PressureBeaconPlaceListener implements Listener {
    private final InfluenceClaims plugin;
    private final PressureBeaconManager manager;

    public PressureBeaconPlaceListener(InfluenceClaims plugin, PressureBeaconManager manager){
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPressureBeaconPlace(BlockPlaceEvent event) {
        String tag = manager.getItemTag(event.getItemInHand());
        if(event.getBlockPlaced().getType() == Material.BEACON && tag != null && tag.equalsIgnoreCase("pressure")) {
            Block block = event.getBlock();
            FileConfiguration cityData = plugin.getCityData();
            FileConfiguration playerData = plugin.getPlayerData();
            String cityUUID = playerData.getString(event.getPlayer().getUniqueId().toString()+".City");
            if(cityUUID == null) return;
            String blockKey = plugin.getBlockKey(block);
            List<String> cityBeacons = cityData.getStringList(cityUUID+".PressureBeacons");
            cityBeacons.add(blockKey);
            cityData.set(cityUUID+".PressureBeacons",cityBeacons);
            plugin.saveCityData();
        }
    }
}
