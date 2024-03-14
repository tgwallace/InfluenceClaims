package hardbuckaroo.influenceclaims;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.elections.CityElectionManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LoginListener implements Listener {
    private final InfluenceClaims plugin;
    public LoginListener(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    @EventHandler (priority = EventPriority.LOW)
    public void onLogin(PlayerJoinEvent event) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration nationData = plugin.getNationData();
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        playerData.set(playerUUID+".LastLogin", LocalDate.now().format(formatter));

        plugin.updateScoreboard();

        if(!playerData.contains(playerUUID+".ChatChannel")) {
            playerData.set(playerUUID + ".ChatChannel", "World");
        }

        List<String> messages = playerData.getStringList(playerUUID+".Messages");
        if(!messages.isEmpty()) {
            player.sendRawMessage(plugin.color("&lWhile you were gone, you missed the following messages:"));
            for (String message : messages) {
                player.sendRawMessage(message);
            }
            playerData.set(playerUUID + ".Messages", null);
        }
        
        if(playerData.contains(playerUUID+".City")) {
            String cityUUID = playerData.getString(playerUUID + ".City");
            String cityName = cityData.getString(cityUUID + ".Name");

            if (cityData.contains(cityUUID + ".Elections")) {
                List<String> cityElectorate;
                for (String issue : cityData.getConfigurationSection(cityUUID + ".Elections").getKeys(false)) {
                    CityElectionManager manager = new CityElectionManager(plugin);
                    cityElectorate = manager.getElectorate(cityUUID,issue);

                    if (cityElectorate.contains(playerUUID)) {
                        player.sendRawMessage(plugin.color("Elections are currently underway in " + cityName + "! Use /CityVote to cast your vote or check the results."));
                        break;
                    }
                }
            }
        } else if (playerData.contains(playerUUID+".Exile")) {
            String exileUUID = playerData.getString(playerUUID+".Exile");
            List<String> cityElectorate;
            String cityName = cityData.getString(exileUUID + ".Name");
            for (String issue : cityData.getConfigurationSection(exileUUID + ".Elections").getKeys(false)) {
                CityElectionManager manager = new CityElectionManager(plugin);
                cityElectorate = manager.getElectorate(exileUUID,issue);

                if (cityElectorate.contains(playerUUID)) {
                    player.sendRawMessage(plugin.color("Elections are currently underway to overthrow the government of " + cityName + "! As an exile, you can use /CityVote to participate. If your faction wins, you will be able to rejoin the city!"));
                    break;
                }
            }
        }

        if(cityData.contains(playerData.getString(playerUUID+".City")+".Nation")) {
            String nationUUID = cityData.getString(playerData.getString(playerUUID+".City")+".Nation");
            String nationColor = nationData.getString(nationUUID + ".Color");
            String nationName = nationData.getString(nationUUID+".Name");

            if(nationData.contains(nationUUID+".Elections")) {
                List<String> nationElectorate = new ArrayList<>();
                String nationGov = nationData.getString(nationUUID + ".Government");
                for (String issue : nationData.getConfigurationSection(nationUUID + ".Elections").getKeys(false)) {
                    if (nationGov.equalsIgnoreCase("Democracy") || issue.equalsIgnoreCase("overthrow")) {
                        for (String city : nationData.getStringList(nationUUID + ".Cities")) {
                            nationElectorate.addAll(cityData.getStringList(city + ".Players"));
                        }
                    } else if (nationGov.equalsIgnoreCase("Oligarchy")) {
                        for (String city : nationData.getStringList(nationUUID + ".Cities")) {
                            nationElectorate.addAll(cityData.getStringList(city + ".Nobles"));
                        }
                    }
                    if (nationElectorate.contains(playerUUID)) {
                        player.sendRawMessage(plugin.color("Elections are currently underway in " + nationName + "! If you are eligible to vote, use /NationVote to cast your vote or check the results."));
                        break;
                    }
                }
            }
        }
        plugin.savePlayerData();
    }
}
