package hardbuckaroo.influenceclaims.city;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ManageClaims {
    private final InfluenceClaims plugin;

    public ManageClaims(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void addTempClaim(String chunkKey, String cityUUID, int amount) {
        String worldName = chunkKey.split(",")[0];
        World world = Bukkit.getServer().getWorld(worldName);

        if((world.getEnvironment().equals(World.Environment.NETHER) && !plugin.getConfig().getBoolean("NetherClaims")) || (world.getEnvironment().equals(World.Environment.THE_END) && !plugin.getConfig().getBoolean("EndClaims"))) return;

        FileConfiguration claimData = plugin.getClaimData();
        FileConfiguration cityData = plugin.getCityData();
        String claimant = plugin.getClaimant(chunkKey);
        String stance = cityData.getString(cityUUID+".Stance."+claimant);

        if(claimant != null && !claimant.equalsIgnoreCase(cityUUID)) {
            if (cityData.getString(cityUUID + ".Nation") != null && cityData.getString(claimant + ".Nation") != null && cityData.getString(cityUUID + ".Nation").equalsIgnoreCase(cityData.getString(claimant + ".Nation"))) {
                addTempClaim(chunkKey, claimant, amount);
                return;
            } else if (stance == null) {
                int oldValueTemp = claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Temporary");
                int newValueTemp = amount + oldValueTemp;
                if(newValueTemp > plugin.getConfig().getInt("ClaimMinimum")) {
                    newValueTemp = plugin.getConfig().getInt("ClaimMinimum");
                }
                claimData.set(chunkKey + ".Claims." + cityUUID + ".Temporary", newValueTemp);
                return;
            } else if (Objects.requireNonNull(stance).equalsIgnoreCase("friendly")) {
                amount = 0;
            } else if (stance.equalsIgnoreCase("hostile")) {
                subtractTempClaim(chunkKey, claimant, amount);
            }
        }

        int oldValueTemp = claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Temporary");
        int newValueTemp = amount + oldValueTemp;
        claimData.set(chunkKey + ".Claims." + cityUUID + ".Temporary", newValueTemp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        claimData.set(chunkKey + ".Claims." + cityUUID + ".LastAdd", LocalDate.now().format(formatter));
        plugin.saveClaimData();
    }

    public void addPermClaim(String chunkKey, String cityUUID, int amount) {
        String worldName = chunkKey.split(",")[0];
        World world = Bukkit.getServer().getWorld(worldName);

        if((world.getEnvironment().equals(World.Environment.NETHER) && !plugin.getConfig().getBoolean("NetherClaims")) || (world.getEnvironment().equals(World.Environment.THE_END) && !plugin.getConfig().getBoolean("EndClaims"))) return;

        FileConfiguration claimData = plugin.getClaimData();
        FileConfiguration cityData = plugin.getCityData();
        String claimant = plugin.getClaimant(chunkKey);
        String stance = cityData.getString(cityUUID+".Stance."+claimant);

        if(claimant != null && !claimant.equalsIgnoreCase(cityUUID)) {
            if (cityData.contains(cityUUID + ".Nation") && cityData.getString(cityUUID + ".Nation").equalsIgnoreCase(cityData.getString(claimant + ".Nation"))) {
                addPermClaim(chunkKey, claimant, amount);
                return;
            } else if (stance == null) {
                int oldValuePerm = claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Permanent");
                int newValuePerm = amount + oldValuePerm;
                if(newValuePerm > plugin.getConfig().getInt("ClaimMinimum")) {
                    newValuePerm = plugin.getConfig().getInt("ClaimMinimum");
                }
                claimData.set(chunkKey + ".Claims." + cityUUID + ".Permanent", newValuePerm);
                return;
            } else if (Objects.requireNonNull(stance).equalsIgnoreCase("friendly")) {
                amount = 0;
            }
        }

        int oldValuePerm = claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Permanent");
        int newValuePerm = amount + oldValuePerm;
        claimData.set(chunkKey + ".Claims." + cityUUID + ".Permanent", newValuePerm);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        claimData.set(chunkKey + ".Claims." + cityUUID + ".LastAdd", LocalDate.now().format(formatter));
        plugin.saveClaimData();
    }

    public void subtractTempClaim(String chunkKey, String cityUUID, int amount) {
        FileConfiguration claimData = plugin.getClaimData();

        if (!claimData.contains(chunkKey + ".Claims." + cityUUID)) {
            return;
        } else {
            int oldValueTemp = claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Temporary");
            int oldValueTotal = oldValueTemp + claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Permanent");
            int newValueTemp = oldValueTemp - amount;
            if(newValueTemp >= 0) {
                claimData.set(chunkKey + ".Claims." + cityUUID + ".Temporary", newValueTemp);
            } else {
                claimData.set(chunkKey + ".Claims." + cityUUID + ".Temporary", 0);
                subtractPermClaim(chunkKey,cityUUID,Math.abs(newValueTemp));
            }
            int oldTotal = claimData.getInt(chunkKey+".Claims."+cityUUID+".OldTotal");
            int newTotal = newValueTemp + claimData.getInt(chunkKey+".Claims."+cityUUID+".Permanent");
            claimData.set(chunkKey + ".Claims." + cityUUID + ".NetChange", newTotal-oldTotal);
            if(claimData.contains(chunkKey+".Claims."+cityUUID+".Monitor")) {
                int claimMax = plugin.getConfig().getInt("ClaimMaximum");
                int claimMin = plugin.getConfig().getInt("ClaimMinimum");
                int newValueTotal = newValueTemp + claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Permanent");

                if(oldValueTotal >= claimMin && newValueTotal < claimMin) {
                    for(String playerUUID : claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor")) {
                        plugin.playerMessage(playerUUID,"Your city's claim has been lost at " + chunkKey+"!");
                    }
                } else if(oldValueTotal >= claimMax && newValueTotal < claimMax) {
                    for(String playerUUID : claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor")) {
                        plugin.playerMessage(playerUUID,"Your city now only has partial protection at " + chunkKey+"!");
                    }
                } else if(oldValueTotal >= (claimMax-claimMin)/2 && newValueTotal < (claimMax-claimMin)/2) {
                    for(String playerUUID : claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor")) {
                        plugin.playerMessage(playerUUID,"Your city is halfway to losing its claim at " + chunkKey+"!");
                    }
                } else if(oldValueTotal >= claimMax*1.1 && newValueTotal < claimMax*1.1) {
                    for(String playerUUID : claimData.getStringList(chunkKey+".Claims."+cityUUID+".Monitor")) {
                        plugin.playerMessage(playerUUID,"Your city is close to losing full protection at " + chunkKey+"!");
                    }
                }
            }
        }
        try {
            plugin.saveClaimData();
        } catch (NullPointerException ignored) {

        }
    }

    public void subtractPermClaim(String chunkKey, String cityUUID, int amount) {
        String worldName = chunkKey.split(",")[0];

        FileConfiguration claimData = plugin.getClaimData();

        if (!claimData.contains(chunkKey + ".Claims." + cityUUID)) {
            return;
        } else {
            int oldValuePerm = claimData.getInt(chunkKey + ".Claims." + cityUUID + ".Permanent");
            int newValuePerm = oldValuePerm - amount;
            if(newValuePerm > 0) {
                claimData.set(chunkKey + ".Claims." + cityUUID + ".Permanent", newValuePerm);
            } else {
                claimData.set(chunkKey + ".Claims." + cityUUID,null);
                if(claimData.getConfigurationSection(chunkKey+".Claims").getKeys(false).isEmpty()) {
                    claimData.set(chunkKey,null);
                }
            }
        }
        try {
            plugin.saveClaimData();
        } catch (NullPointerException ignored) {

        }
    }
}
