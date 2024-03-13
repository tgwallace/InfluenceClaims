package hardbuckaroo.influenceclaims;

import hardbuckaroo.influenceclaims.city.*;
import hardbuckaroo.influenceclaims.city.commands.admin.CityChunkBoostAdmin;
import hardbuckaroo.influenceclaims.city.commands.admin.CityKickAdmin;
import hardbuckaroo.influenceclaims.city.commands.admin.CityUnclaimAdmin;
import hardbuckaroo.influenceclaims.city.elections.*;
import hardbuckaroo.influenceclaims.city.plots.CheckPlot;
import hardbuckaroo.influenceclaims.city.commands.*;
import hardbuckaroo.influenceclaims.city.listeners.*;
import hardbuckaroo.influenceclaims.city.plots.commands.*;
import hardbuckaroo.influenceclaims.city.plots.listeners.ArenaRespawnListener;
import hardbuckaroo.influenceclaims.city.plots.listeners.EnterPlotListener;
import hardbuckaroo.influenceclaims.city.plots.listeners.PlotExpandListenerCity;
import hardbuckaroo.influenceclaims.city.plots.listeners.PlotSelectListenerCity;
import hardbuckaroo.influenceclaims.city.pressurebeacons.ApplyBeaconPressure;
import hardbuckaroo.influenceclaims.city.pressurebeacons.PressureBeaconBreakListener;
import hardbuckaroo.influenceclaims.city.pressurebeacons.PressureBeaconManager;
import hardbuckaroo.influenceclaims.city.pressurebeacons.PressureBeaconPlaceListener;
import hardbuckaroo.influenceclaims.nation.ApplyTimedNationLegitimacy;
import hardbuckaroo.influenceclaims.nation.ManageNationLegitimacy;
import hardbuckaroo.influenceclaims.nation.commands.*;
import hardbuckaroo.influenceclaims.nation.elections.*;
import hardbuckaroo.influenceclaims.nation.listeners.NationChatListener;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.MarkerSet;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class InfluenceClaims extends JavaPlugin {

    private File playerDataFile;
    private File cityDataFile;
    private File nationDataFile;
    private File claimDataFile;

    private FileConfiguration playerData;
    private FileConfiguration cityData;
    private FileConfiguration nationData;
    private FileConfiguration claimData;

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;
    public static DynmapAPI dapi = null;
    public static MarkerSet markerset = null;

    @Override
    public void onEnable() {
        //Creates config file and player/claim/city/nation data files.
        this.saveDefaultConfig();
        this.createCustomConfigs();

        if (getConfig().getBoolean("ChunkBoostEnabled") && !setupEconomy()) {
            this.getConfig().set("ChunkBoostEnabled",false);
            this.saveConfig();
            getLogger().warning(String.format("[%s] - ChunkBoost disabled due to no Vault dependency found!", getDescription().getName()));
        }
        setupPermissions();
        setupChat();

        //Registering commands.
        //Creating a new city:
        CityCreate cityCreate = new CityCreate(this);
        Objects.requireNonNull(getCommand("citycreate")).setExecutor(cityCreate);
        //Leaving a city you're in:
        CityLeave cityLeave = new CityLeave(this);
        Objects.requireNonNull(getCommand("cityleave")).setExecutor(cityLeave);
        //Inviting a player to join your city:
        CityInvite cityInvite = new CityInvite(this);
        Objects.requireNonNull(getCommand("cityinvite")).setExecutor(cityInvite);
        //Accepting an invitation to a city:
        CityAccept cityAccept = new CityAccept(this);
        Objects.requireNonNull(getCommand("cityaccept")).setExecutor(cityAccept);
        //Kicking a player out of your city:
        CityKick cityKick = new CityKick(this);
        Objects.requireNonNull(getCommand("citykick")).setExecutor(cityKick);
        //Getting information on a city:
        CityInfo cityInfo = new CityInfo(this);
        Objects.requireNonNull(getCommand("cityinfo")).setExecutor(cityInfo);
        //Changing city settings:
        CitySet citySet = new CitySet(this);
        Objects.requireNonNull(getCommand("cityset")).setExecutor(citySet);
        //Returns a list of cities:
        CityList cityList = new CityList(this);
        Objects.requireNonNull(getCommand("citylist")).setExecutor(cityList);
        //Turns on plot creation mode for cities:
        PlotCreateCommandCity plotCreateCommandCity = new PlotCreateCommandCity(this);
        Objects.requireNonNull(getCommand("cityplotcreate")).setExecutor(plotCreateCommandCity);
        //Turns on plot expansion mode:
        PlotExpandCommandCity plotExpandCommandCity = new PlotExpandCommandCity(this);
        Objects.requireNonNull(getCommand("cityplotexpand")).setExecutor(plotExpandCommandCity);
        //Adds players to plot whitelist:
        PlotWhitelistCity plotWhitelistCity = new PlotWhitelistCity(this);
        Objects.requireNonNull(getCommand("cityplotwhitelist")).setExecutor(plotWhitelistCity);
        //Removes players from plot whitelist:
        PlotRemoveCity plotRemoveCity = new PlotRemoveCity(this);
        Objects.requireNonNull(getCommand("cityplotremove")).setExecutor(plotRemoveCity);
        //Transfers ownership of a plot:
        PlotTransferCity plotTransferCity = new PlotTransferCity(this);
        Objects.requireNonNull(getCommand("cityplottransfer")).setExecutor(plotTransferCity);
        //Revokes ownership of a plot from another player.
        PlotRevokeCity plotRevokeCity = new PlotRevokeCity(this);
        Objects.requireNonNull(getCommand("cityplotrevoke")).setExecutor(plotRevokeCity);
        //Manages plot settings.
        PlotManageCity plotManageCity = new PlotManageCity(this);
        Objects.requireNonNull(getCommand("cityplotmanage")).setExecutor(plotManageCity);
        //Gets the info of the plot the player is standing in.
        PlotInfoCity plotInfoCity = new PlotInfoCity(this);
        Objects.requireNonNull(getCommand("cityplotinfo")).setExecutor(plotInfoCity);
        //Handles voting in cities.
        CityVote cityVote = new CityVote(this);
        Objects.requireNonNull(getCommand("cityvote")).setExecutor(cityVote);
        //Handles calls to overthrow leader.
        CityOverthrow cityOverthrow = new CityOverthrow(this);
        Objects.requireNonNull(getCommand("cityoverthrow")).setExecutor(cityOverthrow);
        //Allows players in Democracies or Oligarchies to call for a special election.
        CitySpecialElection citySpecialElection = new CitySpecialElection(this);
        Objects.requireNonNull(getCommand("cityspecialelection")).setExecutor(citySpecialElection);
        //Toggles City Chat.
        CityChat cityChat = new CityChat(this);
        Objects.requireNonNull(getCommand("citychat")).setExecutor(cityChat);
        //Gets claim information on current chunk.
        CityChunk cityChunk = new CityChunk(this);
        Objects.requireNonNull(getCommand("citychunk")).setExecutor(cityChunk);
        //Allows players to spend money to boost their chunk claims.
        CityChunkBoost cityChunkBoost = new CityChunkBoost(this);
        Objects.requireNonNull(getCommand("citychunkboost")).setExecutor(cityChunkBoost);
        //Allows city leaders to create a nation.
        NationCreate nationCreate = new NationCreate(this);
        Objects.requireNonNull(getCommand("nationcreate")).setExecutor(nationCreate);
        //Allows nation leaders to invite cities to their nation.
        NationInvite nationInvite = new NationInvite(this);
        Objects.requireNonNull(getCommand("nationinvite")).setExecutor(nationInvite);
        //Toggles nation chat channel.
        NationChat nationChat = new NationChat(this);
        Objects.requireNonNull(getCommand("nationchat")).setExecutor(nationChat);
        //Manages nation settings.
        NationSet nationSet = new NationSet(this);
        Objects.requireNonNull(getCommand("nationset")).setExecutor(nationSet);
        //Handles nation voting.
        NationVote nationVote = new NationVote(this);
        Objects.requireNonNull(getCommand("nationvote")).setExecutor(nationVote);
        //Gets info on a nation.
        NationInfo nationInfo = new NationInfo(this);
        Objects.requireNonNull(getCommand("nationinfo")).setExecutor(nationInfo);
        //Gets list of nations.
        NationList nationList = new NationList(this);
        Objects.requireNonNull(getCommand("nationlist")).setExecutor(nationList);
        //Allows the nation leader to kick cities out of the nation.
        NationKick nationKick = new NationKick(this);
        Objects.requireNonNull(getCommand("nationkick")).setExecutor(nationKick);
        //Allows city leaders to secede from their nation.
        NationLeave nationLeave = new NationLeave(this);
        Objects.requireNonNull(getCommand("nationleave")).setExecutor(nationLeave);
        //Handles calls for special elections in the nation.
        NationSpecialElection nationSpecialElection = new NationSpecialElection(this);
        Objects.requireNonNull(getCommand("nationspecialelection")).setExecutor(nationSpecialElection);
        //Handles calls to overthrow nation leader.
        NationOverthrow nationOverthrow = new NationOverthrow(this);
        Objects.requireNonNull(getCommand("nationoverthrow")).setExecutor(nationOverthrow);
        //Handles city home teleport calls.
        CityHome cityHome = new CityHome(this);
        Objects.requireNonNull(getCommand("cityhome")).setExecutor(cityHome);
        //Allows city to unclaim chunks.
        CityUnclaim cityUnclaim = new CityUnclaim(this);
        Objects.requireNonNull(getCommand("cityunclaim")).setExecutor(cityUnclaim);
        //Allows city leaders to manage roles.
        CityRole cityRole = new CityRole(this);
        Objects.requireNonNull(getCommand("cityrole")).setExecutor(cityRole);
        //Allows city leaders to manage roles.
        CityChunkMonitor cityChunkMonitor = new CityChunkMonitor(this);
        Objects.requireNonNull(getCommand("citychunkmonitor")).setExecutor(cityChunkMonitor);
        //Admin version of ChunkBoost.
        CityChunkBoostAdmin cityChunkBoostAdmin = new CityChunkBoostAdmin(this);
        Objects.requireNonNull(getCommand("citychunkboostadmin")).setExecutor(cityChunkBoostAdmin);
        //Admin version of CityKick:
        CityKickAdmin cityKickAdmin = new CityKickAdmin(this);
        Objects.requireNonNull(getCommand("citykickadmin")).setExecutor(cityKickAdmin);
        //Allows admins to unclaim chunks.
        CityUnclaimAdmin cityUnclaimAdmin = new CityUnclaimAdmin(this);
        Objects.requireNonNull(getCommand("cityunclaimadmin")).setExecutor(cityUnclaimAdmin);
        //Removes a set of corners from a plot.
        PlotShrinkCity plotShrinkCity = new PlotShrinkCity(this);
        Objects.requireNonNull(getCommand("cityplotshrink")).setExecutor(plotShrinkCity);
        //Removes a set of corners from a plot.
        PlotDeleteCity plotDeleteCity = new PlotDeleteCity(this);
        Objects.requireNonNull(getCommand("cityplotdelete")).setExecutor(plotDeleteCity);
        //Lists all citizens of a city.
        PopulationList populationList = new PopulationList(this);
        Objects.requireNonNull(getCommand("citypopulationlist")).setExecutor(populationList);


        //Registering events.
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MiscListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new CraftItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EnchantItemListener(this), this);
        Bukkit.getPluginManager().registerEvents(new FurnaceExtractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BrewListener(this), this);
        Bukkit.getPluginManager().registerEvents(new EnterChunkListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerAttackListener(this), this);
        Bukkit.getPluginManager().registerEvents(new MobAttackListener(this), this);
        Bukkit.getPluginManager().registerEvents(new LoginListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlotSelectListenerCity(this), this);
        Bukkit.getPluginManager().registerEvents(new PlotExpandListenerCity(this), this);
        Bukkit.getPluginManager().registerEvents(new EnterPlotListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CityChatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerTeleportListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ArenaRespawnListener(this), this);
        Bukkit.getPluginManager().registerEvents(new NationChatListener(this), this);

        if(getConfig().getBoolean("RailProtector")) {
            Bukkit.getPluginManager().registerEvents(new RailBreakListener(this), this);
        }

        //Initializing using dependency injection.
        CheckProtection checkProtection = new CheckProtection(this);
        CheckPlot checkPlot = new CheckPlot(this);
        ApplyPressure ap = new ApplyPressure(this);
        CityStartElection cityStartElection = new CityStartElection(this);
        ManageClaims manageClaims = new ManageClaims(this);
        NationStartElection nationStartElection = new NationStartElection(this);
        ManageCityLegitimacy manageCityLegitimacy = new ManageCityLegitimacy(this);
        ManageNationLegitimacy manageNationLegitimacy = new ManageNationLegitimacy(this);

        //Give all cities and nations their daily legitimacy bump. Setting this for just over a day in case resets are irregular.
        ApplyTimedCityLegitimacy applyTimedCityLegitimacy = new ApplyTimedCityLegitimacy(this);
        ApplyTimedNationLegitimacy applyTimedNationLegitimacy = new ApplyTimedNationLegitimacy(this);
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            applyTimedCityLegitimacy.applyLegitimacy();
            applyTimedNationLegitimacy.applyLegitimacy();
        },0,1750000);

        //Setting timer for recurring application of pressure. 72000 is the number of ticks in an hour.
        long pressureTimer = (long) (this.getConfig().getDouble("PressureTimer") * 72000);
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, ap::applyPressure, 30, pressureTimer);

        //Setting timer for recurring checks on resolving elections and starting new scheduled elections.
        long electionResolveTimer = this.getConfig().getLong("ElectionResolveTimer") * 72000;
        CityResolveElections cityResolveElections = new CityResolveElections(this);
        NationResolveElections nationResolveElections = new NationResolveElections(this);
        CityStartScheduledElections cityStartScheduledElections = new CityStartScheduledElections(this);
        NationStartScheduledElections nationStartScheduledElections = new NationStartScheduledElections(this);
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            cityResolveElections.resolveElections();
            nationResolveElections.resolveElections();
            cityStartScheduledElections.startElections();
            nationStartScheduledElections.startElections();
        }, 0, electionResolveTimer);

        if(Bukkit.getPluginManager().getPlugin("DynMap") != null && Bukkit.getServer().getPluginManager().getPlugin("dynmap").isEnabled() && getConfig().getBoolean("DynMapCities")) {
            dapi = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
            UpdateDynMap updateDynMap = new UpdateDynMap(this);
            long dynTimer = this.getConfig().getLong("DynMapFrequency") * 72000;
            Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, updateDynMap::updateDynMap, 30, dynTimer);
        }

        //Set up pressure beacons.
        if(getConfig().getBoolean("PressureBeaconsActive")) {
            PressureBeaconManager pressureBeaconManager = new PressureBeaconManager(this);
            pressureBeaconManager.addRecipe();
            Bukkit.getPluginManager().registerEvents(new PressureBeaconPlaceListener(this, pressureBeaconManager), this);
            Bukkit.getPluginManager().registerEvents(new PressureBeaconBreakListener(this, pressureBeaconManager), this);
            long pressureBeaconTimer = (long) (this.getConfig().getDouble("PressureBeaconTimer") * 72000);
            ApplyBeaconPressure abp = new ApplyBeaconPressure(this, pressureBeaconManager);
            Bukkit.getScheduler().runTaskTimer(this, abp::applyPressure, 0, pressureBeaconTimer);
        }

        Bukkit.getScheduler().runTaskTimer(this, this::saveClaimData,1200,1200);
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for(String playerUUID : playerData.getKeys(false)) {
            playerData.set(playerUUID+".PlotMode",null);
            playerData.set(playerUUID+".PlotExpand",null);
            playerData.set(playerUUID+".PlotCorner1",null);
        }
        saveClaimData();
        saveCityData();
        savePlayerData();
        saveNationData();
        getLogger().info(String.format("[%s] Disabled Version %s", getDescription().getName(), getDescription().getVersion()));
    }

    public FileConfiguration getPlayerData(){
        return this.playerData;
    }

    public FileConfiguration getCityData(){
        return this.cityData;
    }

    public FileConfiguration getNationData(){
        return this.nationData;
    }

    public FileConfiguration getClaimData(){
        return this.claimData;
    }

    public void savePlayerData(){
        try {
            this.playerData.save(this.playerDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        if(rsp != null)
            chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = Objects.requireNonNull(rsp).getProvider();
        return perms != null;
    }

    public void saveCityData(){
        try {
            this.cityData.save(this.cityDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveNationData(){
        try {
            this.nationData.save(this.nationDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveClaimData(){
        try {
            this.claimData.save(this.claimDataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Creates player/claim/city/nation data files if they don't already exist.
    private void createCustomConfigs() {
        playerDataFile = new File(getDataFolder(), "playerData.yml");
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource("playerData.yml", false);
        }

        playerData = new YamlConfiguration();
        try {
            playerData.load(playerDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        cityDataFile = new File(getDataFolder(), "cityData.yml");
        if (!cityDataFile.exists()) {
            cityDataFile.getParentFile().mkdirs();
            saveResource("cityData.yml", false);
        }

        cityData = new YamlConfiguration();
        try {
            cityData.load(cityDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        nationDataFile = new File(getDataFolder(), "nationData.yml");
        if (!nationDataFile.exists()) {
            nationDataFile.getParentFile().mkdirs();
            saveResource("nationData.yml", false);
        }

        nationData = new YamlConfiguration();
        try {
            nationData.load(nationDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        claimDataFile = new File(getDataFolder(), "claimData.yml");
        if (!claimDataFile.exists()) {
            claimDataFile.getParentFile().mkdirs();
            saveResource("claimData.yml", false);
        }

        claimData = new YamlConfiguration();
        try {
            claimData.load(claimDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

    public CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
    }

    //Easy utility method for coloring text.
    public String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    //Returns the claimant for a given chunk key, if there is one.
    public String getClaimant(String chunkKey){
        int claimTotal = 0;
        String claimant = "";

        if(claimData.contains(chunkKey + ".Claims")) {
            for (String claim : Objects.requireNonNull(claimData.getConfigurationSection(chunkKey + ".Claims")).getKeys(false)) {
                int claimTemp = claimData.getInt(chunkKey + ".Claims." + claim + ".Temporary");
                int claimPerm = claimData.getInt(chunkKey + ".Claims." + claim + ".Permanent");
                if (claimTemp + claimPerm > claimTotal) {
                    claimTotal = claimTemp + claimPerm;

                    if(claimTotal >= this.getConfig().getInt("ClaimMinimum"))
                        claimant = claim;
                }
            }
        }
        if (claimant.isEmpty()) return null;
        else return claimant;
    }

    //Returns a chunk key for a given chunk.
    public String getChunkKey(Chunk chunk){
        return chunk.getWorld().getName()+","+chunk.getX()+","+chunk.getZ();
    }

    //Finds the UUID of a city given its name. Used in player commands that reference a city by name.
    public String getCityUUIDFromName(String name){
        for(String cityUUID : cityData.getKeys(false)){
            if(Objects.requireNonNull(cityData.getString(cityUUID + ".Name")).equals(name)){
                return cityUUID;
            }
        }
        return null;
    }

    public String getNationUUIDFromName(String name){
        for(String nationUUID : nationData.getKeys(false)){
            if(Objects.requireNonNull(nationData.getString(nationUUID + ".Name")).equalsIgnoreCase(name)){
                return nationUUID;
            }
        }
        return null;
    }

    //Sends a message to all city members with log set as false.
    public void cityMessage(String cityUUID, String message){
        cityMessage(cityUUID,message,false);
    }

    //Sends a message to all city members, adding it to their messages if the player is offline and log is true.
    public void cityMessage(String cityUUID, String message, Boolean log){
        String cityColor = cityData.getString(cityUUID+".Color");
        for(String playerUUID : cityData.getStringList(cityUUID+".Players")){
            if(Bukkit.getServer().getPlayer(UUID.fromString(playerUUID)) != null){
                Player recipient = Bukkit.getServer().getPlayer(UUID.fromString(playerUUID));
                Objects.requireNonNull(recipient).sendRawMessage(color(cityColor+message));
            } else if(log){
                playerMessage(playerUUID,color(cityColor+message));
            }
        }
    }

    public void nationMessage(String nationUUID, String message, Boolean log){
        String nationColor = nationData.getString(nationUUID+".Color");
        for(String cityUUID : nationData.getStringList(nationUUID+".Cities")) {
            for (String playerUUID : cityData.getStringList(cityUUID + ".Players")) {
                if (Bukkit.getServer().getPlayer(UUID.fromString(playerUUID)) != null) {
                    Player recipient = Bukkit.getServer().getPlayer(UUID.fromString(playerUUID));
                    Objects.requireNonNull(recipient).sendRawMessage(color(nationColor + message));
                } else if (log) {
                    playerMessage(playerUUID, color(nationColor + message));
                }
            }
        }
    }

    //Adds a message to the player's data that will be sent to them when they log in.
    public void playerMessage(String playerUUID, String message){
        if(Bukkit.getServer().getPlayer(UUID.fromString(playerUUID)) != null){
            Player recipient = Bukkit.getServer().getPlayer(UUID.fromString(playerUUID));
            Objects.requireNonNull(recipient).sendRawMessage(color(message));
        } else {
            List<String> messageList = playerData.getStringList(playerUUID+".Messages");
            messageList.add(message);
            playerData.set(playerUUID+".Messages", messageList);
            this.savePlayerData();
        }
    }

    public String[] getPlot(Block block) {
        String claimant = getClaimant(getChunkKey(block.getChunk()));
        if(claimant != null && cityData.contains(claimant + ".Plots")) {
            for (String plotUUID : Objects.requireNonNull(cityData.getConfigurationSection(claimant + ".Plots")).getKeys(false)) {
                for (String key : cityData.getStringList(claimant+".Plots."+plotUUID+".Coords")) {
                    String[] worldPlot = key.split("\\|");
                    String[] corners = worldPlot[1].split(":");
                    List<Integer> corner1 = Arrays.stream(corners[0].split(",")).map(Integer::parseInt).collect(Collectors.toList());
                    List<Integer> corner2 = Arrays.stream(corners[1].split(",")).map(Integer::parseInt).collect(Collectors.toList());

                    int x = block.getX();
                    int y = block.getY();
                    int z = block.getZ();

                    int minX = Math.min(corner1.get(0), corner2.get(0));
                    int maxX = Math.max(corner1.get(0), corner2.get(0));
                    int minY = Math.min(corner1.get(1), corner2.get(1));
                    int maxY = Math.max(corner1.get(1), corner2.get(1));
                    int minZ = Math.min(corner1.get(2), corner2.get(2));
                    int maxZ = Math.max(corner1.get(2), corner2.get(2));

                    if (block.getWorld().getName().equalsIgnoreCase(worldPlot[0]) && x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                        return new String[]{claimant, plotUUID};
                    }
                }
            }
        }
        return null;
    }

    public void updateScoreboard() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            String playerUUID = player.getUniqueId().toString();
            if (playerData.contains(playerUUID + ".City")) {
                String cityUUID = playerData.getString(playerUUID+".City");
                String cityColor = cityData.getString(cityUUID + ".Color");
                String cityTag = cityData.getString(cityUUID+".Tag");

                player.setPlayerListName(color(cityColor + "[" + cityTag + "]&f") + player.getName());
                InfluenceClaims.getChat().setPlayerPrefix(null,player,color(cityColor + "[" + cityTag + "]&f"));
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                Scoreboard board = manager.getNewScoreboard();

                Team ally = board.registerNewTeam("Ally");
                ally.setPrefix(color("[&bA&f]"));
                Team neutral = board.registerNewTeam("Neutral");
                neutral.setPrefix(color("[&7N&f]"));
                Team friendly = board.registerNewTeam("Friendly");
                friendly.setPrefix(color("[&aF&f]"));
                Team hostile = board.registerNewTeam("Hostile");
                hostile.setPrefix(color("[&cH&f]"));
                Team unaligned = board.registerNewTeam("Unaligned");
                unaligned.setPrefix(color("[&7U&f]"));

                for (Player viewed : Bukkit.getOnlinePlayers()) {
                    String viewedName = viewed.getName();
                    if (playerData.contains(viewed.getUniqueId().toString() + ".City")) {
                        String viewedCityUUID = playerData.getString(viewed.getUniqueId().toString() + ".City");
                        String stance = cityData.getString(viewedCityUUID + ".Stances."+cityUUID);
                        if (cityUUID.equalsIgnoreCase(viewedCityUUID) || (cityData.contains(cityUUID+".Nation") && cityData.contains(viewedCityUUID+".Nation") && cityData.getString(cityUUID+".Nation").equalsIgnoreCase(cityData.getString(viewedCityUUID+".Nation")))) {
                           ally.addEntry(viewedName);
                        } else if(stance == null) {
                            neutral.addEntry(viewedName);
                        } else if (stance.equalsIgnoreCase("Friendly")) {
                            friendly.addEntry(viewedName);
                        } else if (stance.equalsIgnoreCase("Hostile")) {
                            hostile.addEntry(viewedName);
                        }
                    } else {
                        unaligned.addEntry(viewedName);
                    }
                }
                player.setScoreboard(board);
            } else {
                player.setPlayerListName(player.getName());
                InfluenceClaims.getChat().setPlayerPrefix(null,player,null);
            }
        }
    }

    public String getBlockKey(Block block) {
        return block.getWorld().getName()+","+block.getX()+","+block.getY()+","+block.getZ();
    }

    public Block getBlockFromKey(String blockKey) {
        String[] keyParts = blockKey.split(",");
        World world = Bukkit.getServer().getWorld(keyParts[0]);
        if(world == null) {
            getLogger().log(Level.WARNING,"Null world check, has "+keyParts[0]+" been deleted?");
            return null;
        }
        else return world.getBlockAt(Integer.parseInt(keyParts[1]),Integer.parseInt(keyParts[2]),Integer.parseInt(keyParts[3]));
    }
}
