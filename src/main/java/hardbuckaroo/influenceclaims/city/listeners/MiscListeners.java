package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.city.CheckProtection;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.Arrays;
import java.util.List;

public class MiscListeners implements Listener {

    private final InfluenceClaims plugin;

    public MiscListeners(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onBucketEmptyEvent(PlayerBucketEmptyEvent event) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Block block = event.getBlock();
        String playerUUID = event.getPlayer().getUniqueId().toString();
        String cityUUID = playerData.getString(playerUUID+".City");
        String claimant = plugin.getClaimant(plugin.getChunkKey(block.getChunk()));

        if((claimant != null && !claimant.equalsIgnoreCase(cityUUID))){
            event.getPlayer().sendRawMessage("This land is claimed by " + cityData.getString(claimant+".Name") + "!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        CheckProtection cp = new CheckProtection(plugin);
        if(player != null && cp.checkProtection(block, player)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void signChangeEvent(SignChangeEvent event) {
        Block block = event.getBlock();
        String playerUUID = event.getPlayer().getUniqueId().toString();
        FileConfiguration playerData = plugin.getPlayerData();
        String cityUUID = playerData.getString(playerUUID+".City");
        FileConfiguration cityData = plugin.getCityData();
        String claimant = plugin.getClaimant(plugin.getChunkKey(block.getChunk()));

        if((claimant != null && !claimant.equalsIgnoreCase(cityUUID))){
            event.getPlayer().sendRawMessage("This land is claimed by " + cityData.getString(claimant+".Name") + "!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void trampleEvent(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction().equals(Action.PHYSICAL) && block != null) {
            String playerUUID = event.getPlayer().getUniqueId().toString();
            FileConfiguration playerData = plugin.getPlayerData();
            String cityUUID = playerData.getString(playerUUID + ".City");
            FileConfiguration cityData = plugin.getCityData();
            String claimant = plugin.getClaimant(plugin.getChunkKey(block.getChunk()));

            if ((claimant != null && !claimant.equalsIgnoreCase(cityUUID))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if(event.getHand() != EquipmentSlot.OFF_HAND) return;
        String playerUUID = event.getPlayer().getUniqueId().toString();
        FileConfiguration playerData = plugin.getPlayerData();
        String cityUUID = playerData.getString(playerUUID+".City");
        String claimant = plugin.getClaimant(plugin.getChunkKey(event.getRightClicked().getLocation().getChunk()));

        if((claimant != null && !claimant.equalsIgnoreCase(cityUUID))){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        String playerUUID = event.getPlayer().getUniqueId().toString();
        FileConfiguration playerData = plugin.getPlayerData();
        String cityUUID = playerData.getString(playerUUID+".City");
        FileConfiguration cityData = plugin.getCityData();
        String claimant = plugin.getClaimant(plugin.getChunkKey(event.getRightClicked().getLocation().getChunk()));

        if((claimant != null && !claimant.equalsIgnoreCase(cityUUID))){
            event.getPlayer().sendRawMessage("This land is claimed by " + cityData.getString(claimant+".Name") + "!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void entitySpawnEvent(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ArmorStand || event.getEntity() instanceof ItemFrame || event.getEntity() instanceof GlowItemFrame) {
            double distance = 10;
            Player player = null;
            for(Entity entity : event.getLocation().getWorld().getNearbyEntities(event.getLocation(),distance,distance,distance)) {
                double between = event.getLocation().distance(entity.getLocation());
                if(entity instanceof Player && between < distance) {
                    player = (Player) entity;
                    distance = between;
                }
            }
            if(player != null) {
                String playerUUID = player.getUniqueId().toString();
                FileConfiguration playerData = plugin.getPlayerData();
                String cityUUID = playerData.getString(playerUUID + ".City");
                FileConfiguration cityData = plugin.getCityData();
                String claimant = plugin.getClaimant(plugin.getChunkKey(event.getLocation().getChunk()));

                if ((claimant != null && !claimant.equalsIgnoreCase(cityUUID))) {
                    player.sendRawMessage("This land is claimed by " + cityData.getString(claimant + ".Name") + "!");
                    event.setCancelled(true);
                }
            } else event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        List<Block> blockList = event.blockList();
        CheckProtection cp = new CheckProtection(plugin);

        blockList.removeIf(cp::checkProtection);
    }

    @EventHandler
    public void onEndermanPickupEvent(EntityChangeBlockEvent event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.ENDERMAN) {
            Block block = event.getBlock();
            CheckProtection cp = new CheckProtection(plugin);

            if(cp.checkProtection(block)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplodeEvent(BlockExplodeEvent event) {
        List<Block> blockList = event.blockList();
        CheckProtection cp = new CheckProtection(plugin);

        blockList.removeIf(cp::checkProtection);
    }

    @EventHandler
    public void onMonsterSpawn(CreatureSpawnEvent event) {
        if(event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL && event.getEntity() instanceof Monster) {
            CheckProtection cp = new CheckProtection(plugin);
            if(cp.checkProtection(event.getLocation().getBlock())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChestOpenEvent(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
            FileConfiguration playerData = plugin.getPlayerData();
            FileConfiguration cityData = plugin.getCityData();
            Block block = event.getClickedBlock();
            Material material = block.getType();
            String name = material.name();
            String playerUUID = event.getPlayer().getUniqueId().toString();
            String cityUUID = playerData.getString(playerUUID+".City");
            String claimant = plugin.getClaimant(plugin.getChunkKey(block.getChunk()));
            if((name.contains("BUTTON") || name.contains("DOOR") || name.contains("PRESSURE") || name.contains("GATE") ||
                    (Arrays.asList(Material.CHEST, Material.CHEST_MINECART, Material.TRAPPED_CHEST,
                    Material.FURNACE, Material.BLAST_FURNACE, Material.FURNACE_MINECART, Material.SMOKER,
                    Material.BARREL, Material.HOPPER, Material.HOPPER_MINECART, Material.LEVER, Material.ENCHANTING_TABLE,
                    Material.ARMOR_STAND, Material.BREWING_STAND, Material.BEEHIVE, Material.BEE_NEST,
                    Material.BUNDLE, Material.CAMPFIRE, Material.SOUL_CAMPFIRE, Material.CAULDRON, Material.LAVA_CAULDRON,
                    Material.CHISELED_BOOKSHELF, Material.DISPENSER, Material.DROPPER, Material.FLOWER_POT,
                    Material.ITEM_FRAME, Material.JUKEBOX, Material.LECTERN, Material.SHULKER_BOX, Material.ITEM_FRAME).contains(material))) && (claimant != null && !claimant.equalsIgnoreCase(cityUUID))) {
                String stance = cityData.getString(claimant+".Stances."+cityUUID);
                String nationUUID = cityData.getString(cityUUID+".Nation");
                String claimantUUID = cityData.getString(claimant+".Nation");
                if(((stance != null && stance.equalsIgnoreCase("Friendly")) || (claimantUUID != null && claimantUUID.equalsIgnoreCase(nationUUID))) && (name.contains("BUTTON") || name.contains("DOOR") || name.contains("PRESSURE") || name.contains("GATE") || material.equals(Material.LEVER))) {
                    return;
                }

                event.getPlayer().sendRawMessage("This land is claimed by " + cityData.getString(claimant+".Name") + "!");
                event.setCancelled(true);
            }
        }
    }
}
