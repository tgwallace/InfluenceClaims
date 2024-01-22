package hardbuckaroo.influenceclaims.city.plots.listeners;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Arrays;
import java.util.UUID;

public class EnterPlotListener implements Listener {
    private final InfluenceClaims plugin;
    public EnterPlotListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onEnterPlot(PlayerMoveEvent event) {
        FileConfiguration cityData = plugin.getCityData();

        String[] oldPlot = plugin.getPlot(event.getFrom().getBlock());
        String[] newPlot = plugin.getPlot(event.getTo().getBlock());

        Player player = event.getPlayer();

        if(newPlot != null && !Arrays.equals(oldPlot, newPlot)){
            if(cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Type").equalsIgnoreCase("Player")) {
                if(!cityData.contains(newPlot[0] + ".Plots." + newPlot[1] + ".Name")) {
                    String ownerName = Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Owner"))).getName();
                    TextComponent component = new TextComponent(plugin.color("&o" + ownerName + "'s Plot"));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                } else {
                    TextComponent component = new TextComponent(plugin.color("&o" + cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Name")));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                }
            } else if(cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Type").equalsIgnoreCase("Arena")) {
                if(!cityData.contains(newPlot[0] + ".Plots." + newPlot[1] + ".Name")) {
                    TextComponent component = new TextComponent(plugin.color("&c&lArena"));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                } else {
                    TextComponent component = new TextComponent(plugin.color("&c&l"+cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Name")+" - Arena"));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                }
            }  else if(cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Type").equalsIgnoreCase("Open")) {
                if(!cityData.contains(newPlot[0] + ".Plots." + newPlot[1] + ".Name")) {
                    TextComponent component = new TextComponent(plugin.color("&f&lOpen"));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                } else {
                    TextComponent component = new TextComponent(plugin.color("&f&l"+cityData.getString(newPlot[0] + ".Plots." + newPlot[1] + ".Name")+" - Open"));
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
                }
            }
        }
    }
}
