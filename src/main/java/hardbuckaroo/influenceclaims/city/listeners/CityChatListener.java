package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.logging.Level;

public class CityChatListener implements Listener {
    private final InfluenceClaims plugin;
    public CityChatListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        if(Objects.requireNonNull(playerData.getString(playerUUID + ".ChatChannel")).equalsIgnoreCase("City")) {
            String cityUUID = playerData.getString(playerUUID+".City");
            String cityColor = cityData.getString(cityUUID+".Color");
            String cityTag = cityData.getString(cityUUID+".Tag");
            List<String> citizens = cityData.getStringList(cityUUID+".Players");
            Set<Player> originalList = event.getRecipients();
            originalList.removeIf(recipient -> !citizens.contains(recipient.getUniqueId().toString()));
            for(Player recipient : originalList) {
                recipient.sendRawMessage(plugin.color(cityColor + "["+cityTag+"]")+player.getName()+": "+event.getMessage());
            }
            plugin.getLogger().log(Level.INFO,plugin.color(cityColor + "["+cityTag+"]")+player.getName()+": "+event.getMessage());
            event.setCancelled(true);
        }
    }
}
