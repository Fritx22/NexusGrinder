package me.morpig.NexusGrinder;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

import de.kumpelblase2.remoteentities.api.features.RemoteTradingFeature;
import de.kumpelblase2.remoteentities.api.features.TradeOffer;
import me.morpig.NexusGrinder.api.GameStartEvent;
import me.morpig.NexusGrinder.api.PhaseChangeEvent;
import me.morpig.NexusGrinder.bar.BarUtil;
import me.morpig.NexusGrinder.chat.ChatListener;
import me.morpig.NexusGrinder.chat.ChatUtil;
import me.morpig.NexusGrinder.commands.NexusGrinderCommand;
import me.morpig.NexusGrinder.commands.ClassCommand;
import me.morpig.NexusGrinder.commands.DistanceCommand;
import me.morpig.NexusGrinder.commands.MapCommand;
import me.morpig.NexusGrinder.commands.StatsCommand;
import me.morpig.NexusGrinder.commands.TeamCommand;
import me.morpig.NexusGrinder.commands.TeamShortcutCommand;
import me.morpig.NexusGrinder.commands.VoteCommand;
import me.morpig.NexusGrinder.listeners.BossListener;
import me.morpig.NexusGrinder.listeners.ClassAbilityListener;
import me.morpig.NexusGrinder.listeners.CraftingListener;
import me.morpig.NexusGrinder.listeners.EnderBrewingStandListener;
import me.morpig.NexusGrinder.listeners.EnderChestListener;
import me.morpig.NexusGrinder.listeners.EnderFurnaceListener;
import me.morpig.NexusGrinder.listeners.PlayerListener;
import me.morpig.NexusGrinder.listeners.ResourceListener;
import me.morpig.NexusGrinder.listeners.SoulboundListener;
import me.morpig.NexusGrinder.listeners.WandListener;
import me.morpig.NexusGrinder.listeners.WorldListener;
import me.morpig.NexusGrinder.manager.BossManager;
import me.morpig.NexusGrinder.manager.ConfigManager;
import me.morpig.NexusGrinder.manager.DatabaseManager;
import me.morpig.NexusGrinder.manager.PhaseManager;
import me.morpig.NexusGrinder.manager.RestartHandler;
import me.morpig.NexusGrinder.manager.ScoreboardManager;
import me.morpig.NexusGrinder.manager.SignManager;
import me.morpig.NexusGrinder.maps.MapLoader;
import me.morpig.NexusGrinder.maps.MapManager;
import me.morpig.NexusGrinder.maps.VotingManager;
import me.morpig.NexusGrinder.object.Boss;
import me.morpig.NexusGrinder.object.GameTeam;
import me.morpig.NexusGrinder.object.Kit;
import me.morpig.NexusGrinder.object.PlayerMeta;
import me.morpig.NexusGrinder.object.Shop;
import me.morpig.NexusGrinder.stats.StatType;
import me.morpig.NexusGrinder.stats.StatsManager;

import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import de.kumpelblase2.remoteentities.api.features.RemoteTamingFeature;
import de.kumpelblase2.remoteentities.api.features.TamingFeature;
import de.kumpelblase2.remoteentities.api.thinking.goals.DesireFollowTamer;
import de.kumpelblase2.remoteentities.api.thinking.InteractBehavior;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

public final class NexusGrinder extends JavaPlugin implements Listener {
	private ConfigManager configManager;
	private VotingManager voting;
	private MapManager maps;
	private PhaseManager timer;
	private ResourceListener resources;
	private EnderFurnaceListener enderFurnaces;
	private EnderBrewingStandListener enderBrewingStands;
	private EnderChestListener enderChests;
	private StatsManager stats;
	private SignManager sign;
	private ScoreboardManager sb;
	private DatabaseManager db;
	private BossManager boss;
    private static GameTeam team;
    private EntityManager npcManager;
    private Location NPCSpawn;
	
	public boolean useMysql = false;
	public boolean updateAvailable = false;
	public String newVersion;


	public int build = 1;
	public int respawn = 10;
	
	@Override
	public void onEnable() {

        getLogger().info("#######################################");
        getLogger().info("            Nexus Grinder              ");
        getLogger().info("                v.12                   ");
        getLogger().info("#######################################");

		configManager = new ConfigManager(this);
		configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml",
				"stats.yml");
		
		MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

		maps = new MapManager(this, mapLoader, configManager.getConfig("maps.yml"));
        getLogger().info("Register " + maps.getRandomMaps() +"map");


        //npc
        Bukkit.getPluginManager().registerEvents(this, this);
        this.npcManager = RemoteEntities.createManager(this);






