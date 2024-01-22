package hardbuckaroo.influenceclaims.city.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CityStartElection {
    private final InfluenceClaims plugin;

    public CityStartElection(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void startElection(String cityUUID){
        FileConfiguration cityData = plugin.getCityData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if(cityData.contains(cityUUID+".Elections.Overthrow")) {
            cityData.set(cityUUID+".Elections.Overthrow",null);
        }

        cityData.set(cityUUID +".Elections.Leader.VoteCount."+cityData.getString(cityUUID+".Leader"),0);
        cityData.set(cityUUID +".Elections.Leader.StartDate",LocalDate.now().format(formatter));
        cityData.set(cityUUID +".LastElection",LocalDate.now().format(formatter));
        plugin.saveCityData();
    }
}
