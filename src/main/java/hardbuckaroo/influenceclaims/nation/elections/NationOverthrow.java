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
import java.util.Objects;

public class NationOverthrow implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public NationOverthrow(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();

        if(!plugin.getConfig().getBoolean("OverthrowEnabled")) {
            player.sendRawMessage("The overthrow mechanic is disabled on this server.");
            return true;
        }

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");

        if(!cityData.contains(cityUUID+".Nation")) {
            player.sendRawMessage("You are not part of a nation.");
            return true;
        }
        String nationUUID = cityData.getString(cityUUID+".Nation");

        if(nationData.contains(nationUUID+".Elections.Overthrow")) {
            player.sendRawMessage("A vote to overthrow the government is already underway!");
            return true;
        }

        String government = nationData.getString(nationUUID+".Government");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        double legitimacy = nationData.getDouble(nationUUID+".Legitimacy");
        double legitimacyThreshold = plugin.getConfig().getDouble("LegitimacyThreshold");

        if(Objects.requireNonNull(nationData.getString(nationUUID + ".Leader")).equalsIgnoreCase(playerUUID)) {
            player.sendRawMessage("Cannot overthrow your own government. Use /NationSet Government [type] instead!");
            return true;
        }

        if(nationData.getBoolean(nationUUID+".OverthrowCooldown")) {
            player.sendRawMessage("Cannot call to overthrow the government when a vote to overthrow has already been called this term.");
            return true;
        }

        if(legitimacy > legitimacyThreshold) {
            player.sendRawMessage("Cannot call to overthrow the government until legitimacy drops below " + String.format("%.0f%%",100*legitimacyThreshold) + ".");
            return true;
        }

        if(strings.length < 1 || !strings[0].equalsIgnoreCase("confirm")) {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call to overthrow the current national government? You will currently need "+String.format("%.0f%%",nationData.getDouble(nationUUID+".Legitimacy")+" of votes to win. Click here to confirm."))));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationoverthrow confirm"));
            player.spigot().sendMessage(component);
            return true;
        } else {
            if (Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Status_Quo", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Democracy", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Oligarchy", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.New_Monarch", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.StartDate", LocalDate.now().format(formatter));
            } else if (government.equalsIgnoreCase("Oligarchy")) {
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Status_Quo", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Democracy", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Monarchy", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.StartDate", LocalDate.now().format(formatter));
                nationData.set(nationUUID+".OverthrowCooldown",true);
            } else if (government.equalsIgnoreCase("Democracy")) {
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Status_Quo", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Oligarchy", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.VoteCount.Monarchy", 0);
                nationData.set(nationUUID + ".Elections.Overthrow.StartDate", LocalDate.now().format(formatter));
                nationData.set(nationUUID+".OverthrowCooldown",true);
            }
        }
        plugin.saveNationData();
        return true;
    }
}
