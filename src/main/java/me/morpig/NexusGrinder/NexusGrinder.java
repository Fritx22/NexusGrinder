package me.morpig.NexusGrinder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import me.morpig.NexusGrinder.Updater.UpdateResult;
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
import me.morpig.NexusGrinder.maps.MapLoader;
import me.morpig.NexusGrinder.maps.MapManager;
import me.morpig.NexusGrinder.maps.VotingManager;
import me.morpig.NexusGrinder.stats.DatabaseHandler;
import me.morpig.NexusGrinder.stats.StatType;
import me.morpig.NexusGrinder.stats.StatsManager;

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
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Team;

public final class NexusGrinder extends JavaPlugin {
	private ConfigManager configManager;
	private VotingManager voting;
	private MapManager maps;
	private PhaseTimer timer;
	private ResourceListener resources;
	private EnderFurnaceListener enderFurnaces;
	private EnderBrewingStandListener enderBrewingStands;
	private EnderChestListener enderChests;
	private StatsManager stats;
	private SignHandler sign;
	private ScoreboardHandler sb;
	private DatabaseHandler db;
	public boolean useMysql = false;
	public boolean updateAvailable = false;
	public String newVersion;

	public int build = 1;
	
	@Override
	public void onEnable() {
		try {
		    Metrics metrics = new Metrics(this);
		    metrics.start();
		} catch (IOException e) {
		    
		}

		configManager = new ConfigManager(this);
		configManager.loadConfigFiles("config.yml", "maps.yml", "shops.yml",
				"stats.yml");
		
		MapLoader mapLoader = new MapLoader(getLogger(), getDataFolder());

		maps = new MapManager(this, mapLoader, configManager.getConfig("maps.yml"));

		Configuration shops = configManager.getConfig("shops.yml");
		new Shop(this, "Weapon", shops);
		new Shop(this, "Brewing", shops);

		stats = new StatsManager(this, configManager);
		resources = new ResourceListener(this);
		enderFurnaces = new EnderFurnaceListener();
		enderBrewingStands = new EnderBrewingStandListener();
		enderChests = new EnderChestListener();
		sign = new SignHandler(this);
		Configuration config = configManager.getConfig("config.yml");
		timer = new PhaseTimer(this, config.getInt("start-delay"),
				config.getInt("phase-period"));
		voting = new VotingManager(this);
		sb = new ScoreboardHandler();

		PluginManager pm = getServer().getPluginManager();

		sign.loadSigns();

		sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
				+ " | " + ChatColor.GOLD + "/vote <name>");

		build = this.getConfig().getInt("build", 1);
		
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
			db = new DatabaseHandler(host, port, name, user, pass, this);

			db.query("CREATE TABLE IF NOT EXISTS `nexusgrinder` ( `username` varchar(16) NOT NULL, "
					+ "`kills` int(16) NOT NULL, `deaths` int(16) NOT NULL, `wins` int(16) NOT NULL, "
					+ "`losses` int(16) NOT NULL, `nexus_damage` int(16) NOT NULL, "
					+ "UNIQUE KEY `username` (`username`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1;");
		} else
			db = new DatabaseHandler(this);

		reset();

		ChatUtil.setRoman(getConfig().getBoolean("roman", false));
	}

	public boolean startTimer() {
		if (timer.isRunning())
			return false;

		timer.start();

		return true;
	}

	public void loadMap(final String map) {
		FileConfiguration config = configManager.getConfig("maps.yml");
		ConfigurationSection section = config.getConfigurationSection(map);

		World w = getServer().getWorld(map);

		for (NexusGrinderTeam team : NexusGrinderTeam.teams()) {
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

		for (NexusGrinderTeam t : NexusGrinderTeam.teams()) {
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
			if (PlayerMeta.getMeta(p).getTeam() != NexusGrinderTeam.NONE)
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
		if (timer.getPhase() == 3)
			resources.spawnDiamonds();
		Bukkit.getPluginManager().callEvent(
				new PhaseChangeEvent(timer.getPhase()));

		getSignHandler().updateSigns(NexusGrinderTeam.RED);
		getSignHandler().updateSigns(NexusGrinderTeam.BLUE);
		getSignHandler().updateSigns(NexusGrinderTeam.GREEN);
		getSignHandler().updateSigns(NexusGrinderTeam.YELLOW);
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

	public DatabaseHandler getDatabaseHandler() {
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

	public ScoreboardHandler getScoreboardHandler() {
		return sb;
	}

	public void endGame(NexusGrinderTeam winner) {
		if (winner == null)
			return;

		ChatUtil.winMessage(winner);
		timer.stop();

		for (Player p : getServer().getOnlinePlayers())
			if (PlayerMeta.getMeta(p).getTeam() == winner)
				stats.incrementStat(StatType.WINS, p);
		long restartDelay = configManager.getConfig("config.yml").getLong(
				"restart-delay");
		new RestartTimer(this, restartDelay).start(timer.getTime());
	}

	public void reset() {
		sb.resetScoreboard(ChatColor.DARK_AQUA + "Voting" + ChatColor.WHITE
				+ " | " + ChatColor.GOLD + "/vote <name>");
		maps.reset();
		timer.reset();
		for (Player p : getServer().getOnlinePlayers()) {
			PlayerMeta.getMeta(p).setTeam(NexusGrinderTeam.NONE);
			p.teleport(maps.getLobbySpawnPoint());
			BarUtil.setMessageAndPercent(p, ChatColor.DARK_AQUA
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
				
				for (NexusGrinderTeam t : NexusGrinderTeam.values())
					if (t != NexusGrinderTeam.NONE) sign.updateSigns(t);
				
				checkStarting();
			}
		}, 2L);
	}

	public void checkWin() {
		int alive = 0;
		NexusGrinderTeam aliveTeam = null;
		for (NexusGrinderTeam t : NexusGrinderTeam.teams()) {
			if (t.getNexus().isAlive()) {
				alive++;
				aliveTeam = t;
			}
		}
		if (alive == 1) {
			endGame(aliveTeam);
		}
	}

	public SignHandler getSignHandler() {
		return sign;
	}

	public void setSignHandler(SignHandler sign) {
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
		
		public static void showClassSelector(Player player) {
			int size = ((Kit.values().length + 8) / 9) * 9;
			Inventory inv = Bukkit.createInventory(null, size, "Class Selector");
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
}
