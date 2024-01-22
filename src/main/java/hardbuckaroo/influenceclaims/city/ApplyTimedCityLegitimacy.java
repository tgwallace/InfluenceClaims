package hardbuckaroo.influenceclaims.city;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ApplyTimedCityLegitimacy {
    private final InfluenceClaims plugin;

    public ApplyTimedCityLegitimacy(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void applyLegitimacy() {
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        LocalDate today = LocalDate.now();
        int legitimacyTimer = plugin.getConfig().getInt("LegitimacyTimer");
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(plugin);

        for (String cityUUID : cityData.getKeys(false)) {
            String leaderUUID = cityData.getString(cityUUID+".Leader");
            LocalDate leaderLastLogin = LocalDate.parse(Objects.requireNonNull(playerData.getString(leaderUUID + ".LastLogin")));
            int diff = (int) ChronoUnit.DAYS.between(today,leaderLastLogin);

            if(diff > legitimacyTimer) {
                manageCityLegitimacy.subtractLegitimacy(cityUUID,plugin.getConfig().getDouble("AbsenteeLegitimacyLoss"));
            } else {
                manageCityLegitimacy.addLegitimacy(cityUUID,plugin.getConfig().getDouble("LegitimacyGain"));
            }
        }
    }
}
