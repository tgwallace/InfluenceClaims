package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageCityLegitimacy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlotRevokeCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotRevokeCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(plugin);

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

        boolean perms = false;
        if(cityData.contains(cityUUID+".Roles")) {
            for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                if (cityData.getStringList(cityUUID + ".Roles.Players").contains(senderUUID)) {
                    perms = cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions.PlotRevoke");
                }
                if(perms) break;
            }
        }

        //Checks whether the sender has permission to revoke plots.
        if(!cityData.getString(cityUUID+".Leader").equalsIgnoreCase(senderUUID) && !perms){
            sender.sendRawMessage("You do not have permission to revoke plots.");
            return true;
        }

        OfflinePlayer recipient = Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(plotData[0]+".Plots."+plotData[1]+"Owner")));
        String name = recipient.getName();

        cityData.set(plotData[0]+".Plots."+plotData[1]+"Owner", senderUUID);
        plugin.saveCityData();

        Player messageRecipient = Bukkit.getPlayer(name);

        //Checks whether player is online to receive a message.
        if(messageRecipient != null) {
            messageRecipient.sendRawMessage(sender.getName() + " has revoked your ownership of the plot located at " + plotData[1] + " in the city of " + cityData.getString(plotData[0]+".Name") + "!");
        } else {
            plugin.playerMessage(recipient.getUniqueId().toString(),sender.getName() + " has revoked your ownership of the plot located at " + plotData[1] + " in the city of " + cityData.getString(plotData[0]+".Name") + "!");
        }
        sender.sendRawMessage("You are now the owner of this plot!");
        manageCityLegitimacy.subtractLegitimacy(cityUUID,0.10);

        return true;
    }
}
