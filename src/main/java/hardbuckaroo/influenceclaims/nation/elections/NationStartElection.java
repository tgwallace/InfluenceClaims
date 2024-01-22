package hardbuckaroo.influenceclaims.nation.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NationStartElection {
    private final InfluenceClaims plugin;

    public NationStartElection(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void startElection(String nationUUID){
        FileConfiguration nationData = plugin.getNationData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if(nationData.contains(nationUUID+".Elections.Overthrow")) {
            nationData.set(nationUUID+".Elections.Overthrow",null);
        }

        nationData.set(nationUUID +".Elections.Leader.VoteCount."+nationData.getString(nationUUID+".Leader"),0);
        nationData.set(nationUUID +".Elections.Leader.StartDate",LocalDate.now().format(formatter));
        nationData.set(nationUUID +".LastElection",LocalDate.now().format(formatter));
        plugin.saveNationData();
    }
}
