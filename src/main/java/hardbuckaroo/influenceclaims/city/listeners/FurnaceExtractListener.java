package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FurnaceExtractListener implements Listener {
    private final InfluenceClaims plugin;
    public FurnaceExtractListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event){
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration claimData = plugin.getClaimData();

        Player player = event.getPlayer();
        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");

        if(cityUUID == null) return;

        Block block = event.getBlock();

        Material material = event.getItemType();
        int amount = event.getItemAmount();
        String chunkKey = block.getWorld() + "," + block.getChunk().getX() + "," + block.getChunk().getZ();

        int blockValue = (plugin.getConfig().getInt("BlockValues." + material.name())*amount)+event.getExpToDrop();

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addTempClaim(chunkKey,cityUUID,blockValue);
    }
}
