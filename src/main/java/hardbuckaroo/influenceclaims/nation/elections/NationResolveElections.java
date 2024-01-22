package hardbuckaroo.influenceclaims.nation.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.nation.ManageNationLegitimacy;
import hardbuckaroo.influenceclaims.nation.elections.NationStartElection;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class NationResolveElections {
    private final InfluenceClaims plugin;

    public NationResolveElections(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void resolveElections(){
        FileConfiguration nationData = plugin.getNationData();
        FileConfiguration cityData = plugin.getCityData();
        LocalDate today = LocalDate.now();
        double interval = plugin.getConfig().getDouble("ElectionLength");
        double victoryThreshold = plugin.getConfig().getDouble("SuperMajorityThreshold");
        NationStartElection nationStartElection = new NationStartElection(plugin);
        ManageNationLegitimacy manageNationLegitimacy = new ManageNationLegitimacy(plugin);

        if(nationData.getKeys(false).size() == 0){
            return;
        }

        for(String nationUUID : nationData.getKeys(false)) {
            String nationName = nationData.getString(nationUUID+".Name");
            String leaderTitle = nationData.getString(nationUUID+".LeaderTitle");
            if(nationData.contains(nationUUID+".Elections")) {
                for(String issue : Objects.requireNonNull(nationData.getConfigurationSection(nationUUID + ".Elections")).getKeys(false)) {
                    LocalDate electionStart = LocalDate.parse(Objects.requireNonNull(nationData.getString(nationUUID + ".Elections." + issue + ".StartDate")));
                    boolean quickResolve = false;
                    boolean requiresSuperMajority = issue.equalsIgnoreCase("Overthrow") || issue.equalsIgnoreCase("SpecialElection") || issue.equalsIgnoreCase("Government");

                    if(issue.equalsIgnoreCase("Overthrow")) {
                        victoryThreshold = nationData.getDouble(nationUUID+".Legitimacy");
                    }

                    String current = "";

                    if(issue.equalsIgnoreCase("leader")) {
                        current = nationData.getString(nationUUID+".Leader");
                    } else if (issue.equalsIgnoreCase("government")) {
                        current = "No";
                    } else if (issue.equalsIgnoreCase("SpecialElection")) {
                        current = "No";
                    } else if (issue.equalsIgnoreCase("Overthrow")) {
                        current = "Status_Quo";
                    } else if(issue.equalsIgnoreCase("runoff")) {
                        current = nationData.getString(nationUUID+".Leader");
                    } else if(issue.equalsIgnoreCase("AddCity")) {
                        current = "No";
                    } else if(issue.equalsIgnoreCase("KickCity")) {
                        current = "No";
                    }

                    String winner = current;
                    double maxVotes = 0;
                    double totalVotes = 0;
                    boolean tie = false;
                    List<String> tieList = new ArrayList<>(Collections.emptyList());
                    for(String key : Objects.requireNonNull(nationData.getConfigurationSection(nationUUID + ".Elections." + issue + ".VoteCount")).getKeys(false)) {
                        int votes = nationData.getInt(nationUUID+".Elections."+issue+".VoteCount."+key);
                        totalVotes+=votes;
                        if(votes > maxVotes) {
                            winner = key;
                            maxVotes = votes;
                            tie = false;
                        } else if (votes == maxVotes && key.equalsIgnoreCase(current)) {
                            winner = key;
                            tie = false;
                        } else if (votes == maxVotes) {
                            tie = true;
                        }
                    }

                    if(tie) {
                        for (String ties : Objects.requireNonNull(nationData.getConfigurationSection(nationUUID + ".Elections." + issue + ".VoteCount")).getKeys(false)) {
                            if (nationData.getInt(nationUUID+".Elections."+issue+".VoteCount."+ties) == maxVotes) {
                                tieList.add(ties);
                            }
                        }
                    }

                    if(!issue.equalsIgnoreCase("Overthrow") && !issue.equalsIgnoreCase("Leader")) {
                        List<String> electorate = new ArrayList<>();
                        String government = nationData.getString(nationUUID+".Government");

                        if(Objects.requireNonNull(government).equalsIgnoreCase("Oligarchy")){
                            for(String city : nationData.getStringList(nationUUID+".Cities")) {
                                electorate.addAll(cityData.getStringList(city + ".Nobles"));
                            }
                        } else {
                            for(String city : nationData.getStringList(nationUUID+".Cities")) {
                                electorate.addAll(cityData.getStringList(city + ".Players"));
                            }
                        }

                        if((!requiresSuperMajority && maxVotes > ((double) electorate.size() /2)) || (requiresSuperMajority && maxVotes > ((double) electorate.size() * victoryThreshold))) {
                            quickResolve = true;
                        }
                    }

                    if (ChronoUnit.DAYS.between(electionStart, today) >= interval || quickResolve) {
                        if(requiresSuperMajority && (maxVotes/totalVotes) < victoryThreshold) {
                            winner = current;
                        }

                        if(issue.equalsIgnoreCase("leader")) {
                            String winnerName = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(winner))).getName();
                            if(winner.equalsIgnoreCase(current)) {
                                plugin.nationMessage(nationUUID, "The election has ended, " + winnerName + " will remain the " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + "!", true);
                                manageNationLegitimacy.addLegitimacy(nationUUID,1);
                            } else if (!tie) {
                                nationData.set(nationUUID+".Leader",winner);
                                plugin.nationMessage(nationUUID, "The election has ended, " + winnerName + " is the new " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + "!", true);
                                manageNationLegitimacy.addLegitimacy(nationUUID,1);
                            } else {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                for(String runoffCandidate : tieList) {
                                    nationData.set(nationUUID+"Elections.Runoff.VoteCount."+runoffCandidate,0);
                                }
                                nationData.set(nationUUID +".Elections.Runoff.StartDate",LocalDate.now().format(formatter));
                                nationData.set(nationUUID +".LastElection",LocalDate.now().format(formatter));
                                plugin.nationMessage(nationUUID,"Because the leading candidates tied, the election for " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + " is going to a runoff!", true);
                            }
                            nationData.set(nationUUID+".SpecialElectionCooldown",false);
                            nationData.set(nationUUID+".OverthrowCooldown",false);
                        } else if(issue.equalsIgnoreCase("runoff")) {
                            String winnerName = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(winner))).getName();
                            if(winner.equalsIgnoreCase(current)) {
                                plugin.nationMessage(nationUUID, "The runoff has ended, " + winnerName + " will remain the " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + "!", true);
                                manageNationLegitimacy.addLegitimacy(nationUUID,1);
                            } else if (!tie) {
                                nationData.set(nationUUID+".Leader",winner);
                                plugin.nationMessage(nationUUID, "The runoff has ended, " + winnerName + " is the new " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + "!", true);
                                manageNationLegitimacy.addLegitimacy(nationUUID,1);
                            } else {
                                Random rand = new Random();
                                winner = tieList.get(rand.nextInt(tieList.size()));
                                winnerName = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(winner))).getName();
                                nationData.set(nationUUID+".Leader",winner);
                                plugin.nationMessage(nationUUID, "The runoff has ended! With nothing to divide the top candidates, " + winnerName + " was chosen as the new " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + " by random draw!", true);
                            }
                            nationData.set(nationUUID+".SpecialElectionCooldown",false);
                            nationData.set(nationUUID+".OverthrowCooldown",false);
                        } else if (issue.equalsIgnoreCase("government")) {
                            if(winner.equalsIgnoreCase("yes")) {
                                String newGov = WordUtils.capitalize(nationData.getString(nationUUID+".Elections.Government.Type"));
                                nationData.set(nationUUID+".Government",newGov);
                                plugin.nationMessage(nationUUID,"The vote has ended, "+newGov + " is the new government type of " + nationData.getString(nationUUID+".Name") + "!",true);
                            } else {
                                plugin.nationMessage(nationUUID,"The vote has ended, "+ nationData.getString(nationUUID+".Government") + " will remain the government type of " + nationData.getString(nationUUID+".Name") + "!",true);
                            }
                        } else if (issue.equalsIgnoreCase("SpecialElection")) {
                            if(winner.equalsIgnoreCase("yes")) {
                                nationStartElection.startElection(nationUUID);
                                plugin.nationMessage(nationUUID,"The vote for a special election has succeeded, and an election is now underway to determine the next " + nationData.getString(nationUUID + ".LeaderTitle") + " of " + nationData.getString(nationUUID + ".Name") + "!",true);
                                manageNationLegitimacy.subtractLegitimacy(nationUUID,0.15);
                            } else {
                                plugin.nationMessage(nationUUID,"The vote has ended and the call for a special election has failed!",true);
                                manageNationLegitimacy.addLegitimacy(nationUUID,0.15);
                            }
                        } else if (issue.equalsIgnoreCase("Overthrow")) {
                            Set<String> voterList = Objects.requireNonNull(nationData.getConfigurationSection(nationUUID + ".Elections.Overthrow.VoteCount")).getKeys(false);
                            List<String> successorList = new ArrayList<>(Collections.emptyList());
                            Random rand = new Random();

                            if(winner.equalsIgnoreCase("Status_Quo")){
                                plugin.nationMessage(nationUUID,"The vote has ended and the call to overthrow the nation government has failed!",true);
                                manageNationLegitimacy.addLegitimacy(nationUUID,0.25);
                            } else if(winner.equalsIgnoreCase("Democracy")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(nationData.getString(nationUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("Democracy")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                nationData.set(nationUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                nationData.set(nationUUID+".Government","Democracy");
                                nationStartElection.startElection(nationUUID);
                                plugin.nationMessage(nationUUID,"The government of " + nationName + " has been overthrown and a Democracy has been installed! " + successorName + " has been chosen as interim " + leaderTitle + " and a special election has begun.",true);
                            } else if(winner.equalsIgnoreCase("Oligarchy")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(nationData.getString(nationUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("Oligarchy")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                nationData.set(nationUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                nationData.set(nationUUID+".Government","Oligarchy");
                                nationStartElection.startElection(nationUUID);
                                plugin.nationMessage(nationUUID,"The government of " + nationName + " has been overthrown and an Oligarchy has been installed! " + successorName + " has been chosen as interim " + leaderTitle + " and a special election has begun.",true);
                            }  else if(winner.equalsIgnoreCase("New_Monarch")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(nationData.getString(nationUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("New_Monarch")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                nationData.set(nationUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                nationStartElection.startElection(nationUUID);
                                plugin.nationMessage(nationUUID,"The government of " + nationName + " has been overthrown! " + successorName + " has been chosen as regent and a special election has begun to choose the new " + leaderTitle + ".",true);
                            }  else if(winner.equalsIgnoreCase("Monarchy")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(nationData.getString(nationUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("Monarchy")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                nationData.set(nationUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                nationData.set(nationUUID+".Government","Monarchy");
                                nationStartElection.startElection(nationUUID);
                                plugin.nationMessage(nationUUID,"The government of " + nationName + " has been overthrown and a Monarchy has been installed! " + successorName + " has been chosen as regent and a special election has begun to determine the new "+leaderTitle+".",true);
                            }
                        } else if (issue.equalsIgnoreCase("AddCity")) {
                            String inviteeUUID = nationData.getString(nationUUID + ".Elections.AddCity.City");
                            String inviteeName = cityData.getString(inviteeUUID+".Name");
                            if(winner.equalsIgnoreCase("Yes")) {
                                List<String> inviteList = cityData.getStringList(inviteeUUID+".NationInvites");
                                inviteList.add(nationUUID);
                                cityData.set(inviteeUUID+".NationInvites",inviteList);

                                List<String> invitees = nationData.getStringList(nationUUID+".Invitees");
                                invitees.add(inviteeUUID);
                                nationData.set(nationUUID+".Invitees",invitees);
                                plugin.playerMessage(cityData.getString(inviteeUUID+".Leader"),inviteeName + " has been invited to join the nation of " + nationData.getString(nationUUID+".Name") + ". Use /NationAccept to review all outstanding nation invites.");
                                plugin.nationMessage(nationUUID, "The city of " + inviteeName + " has been invited to join our nation!", false);
                            } else {
                                plugin.nationMessage(nationUUID,"Our nation has voted not to invite the city of " + inviteeName + " to join the nation!",true);
                            }
                        } else if (issue.equalsIgnoreCase("KickCity")) {
                            String kickUUID = nationData.getString(nationUUID + ".Elections.AddCity.City");
                            String kickName = cityData.getString(kickUUID+".Name");
                            if(winner.equalsIgnoreCase("Yes")) {
                                cityData.set(kickUUID+".Nation",null);
                                plugin.saveCityData();

                                List<String> cityList = nationData.getStringList(nationUUID+".Cities");
                                cityList.remove(kickUUID);
                                nationData.set(nationUUID+".Cities",cityList);
                                plugin.saveNationData();
                                plugin.nationMessage(nationUUID, "We have voted to kick the city of " + kickName + " out of our nation!",true);
                                plugin.cityMessage(kickUUID, "The people have voted to kick us out of the nation of " + nationData.getString(nationUUID+".Name")+"!",true);
                            } else {
                                plugin.nationMessage(nationUUID,"Our nation has voted not to kick the city of " + kickName + " out of the nation!",true);
                            }
                        }
                        nationData.set(nationUUID+".Elections."+issue,null);
                    }
                }
            }
        }
        plugin.saveNationData();
    }
}
