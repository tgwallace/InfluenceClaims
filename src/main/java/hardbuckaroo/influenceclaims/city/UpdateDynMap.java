package hardbuckaroo.influenceclaims.city;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.dynmap.DynmapAPI;

import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.Set;

public class UpdateDynMap {
    private final InfluenceClaims plugin;

    public UpdateDynMap(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void updateDynMap(){
        DynmapAPI dynmap = InfluenceClaims.dapi;
        FileConfiguration claimData = plugin.getClaimData();
        FileConfiguration cityData = plugin.getCityData();

        Set<MarkerSet> allSets = dynmap.getMarkerAPI().getMarkerSets();
        for(MarkerSet delete : allSets) {
            delete.deleteMarkerSet();
        }

        MarkerSet m = dynmap.getMarkerAPI().createMarkerSet("InfluenceClaims.markerset", "Cities", dynmap.getMarkerAPI().getMarkerIcons(), false);

        for(String chunkKey : claimData.getKeys(false)) {
            String claimant = plugin.getClaimant(chunkKey);
            String claimantName = cityData.getString(claimant+".Name");

            String[] keyParts = chunkKey.split(",");
            double blockMinX = Integer.parseInt(keyParts[1])*16;
            double blockMinZ = Integer.parseInt(keyParts[2])*16;
            double blockMaxX = (Integer.parseInt(keyParts[1])*16)+15;
            double blockMaxZ = (Integer.parseInt(keyParts[2])*16)+15;

            double[] x = {blockMinX,blockMaxX};
            double[] z = {blockMinZ,blockMaxZ};

            double opacity = (claimData.getDouble(chunkKey+"."+claimant+".Temporary") + claimData.getDouble(chunkKey+"."+claimant+".Permanent"))/plugin.getConfig().getDouble("ClaimMaximum");
            if(opacity > 1) opacity = 1;

            String claimantColor = cityData.getString(claimant+".Color");
            if(claimantColor.equalsIgnoreCase("&1")) claimantColor = "0x0000AA";
            else if(claimantColor.equalsIgnoreCase("&2")) claimantColor = "0x00AA00";
            else if(claimantColor.equalsIgnoreCase("&3")) claimantColor = "0x00AAAA";
            else if(claimantColor.equalsIgnoreCase("&4")) claimantColor = "0xAA0000";
            else if(claimantColor.equalsIgnoreCase("&5")) claimantColor = "0xAA00AA";
            else if(claimantColor.equalsIgnoreCase("&6")) claimantColor = "0xFFAA00";
            else if(claimantColor.equalsIgnoreCase("&7")) claimantColor = "0xAAAAAA";
            else if(claimantColor.equalsIgnoreCase("&8")) claimantColor = "0x555555";
            else if(claimantColor.equalsIgnoreCase("&9")) claimantColor = "0x5555FF";
            else if(claimantColor.equalsIgnoreCase("&a")) claimantColor = "0x55FF55";
            else if(claimantColor.equalsIgnoreCase("&b")) claimantColor = "0x55FFFF";
            else if(claimantColor.equalsIgnoreCase("&c")) claimantColor = "0xFF5555";
            else if(claimantColor.equalsIgnoreCase("&d")) claimantColor = "0xFF55FF";
            else if(claimantColor.equalsIgnoreCase("&e")) claimantColor = "0xFFFF55";
            else if(claimantColor.equalsIgnoreCase("&f")) claimantColor = "0xFFFFFF";

            AreaMarker am = m.createAreaMarker(chunkKey, claimantName, true, keyParts[0], x, z, false);
            am.setFillStyle(opacity, Integer.parseInt(claimantColor));
        }
    }
}
