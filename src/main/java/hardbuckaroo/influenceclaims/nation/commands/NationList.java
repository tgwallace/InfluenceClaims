package hardbuckaroo.influenceclaims.nation.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class NationList implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationList(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        FileConfiguration nationData = plugin.getNationData();

        if(nationData.getKeys(false).isEmpty()){
            player.sendRawMessage("There are no nations here yet. Wait a little while or start your own nation using /NationCreate!");
        } else {
            TextComponent message = new TextComponent(plugin.color("&lClick a nation to see its info tab:"));
            for(String nationUUID : nationData.getKeys(false)){
                TextComponent subComponent = new TextComponent(plugin.color("\n&l"+nationData.getString(nationUUID+".Color")+nationData.getString(nationUUID+".Name")+"&r: &o" + nationData.getString(nationUUID+".Motto")));
                subComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/nationinfo "+nationData.getString(nationUUID+".Name")));
                message.addExtra(subComponent);
            }
            player.spigot().sendMessage(message);
        }
        return true;
    }
}
