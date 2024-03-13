package hardbuckaroo.influenceclaims.city.pressurebeacons;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;


public class PressureBeaconManager {
    private final InfluenceClaims plugin;
    private final NamespacedKey key;

    public PressureBeaconManager(InfluenceClaims plugin){
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "pressure-beacon");
    }

    public void addRecipe() {
        ItemStack item = tagItem(new ItemStack(Material.BEACON));
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("GGG", "GDG", "OOO");
        /* G = GLASS
           D = DIAMOND
           O = OBSIDIAN */
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('O', Material.OBSIDIAN);

        Bukkit.addRecipe(recipe);
    }

    public String getItemTag(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.get(key, PersistentDataType.STRING);
    }

    public ItemStack tagItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return item;
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, "pressure");
        meta.setDisplayName(ChatColor.RED + "Pressure Beacon");
        item.setItemMeta(meta);
        return item;
    }
}
