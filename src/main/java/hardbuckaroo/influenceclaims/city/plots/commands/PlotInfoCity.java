package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PlotInfoCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotInfoCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;

        Block block = sender.getLocation().getBlock();
        String[] plot = plugin.getPlot(block);
        
        if(plot == null) {
            sender.sendRawMessage("You are not standing in a plot. Please move into the plot you would like to manage.");
        } else {
            TextComponent message = new TextComponent(new TextComponent(plugin.color("&m                                                     ")));
            message.addExtra("\nOwner: " + Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(cityData.getString(plot[0] + ".Plots." + plot[1] + ".Owner")))).getName());

            if(cityData.contains(plot[0] + ".Plots." + plot[1] + ".Name")) {
                message.addExtra("\nPlot Name: " + cityData.getString(plot[0] + ".Plots." + plot[1] + ".Name"));
            }

            List<String> whitelist = cityData.getStringList(plot[0] + ".Plots." + plot[1] + ".Whitelist");
            String whitelistString = "";
            for(String name : whitelist) {
                whitelistString = whitelistString.concat(Bukkit.getOfflinePlayer(UUID.fromString(name)).getName()+", ");
            }
            if(whitelistString.length() > 2) {
                whitelistString = whitelistString.substring(0,whitelistString.length()-2);
            }
            message.addExtra("\nWhitelist: " + whitelistString);

            message.addExtra("\nPlot Type: " + cityData.getString(plot[0] + ".Plots." + plot[1] + ".Type"));

            message.addExtra(plugin.color("\n&m                                                     "));

            sender.spigot().sendMessage(message);
        }
        return true;
    }
}
