package hardbuckaroo.influenceclaims.city.elections;

import hardbuckaroo.influenceclaims.InfluenceClaims;
import hardbuckaroo.influenceclaims.city.ManageCityLegitimacy;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class CityResolveElections {
    private final InfluenceClaims plugin;

    public CityResolveElections(InfluenceClaims plugin){
        this.plugin = plugin;
    }

    public void resolveElections(){
        FileConfiguration cityData = plugin.getCityData();
        FileConfiguration nationData = plugin.getNationData();
        LocalDate today = LocalDate.now();
        double interval = plugin.getConfig().getDouble("ElectionLength");
        double victoryThreshold = plugin.getConfig().getDouble("SuperMajorityThreshold");
        CityStartElection cityStartElection = new CityStartElection(plugin);
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(plugin);

        if(cityData.getKeys(false).size() == 0){
            return;
        }

        for(String cityUUID : cityData.getKeys(false)) {
            String cityName = cityData.getString(cityUUID+".Name");
            String leaderTitle = cityData.getString(cityUUID+".LeaderTitle");
            if(cityData.contains(cityUUID+".Elections")) {
                for(String issue : Objects.requireNonNull(cityData.getConfigurationSection(cityUUID + ".Elections")).getKeys(false)) {
                    LocalDate electionStart = LocalDate.parse(Objects.requireNonNull(cityData.getString(cityUUID + ".Elections." + issue + ".StartDate")));
                    boolean quickResolve = false;
                    boolean requiresSuperMajority = false;

                    if(issue.equalsIgnoreCase("Overthrow") || issue.equalsIgnoreCase("SpecialElection") || issue.equalsIgnoreCase("Government") || issue.equalsIgnoreCase("LeaveNation")) {
                        requiresSuperMajority = true;
                    }

                    if(issue.equalsIgnoreCase("Overthrow")) {
                        victoryThreshold = cityData.getDouble(cityUUID+".Legitimacy");
                    }

                    String current = "";

                    if(issue.equalsIgnoreCase("leader")) {
                        current = cityData.getString(cityUUID+".Leader");
                    } else if (issue.equalsIgnoreCase("government")) {
                        current = "No";
                    } else if (issue.equalsIgnoreCase("SpecialElection")) {
                        current = "No";
                    } else if (issue.equalsIgnoreCase("Overthrow")) {
                        current = "Status_Quo";
                    } else if(issue.equalsIgnoreCase("runoff")) {
                        current = cityData.getString(cityUUID+".Leader");
                    } else if(issue.equalsIgnoreCase("NewNation")) {
                        current = "No";
                    } else if(issue.equalsIgnoreCase("JoinNation")) {
                        current = "No";
                    } else if(issue.equalsIgnoreCase("LeaveNation")) {
                        current = "No";
                    }

                    String winner = current;
                    double maxVotes = 0;
                    double totalVotes = 0;
                    boolean tie = false;
                    List<String> tieList = new ArrayList<>(Collections.emptyList());
                    for(String key : Objects.requireNonNull(cityData.getConfigurationSection(cityUUID + ".Elections." + issue + ".VoteCount")).getKeys(false)) {
                        int votes = cityData.getInt(cityUUID+".Elections."+issue+".VoteCount."+key);
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
                        for (String ties : Objects.requireNonNull(cityData.getConfigurationSection(cityUUID + ".Elections." + issue + ".VoteCount")).getKeys(false)) {
                            if (cityData.getInt(cityUUID+".Elections."+issue+".VoteCount."+ties) == maxVotes) {
                                tieList.add(ties);
                            }
                        }
                    }

                    if(!issue.equalsIgnoreCase("Overthrow") && !issue.equalsIgnoreCase("Leader")) {
                        List<String> electorate;
                        String government = cityData.getString(cityUUID+".Government");

                        if(Objects.requireNonNull(government).equalsIgnoreCase("Oligarchy")){
                            electorate = cityData.getStringList(cityUUID+".Nobles");
                        } else {
                            electorate = cityData.getStringList(cityUUID+".Players");
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
                                plugin.cityMessage(cityUUID, "The election has ended, " + winnerName + " will remain the " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + "!", true);
                            } else if (!tie) {
                                cityData.set(cityUUID+".Leader",winner);
                                plugin.cityMessage(cityUUID, "The election has ended, " + winnerName + " is the new " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + "!", true);
                            } else {
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                for(String runoffCandidate : tieList) {
                                    cityData.set(cityUUID+"Elections.Runoff.VoteCount."+runoffCandidate,0);
                                }
                                cityData.set(cityUUID +".Elections.Runoff.StartDate",LocalDate.now().format(formatter));
                                cityData.set(cityUUID +".LastElection",LocalDate.now().format(formatter));
                                plugin.cityMessage(cityUUID,"Because the leading candidates tied, the election for " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + " is going to a runoff!", true);
                            }
                            cityData.set(cityUUID+".SpecialElectionCooldown",false);
                            cityData.set(cityUUID+".OverthrowCooldown",false);
                            manageCityLegitimacy.addLegitimacy(cityUUID,1);
                        } else if(issue.equalsIgnoreCase("runoff")) {
                            String winnerName = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(winner))).getName();
                            if(winner.equalsIgnoreCase(current)) {
                                plugin.cityMessage(cityUUID, "The runoff has ended, " + winnerName + " will remain the " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + "!", true);
                            } else if (!tie) {
                                cityData.set(cityUUID+".Leader",winner);
                                plugin.cityMessage(cityUUID, "The runoff has ended, " + winnerName + " is the new " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + "!", true);
                            } else {
                                Random rand = new Random();
                                winner = tieList.get(rand.nextInt(tieList.size()));
                                winnerName = Bukkit.getOfflinePlayer(UUID.fromString(Objects.requireNonNull(winner))).getName();
                                cityData.set(cityUUID+".Leader",winner);
                                plugin.cityMessage(cityUUID, "The runoff has ended! With nothing to divide the top candidates, " + winnerName + " was chosen as the new " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + " by random draw!", true);
                            }
                            cityData.set(cityUUID+".SpecialElectionCooldown",false);
                            cityData.set(cityUUID+".OverthrowCooldown",false);
                            manageCityLegitimacy.addLegitimacy(cityUUID,1);
                        } else if (issue.equalsIgnoreCase("government")) {
                            if(winner.equalsIgnoreCase("yes")) {
                                String newGov = WordUtils.capitalize(cityData.getString(cityUUID+".Elections.Government.Type"));
                                cityData.set(cityUUID+".Government",newGov);
                                plugin.cityMessage(cityUUID,"The vote has ended, "+newGov + " is the new government type of " + cityData.getString(cityUUID+".Name") + "!",true);
                            } else {
                                plugin.cityMessage(cityUUID,"The vote has ended, "+ cityData.getString(cityUUID+".Government") + " will remain the government type of " + cityData.getString(cityUUID+".Name") + "!",true);
                            }
                        } else if (issue.equalsIgnoreCase("SpecialElection")) {
                            if(winner.equalsIgnoreCase("yes")) {
                                cityStartElection.startElection(cityUUID);
                                plugin.cityMessage(cityUUID,"The vote for a special election has succeeded, and an election is now underway to determine the next " + cityData.getString(cityUUID + ".LeaderTitle") + " of " + cityData.getString(cityUUID + ".Name") + "!",true);
                                manageCityLegitimacy.subtractLegitimacy(cityUUID,0.15);
                            } else {
                                plugin.cityMessage(cityUUID,"The vote has ended and the call for a special election has failed!",true);
                                manageCityLegitimacy.addLegitimacy(cityUUID,0.15);
                            }
                        } else if (issue.equalsIgnoreCase("Overthrow")) {
                            Set<String> voterList = Objects.requireNonNull(cityData.getConfigurationSection(cityUUID + ".Elections.Overthrow.VoteCount")).getKeys(false);
                            List<String> successorList = new ArrayList<>(Collections.emptyList());
                            Random rand = new Random();

                            if(winner.equalsIgnoreCase("Status_Quo")){
                                plugin.cityMessage(cityUUID,"The vote has ended and the call to overthrow the city government has failed!",true);
                                manageCityLegitimacy.addLegitimacy(cityUUID,0.25);
                            } else if(winner.equalsIgnoreCase("Democracy")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(cityData.getString(cityUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("Democracy")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                cityData.set(cityUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                cityData.set(cityUUID+".Government","Democracy");
                                cityStartElection.startElection(cityUUID);
                                plugin.cityMessage(cityUUID,"The government of " + cityName + " has been overthrown and a Democracy has been installed! " + successorName + " has been chosen as interim " + leaderTitle + " and a special election has begun.",true);
                            } else if(winner.equalsIgnoreCase("Oligarchy")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(cityData.getString(cityUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("Oligarchy")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                cityData.set(cityUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                cityData.set(cityUUID+".Government","Oligarchy");
                                cityStartElection.startElection(cityUUID);
                                plugin.cityMessage(cityUUID,"The government of " + cityName + " has been overthrown and an Oligarchy has been installed! " + successorName + " has been chosen as interim " + leaderTitle + " and a special election has begun.",true);
                            }  else if(winner.equalsIgnoreCase("New_Monarch")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(cityData.getString(cityUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("New_Monarch")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                cityData.set(cityUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                cityStartElection.startElection(cityUUID);
                                plugin.cityMessage(cityUUID,"The government of " + cityName + " has been overthrown! " + successorName + " has been chosen as regent and a special election has begun to choose the new " + leaderTitle + ".",true);
                            }  else if(winner.equalsIgnoreCase("Monarchy")){
                                for(String voter : voterList) {
                                    if(Objects.requireNonNull(cityData.getString(cityUUID + ".Elections.Overthrow.VoteCount." + voter)).equalsIgnoreCase("Monarchy")) {
                                        successorList.add(voter);
                                    }
                                }
                                String successor = successorList.get(rand.nextInt(successorList.size()));

                                cityData.set(cityUUID+".Leader",successor);
                                String successorName = Bukkit.getOfflinePlayer(UUID.fromString(successor)).getName();
                                cityData.set(cityUUID+".Government","Monarchy");
                                cityStartElection.startElection(cityUUID);
                                plugin.cityMessage(cityUUID,"The government of " + cityName + " has been overthrown and a Monarchy has been installed! " + successorName + " has been chosen as regent and a special election has begun to determine the new "+leaderTitle+".",true);
                            }
                        } else if (issue.equalsIgnoreCase("NewNation")) {
                            if(winner.equalsIgnoreCase("Yes")) {
                                String nationUUID = UUID.randomUUID().toString();
                                nationData.set(nationUUID + ".Name", cityData.getString(cityUUID+".Elections.NewNation.Name"));
                                nationData.set(nationUUID + ".Leader", cityData.getString(cityUUID+".Leader"));
                                List<String> nobleList = Collections.singletonList(cityData.getString(cityUUID+".Leader"));
                                nationData.set(nationUUID + ".Nobles", nobleList);
                                nationData.set(nationUUID + ".Color", "&f");
                                nationData.set(nationUUID + ".Motto", " ");
                                nationData.set(nationUUID + ".LeaderTitle", "President");
                                nationData.set(nationUUID + ".NobilityTitle", "Cabinet Member");
                                nationData.set(nationUUID + ".CitizenTitle", "Citizen");
                                nationData.set(nationUUID + ".Government", cityData.getString(cityUUID+".Government"));
                                nationData.set(nationUUID+".Tag", Objects.requireNonNull(cityData.getString(cityUUID + ".Elections.NewNation.Name")).substring(0,Math.min(cityData.getString(cityUUID+".Elections.NewNation.Name").length(),5)).replace(" ",""));
                                List<String> cityList = Collections.singletonList(cityUUID);
                                nationData.set(nationUUID+".Cities",cityList);
                                cityData.set(cityUUID+".Nation",nationUUID);
                                plugin.saveNationData();
                                plugin.cityMessage(cityUUID,"The vote is over, the city of " + cityData.getString(cityUUID+".Name") + " has founded a new nation called " + cityData.getString(cityUUID+".Elections.NewNation.Name") + "!",true);
                            } else {
                                plugin.cityMessage(cityUUID,"The vote is over, the city of " + cityData.getString(cityUUID+".Name") + " has chosen not to found a new nation!",true);
                            }
                        } else if (issue.equalsIgnoreCase("JoinNation")) {
                            String nationUUID = cityData.getString(cityUUID+".Elections.JoinNation.Invite");
                            if(winner.equalsIgnoreCase("Yes")) {
                                List<String> cityList = nationData.getStringList(nationUUID+".Cities");
                                cityList.add(cityUUID);
                                nationData.set(nationUUID+".Cities",cityList);
                                plugin.saveNationData();

                                cityData.set(cityUUID+".Nation",nationUUID);
                                plugin.nationMessage(nationUUID,"The city of " + cityData.getString(cityUUID+".Name") + " has voted to join the nation of " + nationData.getString(nationUUID+".Name") + "!",true);
                            } else {
                                plugin.nationMessage(nationUUID,"The city of " + cityData.getString(cityUUID+".Name") + " has voted against joining our nation!",true);
                                plugin.cityMessage(cityUUID,"The city of " + cityData.getString(cityUUID+".Name") + " has voted against joining the nation of "+nationData.getString(nationUUID+".Name")+".");
                            }
                        } else if (issue.equalsIgnoreCase("LeaveNation")) {
                            if(winner.equalsIgnoreCase("Yes")) {
                                String nationUUID = cityData.getString(cityUUID+".Nation");
                                String nationName = nationData.getString(nationUUID+".Name");
                                cityData.set(cityUUID+".Nation",null);
                                plugin.saveCityData();

                                List<String> cityList = nationData.getStringList(nationUUID+".Cities");
                                cityList.remove(cityUUID);
                                nationData.set(nationUUID+".Cities",cityList);
                                plugin.saveNationData();

                                plugin.nationMessage(nationUUID,"The city of " + cityName + " has seceded from our nation!",true);
                                plugin.cityMessage(cityUUID,"Our city has seceded from the nation of " + nationName + "!",true);
                            } else {
                                String nationUUID = cityData.getString(cityUUID+".Nation");
                                plugin.nationMessage(nationUUID,"The city of " + cityData.getString(cityUUID+".Name") + " has voted against seceding from our nation!",true);
                            }
                        }
                        cityData.set(cityUUID+".Elections."+issue,null);
                    }
                }
                plugin.saveCityData();
            }
        }

    }
}
