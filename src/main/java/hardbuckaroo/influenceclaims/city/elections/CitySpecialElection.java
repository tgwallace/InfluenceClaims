package hardbuckaroo.influenceclaims.city.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CitySpecialElection implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CitySpecialElection(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        String cityUUID;

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        cityUUID = playerData.getString(playerUUID+".City");

        if(cityData.contains(cityUUID+".Elections.Leader")) {
            player.sendRawMessage("Cannot call for a special election while an election is already underway.");
            return true;
        }

        if(cityData.contains(cityUUID+".Elections.SpecialElection")) {
            player.sendRawMessage("Cannot call for a special election while another vote for a special election is already underway.");
            return true;
        }

        if(cityData.contains(cityUUID+".Elections.Overthrow")) {
            player.sendRawMessage("Cannot call for a special election while an overthrow is underway.");
            return true;
        }

        if(cityData.getBoolean(cityUUID+".SpecialElectionCooldown")) {
            player.sendRawMessage("Cannot call for a special election when one has already been called this term.");
            return true;
        }

        String government = cityData.getString(cityUUID+".Government");

        List<String> nobles = new ArrayList<>();
        if(cityData.contains(cityUUID+".Roles")) {
            for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                if(cityData.getBoolean(cityUUID+".Roles."+title+".Permissions.Vote")) {
                    nobles.addAll(cityData.getStringList(cityUUID + ".Roles." + title + ".Players"));
                }
            }
            Set<String> dedupe = new LinkedHashSet<>(nobles);
            nobles.clear();
            nobles.addAll(dedupe);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String leaderTitle = cityData.getString(cityUUID+".LeaderTitle");

        if(Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
            player.sendRawMessage("Cannot call a special election in a Monarchy. Use /CityOverthrow if you wish to call a vote to overthrow the current " + leaderTitle + "!");
            return true;
        } else if (Objects.requireNonNull(government).equalsIgnoreCase("Oligarchy") && !nobles.contains(playerUUID)) {
            player.sendRawMessage("Only those with voting rights can call a special election in an Oligarchy! Use /CityOverthrow if you wish to call a vote to overthrow the current government.");
            return true;
        }

        if(Objects.requireNonNull(government).equalsIgnoreCase("Oligarchy") && nobles.contains(playerUUID) && (strings.length < 1 || !strings[0].equalsIgnoreCase("confirm"))) {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call for a special election? This cannot be undone once started. Click here to confirm.")));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityspecialelection confirm"));
            player.spigot().sendMessage(component);
            return true;
        } else if (government.equalsIgnoreCase("Democracy") && (strings.length < 1 || !strings[0].equalsIgnoreCase("confirm"))) {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call for a special election? This cannot be undone once started. Click here to confirm.")));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityspecialelection confirm"));
            player.spigot().sendMessage(component);
            return true;
        }

        if(government.equalsIgnoreCase("Oligarchy") && nobles.contains(playerUUID) && strings.length >= 1 && strings[0].equalsIgnoreCase("confirm")) {
            cityData.set(cityUUID+".Elections.SpecialElection.VoteCount.Yes",0);
            cityData.set(cityUUID+".Elections.SpecialElection.VoteCount.No",0);
            cityData.set(cityUUID+".Elections.SpecialElection.StartDate",LocalDate.now().format(formatter));
            plugin.cityMessage(cityUUID, "A vote for a special election has been called! If you are eligible, use /CityVote to vote.",true);
        } else if (government.equalsIgnoreCase("Democracy") && strings.length >= 1 && strings[0].equalsIgnoreCase("confirm")) {
            cityData.set(cityUUID+".Elections.SpecialElection.VoteCount.Yes",0);
            cityData.set(cityUUID+".Elections.SpecialElection.VoteCount.No",0);
            cityData.set(cityUUID+".Elections.SpecialElection.StartDate",LocalDate.now().format(formatter));
            plugin.cityMessage(cityUUID, "A vote for a special election has been called! Use /CityVote to vote.",true);
        }
        cityData.set(cityUUID+".SpecialElectionCooldown",true);
        plugin.saveCityData();
        return true;
    }
}
