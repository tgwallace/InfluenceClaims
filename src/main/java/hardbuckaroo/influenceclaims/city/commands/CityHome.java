package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CityHome implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityHome(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();

        if(!playerData.contains(player.getUniqueId().toString()+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(player.getUniqueId().toString() + ".City");

        if(!cityData.contains(cityUUID+".Home")) {
            player.sendRawMessage("Your city has not set a home.");
            return true;
        }

        Location tpLocation = (Location) cityData.get(cityUUID+".Home");
        Block playerLocation = player.getLocation().getBlock();
        player.sendRawMessage("Teleporting in 5 seconds, don't move!");
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(player.getLocation().getBlock().equals(playerLocation)) {
                player.teleport(Objects.requireNonNull(tpLocation), PlayerTeleportEvent.TeleportCause.COMMAND);
            } else {
                player.sendRawMessage("You moved! Teleport cancelled.");
            }
        },100);

        return true;
    }
}
