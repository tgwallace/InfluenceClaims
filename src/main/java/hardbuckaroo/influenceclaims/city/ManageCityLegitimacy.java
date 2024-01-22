package hardbuckaroo.influenceclaims.city;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

public class ManageCityLegitimacy {
    private final InfluenceClaims plugin;

    public ManageCityLegitimacy(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void addLegitimacy(String cityUUID, double amount) {
        FileConfiguration cityData = plugin.getCityData();
        double oldLegitimacy = cityData.getDouble(cityUUID+".Legitimacy");
        double newLegitimacy = oldLegitimacy + amount;

        if(newLegitimacy > 1) {
            newLegitimacy = 1.00;
        }
        cityData.set(cityUUID+".Legitimacy",newLegitimacy);
        plugin.saveCityData();
    }

    public void subtractLegitimacy(String cityUUID, double amount) {
        FileConfiguration cityData = plugin.getCityData();
        double oldLegitimacy = cityData.getDouble(cityUUID+".Legitimacy");
        double newLegitimacy = oldLegitimacy - amount;

        if(newLegitimacy < 0) {
            newLegitimacy = 0;
        }
        cityData.set(cityUUID+".Legitimacy",newLegitimacy);
        plugin.saveCityData();
    }
}
