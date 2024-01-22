package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotManageCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotManageCity(InfluenceClaims plugin){
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

        //Checks whether arguments were provided.
        if(strings.length == 0) {
            sender.sendRawMessage("Please provide arguments for this command. Options are: Type, Name");
            return true;
        }

        if(strings[0].equalsIgnoreCase("type")){
            if(strings.length == 1) {
                sender.sendRawMessage("Please confirm which type you would like this plot to be. Options are: Player, Arena, Open");
            } else if (strings[1].equalsIgnoreCase("player")) {
                cityData.set(plotData[0]+".Plots."+plotData[1]+".Type","Player");
                sender.sendRawMessage("This plot is now a player plot!");
            }  else if (strings[1].equalsIgnoreCase("arena")) {
                cityData.set(plotData[0]+".Plots."+plotData[1]+".Type","Arena");
                sender.sendRawMessage("This plot is now an arena plot!");
            }   else if (strings[1].equalsIgnoreCase("open")) {
                cityData.set(plotData[0]+".Plots."+plotData[1]+".Type","Open");
                sender.sendRawMessage("This plot is now an open plot!");
            } else {
                sender.sendRawMessage("Invalid plot type. Options are: Player, Arena, Open");
            }
        } else if (strings[0].equalsIgnoreCase("name")) {
            String name = String.join(" ",strings);
            name = name.substring(name.indexOf(" ")+1);
            cityData.set(plotData[0]+".Plots."+plotData[1]+".Name",name);
            sender.sendRawMessage("This plot is now called " + name + "!");
        } else {
            sender.sendRawMessage("Invalid arguments for this command. Options are: Type, Name");
        }

        plugin.saveCityData();
        return true;
    }
}
