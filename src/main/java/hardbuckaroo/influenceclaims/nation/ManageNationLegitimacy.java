package hardbuckaroo.influenceclaims.nation;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

public class ManageNationLegitimacy {
    private final InfluenceClaims plugin;

    public ManageNationLegitimacy(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void addLegitimacy(String nationUUID, double amount) {
        FileConfiguration nationData = plugin.getNationData();
        double oldLegitimacy = nationData.getDouble(nationUUID+".Legitimacy");
        double newLegitimacy = oldLegitimacy + amount;

        if(newLegitimacy > 1) {
            newLegitimacy = 1.00;
        }
        nationData.set(nationUUID+".Legitimacy",newLegitimacy);
        plugin.saveNationData();
    }

    public void subtractLegitimacy(String nationUUID, double amount) {
        FileConfiguration nationData = plugin.getNationData();
        double oldLegitimacy = nationData.getDouble(nationUUID+".Legitimacy");
        double newLegitimacy = oldLegitimacy - amount;

        if(newLegitimacy < 0) {
            newLegitimacy = 0;
        }
        nationData.set(nationUUID+".Legitimacy",newLegitimacy);
        plugin.saveNationData();
    }
}
