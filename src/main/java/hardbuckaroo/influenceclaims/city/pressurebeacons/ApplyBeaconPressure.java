package hardbuckaroo.influenceclaims.city.pressurebeacons;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class ApplyBeaconPressure {
    private final InfluenceClaims plugin;
    private final PressureBeaconManager manager;
    int count;

    public ApplyBeaconPressure(InfluenceClaims plugin, PressureBeaconManager manager){
        this.plugin = plugin;
        this.manager = manager;
        this.count = 1;
    }

    public void applyPressure(){
        if(count > 8) count = 1;
        FileConfiguration cityData = plugin.getCityData();
        for(String cityUUID : cityData.getKeys(false)) {
            List<String> beaconList = cityData.getStringList(cityUUID + ".PressureBeacons");
            for(String blockKey : cityData.getStringList(cityUUID+".PressureBeacons")) {
                Block block = plugin.getBlockFromKey(blockKey);
                if(block.getType() == Material.BEACON) {
                    Beacon beacon = (Beacon) block.getState();
                    beacon.setPrimaryEffect(null);
                    beacon.setSecondaryEffect(null);
                    int level = beacon.getTier();
                    if(level == 0) return;

                    String chunkKey = plugin.getChunkKey(block.getChunk());
                    int chunkCount = 0;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            String[] chunkParts = chunkKey.split(",");
                            String pressuredChunk = chunkParts[0] + "," + (parseInt(chunkParts[1]) + x) + "," + (parseInt(chunkParts[2]) + z);
                            String claimant = plugin.getClaimant(pressuredChunk);
                            String stance = cityData.getString(cityUUID+".Stances."+claimant);
                            if(stance != null && stance.equalsIgnoreCase("Hostile")) chunkCount++;
                        }
                    }

                    if(chunkCount > 0 && (level == 4 || (level == 3 && (count == 1 || count == 3 || count == 5 || count == 7)) || (level == 2 && (count == 1 || count == 5)) || (level == 1 && count == 1))) {
                        Block above = block.getRelative(BlockFace.UP);
                        if (above.getState() instanceof Container) {
                            Inventory inventory = ((Container) above.getState()).getInventory();
                            for (ItemStack stack : inventory.getContents()) {
                                if (stack != null) {
                                    Material material = stack.getType();
                                    int value = plugin.getConfig().getInt("BlockValues." + material.name());
                                    int pressure = value / chunkCount;
                                    if (pressure > 0) {
                                        inventory.removeItem(new ItemStack(material, 1));
                                        ManageClaims manageClaims = new ManageClaims(plugin);
                                        for (int x = -1; x <= 1; x++) {
                                            for (int z = -1; z <= 1; z++) {
                                                String[] chunkParts = chunkKey.split(",");
                                                String pressuredChunk = chunkParts[0] + "," + (parseInt(chunkParts[1]) + x) + "," + (parseInt(chunkParts[2]) + z);
                                                String claimant = plugin.getClaimant(pressuredChunk);
                                                String stance = cityData.getString(cityUUID+".Stances."+claimant);
                                                if(stance != null && stance.equalsIgnoreCase("Hostile")) {
                                                    manageClaims.addTempClaim(pressuredChunk, cityUUID, pressure);
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    beaconList.remove(blockKey);
                    cityData.set(cityUUID+".PressureBeacons",beaconList);
                }
            }
        }
        plugin.saveClaimData();
        count++;
    }
}
