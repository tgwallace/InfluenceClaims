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

        if(cityData.getKeys(false).isEmpty()){
            return;
        }

        for(String cityUUID : cityData.getKeys(false)) {
            if((Objects.requireNonNull(cityData.getString(cityUUID + ".Government")).equalsIgnoreCase("Democracy") || Objects.requireNonNull(cityData.getString(cityUUID + ".Government")).equalsIgnoreCase("Oligarchy")) && !cityData.contains(cityUUID+".Elections.Overthrow") && !cityData.contains(cityUUID+".Elections.Leader")) {
                LocalDate lastElection = LocalDate.parse(Objects.requireNonNull(cityData.getString(cityUUID + ".LastElection")));
                if(ChronoUnit.DAYS.between(lastElection, today) >= interval) {
                    CityStartElection cityStartElection = new CityStartElection(plugin);
                    cityStartElection.startElection(cityUUID);
                    if(cityData.contains(cityUUID+".Elections.SpecialElection"))
                        cityData.set(cityUUID+".Elections.SpecialElection",null);
                }
            }
        }
    }
}
