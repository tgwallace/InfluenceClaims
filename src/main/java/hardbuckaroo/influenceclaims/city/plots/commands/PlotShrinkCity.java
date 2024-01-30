package hardbuckaroo.influenceclaims.city.plots.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlotShrinkCity implements CommandExecutor {
    private final InfluenceClaims plugin;
    public PlotShrinkCity(InfluenceClaims plugin){
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
        Location location = sender.getLocation();

        String[] plotData = plugin.getPlot(sender.getLocation().getBlock());
        String owner = cityData.getString(plotData[0] +".Plots."+plotData[1]+".Owner");

        //Checks whether the sender owns this plot.
        if(!owner.equals(sender.getUniqueId().toString())) {
            String ownerName = Bukkit.getOfflinePlayer(UUID.fromString(cityData.getString(plotData[0]+".Plots."+plotData[1]+".Owner"))).getName();
            sender.sendRawMessage("Only the plot owner can shrink a plot. This plot is owned by " + ownerName + ".");
            return true;
        }

        ArrayList<String> cornerList = (ArrayList<String>) plugin.getCityData().getStringList(cityUUID+".Plots."+plotData[1]+".Coords");
        if(cornerList.size() <= 1) {
            sender.sendRawMessage("Cannot shrink the only coords of a plot. Use /PlotDelete instead.");
            return true;
        }

        String finalCorners = "";
        for(String cornersEntry : cornerList) {
            String[] worldPlot = cornersEntry.split("\\|");
            String[] corners = worldPlot[1].split(":");
            List<Integer> corner1 = Arrays.stream(corners[0].split(",")).map(Integer::parseInt).collect(Collectors.toList());
            List<Integer> corner2 = Arrays.stream(corners[1].split(",")).map(Integer::parseInt).collect(Collectors.toList());

            int x = (int) location.getX();
            int y = (int) location.getY();
            int z = (int) location.getZ();

            int minX = Math.min(corner1.get(0), corner2.get(0));
            int maxX = Math.max(corner1.get(0), corner2.get(0));
            int minY = Math.min(corner1.get(1), corner2.get(1));
            int maxY = Math.max(corner1.get(1), corner2.get(1));
            int minZ = Math.min(corner1.get(2), corner2.get(2));
            int maxZ = Math.max(corner1.get(2), corner2.get(2));

            if (location.getWorld().getName().equalsIgnoreCase(worldPlot[0]) && x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                finalCorners = cornersEntry;
                break;
            }
        }

        cornerList.remove(finalCorners);
        cityData.set(cityUUID+".Plots."+plotData[1]+".Coords",cornerList);
        plugin.saveCityData();
        sender.sendRawMessage("Removed corners " + finalCorners + " from this plot!");
        return true;
    }
}
