package hardbuckaroo.influenceclaims.nation;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ApplyTimedNationLegitimacy {
    private final InfluenceClaims plugin;

    public ApplyTimedNationLegitimacy(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void applyLegitimacy() {
        FileConfiguration nationData = plugin.getNationData();
        FileConfiguration playerData = plugin.getPlayerData();
        LocalDate today = LocalDate.now();
        int legitimacyTimer = plugin.getConfig().getInt("LegitimacyTimer");
        ManageNationLegitimacy manageNationLegitimacy = new ManageNationLegitimacy(plugin);

        for (String nationUUID : nationData.getKeys(false)) {
            String leaderUUID = nationData.getString(nationUUID+".Leader");
            LocalDate leaderLastLogin = LocalDate.parse(Objects.requireNonNull(playerData.getString(leaderUUID + ".LastLogin")));
            int diff = (int) ChronoUnit.DAYS.between(today,leaderLastLogin);
            
            if(diff > legitimacyTimer) {
                manageNationLegitimacy.subtractLegitimacy(nationUUID,plugin.getConfig().getDouble("AbsenteeLegitimacyLoss"));
            } else {
                manageNationLegitimacy.addLegitimacy(nationUUID,plugin.getConfig().getDouble("LegitimacyGain"));
            }
        }
    }
}
