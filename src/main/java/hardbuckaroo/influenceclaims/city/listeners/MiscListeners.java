package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.city.CheckProtection;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.plots.CheckPlot;
import net.minecraft.world.entity.monster.EnderMan;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Hangable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
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
        Block block = event.getBlock();

        CheckProtection cp = new CheckProtection(plugin);
        if(cp.checkProtectionSimple(block,event.getPlayer())){
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

        CheckProtection cp = new CheckProtection(plugin);
        if(cp.checkProtectionSimple(block,event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void trampleEvent(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (event.getAction().equals(Action.PHYSICAL) && block != null) {
            CheckProtection cp = new CheckProtection(plugin);
            if(cp.checkProtectionSimple(block,event.getPlayer())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if(event.getRightClicked() instanceof Minecart || event.getRightClicked() instanceof Boat || event.getRightClicked() instanceof Horse) return;

        CheckProtection cp = new CheckProtection(plugin);
        if(cp.checkProtectionSimple(event.getRightClicked().getLocation().getBlock(), event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerBreakItemFrameEvent(EntityDamageByEntityEvent event) {
        CheckProtection cp = new CheckProtection(plugin);
        if(event.getDamager() instanceof Player && event.getEntity() instanceof Hangable && cp.checkProtectionSimple(event.getEntity().getLocation().getBlock(), (Player) event.getDamager())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        CheckProtection cp = new CheckProtection(plugin);
        if(cp.checkProtectionSimple(event.getRightClicked().getLocation().getBlock(),event.getPlayer())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void fireSpreadEventEvent(BlockSpreadEvent event) {
        if(event.getSource().getType() != Material.FIRE) return;
        String claimant = plugin.getClaimant(plugin.getChunkKey(event.getBlock().getChunk()));
        if(claimant != null){
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
                CheckProtection cp = new CheckProtection(plugin);
                if(cp.checkProtectionSimple(event.getLocation().getBlock(),player)){
                    event.setCancelled(true);
                }
            } else event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEntityExplodeEvent(EntityExplodeEvent event) {
        List<Block> blockList = event.blockList();
        CheckProtection cp = new CheckProtection(plugin);

        blockList.removeIf(cp::checkProtection);
    }

    @EventHandler
    public void hangingEntityBreakEvent(HangingBreakByEntityEvent event) {
        CheckProtection cp = new CheckProtection(plugin);
        if(event.getEntity() instanceof Player && cp.checkProtectionSimple(event.getEntity().getLocation().getBlock(), (Player) event.getEntity())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void hangingEntityPlaceEvent(HangingPlaceEvent event) {
        CheckProtection cp = new CheckProtection(plugin);
        if(event.getEntity() instanceof Player && cp.checkProtectionSimple(event.getEntity().getLocation().getBlock(), (Player) event.getEntity())){
            event.setCancelled(true);
        }
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
                    Material.ITEM_FRAME, Material.JUKEBOX, Material.LECTERN, Material.SHULKER_BOX, Material.ITEM_FRAME).contains(material)))) {
                if(claimant != null) {
                    String stance = cityData.getString(claimant+".Stances."+cityUUID);
                    String nationUUID = cityData.getString(cityUUID+".Nation");
                    String claimantUUID = cityData.getString(claimant+".Nation");
                    if(((stance != null && stance.equalsIgnoreCase("Friendly")) || (claimantUUID != null && claimantUUID.equalsIgnoreCase(nationUUID))) && (name.contains("BUTTON") || name.contains("DOOR") || name.contains("PRESSURE") || name.contains("GATE") || material.equals(Material.LEVER))) {
                        return;
                    }

                    CheckProtection cp = new CheckProtection(plugin);
                    if(cp.checkProtectionSimple(block,event.getPlayer())){
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
