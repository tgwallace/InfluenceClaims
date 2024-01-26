package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerAttackListener implements Listener {
    private final InfluenceClaims plugin;
    public PlayerAttackListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAttackEvent(EntityDamageByEntityEvent event){
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();

        Player attacker;
        Player defender;

        if(event.getDamager() instanceof Player){
            attacker = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile){
            Projectile arrow = (Projectile) event.getDamager();
            if(arrow.getShooter() instanceof Player) {
                attacker = (Player) arrow.getShooter();
            } else return;
        } else return;

        if(event.getEntity() instanceof Player) {
            defender = (Player) event.getEntity();
        } else return;

        String attackerUUID = attacker.getUniqueId().toString();
        String defenderUUID = defender.getUniqueId().toString();

        String attackerCityUUID = playerData.getString(attackerUUID+".City");
        String defenderCityUUID = playerData.getString(defenderUUID+".City");

        String attackerStance = cityData.getString(attackerCityUUID+".Stances."+defenderCityUUID);
        String defenderStance = cityData.getString(defenderCityUUID+".Stances."+attackerCityUUID);

        String attackerNation = cityData.getString(attackerCityUUID+".Nation");
        String defenderNation = cityData.getString(defenderCityUUID+".Nation");

        String claimant = plugin.getClaimant(plugin.getChunkKey(defender.getLocation().getChunk()));
        Location location = defender.getLocation();
        String[] plot = plugin.getPlot(location.getBlock());

        if(claimant != null && plot != null && cityData.getString(plot[0] + ".Plots." + plot[1]+".Type").equalsIgnoreCase("arena")){
            return;
        }

        if(attackerCityUUID != null && attackerCityUUID.equalsIgnoreCase(defenderCityUUID)) {
            event.setCancelled(true);
            attacker.sendRawMessage("You cannot attack members of your city outside arena plots!");
        }
        else if(attackerNation != null && attackerNation.equalsIgnoreCase(defenderNation)) {
            event.setCancelled(true);
            attacker.sendRawMessage("You cannot attack members of your nation outside arena plots!");
        }
        else if(attackerStance != null && defenderStance != null && attackerStance.equalsIgnoreCase("Friendly") && defenderStance.equalsIgnoreCase("Friendly")) {
            event.setCancelled(true);
            attacker.sendRawMessage("You cannot attack " + defender.getName() + " because your cities are friendly with one another!");
        }
        else if(claimant != null && (attackerStance == null || !attackerStance.equalsIgnoreCase("Hostile")) && (defenderStance == null || !defenderStance.equalsIgnoreCase("Hostile"))) {
            event.setCancelled(true);
            attacker.sendRawMessage("You cannot attack neutral players in claimed territory!");
        }
    }
}
