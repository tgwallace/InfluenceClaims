package hardbuckaroo.influenceclaims.city;

import com.mojang.authlib.GameProfile;
import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.plots.CheckPlot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CheckProtection {
    private final InfluenceClaims plugin;

    public CheckProtection(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public boolean checkProtection(Block block){
        List<Player> playerList = block.getWorld().getPlayers();
        if(playerList.isEmpty()) return true;
        CraftPlayer player = (CraftPlayer) playerList.get(0);
        ServerPlayer sp = player.getHandle();
        MinecraftServer server = sp.getServer();
        ServerLevel level = sp.serverLevel();
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "FakePlayer");
        ServerPlayer fakeSP = new ServerPlayer(server, level, gameProfile, ClientInformation.createDefault());
        Player fakePlayer = fakeSP.getBukkitEntity().getPlayer();

        return checkProtection(block,fakePlayer);
    }

    HashMap<String, String> map = new HashMap<>();
    public boolean checkProtection(Block block, Player player){
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration claimData = plugin.getClaimData();

        String playerUUID = player.getUniqueId().toString();
        String city = playerData.getString(playerUUID+".City");
        String chunkKey = plugin.getChunkKey(block.getChunk());

        int claimTotal = 0;
        String claimant = "";

        if(claimData.contains(chunkKey+".Claims")) {
            for (String claim : claimData.getConfigurationSection(chunkKey + ".Claims").getKeys(false)) {
                int claimTemp = claimData.getInt(chunkKey+".Claims."+claim+".Temporary");
                int claimPerm = claimData.getInt(chunkKey+".Claims."+claim+".Permanent");

                if(claimTemp+claimPerm > claimTotal) {
                    claimTotal = claimTemp+claimPerm;
                    claimant = claim;
                }
            }
        }

        CheckPlot checkPlot = new CheckPlot(plugin);
        String[] plot = plugin.getPlot(block);
        int maxClaim = plugin.getConfig().getInt("ClaimMaximum");
        int minClaim = plugin.getConfig().getInt("ClaimMinimum");

        if(plot != null && (cityData.getString(plot[0]+".Plots."+plot[1]+".Type").equalsIgnoreCase("Open") || !checkPlot.checkProtection(block, player))) {
            return false;
        } else if(claimTotal >= maxClaim && !claimant.equalsIgnoreCase(city)) {
            if(!map.containsKey(playerUUID) || !map.get(playerUUID).equalsIgnoreCase(chunkKey)) {
                player.sendRawMessage("This land is fully protected by " + cityData.getString(claimant + ".Name") + ".");
                map.put(playerUUID,chunkKey);
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> map.remove(playerUUID),600);
            }
            return true;
        } else if(claimTotal >= minClaim && !claimant.equalsIgnoreCase(city)) {
            double stopChance = ((double) claimTotal / maxClaim)*100;
            Random rand = new Random();
            int roll = rand.nextInt(100)+1;
            if(stopChance > roll){
                if(!map.containsKey(playerUUID) || !map.get(playerUUID).equalsIgnoreCase(chunkKey)) {
                    player.sendRawMessage("This land is protected by " + cityData.getString(claimant+".Name")+" at " + Math.round(stopChance) + "% power.");
                    map.put(playerUUID,chunkKey);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> map.remove(playerUUID),600);
                }
                return true;
            }
        } else if (claimant.equalsIgnoreCase(city) && checkPlot.checkProtection(block, player)) {
            String owner = Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(plot[0]+".Plots."+plot[1]+".Owner"))).getName();
            player.sendRawMessage("This is a protected plot owned by " + owner + "!");
            return true;
        }
        return false;
    }
}
