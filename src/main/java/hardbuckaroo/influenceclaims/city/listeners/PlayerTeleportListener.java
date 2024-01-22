package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;

public class PlayerTeleportListener implements Listener {
    private final InfluenceClaims plugin;
    public PlayerTeleportListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerTeleportEvent(PlayerTeleportEvent event){
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();

        Player attacker = event.getPlayer();
        String attackerUUID = attacker.getUniqueId().toString();

        String attackerCityUUID = playerData.getString(attackerUUID+".City");
        String defenderCityUUID = plugin.getClaimant(plugin.getChunkKey(Objects.requireNonNull(event.getTo()).getChunk()));

        if(defenderCityUUID == null) {
            return;
        }

        String attackerStance = cityData.getString(attackerCityUUID+".Stances."+defenderCityUUID);
        String defenderStance = cityData.getString(defenderCityUUID+".Stances."+attackerCityUUID);

        if ((attackerStance != null && Objects.requireNonNull(attackerStance).equalsIgnoreCase("Hostile")) || (defenderStance != null && Objects.requireNonNull(defenderStance).equalsIgnoreCase("Hostile"))) {
            event.setCancelled(true);
            attacker.sendRawMessage("You cannot teleport into hostile territory!");
        }
    }
}
