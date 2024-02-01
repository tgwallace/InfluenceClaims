package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageCityLegitimacy;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class CityRole implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityRole(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String playerUUID = sender.getUniqueId().toString();

        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        if(!cityData.getString(cityUUID+".Leader").equals(playerUUID)){
            sender.sendRawMessage("Only the " + cityData.getString(cityUUID+".LeaderTitle") + " can manage roles!");
            return true;
        }

        if(strings.length > 0 && strings[0].equalsIgnoreCase("add")) {
            if(strings.length >= 3) {
                String title = String.join(" ",strings);
                title = title.substring(title.indexOf(" ")+1);
                title = title.substring(title.indexOf(" ")+1);

                String addedUUID = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                if (!cityData.contains(cityUUID+".Roles."+title)) {
                    sender.sendRawMessage("Could not find a role with title " + title + ". Please check your spelling and try again.");
                } else if(cityData.getStringList(cityUUID+".Roles."+title+".Players").contains(addedUUID)){
                    sender.sendRawMessage(strings[1] + " is already a " + title+".");
                }
                else if(cityData.getStringList(cityUUID+".Players").contains(addedUUID)){
                    List<String> nobilityList = cityData.getStringList(cityUUID+".Roles."+title+".Players");
                    nobilityList.add(addedUUID);
                    cityData.set(cityUUID+".Roles."+title+".Players",nobilityList);

                    plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".Color")+strings[1] + " has been promoted to " + title + " of " + cityData.getString(cityUUID+".Name") + "!",true);
                    plugin.playerMessage(addedUUID,"You have been promoted to the role of " + title + "!");
                } else {
                    sender.sendRawMessage("Could not find a player named " + strings[1] + " in " + cityData.getString(cityUUID+".Name")+". Please check your spelling and try again.");
                }
            } else {
                sender.sendRawMessage("Invalid input. Use /CityRole add [name] [title]");
            }
        } else if (strings.length > 0 && strings[0].equalsIgnoreCase("remove")) {
            if(strings.length >= 3) {
                String title = String.join(" ",strings);
                title = title.substring(title.indexOf(" ")+1);
                title = title.substring(title.indexOf(" ")+1);

                String removedUUID = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                if (!cityData.contains(cityUUID+".Roles."+title)) {
                    sender.sendRawMessage("Could not find a role with title " + title + ". Please check your spelling and try again.");
                } else if(!cityData.getStringList(cityUUID+".Roles."+title+".Players").contains(removedUUID)){
                    sender.sendRawMessage(strings[1] + " is already not a " + title+".");
                }
                else if(cityData.getStringList(cityUUID+".Players").contains(removedUUID)){
                    List<String> nobilityList = cityData.getStringList(cityUUID+".Roles."+title+".Players");
                    nobilityList.remove(removedUUID);
                    cityData.set(cityUUID+".Roles."+title,nobilityList);

                    plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".Color")+strings[1] + " has been removed from their role of "+title+"!",true);
                    plugin.playerMessage(removedUUID,"You have been removed from the role of " + title + "!");
                } else {
                    sender.sendRawMessage("Could not find a player named " + strings[1] + " in " + cityData.getString(cityUUID+".Name")+". Please check your spelling and try again.");
                }
            } else {
                sender.sendRawMessage("Invalid input. Use /CityRole remove [name] [title]");
            }
        } else if (strings.length > 0 && strings[0].equalsIgnoreCase("create")) {
            if(strings.length >= 2) {
                String title = String.join(" ",strings);
                title = title.substring(title.indexOf(" ")+1);

                cityData.set(cityUUID+".Roles."+title+".Permissions.Invite",false);
                cityData.set(cityUUID+".Roles."+title+".Permissions.Kick",false);
                cityData.set(cityUUID+".Roles."+title+".Permissions.Vote",false);
                cityData.set(cityUUID+".Roles."+title+".Permissions.PlotCreate",false);
                cityData.set(cityUUID+".Roles."+title+".Permissions.PlotRevoke",false);

                sender.sendRawMessage("You have created a new role called " + title + "!");
            } else {
                sender.sendRawMessage("Please provide a title for this role: /CityRole Create [Title]");
            }
        } else if (strings.length > 0 && strings[0].equalsIgnoreCase("delete")) {
            if(strings.length >= 2) {
                String title = String.join(" ",strings);
                title = title.substring(title.indexOf(" ")+1);
                if (cityData.contains(cityUUID+".Roles."+title)) {
                    cityData.set(cityUUID+".Roles."+title,null);
                    sender.sendRawMessage("The role of " + title + " has been deleted!");
                } else {
                    sender.sendRawMessage("Could not find a title named " + title + ". Please check your spelling and try again.");
                }
            } else {
                sender.sendRawMessage("Please provide the title of the role you would like to delete.");
            }
        } else if (strings.length > 0 && strings[0].equalsIgnoreCase("permissions")) {
            if (strings.length >= 2) {
                String title = String.join(" ", strings);

                if(strings.length >=3) {
                    title = title.substring(title.indexOf(" ") + 1);
                    title = title.substring(title.indexOf(" ") + 1);

                    if (cityData.contains(cityUUID + ".Roles." + title + ".Permissions." + strings[1])) {
                        if (cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions." + strings[1])) {
                            cityData.set(cityUUID + ".Roles." + title + ".Permissions." + strings[1], false);
                        } else {
                            cityData.set(cityUUID + ".Roles." + title + ".Permissions." + strings[1], true);
                        }
                        sender.sendRawMessage(title + " permission " + strings[1] + " is now " + cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions." + strings[1]));
                        plugin.saveCityData();
                        return true;
                    }
                }

                title = String.join(" ", strings);
                title = title.substring(title.indexOf(" ") + 1);

                if (cityData.contains(cityUUID + ".Roles." + title)) {
                    sender.sendRawMessage("Permissions for " + title + " are listed below. Click a permission to toggle it.");
                    for (String permission : cityData.getConfigurationSection(cityUUID + ".Roles." + title + ".Permissions").getKeys(false)) {
                        TextComponent component = new TextComponent(permission + ": " + cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions." + permission));
                        component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityrole permissions " + permission + " " + title));
                        sender.spigot().sendMessage(component);
                    }
                    return true;
                }

                sender.sendRawMessage("Invalid input. Use /CityRole Permissions [Permission] [Title] to toggle a specific permission or /CityRole Permissions [Title] for a list of permissions.");
            } else {
                sender.sendRawMessage("Invalid input. Use /CityRole Permissions [Permission] [Title] to toggle a specific permission or /CityRole Permissions [Title] for a list of permissions.\"");
            }
        } else {
            sender.sendRawMessage("Invalid input. Valid options are Add, Remove, Create, Delete, or Permissions.");
        }

        plugin.saveCityData();
        return true;
    }
}
