package hardbuckaroo.influenceclaims.city.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CityElectionManager {
    private final InfluenceClaims plugin;

    public CityElectionManager(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    public List<String> getElectorate(String cityUUID, String issue) {
        FileConfiguration cityData = plugin.getCityData();
        String government = cityData.getString(cityUUID+".Government");
        List<String> electorate = new ArrayList<>();
        if (government.equalsIgnoreCase("Democracy") || issue.equalsIgnoreCase("Overthrow")) {
            electorate = cityData.getStringList(cityUUID + ".Players");
            if(issue.equalsIgnoreCase("Overthrow")) {
                List<String> exiles = cityData.getStringList(cityUUID+".Exiles");
                electorate.addAll(exiles);
            }
        } else if (government.equalsIgnoreCase("Oligarchy")) {
            electorate.add(cityData.getString(cityUUID+".Leader"));
            if(cityData.contains(cityUUID+".Roles")) {
                for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                    if(cityData.getBoolean(cityUUID+".Roles."+title+".Permissions.Vote")) {
                        electorate.addAll(cityData.getStringList(cityUUID + ".Roles." + title + ".Players"));
                    }
                }
                Set<String> dedupe = new LinkedHashSet<>(electorate);
                electorate.clear();
                electorate.addAll(dedupe);
            }
        }
        electorate.removeIf(playerUUID -> ChronoUnit.DAYS.between(LocalDate.parse(cityData.getString(playerUUID + ".LastLogin")), LocalDate.now()) >= plugin.getConfig().getInt("AbsenteeTimer"));
        return electorate;
    }
}