		Configuration shops = configManager.getConfig("shops.yml");
		new Shop(this, "Weapon", shops);
		new Shop(this, "Brewing", shops);

		stats = new StatsManager(this, configManager);
		resources = new ResourceListener(this);
		enderFurnaces = new EnderFurnaceListener(this);
		enderBrewingStands = new EnderBrewingStandListener(this);
		enderChests = new EnderChestListener();
		sign = new SignManager(this);
		Configuration config = configManager.getConfig("config.yml");
		timer = new PhaseManager(this, config.getInt("start-delay"),
				config.getInt("phase-period"));
		voting = new VotingManager(this);
		sb = new ScoreboardManager();
		boss = new BossManager(this);
		
		PluginManager pm = getServer().getPluginManager();

		sign.loadSigns();

		sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
				+ " | " + ChatColor.GOLD + "/vote <name>");

		build = this.getConfig().getInt("build", 5);
		respawn = this.getConfig().getInt("bossRespawnDelay", 10);
		
		pm.registerEvents(resources, this);
		pm.registerEvents(enderFurnaces, this);
		pm.registerEvents(enderBrewingStands, this);
		pm.registerEvents(enderChests, this);
		pm.registerEvents(new ChatListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new WorldListener(), this);
		pm.registerEvents(new SoulboundListener(), this);
		pm.registerEvents(new WandListener(this), this);
		pm.registerEvents(new CraftingListener(), this);
		pm.registerEvents(new ClassAbilityListener(this), this);
		pm.registerEvents(new BossListener(this), this);
		
		getCommand("nexusgrinder").setExecutor(new NexusGrinderCommand(this));
		getCommand("class").setExecutor(new ClassCommand());
		getCommand("stats").setExecutor(new StatsCommand(stats));
		getCommand("team").setExecutor(new TeamCommand(this));
		getCommand("vote").setExecutor(new VoteCommand(voting));
		getCommand("red").setExecutor(new TeamShortcutCommand());
		getCommand("green").setExecutor(new TeamShortcutCommand());
		getCommand("yellow").setExecutor(new TeamShortcutCommand());
		getCommand("blue").setExecutor(new TeamShortcutCommand());
		getCommand("distance").setExecutor(new DistanceCommand(this));
		getCommand("map").setExecutor(new MapCommand(this, mapLoader));



		BarUtil.init(this);

		if (config.getString("stats").equalsIgnoreCase("sql"))
			useMysql = true;

		if (useMysql) {
			String host = config.getString("MySQL.host");
			Integer port = config.getInt("MySQL.port");
			String name = config.getString("MySQL.name");
			String user = config.getString("MySQL.user");
			String pass = config.getString("MySQL.pass");
			db = new DatabaseManager(host, port, name, user, pass, this);

			db.query("CREATE TABLE IF NOT EXISTS `nexusgrinder` ( `username` varchar(16) NOT NULL, "
					+ "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
					+ "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
					+ "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		} else
			db = new DatabaseManager(this);

		reset();

		ChatUtil.setRoman(getConfig().getBoolean("roman", false));
	}

	public boolean startTimer() {
		if (timer.isRunning())
			return false;

		timer.start();

		return true;
	}

    private Location parseLocation(String in) {
        String[] params = in.split(",");
        if (params.length == 3 || params.length == 5) {
            double x = Double.parseDouble(params[0]);
            double y = Double.parseDouble(params[1]);
            double z = Double.parseDouble(params[2]);
            Location loc = new Location(Bukkit.getWorld("lobby"), x, y, z);
            if (params.length == 5) {
                loc.setYaw(Float.parseFloat(params[3]));
                loc.setPitch(Float.parseFloat(params[4]));
            }
            return loc;
        }
        return null;
    }

    @EventHandler
    public void onNPCJoin(PlayerJoinEvent inEvent) throws Exception {
         double x = -16;
         double y = 5;
         double z = -3;
         Location loc = new Location(Bukkit.getWorld("lobby"), x, y, z);


        RemoteEntity entity = npcManager.createNamedEntity(RemoteEntityType.Human, loc, ChatColor.GOLD + "Join Team Blue", true);



        entity.setStationary(true);



        //set new mind
        entity.getMind().addBehaviour(new NPCBehaviour(entity));


    }



    @EventHandler
    public void onPlayerInteractWithEntityEvent(PlayerInteractEntityEvent interactEvent)
    {
        if (!(interactEvent.getRightClicked() instanceof LivingEntity))
        {
            return;
        }

        LivingEntity livingEntity = (LivingEntity)interactEvent.getRightClicked();

        if (RemoteEntities.isRemoteEntity(livingEntity))
        {

            RemoteEntity remoteEntity = RemoteEntities.getRemoteEntityFromEntity(livingEntity);

            if (remoteEntity.getMind().hasBehaviour("Interact"))
            {
                InteractBehavior interactBehavior = (InteractBehavior)remoteEntity.getMind().getBehaviour("Interact");
                if (interactBehavior instanceof NPCBehaviour) {
                    NPCBehaviour npcBehaviours = (NPCBehaviour)interactBehavior;
                    npcBehaviours.onRightClickInteractEventWithPlayer(interactEvent.getPlayer());
                }
            }
        }
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
				enderFurnaces.setFurnaceLocation(team, loc);
				loc.getBlock().setType(Material.FURNACE);
			}
			if (section.contains("brewingstands." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("brewingstands." + name));
				enderBrewingStands.setBrewingStandLocation(team, loc);
				loc.getBlock().setType(Material.BREWING_STAND);
			}
			if (section.contains("enderchests." + name)) {
				Location loc = Util.parseLocation(w,
						section.getString("enderchests." + name));
				enderChests.setEnderChestLocation(team, loc);
				loc.getBlock().setType(Material.ENDER_CHEST);
			}
		}
		
		if (section.contains("bosses")) {
			HashMap<String, Boss> bosses = new HashMap<String, Boss>();
			ConfigurationSection sec = section.getConfigurationSection("bosses");
			for (String boss : sec.getKeys(false))
				bosses.put(boss, 
				new Boss(boss, sec.getInt(boss + ".hearts") * 2, sec.getString(boss + ".name"), 
				Util.parseLocation(w, sec.getString(boss + ".spawn")), 
				Util.parseLocation(w, sec.getString(boss + ".chest"))));
			boss.loadBosses(bosses);
		}

		if (section.contains("diamonds")) {
			Set<Location> diamonds = new HashSet<Location>();
			for (String s : section.getStringList("diamonds"))
				diamonds.add(Util.parseLocation(w, s));
			resources.loadDiamonds(diamonds);
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
				new GameStartEvent(maps.getCurrentMap()));
		sb.scores.clear();

		for (OfflinePlayer score : sb.sb.getPlayers())
			sb.sb.resetScores(score);

		sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
				+ WordUtils.capitalize(voting.getWinner()));

		for (GameTeam t : GameTeam.teams()) {
			sb.scores.put(t.name(), sb.obj.getScore(Bukkit
					.getOfflinePlayer(WordUtils.capitalize(t.name()
							.toLowerCase() + " Nexus"))));
			sb.scores.get(t.name()).setScore(t.getNexus().getHealth());

			Team sbt = sb.sb.registerNewTeam(t.name() + "SB");
			sbt.addPlayer(Bukkit.getOfflinePlayer(WordUtils
					.capitalize(WordUtils.capitalize(t.name().toLowerCase()
							+ " Nexus"))));
			sbt.setPrefix(t.color().toString());
		}

		sb.obj.setDisplayName(ChatColor.DARK_AQUA + "Map: "
				+ WordUtils.capitalize(voting.getWinner()));

		for (Player p : getServer().getOnlinePlayers())
			if (PlayerMeta.getMeta(p).getTeam() != GameTeam.NONE)
				Util.sendPlayerToGame(p);

		sb.update();

		getServer().getScheduler().runTaskTimer(this, new Runnable() {
			@Override
			public void run() {
				for (Player p : getServer().getOnlinePlayers()) {
					if (PlayerMeta.getMeta(p).getKit() == Kit.SCOUT) {
						PlayerMeta.getMeta(p).getKit().addScoutParticles(p);
					}
				}
			}
		}, 0L, 1200L);
	}

	public void advancePhase() {
		ChatUtil.phaseMessage(timer.getPhase());
		
		if (timer.getPhase() == 2)
			boss.spawnBosses();
		
		if (timer.getPhase() == 3)
			resources.spawnDiamonds();
		
		Bukkit.getPluginManager().callEvent(
				new PhaseChangeEvent(timer.getPhase()));

		getSignHandler().updateSigns(GameTeam.RED);
		getSignHandler().updateSigns(GameTeam.BLUE);
		getSignHandler().updateSigns(GameTeam.GREEN);
		getSignHandler().updateSigns(GameTeam.YELLOW);
	}

	public void onSecond() {
		long time = timer.getTime();

		if (time == -5L) {
			String winner = voting.getWinner();
			maps.selectMap(winner);
			getServer().broadcastMessage(
					ChatColor.GREEN + WordUtils.capitalize(winner)
							+ " was chosen!");
			loadMap(winner);

			voting.end();
		}

		if (time == 0L)
			startGame();
	}

	public int getPhase() {
		return timer.getPhase();
	}

	public MapManager getMapManager() {
		return maps;
	}

	public StatsManager getStatsManager() {
		return stats;
	}

	public DatabaseManager getDatabaseHandler() {
		return db;
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
		return voting;
	}

	public ScoreboardManager getScoreboardHandler() {
		return sb;
	}

	public void endGame(GameTeam winner) {
		if (winner == null)
			return;

		ChatUtil.winMessage(winner);
		timer.stop();

		for (Player p : getServer().getOnlinePlayers())
			if (PlayerMeta.getMeta(p).getTeam() == winner)
				stats.incrementStat(StatType.WINS, p);
		long restartDelay = configManager.getConfig("config.yml").getLong(
				"restart-delay");
		new RestartHandler(this, restartDelay).start(timer.getTime());
	}

	public void reset() {
		sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
				+ " | " + ChatColor.GOLD + "/vote <name>");
		maps.reset();
		timer.reset();
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerMeta.getMeta(p).setTeam(GameTeam.NONE);
			p.teleport(maps.getLobbySpawnPoint());
			BarUtil.setMessageAndPercent(p, ChatColor.GOLD
					+ "Welcome to NexusGrinder!", 0.01F);
			p.setMaxHealth(20D);
			p.setHealth(20D);
			p.setFoodLevel(20);
			p.setSaturation(20F);
		}

		voting.start();
		sb.update();
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			for (Player pp : Bukkit.getOnlinePlayers()) {
				p.showPlayer(pp);
				pp.showPlayer(p);
			}
		}
		
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				for (Player p : getServer().getOnlinePlayers()) {
					PlayerInventory inv = p.getInventory();
					inv.setHelmet(null);
					inv.setChestplate(null);
					inv.setLeggings(null);
					inv.setBoots(null);
					
					p.getInventory().clear();
					
					for(PotionEffect effect : p.getActivePotionEffects())
						p.removePotionEffect(effect.getType());
					
					p.setLevel(0);
					p.setExp(0);
					p.setSaturation(20F);
					
					ItemStack selector = new ItemStack(Material.FEATHER);
					ItemMeta itemMeta = selector.getItemMeta();
					itemMeta.setDisplayName(ChatColor.AQUA
							+ "Right click to select class");
					selector.setItemMeta(itemMeta);

					p.getInventory().setItem(0, selector);
					
					p.updateInventory();
				}
				
				for (GameTeam t : GameTeam.values())
					if (t != GameTeam.NONE) sign.updateSigns(t);
				
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
		return sign;
	}

	public void setSignHandler(SignManager sign) {
		this.sign = sign;
	}

	public static class Util {
		public static Location parseLocation(World w, String in) {
			String[] params = in.split(",");
			if (params.length == 3 || params.length == 5) {
				double x = Double.parseDouble(params[0]);
				double y = Double.parseDouble(params[1]);
				double z = Double.parseDouble(params[2]);
				Location loc = new Location(w, x, y, z);
				if (params.length == 5) {
					loc.setYaw(Float.parseFloat(params[4]));
					loc.setPitch(Float.parseFloat(params[5]));
				}
				return loc;
			}
			return null;
		}

		public static void sendPlayerToGame(Player player) {
			PlayerMeta meta = PlayerMeta.getMeta(player);
			if (meta.getTeam() != null) {
				meta.setAlive(true);
				player.teleport(meta.getTeam().getRandomSpawn());
				meta.getKit().give(player, meta.getTeam());
				player.setCompassTarget(meta.getTeam().getNexus().getLocation());
				player.setGameMode(GameMode.ADVENTURE);
				player.setHealth(player.getMaxHealth());
				player.setFoodLevel(20);
				player.setSaturation(20F);
			}
		}

		public static boolean isEmptyColumn(Location loc) {
			boolean hasBlock = false;
			Location test = loc.clone();
			for (int y = 0; y < loc.getWorld().getMaxHeight(); y++) {
				test.setY(y);
				if (test.getBlock().getType() != Material.AIR)
					hasBlock = true;
			}
			return !hasBlock;
		}
		
		public static void showClassSelector(Player player, String title) {
			int size = ((Kit.values().length + 8) / 9) * 9;
			Inventory inv = Bukkit.createInventory(null, size, title);
			for (Kit kit : Kit.values())
				inv.addItem(kit.getIcon());
			player.openInventory(inv);
		}
	}

	public void checkStarting() {
		if (!timer.isRunning()) {
			if (Bukkit.getOnlinePlayers().length >= getConfig().getInt("requiredToStart"))
				timer.start();

	  }
    }



	public BossManager getBossManager() {
		return boss;
	}
}
