package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotTransferCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotTransferCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        //Checks if sender is part of a city.
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city. Create a city using /CityCreate [city name] to create a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        //Checks whether the sender is standing in a plot.
        if(plugin.getPlot(sender.getLocation().getBlock()) == null) {
            sender.sendRawMessage("You are not standing in a plot. Stand inside the plot you would like to manage.");
            return true;
        }

        String[] plotData = plugin.getPlot(sender.getLocation().getBlock());
        String owner = cityData.getString(plotData[0] +".Plots."+plotData[1]+".Owner");

        //Checks whether the sender owns this plot.
        if(!owner.equals(sender.getUniqueId().toString())) {
            String ownerName = Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(plotData[0]+".Plots."+plotData[1]+".Owner"))).getName();
            sender.sendRawMessage("Only the plot owner can manage a plot. This plot is owned by " + ownerName + ".");
            return true;
        }

        //Checks whether the sender provided a name.
        if(strings.length == 0){
            sender.sendRawMessage("Please provide the name of the player you wish to transfer ownership to.");
            return false;
        }

        String name = strings[0];
        OfflinePlayer recipient = Bukkit.getOfflinePlayer(name);
        if(!playerData.contains(recipient.getUniqueId().toString())){
            sender.sendRawMessage("Could not locate a player named " + name + ". Players must have logged in to the server at least once before to be added to a plot.");
        } else {
            cityData.set(plotData[0]+".Plots."+plotData[1]+".Owner", recipient.getUniqueId().toString());

            Player messageRecipient = Bukkit.getPlayer(name);
            //Checks whether player is online to receive a message.
            if(messageRecipient != null) {
                messageRecipient.sendRawMessage(sender.getName() + " has made you the owner of a plot located at " + plotData[1] + " in the city of " + cityData.getString(plotData[0]+".Name") + "!");
            } else {
                plugin.playerMessage(recipient.getUniqueId().toString(),sender.getName() + " has made you the owner of a plot located at " + plotData[1] + " in the city of " + cityData.getString(plotData[0]+".Name") + "!");
            }
            sender.sendRawMessage(name + " has been made the owner of this plot!");
        }
        plugin.saveCityData();
        return true;
    }
}
