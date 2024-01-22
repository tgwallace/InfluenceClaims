package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotDeleteCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotDeleteCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        //Checks if sender is part of a city.
        if (!playerData.contains(sender.getUniqueId().toString() + ".City")) {
            sender.sendRawMessage("You are not part of a city. Create a city using /CityCreate [city name] to create a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        //Checks whether the sender is standing in a plot.
        if (plugin.getPlot(sender.getLocation().getBlock()) == null) {
            sender.sendRawMessage("You are not standing in a plot. Stand inside the plot you would like to manage.");
            return true;
        }

        String[] plotData = plugin.getPlot(sender.getLocation().getBlock());
        String owner = cityData.getString(plotData[0] + ".Plots." + plotData[1] + ".Owner");

        //Checks whether the sender owns this plot.
        if (!owner.equals(sender.getUniqueId().toString())) {
            String ownerName = Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(plotData[0] + ".Plots." + plotData[1] + ".Owner"))).getName();
            sender.sendRawMessage("Only the plot owner can manage a plot. This plot is owned by " + ownerName + ".");
            return true;
        }

        if (strings.length < 1 || !strings[0].equalsIgnoreCase("confirm")) {
            String message;
            message = "&c Are you sure? This plot will be permanently deleted. Click here to confirm.";
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message)));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityplotdelete confirm"));
            sender.spigot().sendMessage(component);
            return true;
        } else {
            cityData.set(plotData[0] + ".Plots." + plotData[1], null);
            plugin.saveCityData();

            sender.sendRawMessage("This plot has been deleted.");
        }

        return true;
    }
}
