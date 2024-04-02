package hardbuckaroo.influenceclaims.city.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CityUnclaim implements CommandExecutor {
    private final InfluenceClaims plugin;
    public CityUnclaim(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration claimData = plugin.getClaimData();
        Player sender = (Player) commandSender;
        String playerUUID = sender.getUniqueId().toString();

        if(!playerData.contains(sender.getUniqueId().toString()+".City")) {
            sender.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(sender.getUniqueId().toString() + ".City");

        boolean perms = false;
        if(cityData.contains(cityUUID+".Roles")) {
            for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                if (cityData.getStringList(cityUUID + ".Roles.Players").contains(playerUUID)) {
                    perms = cityData.getBoolean(cityUUID + ".Roles." + title + ".Permissions.Unclaim");
                }
                if(perms) break;
            }
        }

        if(!cityData.getString(cityUUID+".Leader").equals(playerUUID) && !perms){
            sender.sendRawMessage("You do not have permission to unclaim chunks!");
            return true;
        }

        if(strings.length < 1 || !strings[0].equalsIgnoreCase("confirm")){
            String message = "&cAre you sure? This cannot be undone. Click here or use /CityUnclaim confirm to confirm.";
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',message)));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/cityunclaim confirm"));
            sender.spigot().sendMessage(component);
        } else {
            String chunkKey = plugin.getChunkKey(sender.getLocation().getChunk());
            if(claimData.contains(chunkKey + ".Claims." + cityUUID)) {
                claimData.set(chunkKey + ".Claims." + cityUUID, null);
            } else {
                sender.sendRawMessage("Your city does not have any claims in this chunk!");
            }
        }
        plugin.saveClaimData();
        return true;
    }
}
