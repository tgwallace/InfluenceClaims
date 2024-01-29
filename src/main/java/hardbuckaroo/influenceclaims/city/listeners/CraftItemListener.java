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
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class CraftItemListener implements Listener {
    private final InfluenceClaims plugin;
    public CraftItemListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onCraft(InventoryClickEvent event){
        FileConfiguration playerData = plugin.getPlayerData();

        if(!(event.getInventory().getHolder() instanceof Player)) return;
        Player player = (Player) event.getInventory().getHolder();
        if(player == null) return;
        String cityUUID = playerData.getString(player.getUniqueId().toString()+".City");
        if(event.getSlot() != 0 || cityUUID == null) return;

        Block block = player.getLocation().getBlock();
        String chunkKey = player.getWorld() + "," + block.getChunk().getX() + "," + block.getChunk().getZ();

        Material material = event.getCurrentItem().getType();

        if(Arrays.asList(Material.BONE_BLOCK, Material.BONE_MEAL,
                Material.COAL_BLOCK, Material.COAL,
                Material.COPPER_BLOCK, Material.WAXED_COPPER_BLOCK, Material.COPPER_INGOT,
                Material.DIAMOND_BLOCK, Material.DIAMOND,
                Material.DRIED_KELP_BLOCK, Material.DRIED_KELP,
                Material.EMERALD_BLOCK, Material.EMERALD,
                Material.GOLD_BLOCK, Material.GOLD_INGOT,
                Material.HAY_BLOCK, Material.WHEAT,
                Material.HONEY_BLOCK, Material.HONEY_BOTTLE,
                Material.IRON_BLOCK, Material.IRON_INGOT,
                Material.LAPIS_BLOCK, Material.LAPIS_LAZULI,
                Material.RAW_COPPER_BLOCK, Material.RAW_COPPER,
                Material.RAW_GOLD_BLOCK, Material.RAW_GOLD,
                Material.RAW_IRON_BLOCK, Material.RAW_IRON,
                Material.REDSTONE_BLOCK, Material.REDSTONE, Material.REDSTONE_WIRE,
                Material.SLIME_BLOCK, Material.SLIME_BALL,
                Material.GOLD_NUGGET, Material.IRON_NUGGET).contains(material)) {
            return;
        }

        int stackCount = event.getCurrentItem().getAmount();

        if (event.isShiftClick()) {
            final ItemStack recipeResult = event.getCurrentItem();
            final int resultAmt = recipeResult.getAmount(); // Bread = 1, Cookie = 8, etc.
            int leastIngredient = -1;
            for (ItemStack item : event.getInventory().getContents()) {
                if (item != null && !item.getType().equals(Material.AIR)) {
                    final int re = item.getAmount() * resultAmt;
                    if (leastIngredient == -1 || re < leastIngredient) {
                        leastIngredient = item.getAmount() * resultAmt;
                    }
                }
            }
            ItemStack finalStack = new ItemStack(recipeResult.getType(), leastIngredient, recipeResult.getDurability());
            stackCount = finalStack.getAmount();
        }

        int blockValue = stackCount * plugin.getConfig().getInt("BlockValues." + material.name());
        if (blockValue == 0) blockValue = stackCount * plugin.getConfig().getInt("DefaultValue");

        ManageClaims manageClaims = new ManageClaims(plugin);
        manageClaims.addTempClaim(chunkKey,cityUUID,blockValue);
    }
}
