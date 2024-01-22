package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlotExpandCommandCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotExpandCommandCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        if (playerData.getBoolean(senderUUID + ".PlotExpand") || playerData.getBoolean(senderUUID + ".PlotMode")) {
            sender.sendRawMessage("Plot expand is already active!");
            return true;
        }

        //Checks if sender is part of a city.
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city. Create a city using /CityCreate [city name] to create a city and start creating plots.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        //Checks whether the sender has permission to create plots.
        if(!cityData.getString(cityUUID+".Leader").equalsIgnoreCase(senderUUID) && !cityData.getStringList(cityUUID+".Nobles").contains(senderUUID)){
            sender.sendRawMessage("Only the " + cityData.getString(cityUUID+".LeaderTitle") + " or a " + cityData.getString(cityUUID+".NobilityTitle") + " can expand plots.");
            return true;
        }

        //Checks whether the sender is standing in a plot.
        if(plugin.getPlot(sender.getLocation().getBlock()) == null) {
            sender.sendRawMessage("You are not standing in a plot. Stand inside the plot you would like to manage.");
            return true;
        }
        String[] plot = plugin.getPlot(sender.getLocation().getBlock());

        playerData.set(senderUUID+".PlotExpand",true);
        playerData.set(senderUUID+".PlotExpanding",plot[1]);
        plugin.savePlayerData();
        sender.sendRawMessage("Plot expansion mode enabled. Right click each corner of an area to expand a plot.");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(playerData.getBoolean(senderUUID+".PlotExpand")) {
                playerData.set(senderUUID + ".PlotExpand", false);
                playerData.set(senderUUID + ".PlotCorner1", null);
                playerData.set(senderUUID+".PlotExpanding",null);
                plugin.savePlayerData();
                sender.sendRawMessage("Plot expansion mode timed out.");
            }
        }, 1200L);

        return true;
    }
}
