package hardbuckaroo.influenceclaims.city;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;
import org.dynmap.DynmapAPI;

import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.awt.*;
import java.util.Set;
import java.util.logging.Level;

public class UpdateDynMap {
    private final InfluenceClaims plugin;

    public UpdateDynMap(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void updateDynMap(){
        DynmapAPI dynmap = InfluenceClaims.dapi;
        FileConfiguration claimData = plugin.getClaimData();
        FileConfiguration cityData = plugin.getCityData();
        plugin.getLogger().log(Level.INFO,"Updating Dynmap.");

        Set<MarkerSet> allSets = dynmap.getMarkerAPI().getMarkerSets();
        for(MarkerSet delete : allSets) {
            delete.deleteMarkerSet();
        }

        MarkerSet m = dynmap.getMarkerAPI().createMarkerSet("InfluenceClaims.markerset", "Cities", dynmap.getMarkerAPI().getMarkerIcons(), false);

        for(String chunkKey : claimData.getKeys(false)) {
            String claimant = plugin.getClaimant(chunkKey);
            if (claimant != null) {
                String claimantName = cityData.getString(claimant + ".Name");

                String[] keyParts = chunkKey.split(",");
                Chunk chunk = Bukkit.getWorld(keyParts[0]).getChunkAt(Integer.parseInt(keyParts[1]), Integer.parseInt(keyParts[2]));

                double blockMinX = chunk.getBlock(0, 0, 0).getX();
                double blockMinZ = chunk.getBlock(0, 0, 0).getZ();
                double blockMaxX = chunk.getBlock(15, 0, 15).getX() + 1;
                double blockMaxZ = chunk.getBlock(15, 0, 15).getZ() + 1;

                double[] x = {blockMinX, blockMaxX};
                double[] z = {blockMinZ, blockMaxZ};

                double opacity = (double) (claimData.getInt(chunkKey + ".Claims." + claimant + ".Temporary") + claimData.getInt(chunkKey + ".Claims." + claimant + ".Permanent")) / plugin.getConfig().getInt("ClaimMaximum");

                if(opacity >= 1) claimantName += " - Full Protection";
                else claimantName += " - " + Math.round(opacity*100) + "% Protection";

                if(opacity > 0.8) opacity = 0.8;

                String claimantColor = cityData.getString(claimant + ".Color");
                int claimantRGB;
                if (claimantColor == null) claimantRGB = 0xFFFFFF;
                else if (claimantColor.equalsIgnoreCase("&1")) claimantRGB = 0x0000AA;
                else if (claimantColor.equalsIgnoreCase("&2")) claimantRGB = 0x00AA00;
                else if (claimantColor.equalsIgnoreCase("&3")) claimantRGB = 0x00AAAA;
                else if (claimantColor.equalsIgnoreCase("&4")) claimantRGB = 0xAA0000;
                else if (claimantColor.equalsIgnoreCase("&5")) claimantRGB = 0xAA00AA;
                else if (claimantColor.equalsIgnoreCase("&6")) claimantRGB = 0xFFAA00;
                else if (claimantColor.equalsIgnoreCase("&7")) claimantRGB = 0xAAAAAA;
                else if (claimantColor.equalsIgnoreCase("&8")) claimantRGB = 0x555555;
                else if (claimantColor.equalsIgnoreCase("&9")) claimantRGB = 0x5555FF;
                else if (claimantColor.equalsIgnoreCase("&a")) claimantRGB = 0x55FF55;
                else if (claimantColor.equalsIgnoreCase("&b")) claimantRGB = 0x55FFFF;
                else if (claimantColor.equalsIgnoreCase("&c")) claimantRGB = 0xFF5555;
                else if (claimantColor.equalsIgnoreCase("&d")) claimantRGB = 0xFF55FF;
                else if (claimantColor.equalsIgnoreCase("&e")) claimantRGB = 0xFFFF55;
                else if (claimantColor.equalsIgnoreCase("&f")) claimantRGB = 0xFFFFFF;
                else claimantRGB = 0xFFFFFF;

                AreaMarker am = m.createAreaMarker(chunkKey, claimantName, true, keyParts[0], x, z, false);
                am.setFillStyle(opacity, claimantRGB);
                am.setLineStyle(1, 1, claimantRGB);
            }
        }
    }
}
