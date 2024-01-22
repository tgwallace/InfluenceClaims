package hardbuckaroo.influenceclaims.city.plots;

import com.mojang.authlib.GameProfile;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CheckPlot {
    private final InfluenceClaims plugin;

    public CheckPlot(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    public boolean checkProtection(Block block, Player player){
        FileConfiguration cityData = plugin.getCityData();

        String[] cityPlot = plugin.getPlot(block);
        if(cityPlot != null) {
            String city = cityPlot[0];
            String plot = cityPlot[1];
            String playerUUID = player.getUniqueId().toString();
            List<String> whitelist = cityData.getStringList(city+".Plots."+plot+".Whitelist");
            whitelist.add(cityData.getString(city+".Plots."+plot+".Owner"));
            return !whitelist.contains(playerUUID);
        }
        return false;
    }
}
