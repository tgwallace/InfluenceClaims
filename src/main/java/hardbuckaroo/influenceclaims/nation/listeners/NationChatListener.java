package hardbuckaroo.influenceclaims.nation.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;
import java.util.logging.Level;

public class NationChatListener implements Listener {
    private final InfluenceClaims plugin;
    public NationChatListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration nationData = plugin.getNationData();
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        if(Objects.requireNonNull(playerData.getString(playerUUID + ".ChatChannel")).equalsIgnoreCase("Nation")) {
            String playerCityUUID = playerData.getString(playerUUID+".City");
            String nationUUID = cityData.getString(playerCityUUID+".Nation");
            String nationColor = nationData.getString(nationUUID+".Color");
            String nationTag = nationData.getString(nationUUID+".Tag");
            List<String> citizens = new ArrayList<>();

            for(String cityUUID : Objects.requireNonNull(nationData.getStringList(nationUUID + ".Cities"))) {
                citizens.addAll(cityData.getStringList(cityUUID + ".Players"));
            }

            for(String recipientUUID : citizens) {
                Player recipient = Bukkit.getPlayer(UUID.fromString(recipientUUID));
                if(recipient != null) {
                    recipient.sendRawMessage(plugin.color(nationColor + "[" + nationTag + "]") + player.getName() + ": " + event.getMessage());
                }
            }
            plugin.getLogger().log(Level.INFO,plugin.color(nationColor + "["+nationTag+"]")+player.getName()+": "+event.getMessage());
            event.setCancelled(true);
        }
    }
}
