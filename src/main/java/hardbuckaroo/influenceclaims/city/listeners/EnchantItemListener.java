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
import org.bukkit.event.enchantment.EnchantItemEvent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EnchantItemListener implements Listener {
    private final InfluenceClaims plugin;
    public EnchantItemListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event){
        FileConfiguration playerData = plugin.getPlayerData();

        Player player = event.getEnchanter();
        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");

        if(cityUUID == null) return;

        Block block = event.getEnchantBlock();

        Material material = event.getItem().getType();
        String chunkKey = plugin.getChunkKey(block.getChunk());

        int blockValue = (20*event.getExpLevelCost()) + plugin.getConfig().getInt("BlockValues." + material.name());

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addTempClaim(chunkKey,cityUUID,blockValue);
    }
}
