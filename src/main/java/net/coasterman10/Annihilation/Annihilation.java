/*
 Copyright 2014 stuntguy3000 (Luke Anderson), coasterman10 and F3DEX22.

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 MA 02110-1301, USA.
 */
package net.coasterman10.Annihilation;

import net.coasterman10.Annihilation.api.GameStartEvent;
import net.coasterman10.Annihilation.api.PhaseChangeEvent;
import net.coasterman10.Annihilation.bar.BarUtil;
import net.coasterman10.Annihilation.chat.ChatListener;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.commands.AnnihilationCommand;
import net.coasterman10.Annihilation.commands.ClassCommand;
import net.coasterman10.Annihilation.commands.DistanceCommand;
import net.coasterman10.Annihilation.commands.MapCommand;
import net.coasterman10.Annihilation.commands.StatsCommand;
import net.coasterman10.Annihilation.commands.TeamCommand;
import net.coasterman10.Annihilation.commands.TeamShortcutCommand;
import net.coasterman10.Annihilation.commands.VoteCommand;
import net.coasterman10.Annihilation.listeners.BossListener;
import net.coasterman10.Annihilation.listeners.ClassAbilityListener;
import net.coasterman10.Annihilation.listeners.CraftingListener;
import net.coasterman10.Annihilation.listeners.EnderBrewingStandListener;
import net.coasterman10.Annihilation.listeners.EnderChestListener;
import net.coasterman10.Annihilation.listeners.EnderFurnaceListener;
import net.coasterman10.Annihilation.listeners.PlayerListener;
import net.coasterman10.Annihilation.listeners.ResourceListener;
import net.coasterman10.Annihilation.listeners.SoulboundListener;
import net.coasterman10.Annihilation.listeners.WandListener;
import net.coasterman10.Annihilation.listeners.WorldListener;
import net.coasterman10.Annihilation.manager.BossManager;
import net.coasterman10.Annihilation.manager.ConfigManager;
import net.coasterman10.Annihilation.manager.DatabaseManager;
import net.coasterman10.Annihilation.manager.MapManager;
import net.coasterman10.Annihilation.manager.PhaseManager;
import net.coasterman10.Annihilation.manager.RestartHandler;
import net.coasterman10.Annihilation.manager.ScoreboardManager;
import net.coasterman10.Annihilation.manager.SignManager;
import net.coasterman10.Annihilation.manager.VotingManager;
import net.coasterman10.Annihilation.maps.MapLoader;
import net.coasterman10.Annihilation.object.Boss;
import net.coasterman10.Annihilation.object.GameTeam;
import net.coasterman10.Annihilation.object.Kit;
import net.coasterman10.Annihilation.object.PlayerMeta;
import net.coasterman10.Annihilation.object.Shop;
import net.coasterman10.Annihilation.stats.StatType;
import net.coasterman10.Annihilation.stats.StatsManager;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import static net.coasterman10.Annihilation.Translation.get;

//import com.bergerkiller.bukkit.common.events.EntityMoveEvent;
//import net.gravitydevelopment.updater.Updater;
//import net.gravitydevelopment.updater.Updater.UpdateResult;

public final class Annihilation extends JavaPlugin implements Listener {

    private static Annihilation instance = null;

    private ConfigManager configManager;
    private VotingManager votingManager;
    private MapManager mapManager;
    private PhaseManager phaseManager;
    private ResourceListener resourceListener;
    private EnderFurnaceListener enderFurnaceListener;
    private EnderBrewingStandListener enderBrewingStandListener;
    private EnderChestListener enderChestListener;
    private StatsManager statsManager;
    private SignManager signManager;
    private ScoreboardManager scoreboardManager;
    private DatabaseManager databaseManager;
    private BossManager bossManager;

    private YamlConfiguration pluginConfiguration;

    private static HashMap<String, String> messages = new HashMap<String, String>();
    public boolean useMysql = false;
    public boolean messageOfTheDay = true;

    //public String newVersion;
    //public boolean updateAvailable = false;

    public int nexusBuildRadius;
    public int lastJoinPhase;
    public int respawn;

