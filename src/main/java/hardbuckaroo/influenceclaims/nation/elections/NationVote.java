package hardbuckaroo.influenceclaims.nation.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NationVote implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public NationVote(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String uuid = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();

        if(!playerData.contains(uuid+".City")) {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }
        String cityUUID = playerData.getString(uuid+".City");

        if(!cityData.contains(cityUUID+".Nation")) {
            player.sendRawMessage("You are not part of a nation.");
            return true;
        }
        String nationUUID = cityData.getString(cityUUID+".Nation");

        String government = nationData.getString(nationUUID+".Government");
        List<String> electorate = new ArrayList<>();
        
        if(nationData.getConfigurationSection(nationUUID+".Elections") == null) {
            player.sendRawMessage("There are no active elections in " + nationData.getString(nationUUID+".Name") + ".");
            return true;
        }

        //Provides voting menu in chat if not enough arguments are provided to submit a vote.
        if(strings.length < 2) {
            //Opening with solid line in city's color.
            TextComponent message = new TextComponent(plugin.color(nationData.getString(nationUUID + ".Color") + "&m                                                     "));
            Boolean canVote = false;
            for (String issue : nationData.getConfigurationSection(nationUUID + ".Elections").getKeys(false)) {
                if (government.equalsIgnoreCase("Democracy") || issue.equalsIgnoreCase("overthrow")) {
                    for(String city : nationData.getStringList(nationUUID+".Cities")) {
                        electorate.addAll(cityData.getStringList(city + ".Players"));
                    }
                } else if (government.equalsIgnoreCase("Oligarchy")) {
                    for(String city : nationData.getStringList(nationUUID+".Cities")) {
                        electorate.add(cityData.getString(city + ".Leader"));
                    }
                }

                if(electorate.contains(uuid)) {
                    canVote = true;
                    if (issue.equalsIgnoreCase("government")) {
                        message.addExtra(plugin.color("\n&lNew National Government: " + WordUtils.capitalize(nationData.getString(nationUUID + ".Elections.Government.Type"))));
                    } else if (issue.equalsIgnoreCase("leader")) {
                        message.addExtra(plugin.color("\n&lNext " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name")));
                    } else if (issue.equalsIgnoreCase("runoff")) {
                        message.addExtra(plugin.color("\n&lRunoff For Next " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name")));
                    } else if (issue.equalsIgnoreCase("overthrow")) {
                        message.addExtra(plugin.color("\n&lOverthrow the Government of " + nationData.getString(nationUUID + ".Name")));
                    }  else if (issue.equalsIgnoreCase("addcity")) {
                        message.addExtra(plugin.color("\n&lInvite the city of " + cityData.getString(nationData.getString(nationUUID+".Elections.AddCity.City")+".Name") + " to join " + nationData.getString(nationUUID+".Name")));
                    }   else if (issue.equalsIgnoreCase("kickcity")) {
                        message.addExtra(plugin.color("\n&lKick the city of " + cityData.getString(nationData.getString(nationUUID+".Elections.AddCity.City")+".Name") + " out of " + nationData.getString(nationUUID+".Name")));
                    }
                    LocalDate electionStart = LocalDate.parse(Objects.requireNonNull(cityData.getString(nationUUID + ".Elections." + issue + ".StartDate")));
                    LocalDate today = LocalDate.now();
                    double interval = plugin.getConfig().getDouble("ElectionLength");
                    int daysLeft = (int) (interval - ChronoUnit.DAYS.between(electionStart, today));
                    message.addExtra(plugin.color(" - &o"+daysLeft+" days remain"));
                    message.addExtra(plugin.color("\n&oClick an option below to cast your vote!"));

                    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
                    Map<String, Integer> mapSorted = new LinkedHashMap<String, Integer>();

                    for (String option : nationData.getConfigurationSection(nationUUID + ".Elections." + issue + ".VoteCount").getKeys(false)) {
                        map.put(option, nationData.getInt(nationUUID + ".Elections." + issue + ".VoteCount." + option));
                    }

                    map.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).forEachOrdered(x -> mapSorted.put(x.getKey(), x.getValue()));

                    for (Map.Entry<String, Integer> entry : mapSorted.entrySet()) {
                        String option = entry.getKey();
                        String optionText;
                        int count = entry.getValue();
                        if (issue.equalsIgnoreCase("leader") || issue.equalsIgnoreCase("runoff")) {
                            optionText = Bukkit.getOfflinePlayer(UUID.fromString(option)).getName();
                        } else {
                            optionText = option.replace("_"," ");
                        }
                        TextComponent voteComponent = new TextComponent("\n-" + optionText + ": " + count);
                        if(issue.equalsIgnoreCase("leader")) {
                            voteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationvote " + issue + " " + optionText));
                        } else {
                            voteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nationvote " + issue + " " + option));
                        }
                        message.addExtra(voteComponent);
                    }
                    if (issue.equalsIgnoreCase("leader")) {
                        message.addExtra("\nUse '/NationVote Leader [username]' to nominate another player!");
                    }
                    message.addExtra(plugin.color(nationData.getString(nationUUID + ".Color") + "\n&m                                                     "));
                }
            }
            if(canVote) {
                player.spigot().sendMessage(message);
            } else {
                player.sendRawMessage("There are no active elections that you are eligible to vote in.");
            }
        } else {
            String voteIssue = WordUtils.capitalize(strings[0]);
            String voteChoice = strings[1];
            String voteName = voteChoice;

            if(nationData.contains(nationUUID+".Elections."+voteIssue)) {
                if (government.equalsIgnoreCase("Democracy") || voteIssue.equalsIgnoreCase("overthrow")) {
                    for(String city : nationData.getStringList(nationUUID+".Cities")) {
                        electorate.addAll(cityData.getStringList(city + ".Players"));
                    }
                } else if (government.equalsIgnoreCase("Oligarchy")) {
                    for(String city : nationData.getStringList(nationUUID+".Cities")) {
                        electorate.addAll(cityData.getStringList(city + ".Nobles"));
                    }
                }

                if(!electorate.contains(uuid)) {
                    player.sendRawMessage("You are ineligible to vote in this election.");
                    return true;
                }
                
                if(voteIssue.equalsIgnoreCase("leader")) {
                    voteChoice = Bukkit.getOfflinePlayer(voteChoice).getUniqueId().toString();
                    if (!nationData.contains(nationUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)) {
                        if(electorate.contains(voteChoice)) {
                            nationData.set(nationUUID+".Elections."+voteIssue+".VoteCount."+voteChoice,0);
                        } else {
                            player.sendRawMessage(voteName + " is ineligible to run in this election. Eligible candidates are: " + String.join(", ",electorate));
                            return true;
                        }
                    }
                } else if (!nationData.contains(nationUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)) {
                    player.sendRawMessage("Invalid vote choice. Please use /NationVote to review valid choices.");
                    return true;
                }

                if(nationData.contains(nationUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)) {
                    if(nationData.contains(nationUUID+".Elections."+voteIssue+".Voters."+uuid)) {
                        String pastVote = nationData.getString(nationUUID+".Elections."+voteIssue+".Voters."+uuid);
                        int voteDrop = nationData.getInt(nationUUID+".Elections."+voteIssue+".VoteCount."+pastVote)-1;
                        nationData.set(nationUUID+".Elections."+voteIssue+".VoteCount."+pastVote,voteDrop);
                    }
                    int voteCount = nationData.getInt(nationUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)+1;
                    nationData.set(nationUUID+".Elections."+voteIssue+".VoteCount."+voteChoice,voteCount);
                    nationData.set(nationUUID+".Elections."+voteIssue+".Voters."+uuid,voteChoice);
                    player.sendRawMessage("You have successfully cast your vote for " + voteName + "!");
                } else {
                    player.sendRawMessage("Invalid choice. Please select an option below.");
                    player.performCommand("/NationVote");
                }
            } else {
                player.sendRawMessage("Invalid issue. Please select an option below.");
                player.performCommand("/NationVote");
            }
        }
        plugin.saveNationData();
        return true;
    }
}
