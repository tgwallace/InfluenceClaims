package hardbuckaroo.influenceclaims.nation.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.nation.elections.NationStartElection;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class NationStartScheduledElections {
    private final InfluenceClaims plugin;

    public NationStartScheduledElections(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void startElections(){
        FileConfiguration nationData = plugin.getNationData();
        LocalDate today = LocalDate.now();
        int interval = plugin.getConfig().getInt("ElectionFrequency");

        if(nationData.getKeys(false).size() == 0){
            return;
        }

        for(String nationUUID : nationData.getKeys(false)) {
            if(Objects.requireNonNull(nationData.getString(nationUUID + ".Government")).equalsIgnoreCase("Democracy") || Objects.requireNonNull(nationData.getString(nationUUID + ".Government")).equalsIgnoreCase("Oligarchy")) {
                LocalDate lastElection = LocalDate.parse(Objects.requireNonNull(nationData.getString(nationUUID + ".LastElection")));
                if(ChronoUnit.DAYS.between(lastElection, today) >= interval && !nationData.contains(nationUUID+".Elections.Leader")) {
                    NationStartElection nationStartElection = new NationStartElection(plugin);
                    nationStartElection.startElection(nationUUID);
                }
            }
        }
    }
}
