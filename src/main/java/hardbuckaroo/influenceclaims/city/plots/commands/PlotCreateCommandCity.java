package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlotCreateCommandCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotCreateCommandCity(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String senderUUID = sender.getUniqueId().toString();

        if (playerData.getBoolean(senderUUID + ".PlotMode") || playerData.getBoolean(senderUUID + ".PlotExpand")) {
            sender.sendRawMessage("Plot mode is already active!");
            return true;
        }

        //Checks if sender is part of a city.
        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city. Create a city using /CityCreate [city name] to create a city and start creating plots.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        boolean perms = false;
        if(cityData.contains(cityUUID+".Roles")) {
            for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                if (cityData.getStringList(cityUUID + ".Roles."+title+".Players").contains(senderUUID)) {
                    perms = cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions.PlotCreate");
                }
                if(perms) break;
            }
        }

        //Checks whether the sender has permission to create plots.
        if(!cityData.getString(cityUUID+".Leader").equalsIgnoreCase(senderUUID) && !perms){
            sender.sendRawMessage("You do not have permission to create plots.");
            return true;
        }

        playerData.set(senderUUID+".PlotMode",true);
        plugin.savePlayerData();
        sender.sendRawMessage("Plot creation mode enabled. Right click each corner of an area to create a plot.");

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if(playerData.getBoolean(senderUUID+".PlotMode")) {
                playerData.set(senderUUID + ".PlotMode", false);
                playerData.set(senderUUID + ".PlotCorner1", null);
                plugin.savePlayerData();
                sender.sendRawMessage("Plot creation mode timed out.");
            }
        }, 1200L);

        return true;
    }
}
