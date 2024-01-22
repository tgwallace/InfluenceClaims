package hardbuckaroo.influenceclaims.city.plots.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Objects;
import java.util.Random;

public class ArenaRespawnListener implements Listener {
    private final InfluenceClaims plugin;
    public ArenaRespawnListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event){
        FileConfiguration cityData = plugin.getCityData();
        Player defender = event.getPlayer();
        Location location = defender.getLocation();
        String[] plot = plugin.getPlot(location.getBlock());
        if(ArrayUtils.isEmpty(plot)) return;

        String plotType = cityData.getString(plot[0] + ".Plots." + plot[1]+".Type");

        if(Objects.requireNonNull(plotType).equalsIgnoreCase("arena")){
            Location location1 = location, location2 = location, location3 = location, location4 = location;
            while(true) {
                location1 = location1.getBlock().getRelative(1,0,0).getLocation();
                plot = plugin.getPlot(location1.getBlock());
                plotType = cityData.getString(plot[0] + ".Plots." + plot[1]+".Type");
                if(plotType == null || !plotType.equalsIgnoreCase("arena")) {
                    break;
                }

                location2 = location2.getBlock().getRelative(-1,0,0).getLocation();
                plot = plugin.getPlot(location2.getBlock());
                plotType = cityData.getString(plot[0] + ".Plots." + plot[1]+".Type");
                if(plotType == null || !plotType.equalsIgnoreCase("arena")) {
                    break;
                }

                location3 = location3.getBlock().getRelative(0,0,1).getLocation();
                plot = plugin.getPlot(location3.getBlock());
                plotType = cityData.getString(plot[0] + ".Plots." + plot[1]+".Type");
                if(plotType == null || !plotType.equalsIgnoreCase("arena")) {
                    break;
                }

                location4 = location4.getBlock().getRelative(0,0,-1).getLocation();
                plot = plugin.getPlot(location4.getBlock());
                plotType = cityData.getString(plot[0] + ".Plots." + plot[1]+".Type");
                if(plotType == null || !plotType.equalsIgnoreCase("arena")) {
                    break;
                }
            }
            event.setRespawnLocation(location);
        }
    }
}
