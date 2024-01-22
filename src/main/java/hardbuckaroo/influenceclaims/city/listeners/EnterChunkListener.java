package hardbuckaroo.influenceclaims.city.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class EnterChunkListener implements Listener {
    private final InfluenceClaims plugin;
    public EnterChunkListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnterChunk(PlayerMoveEvent event) {
        FileConfiguration claimData = plugin.getClaimData();
        FileConfiguration cityData = plugin.getCityData();

        Chunk oldChunk = event.getFrom().getChunk();
        Chunk newChunk = event.getTo().getChunk();

        Player player = event.getPlayer();

        if(!oldChunk.equals(newChunk)){
            String oldChunkKey = plugin.getChunkKey(oldChunk);
            String newChunkKey = plugin.getChunkKey(newChunk);

            if(claimData.contains(newChunkKey+".Claims")) {
                String oldChunkClaimant = plugin.getClaimant(oldChunkKey);
                String newChunkClaimant = plugin.getClaimant(newChunkKey);

                if(newChunkClaimant != null && !newChunkClaimant.equals(oldChunkClaimant)) {
                    String name = plugin.color(cityData.getString(newChunkClaimant + ".Color") + "&l&n" + cityData.getString(newChunkClaimant + ".Name"));
                    TextComponent component = new TextComponent(name);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,component);
                } else if (newChunkClaimant == null && oldChunkClaimant != null) {
                    String name = plugin.color("&l&2" + "Wilderness");
                    TextComponent component = new TextComponent(name);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR,component);
                }
            }
        }
    }
}