    public boolean isEndGameCommandsEnabled = false;
    public List<String> commands = new ArrayList<String>();

    private String mysqlTable;

    public String getMysqlTable() {
        return mysqlTable;
    }

    public static Annihilation getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;

        // Configuration files setup
        configManager = new ConfigManager(this);
        configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml", "stats.yml", "messages.yml");

        this.pluginConfiguration = configManager.getConfig("config.yml");
        YamlConfiguration messagesConfig = YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "messages.yml"));
        YamlConfiguration shops = configManager.getConfig("shops.yml");

        // Store messages in the HashMap
        for (String id : messagesConfig.getKeys(false))
            messages.put(id, messagesConfig.getString(id));

        MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

        isEndGameCommandsEnabled = getConfig().contains("commandsToRunAtEndGame");

        if (isEndGameCommandsEnabled) {
            commands = getConfig().getStringList("commandsToRunAtEndGame");
        } else commands = null;

        mapManager = new MapManager(this, mapLoader, configManager.getConfig("maps.yml"));

        // Setup shops
        new Shop(this, "Weapon", shops);
        new Shop(this, "Brewing", shops);

        // Setup listeners
        resourceListener = new ResourceListener(this);
        enderFurnaceListener = new EnderFurnaceListener(this);
        enderBrewingStandListener = new EnderBrewingStandListener(this);
        enderChestListener = new EnderChestListener();

        // Setup managers
        statsManager = new StatsManager(this, configManager);
        this.setSignHandler(new SignManager(this));
        phaseManager = new PhaseManager(this, pluginConfiguration.getInt("start-delay"), pluginConfiguration.getInt("phase-period"));
        votingManager = new VotingManager(this);
        scoreboardManager = new ScoreboardManager();
        bossManager = new BossManager(this);

        PluginManager pm = getServer().getPluginManager();

        signManager.loadSigns();

        scoreboardManager.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
                + " | " + ChatColor.GOLD + "/vote <name>");

        nexusBuildRadius = this.getConfig().getInt("build", 5);
        lastJoinPhase = this.getConfig().getInt("lastJoinPhase", 2);
        respawn = this.getConfig().getInt("bossRespawnDelay", 10);

        pm.registerEvents(resourceListener, this);
        pm.registerEvents(enderFurnaceListener, this);
        pm.registerEvents(enderBrewingStandListener, this);
        pm.registerEvents(enderChestListener, this);
        pm.registerEvents(this, this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new WorldListener(), this);
        pm.registerEvents(new SoulboundListener(), this);
        pm.registerEvents(new WandListener(this), this);
        pm.registerEvents(new CraftingListener(), this);
        pm.registerEvents(new ClassAbilityListener(this), this);
        pm.registerEvents(new BossListener(this), this);

        getCommand("nexusgrinder").setExecutor(new AnnihilationCommand(this));
        getCommand("class").setExecutor(new ClassCommand());
        getCommand("stats").setExecutor(new StatsCommand(statsManager));
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("vote").setExecutor(new VoteCommand(votingManager));
        getCommand("red").setExecutor(new TeamShortcutCommand());
        getCommand("green").setExecutor(new TeamShortcutCommand());
        getCommand("yellow").setExecutor(new TeamShortcutCommand());
        getCommand("blue").setExecutor(new TeamShortcutCommand());
        getCommand("distance").setExecutor(new DistanceCommand(this));
        getCommand("map").setExecutor(new MapCommand(this, mapLoader));

        BarUtil.init(this);

        if (pluginConfiguration.getString("stats").equalsIgnoreCase("sql"))
            useMysql = true;

        messageOfTheDay = pluginConfiguration.getBoolean("enableMotd", true);

        if (useMysql) {
            String mysqlHost = pluginConfiguration.getString("MySQL.host", "localhost");
            int mysqlPort = pluginConfiguration.getInt("MySQL.port", 3306);
            String mysqlDatabase = pluginConfiguration.getString("MySQL.database-name", "annihilation");
            mysqlTable = pluginConfiguration.getString("MySQL.table-name", "annihilation");
            String mysqlUser = pluginConfiguration.getString("MySQL.user", "annihilation");
            String mysqlPass = pluginConfiguration.getString("MySQL.pass", "myPassword");
            databaseManager = new DatabaseManager(mysqlHost, mysqlPort, mysqlDatabase, mysqlUser, mysqlPass, this);

            databaseManager.query("CREATE TABLE IF NOT EXISTS `" + mysqlTable + "` ( `username` varchar(16) NOT NULL, "
                    + "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
                    + "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
                    + "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        } else
            databaseManager = new DatabaseManager(this);

        if (getServer().getPluginManager().isPluginEnabled("Vault")) {
            if(!VaultHooks.getInstance().setupPermissions())
                getLogger().severe("Permission plugin not found. Unable to load vault.");
            if(!VaultHooks.getInstance().setupChat())
                getLogger().severe("Chat plugin not found. Unable load vault.");
        } else {
            getLogger().warning("Vault not found! Permissions features disabled.");
            VaultHooks.setVault(false);
        }

        setupServerForGame();

        for (Entity e : Bukkit.getWorld("lobby").getEntities()) {
            e.remove();
        }
        getLogger().info("Remove entities for NPC, success.");

        ChatUtil.setRoman(getConfig().getBoolean("roman", false));

        /*
        //sheep npc
        //BLUE
        double xblue = 11;
        double yblue = 5;
        double zblue = 0;
        Location locblue = new Location(Bukkit.getWorld("lobby"), xblue, yblue, zblue);
        //RED
        double xred = -11;
        double yred = 5;
        double zred = 0;
        Location locred = new Location(Bukkit.getWorld("lobby"), xred, yred, zred);
        //ORANGE
        double xorange = 0;
        double yorange = 5;
        double zorange = -11;
        Location locorange = new Location(Bukkit.getWorld("lobby"), xorange, yorange, zorange);
        //GREEN
        double xgreen = 0;
        double ygreen = 5;
        double zgreen = 10;
        Location locgreen = new Location(Bukkit.getWorld("lobby"), xgreen, ygreen, zgreen);



        // BLUE
        Sheep sblue = (Sheep)locblue.getWorld().spawnCreature(locblue, EntityType.SHEEP);
        sblue.setColor(DyeColor.BLUE);
        sblue.getAgeLock();
        sblue.teleport(locblue);
        sblue.isAdult();
        sblue.getLocation().setX(xblue);
        sblue.getLocation().setY(yblue);
        sblue.getLocation().setZ(zblue);
        sblue.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Blue sheep, spawned.");

        //RED
        Sheep sred = (Sheep)locred.getWorld().spawnCreature(locred, EntityType.SHEEP);
        sred.setColor(DyeColor.RED);
        sred.getAgeLock();
        sred.teleport(locred);
        sred.isAdult();
        sred.getLocation().setX(xred);
        sred.getLocation().setY(yred);
        sred.getLocation().setZ(zred);
        sred.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Red sheep, spawned.");

        //ORANGE
        Sheep sorange = (Sheep)locorange.getWorld().spawnCreature(locorange, EntityType.SHEEP);
        sorange.setColor(DyeColor.ORANGE);
        sorange.getAgeLock();
        sorange.teleport(locorange);
        sorange.isAdult();
        sorange.getLocation().setX(xorange);
        sorange.getLocation().setY(yorange);
        sorange.getLocation().setZ(zorange);
        sorange.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.YELLOW + " YELLOW TEAM" + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Yellow sheep, spawned.");

        //GREEN
        Sheep sgreen = (Sheep)locgreen.getWorld().spawnCreature(locgreen, EntityType.SHEEP);
        sgreen.setColor(DyeColor.GREEN);
        sgreen.getAgeLock();
        sgreen.teleport(locgreen);
        sgreen.isAdult();
        sgreen.getLocation().setX(xgreen);
        sgreen.getLocation().setY(ygreen);
        sgreen.getLocation().setZ(zgreen);
        sgreen.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + " GREEN TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + "0 " + "Players" );
        getLogger().info("Green sheep, spawned.");
         */
    }

    public YamlConfiguration getConfig() {
        return this.pluginConfiguration;
    }

    public HashMap<String, String> getMessagesConfig() {
        return messages;
    }

    /*
    @EventHandler
    public void sheepMoveEvent(EntityMoveEvent event) {


        if (event.getEntity() instanceof Sheep) {

            //BLUE
            double xblue = 11;
            double yblue = 5;
            double zblue = 0;
            Location locblue = new Location(Bukkit.getWorld("lobby"), xblue, yblue, zblue);
            //RED
            double xred = -11;
            double yred = 5;
            double zred = 0;
            Location locred = new Location(Bukkit.getWorld("lobby"), xred, yred, zred);
            //ORANGE
            double xorange = 0;
            double yorange = 5;
            double zorange = -11;
            Location locorange = new Location(Bukkit.getWorld("lobby"), xorange, yorange, zorange);
            //GREEN
            double xgreen = 0;
            double ygreen = 5;
            double zgreen = 10;
            Location locgreen = new Location(Bukkit.getWorld("lobby"), xgreen, ygreen, zgreen);

            Sheep sheep = (Sheep) event.getEntity();


            if (sheep.getColor() == DyeColor.BLUE) {
                sheep.teleport(locblue);
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (metas.getTeam() == GameTeam.BLUE)
                            size++;
                    }

                    if (size != 1) {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }
                }


            } else if (sheep.getColor() == DyeColor.RED) {
                sheep.teleport(locred);
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (metas.getTeam() == GameTeam.RED)
                            size++;
                    }

                    if (size != 1) {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }
                }


            } else if (sheep.getColor() == DyeColor.ORANGE) {
                sheep.teleport(locorange);
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (metas.getTeam() == GameTeam.YELLOW)
                            size++;
                    }

                    if (size != 1) {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.YELLOW + " YELLOW TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.YELLOW + " YELLOW TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }
                }
            } else if (sheep.getColor() == DyeColor.GREEN) {
                sheep.teleport(locgreen);
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (metas.getTeam() == GameTeam.GREEN)
                            size++;
                    }

                    if (size != 1) {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + " GREEN TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        sheep.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + " GREEN TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }
                }
            }


        }
    }

    @EventHandler
    public void sheepRightClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Sheep && event.getPlayer() instanceof Player) {
            Sheep s = (Sheep) event.getRightClicked();
            Player player = event.getPlayer();
            PlayerMeta meta = PlayerMeta.getMeta(player);
            GameTeam target;
            if (s.getColor() == DyeColor.BLUE) {
                player.performCommand("team blue");
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (meta.getTeam() == GameTeam.BLUE)
                            size++;
                    }

                    if (size != 1) {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.BLUE + " BLU TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }

                }

            } else if (s.getColor() == DyeColor.RED) {
                player.performCommand("team red");
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (meta.getTeam() == GameTeam.RED)
                            size++;
                    }

                    if (size != 1) {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.RED + " RED TEAM " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }

                }

            } else if (s.getColor() == DyeColor.GREEN) {
                player.performCommand("team green");
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (meta.getTeam() == GameTeam.GREEN)
                            size++;
                    }

                    if (size != 1) {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + " GREEN TEAM" + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.GREEN + " GREEN TEA " + ChatColor.DARK_GREEN + "<" + ChatColor.GREEN + "Join " + size + " Players");
                    }

                }
            } else if (s.getColor() == DyeColor.ORANGE) {
                player.performCommand("team yellow");
                for (GameTeam t : GameTeam.teams()) {
                    int size = 0;

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        PlayerMeta metas = PlayerMeta.getMeta(p);
                        if (meta.getTeam() == GameTeam.YELLOW)
                            size++;
                    }

                    if (size != 1) {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.YELLOW + " YELLOW TEAM " + ChatColor.DARK_GREEN + "< " + ChatColor.GREEN + "Join " + size + " Players" );
                    } else {
                        s.setCustomName(ChatColor.GREEN + "Join" + ChatColor.DARK_GREEN + ">" + ChatColor.YELLOW + " YELLOW TEAM " + ChatColor.DARK_GREEN + "< " + ChatColor.GREEN + "Join " + size + " Players");
                    }

                }
            }
        }
    }
     */

    //WATER DAMAGE



    public boolean startTimer() {
        if (phaseManager.isRunning())
            return false;

        phaseManager.start();

        return true;
    }

    public void loadMap(final String map) {
        FileConfiguration config = configManager.getConfig("maps.yml");
        ConfigurationSection section = config.getConfigurationSection(map);

        World w = getServer().getWorld(map);

        for (GameTeam team : GameTeam.teams()) {
            String name = team.name().toLowerCase();
            if (section.contains("spawns." + name)) {
                for (String s : section.getStringList("spawns." + name))
                    team.addSpawn(Util.parseLocation(getServer().getWorld(map),
                            s));
            }
            if (section.contains("nexuses." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("nexuses." + name));
                team.loadNexus(loc, 75);
            }
            if (section.contains("furnaces." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("furnaces." + name));
                enderFurnaceListener.setFurnaceLocation(team, loc);
                loc.getBlock().setType(Material.FURNACE);
            }
            if (section.contains("brewingstands." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("brewingstands." + name));
                enderBrewingStandListener.setBrewingStandLocation(team, loc);
                loc.getBlock().setType(Material.BREWING_STAND);
            }
            if (section.contains("enderchests." + name)) {
                Location loc = Util.parseLocation(w,
                        section.getString("enderchests." + name));
                enderChestListener.setEnderChestLocation(team, loc);
                loc.getBlock().setType(Material.ENDER_CHEST);
            }
        }

        if (section.contains("bosses")) {
            HashMap<String, Boss> bosses = new HashMap<String, Boss>();
            ConfigurationSection sec = section
                    .getConfigurationSection("bosses");
            for (String boss : sec.getKeys(false))
                bosses.put(
                        boss,
                        new Boss(boss, sec.getInt(boss + ".hearts") * 2, sec
                                .getString(boss + ".name"), Util.parseLocation(
                                w, sec.getString(boss + ".spawn")), Util
                                .parseLocation(w,
                                        sec.getString(boss + ".chest"))));
            bossManager.loadBosses(bosses);
        }

        if (section.contains("diamonds")) {
            Set<Location> diamonds = new HashSet<Location>();
            for (String s : section.getStringList("diamonds"))
                diamonds.add(Util.parseLocation(w, s));
            resourceListener.loadDiamonds(diamonds);
        }
    }

    public void startGame() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getPluginManager().callEvent(
                new GameStartEvent(mapManager.getCurrentMap()));
        scoreboardManager.scores.clear();

        for (OfflinePlayer score : scoreboardManager.sb.getPlayers())
            scoreboardManager.sb.resetScores(score);

        scoreboardManager.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
                + WordUtils.capitalize(votingManager.getWinner()));

        for (GameTeam t : GameTeam.teams()) {
            scoreboardManager.scores.put(t.name(), scoreboardManager.obj.getScore(Bukkit
                    .getOfflinePlayer(WordUtils.capitalize(t.name()
                            .toLowerCase() + " Nexus"))));
            scoreboardManager.scores.get(t.name()).setScore(t.getNexus().getHealth());

            Team sbt = scoreboardManager.sb.registerNewTeam(t.name() + "SB");
            sbt.addPlayer(Bukkit.getOfflinePlayer(WordUtils
                    .capitalize(WordUtils.capitalize(t.name().toLowerCase()
                            + " Nexus"))));
            sbt.setPrefix(t.color().toString());
        }

        scoreboardManager.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
                + WordUtils.capitalize(votingManager.getWinner()));

        for (Player p : getServer().getOnlinePlayers())
            if (PlayerMeta.getMeta(p).getTeam() != GameTeam.NONE)
                Util.sendPlayerToGame(p, this);

        scoreboardManager.update();

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    if (PlayerMeta.getMeta(p).getKit() == Kit.SCOUT) {
                        PlayerMeta.getMeta(p).getKit().addScoutParticles(p);
                    }
                }
            }
        }, 0L, 1200L);

        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            public void run() {
                for (GameTeam t : GameTeam.values()) {
                    if (t != GameTeam.NONE && t.getNexus().isAlive()) {
                        Location nexus = t.getNexus().getLocation().clone();
                        nexus.add(0.5, 0, 0.5);
                        Util.ParticleEffects.sendToLocation(Util.ParticleEffects.ENDER, nexus, 1F, 1F, 1F, 0, 20);
                        Util.ParticleEffects.sendToLocation(Util.ParticleEffects.ENCHANTMENT_TABLE, nexus, 1F, 1F, 1F, 0, 20);
                    }
                }
            }
        }, 100L, 5L);
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerSwim(PlayerMoveEvent e) {
        if (e.getTo().getBlock().getType().equals(Material.STATIONARY_WATER)) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1000, 10));
        }
    }

    public void advancePhase() {
        ChatUtil.phaseMessage(phaseManager.getPhase());

        if (phaseManager.getPhase() == 2)
            bossManager.spawnBosses();

        if (phaseManager.getPhase() == 3)
            resourceListener.spawnDiamonds();

        Bukkit.getPluginManager().callEvent(
                new PhaseChangeEvent(phaseManager.getPhase()));

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
        getSignHandler().updateSigns(GameTeam.GREEN);
        getSignHandler().updateSigns(GameTeam.YELLOW);
    }

    public void onSecond() {
        long time = phaseManager.getTime();

        if (time == -5L) {

            String winner = votingManager.getWinner();
            votingManager.end();
            getServer().broadcastMessage(ChatColor.GOLD + "Voting is now closed!");
            mapManager.selectMap(winner);
            getServer().broadcastMessage(
                    ChatColor.GREEN + WordUtils.capitalize(winner)
                            + " was chosen!");
            loadMap(winner);

            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 10, 1);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
            }
        }

        if (time <= -5L) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 10, 1);
                p.playSound(p.getLocation(), Sound.ENDERDRAGON_GROWL, 10, 1);
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 10, 2F);
                p.playSound(p.getLocation(), Sound.NOTE_BASS_GUITAR, 10, 2F);
                p.playSound(p.getLocation(), Sound.NOTE_PIANO, 10, 2F);
            }
        }

        if (time == 0L)
            startGame();
    }

    public int getPhase() {
        return phaseManager.getPhase();
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public DatabaseManager getDatabaseHandler() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public int getPhaseDelay() {
        return configManager.getConfig("config.yml").getInt("phase-period");
    }

    public void log(String m, Level l) {
        getLogger().log(l, m);
    }

    public VotingManager getVotingManager() {
        return votingManager;
    }

    public ScoreboardManager getScoreboardHandler() {
        return scoreboardManager;
    }

    public void endGame(GameTeam winner) {
        if (winner == null)
            return;

        ChatUtil.winMessage(winner);
        phaseManager.stop();

        for (Player p : getServer().getOnlinePlayers()) {
            if (PlayerMeta.getMeta(p).getTeam() == winner)
                statsManager.incrementStat(StatType.WINS, p);
        }

        long restartDelay = configManager.getConfig("config.yml").getLong(
                "restart-delay");
        RestartHandler rs = new RestartHandler(this, restartDelay);
        rs.start(phaseManager.getTime(), winner.getColor(winner));
    }

    public void setupServerForGame() {

        scoreboardManager.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
                + " | " + ChatColor.GOLD + "/vote <name>");

        mapManager.reset();
        PlayerMeta.reset();
        phaseManager.reset();

        for (Player p : getServer().getOnlinePlayers()) {
            PlayerMeta.getMeta(p).setTeam(GameTeam.NONE);
            p.teleport(mapManager.getLobbySpawnPoint());
            BarUtil.setMessageAndPercent(p, ChatColor.DARK_AQUA + "Welcome to NexusGrinder!", 0.01F);
            p.setMaxHealth(20D);
            p.setHealth(20D);
            p.setFoodLevel(20);
            p.setSaturation(20F);
        }

        votingManager.start();
        scoreboardManager.update();

        for (Player p : Bukkit.getOnlinePlayers()) {
            for (Player pp : Bukkit.getOnlinePlayers()) {
                p.showPlayer(pp);
                pp.showPlayer(p);
            }
        }

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            public void run() {
                for (Player p : getServer().getOnlinePlayers()) {
                    PlayerInventory inv = p.getInventory();
                    inv.setHelmet(null);
                    inv.setChestplate(null);
                    inv.setLeggings(null);
                    inv.setBoots(null);

                    p.getInventory().clear();

                    for (PotionEffect effect : p.getActivePotionEffects())
                        p.removePotionEffect(effect.getType());

                    p.setLevel(0);
                    p.setExp(0);
                    p.setSaturation(20F);

                    ItemStack selector = new ItemStack(Material.FEATHER);
                    ItemMeta itemMeta = selector.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.AQUA + "Right click to select class");
                    selector.setItemMeta(itemMeta);

                    p.getInventory().setItem(0, selector);

                    p.updateInventory();
                }

                for (GameTeam t : GameTeam.values())
                    if (t != GameTeam.NONE)
                        signManager.updateSigns(t);

                checkStarting();
            }
        }, 2L);
    }

    public void checkWin() {
        int alive = 0;
        GameTeam aliveTeam = null;
        for (GameTeam t : GameTeam.teams()) {
            if (t.getNexus().isAlive()) {
                alive++;
                aliveTeam = t;
            }
        }
        if (alive == 1) {
            endGame(aliveTeam);
        }
    }

    public SignManager getSignHandler() {
        return signManager;
    }

    public void setSignHandler(SignManager sign) {
        this.signManager = sign;
    }

    public void checkStarting() {
        if (!phaseManager.isRunning()) {
            if (Bukkit.getOnlinePlayers().size() >= getConfig().getInt(
                    "requiredToStart"))
                phaseManager.start();
        }
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public PhaseManager getPhaseManager() {
        return phaseManager;
    }

    public void listTeams(CommandSender sender) {
        sender.sendMessage(ChatColor.GRAY + "============[ "
                + ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
                + " ]============");
        for (GameTeam t : GameTeam.teams()) {
            int size = 0;

            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerMeta meta = PlayerMeta.getMeta(p);
                if (meta.getTeam() == t)
                    size++;
            }

            if (size != 1) {
                sender.sendMessage(t.coloredName() + " - " + size + " " + get("INFO_TEAM_LIST_PLAYERS") + get("DYNAMIC_S"));
            } else {
                sender.sendMessage(t.coloredName() + " - " + size + " " + get("INFO_TEAM_LIST_PLAYERS"));
            }
        }
        sender.sendMessage(ChatColor.GRAY + "===============================");
    }

    public void joinTeam(Player player, String team) {
        PlayerMeta meta = PlayerMeta.getMeta(player);
        if (meta.getTeam() != GameTeam.NONE && !player.hasPermission("annihilation.bypass.teamlimitor")) {
            player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.DARK_AQUA + get("ERROR_PLAYER_NOSWITCHTEAM"));
            return;
        }

        GameTeam target;
        try {
            target = GameTeam.valueOf(team.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.RED + get("ERROR_GAME_INVALIDTEAM"));
            listTeams(player);
            return;
        }

        if (Util.isTeamTooBig(target)
                && !player.hasPermission("annihilation.bypass.teamlimit")) {
            player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.RED + get("ERROR_GAME_TEAMFULL"));
            return;
        }

        if (target.getNexus() != null) {
            if (target.getNexus().getHealth() == 0 && getPhase() > 1) {
                player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.RED + get("ERROR_GAME_TEAMNONEXUS"));
                return;
            }
        }

        if (getPhase() > lastJoinPhase
                && !player.hasPermission("annhilation.bypass.phaselimiter")) {
            player.kickPlayer(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.RED
                    + "You cannot join the game in this phase!");
            return;
        }

        player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.DARK_AQUA + "You joined "
                + target.coloredName());
        meta.setTeam(target);

        getScoreboardHandler().teams.get(team.toUpperCase()).addPlayer(
                player);

        if (getPhase() > 0) {
            Util.sendPlayerToGame(player, this);
        }

        getSignHandler().updateSigns(GameTeam.RED);
        getSignHandler().updateSigns(GameTeam.BLUE);
        getSignHandler().updateSigns(GameTeam.GREEN);
        getSignHandler().updateSigns(GameTeam.YELLOW);
    }
}
