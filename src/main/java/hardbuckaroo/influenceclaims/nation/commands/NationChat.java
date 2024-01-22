package hardbuckaroo.influenceclaims.nation.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class NationChat implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationChat(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You cannot enable nation chat if you are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");

        if(!cityData.contains(cityUUID+".Nation")) {
            player.sendRawMessage("You cannot enable nation chat if you are not part of a nation.");
            return true;
        }

        if(!Objects.requireNonNull(playerData.getString(playerUUID + ".ChatChannel")).equalsIgnoreCase("Nation")) {
            playerData.set(playerUUID+".ChatChannel","Nation");
            plugin.savePlayerData();
            player.sendRawMessage("Chat channel has been set to Nation!");
        } else if(Objects.requireNonNull(playerData.getString(playerUUID + ".ChatChannel")).equalsIgnoreCase("Nation")) {
            playerData.set(playerUUID+".ChatChannel","World");
            plugin.savePlayerData();
            player.sendRawMessage("Chat channel has been set to World!");
        }
        return true;
    }
}
