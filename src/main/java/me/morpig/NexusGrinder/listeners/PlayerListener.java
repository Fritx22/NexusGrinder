package me.morpig.NexusGrinder.listeners;

import java.util.HashMap;

import me.morpig.NexusGrinder.NexusGrinder;
import me.morpig.NexusGrinder.api.NexusDamageEvent;
import me.morpig.NexusGrinder.api.NexusDestroyEvent;
import me.morpig.NexusGrinder.bar.BarUtil;
import me.morpig.NexusGrinder.chat.ChatUtil;
import me.morpig.NexusGrinder.object.GameTeam;
import me.morpig.NexusGrinder.object.Kit;
import me.morpig.NexusGrinder.object.PlayerMeta;
import me.morpig.NexusGrinder.stats.StatType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class PlayerListener implements Listener {
	private final NexusGrinder plugin;

	private HashMap<String, Kit> kitsToGive = new HashMap<String, Kit>();

	public PlayerListener(NexusGrinder plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player player = e.getPlayer();
		PlayerMeta pmeta = PlayerMeta.getMeta(player);
		Action a = e.getAction();
		if (a == Action.RIGHT_CLICK_AIR || a == Action.RIGHT_CLICK_BLOCK) {
			ItemStack handItem = player.getItemInHand();
			if (handItem != null) {
				if (handItem.getType() == Material.FEATHER) {
					if (handItem.getItemMeta().hasDisplayName()) {
						if (handItem.getItemMeta().getDisplayName()
								.contains("Right click to select class")) {
							NexusGrinder.Util.showClassSelector(e.getPlayer(),
									"Select Class");
							return;
						}
					}
				}
				if (handItem.getType() == Material.COMPASS) {
					boolean setCompass = false;
					boolean setToNext = false;
					while (!setCompass) {
						for (GameTeam team : GameTeam.teams()) {
							if (setToNext) {
								ItemMeta meta = handItem.getItemMeta();
								meta.setDisplayName(team.color()
										+ "Pointing to " + team.toString()
										+ " Nexus");
								handItem.setItemMeta(meta);
								player.setCompassTarget(team.getNexus()
										.getLocation());
								setCompass = true;
								break;
							}
							if (handItem.getItemMeta().getDisplayName()
									.contains(team.toString()))
								setToNext = true;
						}
					}
				}
			}
		}

		if (e.getClickedBlock() != null) {
			Material clickedType = e.getClickedBlock().getType();
			if (clickedType == Material.SIGN_POST
					|| clickedType == Material.WALL_SIGN) {
				Sign s = (Sign) e.getClickedBlock().getState();
				if (s.getLine(0).contains(ChatColor.DARK_PURPLE + "[Team]")) {
					String teamName = ChatColor.stripColor(s.getLine(1));
					GameTeam team = GameTeam.valueOf(teamName
							.toUpperCase());
					if (team != null) {
						if (pmeta.getTeam() == GameTeam.NONE) {
							if (team.getNexus() != null) {
								if (team.getNexus().getHealth() == 0
										&& plugin.getPhase() > 1) {
									player.sendMessage(ChatColor.RED
											+ "You cannot join a team without a Nexus!");
									return;
								}
							}

							pmeta.setTeam(team);
							plugin.getScoreboardHandler().teams
									.get(team.name()).addPlayer(player);
							player.sendMessage(ChatColor.DARK_AQUA
									+ "You joined " + team.coloredName());
							if (plugin.getPhase() > 0)
								NexusGrinder.Util.sendPlayerToGame(player);
						} else {
							player.sendMessage(ChatColor.DARK_AQUA
									+ "You are already on "
									+ pmeta.getTeam().coloredName());
						}

						plugin.getSignHandler().updateSigns(team);
					}
				}
			}
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player player = e.getPlayer();
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.isAlive()) {
			if (kitsToGive.containsKey(e.getPlayer().getName())) {
				meta.setKit(kitsToGive.get(e.getPlayer().getName()));
				kitsToGive.remove(e.getPlayer().getName());
			}
			e.setRespawnLocation(meta.getTeam().getRandomSpawn());
			meta.getKit().give(player, meta.getTeam());
		} else {
			e.setRespawnLocation(plugin.getMapManager().getLobbySpawnPoint());
			ItemStack selector = new ItemStack(Material.FEATHER);
			ItemMeta itemMeta = selector.getItemMeta();
			itemMeta.setDisplayName(ChatColor.AQUA
					+ "Right click to select class");
			selector.setItemMeta(itemMeta);

			player.getInventory().setItem(0, selector);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		String prefix = ChatColor.AQUA + "[NexusGrinder] " + ChatColor.GRAY;
		Player player = e.getPlayer();

		PlayerMeta meta = PlayerMeta.getMeta(player);

		if (player.hasPermission("annihilation.misc.updatenotify")
				&& plugin.updateAvailable) {
			player.sendMessage(prefix
					+ ChatColor.GOLD
					+ "An update is available! Please restart the server to apply this update.");
			player.sendMessage(prefix + "Current Version: " + ChatColor.WHITE
					+ plugin.getDescription().getVersion()
					+ ChatColor.DARK_GRAY + " | " + ChatColor.GRAY
					+ "Newest Version: " + ChatColor.WHITE + plugin.newVersion);
		}

		if (meta.isAlive())
			player.teleport(meta.getTeam().getRandomSpawn());
		else {
			player.teleport(plugin.getMapManager().getLobbySpawnPoint());
			PlayerInventory inv = player.getInventory();
			inv.setHelmet(null);
			inv.setChestplate(null);
			inv.setLeggings(null);
			inv.setBoots(null);

			player.getInventory().clear();

			for (PotionEffect effect : player.getActivePotionEffects())
				player.removePotionEffect(effect.getType());

			player.setLevel(0);
			player.setExp(0);
			player.setSaturation(20F);

			ItemStack selector = new ItemStack(Material.FEATHER);
			ItemMeta itemMeta = selector.getItemMeta();
			itemMeta.setDisplayName(ChatColor.AQUA
					+ "Right click to select class");
			selector.setItemMeta(itemMeta);

			player.getInventory().setItem(0, selector);

			player.updateInventory();
		}

		if (plugin.useMysql)
			plugin.getDatabaseHandler()
					.query("INSERT IGNORE INTO `annihilation` (`username`, `kills`, "
							+ "`deaths`, `wins`, `losses`, `nexus_damage`) VALUES "
							+ "('"
							+ player.getName()
							+ "', '0', '0', '0', '0', '0');");

		if (plugin.getPhase() == 0 && plugin.getVotingManager().isRunning()) {
			BarUtil.setMessageAndPercent(player, ChatColor.GOLD
					+ "Welcome to NexusGrinder!", 0.01f);
			plugin.checkStarting();
		}

		plugin.getSignHandler().updateSigns(meta.getTeam());
		plugin.getScoreboardHandler().update();
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		if (plugin.getPhase() > 0) {
			PlayerMeta meta = PlayerMeta.getMeta(p);
			if (!meta.getTeam().getNexus().isAlive()) {
				meta.setAlive(false);
				for (Player pp : Bukkit.getOnlinePlayers())
					pp.hidePlayer(p);
			}
		}

		plugin.getStatsManager().setValue(StatType.DEATHS, p,
				plugin.getStatsManager().getStat(StatType.DEATHS, p) + 1);

		if (p.getKiller() != null && !p.getKiller().equals(p)) {
			Player killer = p.getKiller();
			plugin.getStatsManager().incrementStat(StatType.KILLS, killer);
			e.setDeathMessage(ChatUtil.formatDeathMessage(p, p.getKiller(),
					e.getDeathMessage()));

			if (PlayerMeta.getMeta(killer).getKit() == Kit.BERSERKER) {
				addHeart(killer);
			}
		} else
			e.setDeathMessage(ChatUtil.formatDeathMessage(p,
					e.getDeathMessage()));
		e.setDroppedExp(p.getTotalExperience());
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (e.getEntity().getWorld().getName().equals("lobby")) {
				e.setCancelled(true);

				if (e.getCause() == DamageCause.VOID)
					e.getEntity().teleport(
							plugin.getMapManager().getLobbySpawnPoint());
			}
		}
	}

	@EventHandler
	public void onPlayerPortal(PlayerPortalEvent e) {
		Player player = e.getPlayer();
		NexusGrinder.Util.showClassSelector(player, "Select Class   ");
	}

	@EventHandler
	public void onPlayerAttack(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if (damager instanceof Player) {
			if (damager.getWorld().getName().equals("lobby")) {
				e.setCancelled(true);
				return;
			}
			if (plugin.getPhase() < 1) {
				e.setCancelled(true);
				return;
			}

			Player attacker = (Player) damager;
			if (PlayerMeta.getMeta(attacker).getKit() == Kit.WARRIOR) {
				ItemStack hand = attacker.getItemInHand();
				if (hand != null) {
					String lowercaseName = hand.getType().toString()
							.toLowerCase();
					if (lowercaseName.contains("sword")
							|| lowercaseName.contains("axe"))
						e.setDamage(e.getDamage() + 1.0);
				}
			}
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		if (plugin.getPhase() > 0) {
			if (NexusGrinder.Util.isEmptyColumn(e.getBlock().getLocation()))
				e.setCancelled(true);

			if (tooClose(e.getBlock().getLocation())
					&& !e.getPlayer().hasPermission("annihilation.buildbypass")) {
				e.getPlayer().sendMessage(
						ChatColor.RED
								+ "You cannot build this close to the nexus!");
				e.setCancelled(true);
			}
		} else {
			if (!e.getPlayer().hasPermission("annihilation.buildbypass"))
				e.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBreak(BlockBreakEvent e) {
		if (plugin.getPhase() > 0) {
			for (GameTeam t : GameTeam.teams()) {
				if (t.getNexus().getLocation()
						.equals(e.getBlock().getLocation())) {
					e.setCancelled(true);
					if (t.getNexus().isAlive())
						breakNexus(t, e.getPlayer());
					return;
				}
			}

			if (tooClose(e.getBlock().getLocation())
					&& !e.getPlayer().hasPermission("annihilation.buildbypass")) {
				e.getPlayer().sendMessage(
						ChatColor.RED
								+ "You cannot build this close to the nexus!");
				e.setCancelled(true);
			}
		} else {
			if (!e.getPlayer().hasPermission("annihilation.buildbypass"))
				e.setCancelled(true);
		}
	}

	private boolean tooClose(Location loc) {
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();

		for (GameTeam team : GameTeam.teams()) {
			Location nexusLoc = team.getNexus().getLocation();
			double nX = nexusLoc.getX();
			double nY = nexusLoc.getY();
			double nZ = nexusLoc.getZ();
			if (Math.abs(nX - x) <= plugin.build
					&& Math.abs(nY - y) <= plugin.build
					&& Math.abs(nZ - z) <= plugin.build)
				return true;
		}

		return false;
	}

	@EventHandler
	public void onArmorEquip(InventoryClickEvent e) {
		final Player player = (Player) e.getWhoClicked();
		if (PlayerMeta.getMeta(player).getKit() == Kit.SCOUT) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				public void run() {
					PlayerMeta.getMeta(player).getKit()
							.addScoutParticles(player);
				}
			}, 2L);
		}
	}

	private void addHeart(Player player) {
		double maxHealth = player.getMaxHealth();
		if (maxHealth < 30.0) {
			double newMaxHealth = maxHealth + 2.0;
			player.setMaxHealth(newMaxHealth);
			player.setHealth(player.getHealth() + 2.0);
		}
	}

	private void breakNexus(final GameTeam victim, Player breaker) {
		GameTeam attacker = PlayerMeta.getMeta(breaker).getTeam();
		if (victim == attacker)
			breaker.sendMessage(ChatColor.DARK_AQUA
					+ "You can't damage your own nexus");
		else if (plugin.getPhase() == 1)
			breaker.sendMessage(ChatColor.DARK_AQUA
					+ "Nexuses are invincible in phase 1");
		else {
			plugin.getScoreboardHandler().sb.getTeam(victim.name() + "SB")
					.setPrefix(ChatColor.RESET.toString());
			victim.getNexus().damage(plugin.getPhase() == 5 ? 2 : 1);

			plugin.getStatsManager().incrementStat(StatType.NEXUS_DAMAGE,
					breaker, plugin.getPhase() == 5 ? 2 : 1);

			String msg = ChatUtil.nexusBreakMessage(breaker, attacker, victim);
			for (Player p : attacker.getPlayers())
				p.sendMessage(msg);

			plugin.getScoreboardHandler().scores.get(victim.name()).setScore(
					victim.getNexus().getHealth());
			Bukkit.getServer()
					.getPluginManager()
					.callEvent(
							new NexusDamageEvent(breaker, victim, victim
									.getNexus().getHealth()));

			Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				@Override
				public void run() {
					plugin.getScoreboardHandler().sb.getTeam(
							victim.name() + "SB").setPrefix(
							victim.color().toString());
				}
			}, 2L);

			if (victim.getNexus().getHealth() == 0) {
				plugin.getScoreboardHandler().sb.resetScores(plugin
						.getScoreboardHandler().scores.remove(victim.name())
						.getPlayer());
				Bukkit.getServer().getPluginManager()
						.callEvent(new NexusDestroyEvent(breaker, victim));
				ChatUtil.nexusDestroyed(attacker, victim, breaker);
				plugin.checkWin();
				for (Player p : victim.getPlayers()) {
					plugin.getStatsManager().incrementStat(StatType.LOSSES, p);
				}
			}

			plugin.getSignHandler().updateSigns(victim);
		}
	}

	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (event.getEntity().getWorld().getName().equals("lobby")) {
			event.setCancelled(true);
			event.setFoodLevel(20);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Inventory inv = e.getInventory();
		Player player = (Player) e.getWhoClicked();
		if (inv.getTitle().startsWith("Select Class")) {
			if (e.getCurrentItem().getType() == Material.AIR)
				return;
			player.closeInventory();
			String name = e.getCurrentItem().getItemMeta().getDisplayName();
			PlayerMeta meta = PlayerMeta.getMeta(player);
			if (meta.isAlive() && !inv.getTitle().endsWith(" ")) {
				player.sendMessage(ChatColor.GREEN
						+ "You will recieve this class when you respawn.");
				kitsToGive.put(player.getName(),
						Kit.getKit(ChatColor.stripColor(name)));
			} else {
				meta.setKit(Kit.getKit(ChatColor.stripColor(name)));
				if (meta.isAlive())
					player.setHealth(0.0);
			}
			player.sendMessage(ChatColor.DARK_AQUA + "Selected class "
					+ ChatColor.stripColor(name));
		}
	}
}
