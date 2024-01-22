package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class MobAttackListener implements Listener {
    private final InfluenceClaims plugin;
    public MobAttackListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAttackEvent(EntityDamageByEntityEvent event){
        FileConfiguration playerData = plugin.getPlayerData();

        Player attacker;
        Entity defender;

        if(event.getDamager() instanceof Player){
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile){
            Projectile arrow = (Projectile) event.getDamager();
            if(arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            } else return;
        } else return;

        if(event.getEntity() instanceof Animals || event.getEntity() instanceof Villager) {
            defender = event.getEntity();
        } else return;

        String attackerUUID = attacker.getUniqueId().toString();
        String attackerCityUUID = playerData.getString(attackerUUID+".City");

        String claimant = plugin.getClaimant(plugin.getChunkKey(defender.getLocation().getChunk()));
        if((claimant != null && attackerCityUUID != null && !attackerCityUUID.equalsIgnoreCase(claimant)) || (attackerCityUUID == null && claimant != null)){
            event.setCancelled(true);
        }
    }
}
