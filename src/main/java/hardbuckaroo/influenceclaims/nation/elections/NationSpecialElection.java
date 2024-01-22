package hardbuckaroo.influenceclaims.nation.elections;

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
import java.util.List;
import java.util.Objects;

public class NationSpecialElection implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public NationSpecialElection(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");

        if(!cityData.contains(playerUUID+".Nation")) {
            player.sendRawMessage("You are not part of a nation.");
            return true;
        }
        String nationUUID = playerData.getString(playerUUID+".City");

        if(nationData.contains(nationUUID+".Elections.Leader")) {
            player.sendRawMessage("Cannot call for a special election while an election is already underway.");
            return true;
        }

        if(nationData.contains(nationUUID+".Elections.SpecialElection")) {
            player.sendRawMessage("Cannot call for a special election while another vote for a special election is already underway.");
            return true;
        }

        if(nationData.getBoolean(nationUUID+".SpecialElectionCooldown")) {
            player.sendRawMessage("Cannot call for a special election when one has already been called this term.");
            return true;
        }

        String government = nationData.getString(nationUUID+".Government");
        List<String> nobles = nationData.getStringList(nationUUID+".Nobles");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String leaderTitle = nationData.getString(nationUUID+".LeaderTitle");
        String nobilityTitle = nationData.getString(nationUUID+".NobilityTitle");

        if(Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
            player.sendRawMessage("Cannot call a special election in a Monarchy. Use /NationOverthrow if you wish to call a vote to overthrow the current " + leaderTitle + "!");
            return true;
        } else if (Objects.requireNonNull(government).equalsIgnoreCase("Oligarchy") && !nobles.contains(playerUUID)) {
            player.sendRawMessage("Only a " + nobilityTitle + " can call a special election in an Oligarchy! Use /NationOverthrow if you wish to call a vote to overthrow the current government.");
            return true;
        }

        if(Objects.requireNonNull(government).equalsIgnoreCase("Oligarchy") && nobles.contains(playerUUID) && (strings.length < 1 || !strings[0].equalsIgnoreCase("confirm"))) {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call for a special election? This cannot be undone once started. Click here to confirm.")));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationspecialelection confirm"));
            player.spigot().sendMessage(component);
            return true;
        } else if (government.equalsIgnoreCase("Democracy") && (strings.length < 1 || !strings[0].equalsIgnoreCase("confirm"))) {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call for a special election? This cannot be undone once started. Click here to confirm.")));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationspecialelection confirm"));
            player.spigot().sendMessage(component);
            return true;
        }

        if(government.equalsIgnoreCase("Oligarchy") && nobles.contains(playerUUID) && strings.length >= 1 && strings[0].equalsIgnoreCase("confirm")) {
            nationData.set(nationUUID+".Elections.SpecialElection.VoteCount.Yes",0);
            nationData.set(nationUUID+".Elections.SpecialElection.VoteCount.No",0);
            nationData.set(nationUUID+".Elections.SpecialElection.StartDate",LocalDate.now().format(formatter));
            plugin.nationMessage(nationUUID, "A vote for a special election has been called! If you are a " + nationData.getString(nationUUID+".NobilityTitle") + ", use /NationVote to vote.",true);
        } else if (government.equalsIgnoreCase("Democracy") && strings.length >= 1 && strings[0].equalsIgnoreCase("confirm")) {
            nationData.set(nationUUID+".Elections.SpecialElection.VoteCount.Yes",0);
            nationData.set(nationUUID+".Elections.SpecialElection.VoteCount.No",0);
            nationData.set(nationUUID+".Elections.SpecialElection.StartDate",LocalDate.now().format(formatter));
            plugin.nationMessage(nationUUID, "A vote for a special election has been called! Use /NationVote to vote.",true);
        }
        nationData.set(nationUUID+".SpecialElectionCooldown",true);
        plugin.saveNationData();
        return true;
    }
}
