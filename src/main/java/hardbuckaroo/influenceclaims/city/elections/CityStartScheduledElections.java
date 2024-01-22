package hardbuckaroo.influenceclaims.city.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CityStartScheduledElections {
    private final InfluenceClaims plugin;

    public CityStartScheduledElections(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void startElections(){
        FileConfiguration cityData = plugin.getCityData();
        LocalDate today = LocalDate.now();
        int interval = plugin.getConfig().getInt("ElectionFrequency");

        if(cityData.getKeys(false).size() == 0){
            return;
        }

        for(String cityUUID : cityData.getKeys(false)) {
            if(Objects.requireNonNull(cityData.getString(cityUUID + ".Government")).equalsIgnoreCase("Democracy") || Objects.requireNonNull(cityData.getString(cityUUID + ".Government")).equalsIgnoreCase("Oligarchy")) {
                LocalDate lastElection = LocalDate.parse(Objects.requireNonNull(cityData.getString(cityUUID + ".LastElection")));
                if(ChronoUnit.DAYS.between(lastElection, today) >= interval && !cityData.contains(cityUUID+".Elections.Leader")) {
                    CityStartElection cityStartElection = new CityStartElection(plugin);
                    cityStartElection.startElection(cityUUID);
                }
            }
        }
    }
}
