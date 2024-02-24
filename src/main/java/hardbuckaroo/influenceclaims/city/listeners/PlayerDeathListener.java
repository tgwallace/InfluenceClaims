package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;

public class PlayerDeathListener implements Listener {
    private final InfluenceClaims plugin;
    public PlayerDeathListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event){
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();

        Player defender = event.getEntity();
        Player attacker = defender.getKiller();

        String chunkKey = plugin.getChunkKey(defender.getLocation().getChunk());
        String claimant = plugin.getClaimant(chunkKey);
        Location location = defender.getLocation();
        String[] plot = plugin.getPlot(location.getBlock());

        if (claimant != null && plot != null && Objects.requireNonNull(cityData.getString(plot[0] + ".Plots." + plot[1] + ".Type")).equalsIgnoreCase("arena")) {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            return;
        }

        if(attacker != null) {
            String attackerUUID = Objects.requireNonNull(attacker).getUniqueId().toString();
            String defenderUUID = Objects.requireNonNull(defender).getUniqueId().toString();

            if (attackerUUID.equalsIgnoreCase(defenderUUID)) {
                return;
            }

            String attackerCityUUID = playerData.getString(attackerUUID + ".City");
            String defenderCityUUID = playerData.getString(defenderUUID + ".City");

            int killValue = plugin.getConfig().getInt("KillValue");

            if (claimant != null && (claimant.equalsIgnoreCase(attackerCityUUID) || claimant.equalsIgnoreCase(defenderCityUUID))) {
                ManageClaims manageClaims = new ManageClaims(plugin);
                manageClaims.addTempClaim(chunkKey, attackerCityUUID, killValue);
                manageClaims.subtractTempClaim(chunkKey, defenderCityUUID, killValue);
            }
        }
    }
}
