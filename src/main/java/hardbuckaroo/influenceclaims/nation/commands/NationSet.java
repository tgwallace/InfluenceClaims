package hardbuckaroo.influenceclaims.nation.commands;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NationSet implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationSet(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration nationData = plugin.getNationData();
        Player sender = (Player) commandSender;
        String playerUUID = sender.getUniqueId().toString();

        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");
        
        if(!cityData.contains(cityUUID+".Nation")) {
            sender.sendRawMessage("You are not part of a nation.");
            return true;
        }
        String nationUUID = cityData.getString(cityUUID+".Nation");

        if(!nationData.getString(nationUUID+".Leader").equals(playerUUID)){
            sender.sendRawMessage("Only the " + nationData.getString(nationUUID+".LeaderTitle") + " can change city settings!");
            return true;
        }

        List<String> nationPlayers = new ArrayList<>();
        for(String city : Objects.requireNonNull(nationData.getStringList(nationUUID + ".Cities"))) {
            List<String> citizens = cityData.getStringList(city + ".Players");
            nationPlayers.addAll(citizens);
        }

        if(strings.length >0 && strings[0].equalsIgnoreCase("color")) {
            if(strings.length > 1 && Arrays.asList("&1","&2","&3","&4","&5","&6","&7","&8","&9","&a","&b","&c","&d","&e","&f").contains(strings[1])){
                nationData.set(nationUUID+".Color",strings[1]);
                sender.sendRawMessage(plugin.color(strings[1])+"Nation color has been successfully set!");
            }
            else{
                sender.sendRawMessage("Input must be a two-character chat color code starting with & and ending with 1-9 or a-f.");
                return true;
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("motto")) {
            String motto = String.join(" ",strings);
            motto = motto.substring(motto.indexOf(" ")+1);
            nationData.set(nationUUID+".Motto",motto);
            sender.sendRawMessage("Nation motto has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("leadertitle")) {
            String leadertitle = String.join(" ",strings);
            leadertitle = leadertitle.substring(leadertitle.indexOf(" ")+1);
            nationData.set(nationUUID+".LeaderTitle",leadertitle);
            sender.sendRawMessage("Nation Leader Title has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("citizentitle")) {
            String citizentitle = String.join(" ",strings);
            citizentitle = citizentitle.substring(citizentitle.indexOf(" ")+1);
            nationData.set(nationUUID+".CitizenTitle",citizentitle);
            sender.sendRawMessage("Nation Citizen Title has been successfully updated!");
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("name")) {
            String name = String.join(" ",strings);
            name = name.substring(name.indexOf(" ")+1);
            nationData.set(nationUUID+".Name",name);
            sender.sendRawMessage("Nation Name has been successfully updated!");
        }  else if(strings.length >0 && strings[0].equalsIgnoreCase("leader")) {
            if(strings.length > 2 && strings[2].equalsIgnoreCase("confirm")){
                String addedUUID = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                if(nationPlayers.contains(addedUUID)){
                    nationData.set(nationUUID+".Leader",addedUUID);
                    plugin.nationMessage(nationUUID,nationData.getString(nationUUID+".Color")+"&l"+strings[1]+" has been made the "+ nationData.getString(nationUUID+".LeaderTitle") + " of " + nationData.getString(nationUUID+".Name") + "!",true);
                } else {
                    sender.sendRawMessage("Could not locate a player named " + strings[1] + ". Please check your spelling and try again.");
                }
            } else if (strings.length > 1){
                String addedUUID = Bukkit.getOfflinePlayer(strings[1]).getUniqueId().toString();
                if(!nationPlayers.contains(addedUUID)){
                    sender.sendRawMessage("Could not locate a player named " + strings[1] + " in " + nationData.get(nationUUID+".Name") + ". Please check your spelling and try again.");
                } else {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to hand the title of " + nationData.get(nationUUID + ".LeaderTitle") + " to " + strings[1] + "? Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationset leader " + strings[1] + " confirm"));
                    sender.spigot().sendMessage(component);
                }
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("government")) {
            if(strings.length == 1) {
                sender.sendRawMessage("Please select a government type. Options are: Monarchy, Oligarchy, Democracy");
                return true;
            }

            String oldGov = nationData.getString(nationUUID+".Government");
            String newGov = strings[1];

            if(oldGov.equalsIgnoreCase(newGov)) {
                sender.sendRawMessage("Government type is already " + newGov + "!");
                return true;
            }

            if(oldGov.equalsIgnoreCase("Monarchy") && (newGov.equalsIgnoreCase("Oligarchy") || newGov.equalsIgnoreCase("Democracy"))) {
                if(strings.length < 3 || !strings[2].equalsIgnoreCase("confirm")) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to change the government of " + nationData.getString(nationUUID+".Name") + " to " + newGov + "? You will not be able to change it back without triggering an election. Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationset government " + newGov + " confirm"));
                    sender.spigot().sendMessage(component);
                } else {
                    nationData.set(nationUUID + ".Government", newGov);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    nationData.set(nationUUID+".LastElection",LocalDate.now().format(formatter));
                    plugin.nationMessage(nationUUID, "The government of " + nationData.getString(nationUUID + ".Name") + " has been changed to " + newGov + "!", true);
                }
            } else if (newGov.equalsIgnoreCase("Oligarchy") || newGov.equalsIgnoreCase("Democracy") || newGov.equalsIgnoreCase("Monarchy")) {
                if(nationData.contains(nationUUID + ".Elections.Government")) {
                    sender.sendRawMessage("A vote is already underway to change the government of " + nationData.getString(nationUUID+".Name") + ". Vote in that election using /NationVote.");
                }
                else if(strings.length < 3 || !strings[2].equalsIgnoreCase("confirm")) {
                    TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call a vote to change the government of " + nationData.getString(nationUUID+".Name") + " to " + newGov + "? Click here to confirm.")));
                    component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationset government " + newGov + " confirm"));
                    sender.spigot().sendMessage(component);
                } else {
                    nationData.set(nationUUID + ".Elections.Government.Type", newGov);
                    nationData.set(nationUUID + ".Elections.Government.VoteCount.Yes", 1);
                    nationData.set(nationUUID + ".Elections.Government.VoteCount.No", 0);
                    nationData.set(nationUUID + ".Elections.Government.Voters."+playerUUID, "Yes");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    nationData.set(nationUUID + ".Elections.Government.StartDate", LocalDate.now().format(formatter));
                    plugin.nationMessage(nationUUID, nationData.getString(nationUUID+".LeaderTitle") + " " + sender.getName() + " has called a vote to change the government of " + nationData.getString(nationUUID+".Name") + " to " + newGov + "! If you are eligible, use /NationVote to vote.", true);
                }
            } else {
                sender.sendRawMessage("Invalid government type. Options are: Monarchy, Oligarchy, Democracy");
            }
        } else if(strings.length >0 && strings[0].equalsIgnoreCase("tag")) {
            if(strings.length == 2 && strings[1].length() <= 10) {
                String tag = strings[1];
                nationData.set(nationUUID+".Tag", tag);
                sender.sendRawMessage("Nation tag has been changed to "+tag+"!");
            } else {
                sender.sendRawMessage("Nation tag must be 10 characters or less with no spaces!");
            }
        } else {
            sender.sendRawMessage("Invalid input. Options are: Name, Leader, Color, Motto, LeaderTitle, NobilityTitle, CitizenTitle, Government");
        }

        plugin.saveNationData();
        return true;
    }
}
