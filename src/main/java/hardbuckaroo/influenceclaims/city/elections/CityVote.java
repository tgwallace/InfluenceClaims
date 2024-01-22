package hardbuckaroo.influenceclaims.city.elections;

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

public class CityVote implements CommandExecutor, Listener {
    private final InfluenceClaims plugin;
    public CityVote(InfluenceClaims plugin){
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Player player = (Player) commandSender;
        String uuid = player.getUniqueId().toString();
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration playerData = plugin.getPlayerData();
        FileConfiguration nationData = plugin.getNationData();
        String cityUUID;

        if(playerData.contains(uuid+".City")) {
            cityUUID = playerData.getString(uuid+".City");
        }
        else {
            player.sendRawMessage("You are not part of a city.");
            return true;
        }

        String government = cityData.getString(cityUUID+".Government");
        List<String> electorate = new ArrayList<>();
        
        if(cityData.getConfigurationSection(cityUUID+".Elections") == null) {
            player.sendRawMessage("There are no active elections in " + cityData.getString(cityUUID+".Name") + ".");
            return true;
        }

        //Provides voting menu in chat if not enough arguments are provided to submit a vote.
        if(strings.length < 2) {
            //Opening with solid line in city's color.
            TextComponent message = new TextComponent(plugin.color(cityData.getString(cityUUID + ".Color") + "&m                                                     "));
            Boolean canVote = false;
            for (String issue : cityData.getConfigurationSection(cityUUID + ".Elections").getKeys(false)) {
                if (government.equalsIgnoreCase("Democracy") || issue.equalsIgnoreCase("Overthrow")) {
                    electorate = cityData.getStringList(cityUUID + ".Players");
                } else if (government.equalsIgnoreCase("Oligarchy")) {
                    if(cityData.contains(cityUUID+".Roles")) {
                        for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                            if(cityData.getBoolean(cityUUID+".Roles."+title+".Permissions.Vote")) {
                                electorate.addAll(cityData.getStringList(cityUUID + ".Roles." + title + ".Players"));
                            }
                        }
                        Set<String> dedupe = new LinkedHashSet<>(electorate);
                        electorate.clear();
                        electorate.addAll(dedupe);
                    }
                }

                if(electorate.contains(uuid)) {
                    canVote = true;
                    if (issue.equalsIgnoreCase("government")) {
                        message.addExtra(plugin.color("\n&lNew City Government: " + WordUtils.capitalize(cityData.getString(cityUUID + ".Elections.Government.Type"))));
                    } else if (issue.equalsIgnoreCase("leader")) {
                        message.addExtra(plugin.color("\n&lNext " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name")));
                    } else if (issue.equalsIgnoreCase("runoff")) {
                        message.addExtra(plugin.color("\n&lRunoff For Next " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name")));
                    } else if (issue.equalsIgnoreCase("overthrow")) {
                        message.addExtra(plugin.color("\n&lOverthrow the Government of " + cityData.getString(cityUUID + ".Name")));
                    } else if (issue.equalsIgnoreCase("NewNation")) {
                        message.addExtra(plugin.color("\n&lCreate a new nation called " + cityData.getString(cityUUID+".Elections.NewNation.Name")));
                    } else if (issue.equalsIgnoreCase("joinnation")) {
                        message.addExtra(plugin.color("\n&lJoin the nation of " + nationData.getString(cityData.getString(cityUUID+".Elections.JoinNation.Invite")+".Name")));
                    } else if (issue.equalsIgnoreCase("leavenation")) {
                        message.addExtra(plugin.color("\n&lLeave the nation of " + nationData.getString(cityData.getString(cityUUID+".Elections.JoinNation.Invite")+".Name")));
                    }
                    LocalDate electionStart = LocalDate.parse(Objects.requireNonNull(cityData.getString(cityUUID + ".Elections." + issue + ".StartDate")));
                    LocalDate today = LocalDate.now();
                    double interval = plugin.getConfig().getDouble("ElectionLength");
                    int daysLeft = (int) (interval - ChronoUnit.DAYS.between(electionStart, today));
                    message.addExtra(plugin.color(" - &o"+daysLeft+" days remain"));
                    message.addExtra(plugin.color("\n&oClick an option below to cast your vote!"));

                    Map<String, Integer> map = new LinkedHashMap<String, Integer>();
                    Map<String, Integer> mapSorted = new LinkedHashMap<String, Integer>();

                    for (String option : cityData.getConfigurationSection(cityUUID + ".Elections." + issue + ".VoteCount").getKeys(false)) {
                        map.put(option, cityData.getInt(cityUUID + ".Elections." + issue + ".VoteCount." + option));
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
                            voteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityvote " + issue + " " + optionText));
                        } else {
                            voteComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cityvote " + issue + " " + option));
                        }
                        message.addExtra(voteComponent);
                    }
                    if (issue.equalsIgnoreCase("leader")) {
                        message.addExtra("\nUse '/CityVote Leader [username]' to nominate another player!");
                    }
                    message.addExtra(plugin.color(cityData.getString(cityUUID + ".Color") + "\n&m                                                     "));
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

            if(cityData.contains(cityUUID+".Elections."+voteIssue)) {
                if (government.equalsIgnoreCase("Democracy") || voteIssue.equalsIgnoreCase("Overthrow")) {
                    electorate = cityData.getStringList(cityUUID + ".Players");
                } else if (government.equalsIgnoreCase("Oligarchy")) {
                    if(cityData.contains(cityUUID+".Roles")) {
                        for (String title : cityData.getConfigurationSection(cityUUID + ".Roles").getKeys(false)) {
                            if(cityData.getBoolean(cityUUID+".Roles."+title+".Permissions.Vote")) {
                                electorate.addAll(cityData.getStringList(cityUUID + ".Roles." + title + ".Players"));
                            }
                        }
                        Set<String> dedupe = new LinkedHashSet<>(electorate);
                        electorate.clear();
                        electorate.addAll(dedupe);
                    }
                }

                if(!electorate.contains(uuid)) {
                    player.sendRawMessage("You are ineligible to vote in this election.");
                    return true;
                }

                if(voteIssue.equalsIgnoreCase("leader")) {
                    voteChoice = Bukkit.getOfflinePlayer(voteChoice).getUniqueId().toString();
                    if (!cityData.contains(cityUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)) {
                        if(electorate.contains(voteChoice)) {
                            cityData.set(cityUUID+".Elections."+voteIssue+".VoteCount."+voteChoice,0);
                        } else {
                            player.sendRawMessage(voteName + " is ineligible to run in this election. Eligible candidates are: " + String.join(", ",electorate));
                            return true;
                        }
                    }
                } else if (!cityData.contains(cityUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)) {
                    player.sendRawMessage("Invalid vote choice. Please use /CityVote to review valid choices.");
                    return true;
                }

                if(cityData.contains(cityUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)) {
                    if(cityData.contains(cityUUID+".Elections."+voteIssue+".Voters."+uuid)) {
                        String pastVote = cityData.getString(cityUUID+".Elections."+voteIssue+".Voters."+uuid);
                        int voteDrop = cityData.getInt(cityUUID+".Elections."+voteIssue+".VoteCount."+pastVote)-1;
                        cityData.set(cityUUID+".Elections."+voteIssue+".VoteCount."+pastVote,voteDrop);
                    }
                    int voteCount = cityData.getInt(cityUUID+".Elections."+voteIssue+".VoteCount."+voteChoice)+1;
                    cityData.set(cityUUID+".Elections."+voteIssue+".VoteCount."+voteChoice,voteCount);
                    cityData.set(cityUUID+".Elections."+voteIssue+".Voters."+uuid,voteChoice);
                    player.sendRawMessage("You have successfully cast your vote for " + voteName + "!");
                } else {
                    player.sendRawMessage("Invalid choice. Please select an option below.");
                    player.performCommand("/CityVote");
                }
            } else {
                player.sendRawMessage("Invalid issue. Please select an option below.");
                player.performCommand("/CityVote");
            }
        }
        plugin.saveCityData();
        return true;
    }
}
