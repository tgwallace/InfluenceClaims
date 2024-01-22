package hardbuckaroo.influenceclaims.city.plots.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlotExpandListenerCity implements Listener {
    private final InfluenceClaims plugin;
    public PlotExpandListenerCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockClickEvent(PlayerInteractEvent event) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();

        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        Block block = event.getClickedBlock();

        if(event.getHand() != EquipmentSlot.OFF_HAND || !playerData.getBoolean(playerUUID + ".PlotExpand") || block == null) return;

        if(plugin.getPlot(block) != null) {
            player.sendRawMessage("Plots may not overlap! That block is already part of another plot. Please select again.");
            return;
        }

        if(plugin.getClaimant(plugin.getChunkKey(block.getChunk())) == null) {
            player.sendRawMessage("Plots cannot be created in unclaimed chunks. Please select again.");
            return;
        }

        if(!playerData.contains(playerUUID+".PlotCorner1")) {
            String block1Coords = block.getWorld().getName()+"|"+block.getX()+","+block.getY()+","+block.getZ();
            playerData.set(playerUUID+".PlotCorner1",block1Coords);
            plugin.savePlayerData();
            player.sendRawMessage("Plot corner 1 set!");
        } else {
            String[] worldPlot = playerData.getString(playerUUID+".PlotCorner1").split("\\|");
            String world = worldPlot[0];

            if(!block.getWorld().getName().equalsIgnoreCase(world)) {
                player.sendRawMessage("Cannot have a plot split between worlds!");
                return;
            }

            String coords = worldPlot[1];
            String[] block1String = coords.split(",");
            String cityUUID = playerData.getString(playerUUID+".City");
            int[] block1 = {Integer.parseInt(block1String[0]),Integer.parseInt(block1String[1]),Integer.parseInt(block1String[2])};
            int[] block2 = {block.getX(), block.getY(), block.getZ()};

            int newMinX = Math.min(block1[0],block2[0]);
            int newMaxX = Math.max(block1[0],block2[0]);
            int newMinY = Math.min(block1[1],block2[1]);
            int newMaxY = Math.max(block1[1],block2[1]);
            int newMinZ = Math.min(block1[2],block2[2]);
            int newMaxZ = Math.max(block1[2],block2[2]);

            for(int x = newMinX; x<=newMaxX; x++) {
                for(int y = newMinY; y<=newMaxY; y++) {
                    for(int z = newMinZ; z<=newMaxZ; z++) {
                        Block testBlock = Bukkit.getWorld(world).getBlockAt(x,y,z);
                        if(plugin.getPlot(testBlock) != null) {
                            player.sendRawMessage("Plots may not overlap! This plot intersects another plot at "+x+","+y+","+z+". Please select again.");
                            return;
                        }
                        String testClaimant = plugin.getClaimant(plugin.getChunkKey(testBlock.getChunk()));
                        if(testClaimant == null || !testClaimant.equalsIgnoreCase(cityUUID)) {
                            player.sendRawMessage("Plots may not cross through territory not claimed by your city! This plot leaves your city's claim at "+x+","+y+","+z+". Please select again.");
                            return;
                        }
                    }
                }
            }

            String plotString = block.getWorld().getName()+"|"+block1[0] + "," + block1[1] + "," + block1[2] + ":" + block2[0] + "," + block2[1] + "," + block2[2];
            String plotUUID = playerData.getString(playerUUID+".PlotExpanding");


            List<String> coordsList = cityData.getStringList(cityUUID+".Plots."+plotUUID+".Coords");
            coordsList.add(plotString);
            cityData.set(cityUUID+".Plots."+plotUUID+".Coords",coordsList);

            playerData.set(playerUUID+".PlotCorner1",null);
            playerData.set(playerUUID+".PlotExpand",false);

            plugin.saveCityData();
            plugin.savePlayerData();

            player.sendRawMessage("You have successfully expanded this plot!");
        }
    }
}
