package hardbuckaroo.influenceclaims.city.elections;

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
import org.bukkit.event.Listener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CityOverthrow implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityOverthrow(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        String cityUUID;

        if(!plugin.getConfig().getBoolean("OverthrowEnabled")) {
            player.sendRawMessage("The overthrow mechanic is disabled on this server.");
            return true;
        }

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        cityUUID = playerData.getString(playerUUID+".City");

        if(cityData.contains(cityUUID+".Elections.Overthrow")) {
            player.sendRawMessage("A vote to overthrow the government is already underway!");
            return true;
        }

        String government = cityData.getString(cityUUID+".Government");
        String leaderTitle = cityData.getString(cityUUID+".LeaderTitle");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate today = LocalDate.now();
        LocalDate leaderLastLogin = LocalDate.parse(Objects.requireNonNull(playerData.getString(cityData.getString(cityUUID + ".Leader")+".LastLogin")));
        double legitimacy = cityData.getDouble(cityUUID+".Legitimacy");
        double legitimacyThreshold = plugin.getConfig().getDouble("LegitimacyThreshold");

        if(Objects.requireNonNull(cityData.getString(cityUUID + ".Leader")).equalsIgnoreCase(playerUUID)) {
            player.sendRawMessage("Cannot overthrow your own government. Use /CitySet Government [type] instead!");
            return true;
        }

        if(cityData.getBoolean(cityUUID+".OverthrowCooldown")) {
            player.sendRawMessage("Cannot call to overthrow the government when a vote to overthrow has already been called this term.");
            return true;
        }

        if(legitimacy > legitimacyThreshold) {
            player.sendRawMessage("Cannot call to overthrow the government until legitimacy drops below " + String.format("%.0f%%",100*legitimacyThreshold) + ".");
            return true;
        }

        if(strings.length < 1 || !strings[0].equalsIgnoreCase("confirm")) {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "Are you sure you want to call to overthrow the current city government? You will currently need "+String.format("%.0f%%",cityData.getDouble(cityUUID+".Legitimacy")+" of votes to win. Click here to confirm."))));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityoverthrow confirm"));
            player.spigot().sendMessage(component);
            return true;
        } else {
            if (Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Status_Quo", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Democracy", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Oligarchy", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.New_Monarch", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.StartDate", LocalDate.now().format(formatter));
            } else if (government.equalsIgnoreCase("Oligarchy")) {
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Status_Quo", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Democracy", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Monarchy", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.StartDate", LocalDate.now().format(formatter));
                cityData.set(cityUUID+".OverthrowCooldown",true);
            } else if (government.equalsIgnoreCase("Democracy")) {
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Status_Quo", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Oligarchy", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.VoteCount.Monarchy", 0);
                cityData.set(cityUUID + ".Elections.Overthrow.StartDate", LocalDate.now().format(formatter));
                cityData.set(cityUUID+".OverthrowCooldown",true);
            }
        }
        plugin.saveCityData();
        return true;
    }
}
