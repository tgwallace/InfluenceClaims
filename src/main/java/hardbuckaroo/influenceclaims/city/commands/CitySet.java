package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageCityLegitimacy;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class CitySet implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CitySet(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player sender = (Player) commandSender;
        String playerUUID = sender.getUniqueId().toString();
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(plugin);

        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        if(!cityData.getString(cityUUID+".Leader").equals(playerUUID)){
            sender.sendRawMessage("Only the " + cityData.getString(cityUUID+".LeaderTitle") + " can change city settings!");
            return true;
        }

        if(strings.length >0 && strings[0].equalsIgnoreCase("color")) {
            if(strings.length > 1 && Arrays.asList("&1","&2","&3","&4","&5","&6","&7","&8","&9","&a","&b","&c","&d","&e","&f").contains(strings[1])){
                cityData.set(cityUUID+".Color",strings[1]);
                sender.sendRawMessage(plugin.color(strings[1])+"City color has been successfully set!");
            }
            else{
                sender.sendRawMessage("Input must be a two-character chat color code starting with & and ending with 1-9 or a-f.");
                return true;
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("motto")) {
            String motto = String.join(" ",strings);
            motto = motto.substring(motto.indexOf(" ")+1);
            cityData.set(cityUUID+".Motto",motto);
            sender.sendRawMessage("City motto has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("leadertitle")) {
            String leadertitle = String.join(" ",strings);
            leadertitle = leadertitle.substring(leadertitle.indexOf(" ")+1);
            cityData.set(cityUUID+".LeaderTitle",leadertitle);
            sender.sendRawMessage("City Leader Title has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("citizentitle")) {
            String citizentitle = String.join(" ",strings);
            citizentitle = citizentitle.substring(citizentitle.indexOf(" ")+1);
            cityData.set(cityUUID+".CitizenTitle",citizentitle);
            sender.sendRawMessage("City Citizen Title has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("name")) {
            String name = String.join(" ",strings);
            name = name.substring(name.indexOf(" ")+1);
            cityData.set(cityUUID+".Name",name);
            sender.sendRawMessage("City Name has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("leader")) {
            if(strings.length > 2 && strings[2].equalsIgnoreCase("confirm")){
                String addedUUID = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                if(cityData.getStringList(cityUUID+".Players").contains(addedUUID)){
                    cityData.set(cityUUID+".Leader",addedUUID);
                    plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".Color")+"&l"+strings[1]+" has been made the "+ cityData.getString(cityUUID+".LeaderTitle") + " of " + cityData.getString(cityUUID+".Name") + "!",true);
                    manageCityLegitimacy.subtractLegitimacy(cityUUID,0.15);
                } else {
                    sender.sendRawMessage("Could not locate a player named " + strings[1] + ". Please check your spelling and try again.");
                }
            } else if (strings.length > 1){
                String addedUUID = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                if(!cityData.getStringList(cityUUID+".Players").contains(addedUUID)){
                    sender.sendRawMessage("Could not locate a player named " + strings[1] + " in " + cityData.get(cityUUID+".Name") + ". Please check your spelling and try again.");
                } else {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to hand the title of " + cityData.get(cityUUID + ".LeaderTitle") + " to " + strings[1] + "? Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityset leader " + strings[1] + " confirm"));
                    sender.spigot().sendMessage(component);
                }
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("stance")) {
            String targetCityName = String.join(" ",strings);
            targetCityName = targetCityName.substring(targetCityName.indexOf(" ")+1);
            targetCityName = targetCityName.substring(targetCityName.indexOf(" ")+1);
            String targetCityUUID = plugin.getCityUUIDFromName(targetCityName);
            String senderCityUUID = playerData.getString(playerUUID+".City");
            if(strings.length > 1 && targetCityUUID!= null && !targetCityUUID.equalsIgnoreCase(senderCityUUID)) {
                if(strings[1].equalsIgnoreCase("Hostile")) {
                    cityData.set(senderCityUUID + ".Stances." + targetCityUUID, "Hostile");
                    plugin.cityMessage(senderCityUUID, "The city of " + cityData.getString(targetCityUUID+".Name") + " has been declared an enemy!",true);
                    plugin.cityMessage(targetCityUUID, "The city of " + cityData.getString(senderCityUUID+".Name") + " has declared us an enemy!",true);
                } else if(strings[1].equalsIgnoreCase("Neutral")) {
                    cityData.set(senderCityUUID + ".Stances." + targetCityUUID, null);
                    plugin.cityMessage(senderCityUUID, "The city of " + cityData.getString(targetCityUUID+".Name") + " has been declared neither friend nor foe.",true);
                    plugin.cityMessage(targetCityUUID, "The city of " + cityData.getString(senderCityUUID+".Name") + " is now approaching us with neutrality.",true);
                } else if(strings[1].equalsIgnoreCase("Friendly")) {
                    cityData.set(senderCityUUID + ".Stances." + targetCityUUID, "Friendly");
                    plugin.cityMessage(senderCityUUID, "The city of " + cityData.getString(targetCityUUID+".Name") + " has been declared a friend!",true);
                    plugin.cityMessage(targetCityUUID, "The city of " + cityData.getString(senderCityUUID+".Name") + " is now considering us a friend!",true);
                } else {
                    sender.sendRawMessage("Invalid input. Options are Hostile, Friendly, or Neutral.");
                }
                plugin.updateScoreboard();
            } else if (targetCityUUID == null) {
                sender.sendRawMessage("Could not find a city named " + targetCityName + ". Please check your spelling and try again. Correct usage is /CitySet Stance [stance] [city].");
            } else {
                sender.sendRawMessage("Invalid input. Must target a foreign city and declare stance as Hostile, Friendly, or Neutral. Correct usage is /CitySet Stance [stance] [city].");
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("government")) {
            if(strings.length == 1) {
                sender.sendRawMessage("Please select a government type. Options are: Monarchy, Oligarchy, Democracy");
                return true;
            }

            String oldGov = cityData.getString(cityUUID+".Government");
            String newGov = strings[1];

            if(oldGov.equalsIgnoreCase(newGov)) {
                sender.sendRawMessage("Government type is already " + newGov + "!");
                return true;
            }

            if(oldGov.equalsIgnoreCase("Monarchy") && (newGov.equalsIgnoreCase("Oligarchy") || newGov.equalsIgnoreCase("Democracy"))) {
                if(strings.length < 3 || !strings[2].equalsIgnoreCase("confirm")) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to change the government of " + cityData.getString(cityUUID+".Name") + " to " + newGov + "? You will not be able to change it back without triggering an election. Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityset government " + newGov + " confirm"));
                    sender.spigot().sendMessage(component);
                } else {
                    cityData.set(cityUUID + ".Government", newGov);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    cityData.set(cityUUID+".LastElection",LocalDate.now().format(formatter));
                    plugin.cityMessage(cityUUID, "The government of " + cityData.getString(cityUUID + ".Name") + " has been changed to " + newGov + "!", true);
                }
            } else if (newGov.equalsIgnoreCase("Oligarchy") || newGov.equalsIgnoreCase("Democracy") || newGov.equalsIgnoreCase("Monarchy")) {
                if(cityData.contains(cityUUID + ".Elections.Government")) {
                    sender.sendRawMessage("A vote is already underway to change the government of " + cityData.getString(cityUUID+".Name") + ". Vote in that election using /CityVote.");
                }
                else if(strings.length < 3 || !strings[2].equalsIgnoreCase("confirm")) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call a vote to change the government of " + cityData.getString(cityUUID+".Name") + " to " + newGov + "? Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityset government " + newGov + " confirm"));
                    sender.spigot().sendMessage(component);
                } else {
                    cityData.set(cityUUID + ".Elections.Government.Type", newGov);
                    cityData.set(cityUUID + ".Elections.Government.VoteCount.Yes", 1);
                    cityData.set(cityUUID + ".Elections.Government.VoteCount.No", 0);
                    cityData.set(cityUUID + ".Elections.Government.Voters."+playerUUID, "Yes");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    cityData.set(cityUUID + ".Elections.Government.StartDate", LocalDate.now().format(formatter));
                    plugin.cityMessage(cityUUID, cityData.getString(cityUUID+".LeaderTitle") + " " + sender.getName() + " has called a vote to change the government of " + cityData.getString(cityUUID+".Name") + " to " + newGov + "! If you are eligible, use /CityVote to vote.", true);
                    manageCityLegitimacy.subtractLegitimacy(cityUUID,0.10);
                }
            } else {
                sender.sendRawMessage("Invalid government type. Options are: Monarchy, Oligarchy, Democracy");
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("home")) {
            Location location = sender.getLocation();
            if(Objects.requireNonNull(cityUUID).equalsIgnoreCase(plugin.getClaimant(plugin.getChunkKey(location.getChunk())))) {
                cityData.set(cityUUID + ".Home", location);
                sender.sendRawMessage("City home has been successfully updated!");
            } else {
                sender.sendRawMessage("City home can only be set in chunks claimed by the city!");
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("tag")) {
            if(strings.length == 2 && strings[1].length() <= 10) {
                String tag = strings[1];
                cityData.set(cityUUID+".Tag", tag);
                sender.sendRawMessage("City tag has been changed to "+tag+"!");
            } else {
                sender.sendRawMessage("City tag must be 10 characters or less with no spaces!");
            }
        } else {
            sender.sendRawMessage("Invalid input. Options are: Name, Leader, Color, Motto, Stance, LeaderTitle, CitizenTitle, Government, Home, Tag");
        }

        plugin.updateScoreboard();
        plugin.saveCityData();
        return true;
    }
}
