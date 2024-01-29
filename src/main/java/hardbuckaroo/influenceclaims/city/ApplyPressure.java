package hardbuckaroo.influenceclaims.city;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static java.lang.Integer.parseInt;

public class ApplyPressure {
    private final InfluenceClaims plugin;

    public ApplyPressure(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void applyPressure(){
        FileConfiguration claimData = plugin.getClaimData();
        int decayConstant = plugin.getConfig().getInt("DecayConstant");

        Set<String> keys = claimData.getKeys(false);
        for(String chunkKey : keys) {
            for (String claim : claimData.getConfigurationSection(chunkKey + ".Claims").getKeys(false)) {
                int oldValueTemp = claimData.getInt(chunkKey + ".Claims." + claim + ".Temporary");
                int oldValuePerm = claimData.getInt(chunkKey + ".Claims." + claim + ".Permanent");

                int influenceDecay = (int) (oldValueTemp*0.01);
                int hardening = influenceDecay/9;
                int freePressure = (int) (oldValuePerm*0.01);
                int pressure = (influenceDecay+freePressure)/9;

                if(influenceDecay < decayConstant || influenceDecay < pressure) {
                    pressure = 0;
                    hardening = 0;
                }

                influenceDecay += decayConstant;

                int daysSinceLastAdd = 0;
                if(claimData.contains(chunkKey + ".Claims." + claim + ".LastAdd")) {
                    daysSinceLastAdd = (int) ChronoUnit.DAYS.between(LocalDate.parse(Objects.requireNonNull(claimData.getString(chunkKey + ".Claims." + claim + ".LastAdd"))), LocalDate.now());
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    claimData.set(chunkKey + ".Claims." + claim + ".LastAdd", LocalDate.now().format(formatter));
                }
                int decayBoostTimer = plugin.getConfig().getInt("DecayBoostTimer");
                double decayBoostModifier = plugin.getConfig().getDouble("DecayBoostModifier");

                if(daysSinceLastAdd > decayBoostTimer) {
                    double decayBoost = (daysSinceLastAdd-decayBoostTimer)*decayBoostModifier;
                    influenceDecay = (int) (influenceDecay*decayBoost);
                }

                claimData.set(chunkKey+".Claims."+claim+".Pressure",pressure);
                claimData.set(chunkKey+".Claims."+claim+".InfluenceDecay",influenceDecay);
                claimData.set(chunkKey+".Claims."+claim+".Hardening",hardening);
            }
        }

        keys = claimData.getKeys(false);
        for(String chunkKey : keys){
            for (String claim : claimData.getConfigurationSection(chunkKey + ".Claims").getKeys(false)) {
                int influenceDecay = claimData.getInt(chunkKey+".Claims."+claim+".InfluenceDecay");
                int pressure = claimData.getInt(chunkKey+".Claims."+claim+".Pressure");
                int hardening = claimData.getInt(chunkKey+".Claims."+claim+".Hardening");

                ManageClaims manageClaims = new ManageClaims(plugin);
                manageClaims.subtractTempClaim(chunkKey, claim, influenceDecay);

                if (pressure > 0) {
                    for(int x=-1;x<=1;x++){
                        for(int z=-1;z<=1;z++){
                            String[] chunkParts = chunkKey.split(",");
                            String pressuredChunk = chunkParts[0] + "," + (parseInt(chunkParts[1]) + x) + "," + (parseInt(chunkParts[2]) + z);
                            if(x != 0 || z != 0) {
                                manageClaims.addTempClaim(pressuredChunk, claim, pressure);
                            } else {
                                manageClaims.addPermClaim(pressuredChunk, claim, hardening);
                            }
                        }
                    }
                }
            }
        }

        keys = claimData.getKeys(false);
        for(String chunkKey : keys) {
            if(claimData.getConfigurationSection(chunkKey+".Claims").getKeys(false).isEmpty()) {
                claimData.set(chunkKey,null);
            }
        }

        plugin.saveClaimData();
    }
}
