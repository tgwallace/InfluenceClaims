package hardbuckaroo.influenceclaims.nation.commands;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class NationAccept implements CommandExecutor {
    private final InfluenceClaims plugin;
    public NationAccept(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration nationData = plugin.getNationData();
        Player player = (Player) commandSender;
        String playerUUID = player.getUniqueId().toString();

        if(!playerData.contains(playerUUID+".City")) {
            player.sendRawMessage("You are not a member of a city. Only city leaders can accept nation invites.");
            return true;
        }
        String cityUUID = playerData.getString(playerUUID+".City");
        String government = cityData.getString(cityUUID+".Government");

        if(!cityData.getString(cityUUID+".Leader").equalsIgnoreCase(playerUUID)) {
            player.sendRawMessage("Only city leaders can accept nation invites.");
            return true;
        }

        if(cityData.contains(cityUUID+".Elections.JoinNation")) {
            player.sendRawMessage("Cannot start a vote to join a nation while another vote to join a nation is underway.");
            return true;
        }

        //Checks if the city has invites to a nation.
        if(!cityData.contains(cityUUID+".NationInvites")) {
            player.sendRawMessage("You do not have any nation invites right now. Start your own nation using /NationCreate or ask around for a nation to join!");
            return true;
        } else if (strings.length == 0){
            TextComponent component = new TextComponent();
            if(government.equalsIgnoreCase("Monarchy")) {
                component.addExtra("Please select an invite below to accept:");
            } else {
                component.addExtra("Please select an invite below to call a vote to accept:");
            }
            player.spigot().sendMessage(component);

            //Sends the player a list of clickable invites.
            for(String invite : cityData.getStringList(cityUUID + ".NationInvites")) {
                component = new TextComponent(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "You have been invited to join the nation of &l" + nationData.getString(invite + ".Name") + "&r. Click here to accept.")));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityaccept " + invite));
                player.spigot().sendMessage(component);
            }
            return true;
        } else {
            //Should only get to this branch if player clicks invite to execute command sent from above branch.
            String invite = strings[0];
            List<String> inviteList = cityData.getStringList(cityUUID + ".NationInvites");

            if(!inviteList.contains(invite)) {
                player.sendRawMessage("You do not have an invite from that nation.");
            } else {
                if(Objects.requireNonNull(government).equalsIgnoreCase("Monarchy")) {
                    cityData.set(cityUUID+".Nation",invite);
                    cityData.set(cityUUID+".NationInvites",null);
                    plugin.saveCityData();

                    List<String> cityList = nationData.getStringList(invite+".Cities");
                    cityList.add(cityUUID);
                    nationData.set(invite+".Cities",cityList);
                    plugin.saveNationData();
                    plugin.nationMessage(invite,"The city of " + cityData.getString(cityUUID+".Name") + " has joined the nation of " + nationData.getString(invite+".Name") + "!",true);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    cityData.set(cityUUID+".Elections.JoinNation.StartDate", LocalDate.now().format(formatter));
                    cityData.set(cityUUID+".Elections.JoinNation.VoteCount.Yes",0);
                    cityData.set(cityUUID+".Elections.JoinNation.VoteCount.No",0);
                    cityData.set(cityUUID+".Elections.JoinNation.Invite",invite);
                    plugin.cityMessage(cityUUID,cityData.getString(cityUUID+".LeaderTitle")+ " " + player.getName() + " has called a vote to join the nation of " + nationData.getString(invite+".Name") + "!",true);
                }
            }
            return true;
        }
    }
}
