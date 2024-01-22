package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class CityChunkBoost implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityChunkBoost(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration playerData = plugin.getPlayerData();
        String chunkKey = plugin.getChunkKey(player.getLocation().getChunk());
        String playerUUID = player.getUniqueId().toString();

        if(!plugin.getConfig().getBoolean("ChunkBoostEnabled")) {
            player.sendRawMessage("ChunkBoost is currently disabled on this server.");
            return true;
        }

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");

        if(plugin.getClaimant(chunkKey) != null && !plugin.getClaimant(chunkKey).equalsIgnoreCase(cityUUID)) {
            player.sendRawMessage("Cannot use ChunkBoost in chunks claimed by other cities.");
            return true;
        }

        Economy economy = InfluenceClaims.getEconomy();

        if(strings.length == 0) {
            player.sendRawMessage("Please provide the amount of money that you would like to spend to boost this claim.");
            return false;
        }

        double spend;
        try {
            spend = Double.parseDouble(strings[0]);
        } catch(NumberFormatException e) {
            player.sendRawMessage("Please provide the amount of money that you would like to spend to boost this claim.");
            return false;
        }

        if(economy.getBalance(player) < spend) {
            player.sendRawMessage("Insufficient funds to complete this ChunkBoost");
            return true;
        }

        int tempAdd = (int) (plugin.getConfig().getDouble("ChunkBoostTemporaryRate")*spend);
        int permAdd = (int) (plugin.getConfig().getDouble("ChunkBoostPermanentRate")*spend);

        if(strings.length < 2 || !strings[1].equalsIgnoreCase("confirm")) {
            TextComponent component = new TextComponent("Spend " + spend + " to receive " + permAdd + " permanent claims and " + tempAdd + " temporary claims in this chunk? ");
            component.addExtra("Click here to confirm.");
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/citychunkboost "+spend+" confirm"));
            player.spigot().sendMessage(component);
            return true;
        } else {
            economy.withdrawPlayer(player, spend);
            ManageClaims manageClaims = new ManageClaims(plugin);
            manageClaims.addTempClaim(chunkKey,cityUUID,tempAdd);
            manageClaims.addPermClaim(chunkKey,cityUUID,permAdd);
            player.sendRawMessage("Successfully spent " + spend + " to boost your claim on this chunk!");
        }
        return true;
    }
}
