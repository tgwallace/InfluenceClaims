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
import org.bukkit.entity.Entity;
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
        List<Block> blockList = event.getBlocks();
        CoreProtectAPI CoreProtect = plugin.getCoreProtect();
        BlockFace direction = event.getDirection();

        for(Block block : blockList) {
            block = block.getRelative(direction);
            double distance = 100;
            Player player = null;
            for(Entity entity : block.getLocation().getWorld().getNearbyEntities(block.getLocation(),distance,distance,distance)) {
                double between = block.getLocation().distance(entity.getLocation());
                if(entity instanceof Player && between < distance) {
                    player = (Player) entity;
                    distance = between;
                }
            }
            CoreProtect.logPlacement(player.getName(),block.getLocation(),block.getType(), block.getBlockData());
        }
    }

    @EventHandler
    public void onPistonRetractEvent(BlockPistonRetractEvent event){
        List<Block> blockList = event.getBlocks();
        CoreProtectAPI CoreProtect = plugin.getCoreProtect();
        BlockFace direction = event.getDirection();

        for(Block block : blockList) {
            block = block.getRelative(direction);
            double distance = 100;
            Player player = null;
            for(Entity entity : block.getLocation().getWorld().getNearbyEntities(block.getLocation(),distance,distance,distance)) {
                double between = block.getLocation().distance(entity.getLocation());
                if(entity instanceof Player && between < distance) {
                    player = (Player) entity;
                    distance = between;
                }
            }
            CoreProtect.logPlacement(player.getName(),block.getLocation(),block.getType(), block.getBlockData());
        }
    }
}