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
package net.coasterman10.Annihilation.listeners;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.Util;
import net.coasterman10.Annihilation.api.NexusDamageEvent;
import net.coasterman10.Annihilation.api.NexusDestroyEvent;
import net.coasterman10.Annihilation.bar.BarUtil;
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.manager.PhaseManager;
import net.coasterman10.Annihilation.object.TeamEnum;
import net.coasterman10.Annihilation.object.Kit;
import net.coasterman10.Annihilation.object.PlayerMeta;
import net.coasterman10.Annihilation.stats.StatType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Random;

import static net.coasterman10.Annihilation.Translation.get;

public class PlayerListener implements Listener {
    private Annihilation plugin;

    private HashMap<String, Kit> kitsToGive = new HashMap<String, Kit>();

    public PlayerListener(Annihilation plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerSwim(PlayerMoveEvent e) {
        if (e.getTo().getBlock().getType().equals(Material.STATIONARY_WATER)) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 1000, 10));
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onMOTDPing(ServerListPingEvent e) {
        if (plugin.messageOfTheDay) {
            String motd = plugin.getConfig().getString("motd");
            try {
                motd = motd.replaceAll("%PHASE%",
                        String.valueOf(plugin.getPhase() == 0 ? "Starting" : plugin.getPhase()));
                motd = motd.replaceAll("%TIME%", PhaseManager
                        .timeString(plugin.getPhaseManager().getTime()));
                motd = motd.replaceAll("%PLAYERCOUNT",
                        String.valueOf(Bukkit.getOnlinePlayers().size()));
                motd = motd.replaceAll("%MAXPLAYERS%",
                        String.valueOf(Bukkit.getMaxPlayers()));
                motd = motd.replaceAll("%GREENNEXUS%",
                        String.valueOf(getNexus(TeamEnum.GREEN)));
                motd = motd.replaceAll("%GREENCOUNT%",
                        String.valueOf(getPlayers(TeamEnum.GREEN)));
                motd = motd.replaceAll("%REDNEXUS%",
                        String.valueOf(getNexus(TeamEnum.RED)));
                motd = motd.replaceAll("%REDCOUNT%",
                        String.valueOf(getPlayers(TeamEnum.GREEN)));
                motd = motd.replaceAll("%BLUENEXUS%",
                        String.valueOf(getNexus(TeamEnum.BLUE)));
                motd = motd.replaceAll("%BLUECOUNT%",
                        String.valueOf(getPlayers(TeamEnum.GREEN)));
                motd = motd.replaceAll("%YELLOWNEXUS%",
                        String.valueOf(getNexus(TeamEnum.YELLOW)));
                motd = motd.replaceAll("%YELLOWCOUNT%",
                        String.valueOf(getPlayers(TeamEnum.GREEN)));

                e.setMotd(ChatColor.translateAlternateColorCodes('&', motd));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private int getNexus(TeamEnum t) {
        int health = 0;

        if (t.getNexus() != null)
            health = t.getNexus().getHealth();

        return health;
    }

    private int getPlayers(TeamEnum t) {
        int size = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerMeta meta = PlayerMeta.getMeta(p);
            if (meta.getTeam() == t)
                size++;
        }

        return size;
    }

    @SuppressWarnings("unused")
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
                            Util.showClassSelector(e.getPlayer(),
                                    "Select Class");
                            return;
                        }
                    }
                }
                if (handItem.getType() == Material.COMPASS) {
                    boolean setCompass = false;
                    boolean setToNext = false;
                    while (!setCompass) {
                        for (TeamEnum team : TeamEnum.teams()) {
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
                    TeamEnum team = TeamEnum.valueOf(teamName.toUpperCase());
                    if (pmeta.getTeam() == TeamEnum.NONE)
                        plugin.joinTeam(e.getPlayer(), teamName);
                }
            }
        }
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @EventHandler
    public void onSignPlace(SignChangeEvent e) {
        if (e.getPlayer().hasPermission("annihilation.buildbypass"))
            if (e.getLine(0).toLowerCase().contains("[Shop]".toLowerCase()))
                e.setLine(0, ChatColor.DARK_PURPLE + "[Shop]");
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onKick(PlayerKickEvent e) {
        if (e.getReason().equals(ChatColor.RED + "ANNIHILATION-TRIGGER-KICK-01")) {
            e.setReason(ChatColor.RED + "You cannot join the game in this phase!");
            e.setLeaveMessage(null);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        String prefix = ChatColor.GOLD + "[NexusGrinder] " + ChatColor.GRAY;
        final Player player = e.getPlayer();

        PlayerMeta meta = PlayerMeta.getMeta(player);

        if (plugin.getPhase() > plugin.lastJoinPhase
                && !player.hasPermission("annhilation.bypass.phaselimiter")) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    player.kickPlayer((ChatColor.RED
                            + "ANNIHILATION-TRIGGER-KICK-01"));
                }
            }, 1L);
            e.setJoinMessage(null);
            return;
        }

        player.sendMessage(prefix + ChatColor.GREEN + "Welcome to NexusGrinder!");
        player.sendMessage(prefix + ChatColor.GRAY
                + "Idea from the brain of _BritishCow_");
        player.sendMessage(prefix + ChatColor.GOLD + "Coded by morpigthekidMC");
        player.sendMessage(prefix + ChatColor.RED + ChatColor.BOLD + ChatColor.UNDERLINE + "THIS GAME COULD TOOK 1 HOUR OR MORE. THIS IS A GAME. HAVE FUN AND GOOD LUCK");

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
            BarUtil.setMessageAndPercent(player, ChatColor.DARK_AQUA
                    + "Welcome to NexusGrinder!", 0.01f);
            plugin.checkStarting();
        }

        plugin.getSignHandler().updateSigns(meta.getTeam());
        plugin.getScoreboardHandler().update();
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        final Player p = e.getEntity();

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

        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                p.spigot().respawn();
            }
        }, 1L);
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        final Player player = e.getPlayer();
        player.setHealth(0D);
        Bukkit.getScheduler().runTaskLater(plugin , new Runnable() {
            public void run() {
                Util.showClassSelector(player, "Select Class");
            }
        }, 41);
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.getPhase() > 0) {
            if (Util.isEmptyColumn(e.getBlock().getLocation()))
                e.setCancelled(true);

            if (tooClose(e.getBlock().getLocation())
                    && !e.getPlayer().hasPermission("annihilation.buildbypass")) {
                e.getPlayer().sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") +
                        ChatColor.RED
                                + "You cannot build this close to the nexus!");
                e.setCancelled(true);
            }
        } else {
            if (!e.getPlayer().hasPermission("annihilation.buildbypass"))
                e.setCancelled(true);
        }

        if (!e.isCancelled()) {
            if (e.getBlock().getState() instanceof Sign) {
                Sign s = (Sign) e.getBlock().getState();
                if (e.getPlayer().hasPermission("annihilation.buildbypass")) {
                    if (s.getLine(0).equals("Shop"))
                        s.setLine(0, ChatColor.GOLD + "Shop");
                    s.update(true);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getPhase() > 0) {
            for (TeamEnum t : TeamEnum.teams()) {
                if (t.getNexus().getLocation()
                        .equals(e.getBlock().getLocation())) {
                    e.setCancelled(true);
                    if (t.getNexus().isAlive())
                        breakNexus(t, e.getPlayer());
                    return;
                }
            }

            if (tooClose(e.getBlock().getLocation())
                    && !e.getPlayer().hasPermission("annihilation.buildbypass")
                    && e.getBlock().getType() != Material.ENDER_STONE) {
                e.getPlayer().sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") +
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

        for (TeamEnum team : TeamEnum.teams()) {
            Location nexusLoc = team.getNexus().getLocation();
            double nX = nexusLoc.getX();
            double nY = nexusLoc.getY();
            double nZ = nexusLoc.getZ();
            if (Math.abs(nX - x) <= plugin.nexusBuildRadius
                    && Math.abs(nY - y) <= plugin.nexusBuildRadius
                    && Math.abs(nZ - z) <= plugin.nexusBuildRadius)
                return true;
        }

        return false;
    }

    private void addHeart(Player player) {
        double maxHealth = player.getMaxHealth();
        if (maxHealth < 30.0) {
            double newMaxHealth = maxHealth + 2.0;
            player.setMaxHealth(newMaxHealth);
            player.setHealth(player.getHealth() + 2.0);
        }
    }

    private void breakNexus(final TeamEnum victim, Player breaker) {
        final TeamEnum attacker = PlayerMeta.getMeta(breaker).getTeam();
        if (victim == attacker)
            breaker.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.DARK_AQUA
                    + "You can't damage your own nexus");
        else if (plugin.getPhase() == 1)
            breaker.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.DARK_AQUA
                    + "Nexuses are invincible in phase 1");
        else {
            plugin.getScoreboardHandler().sb.getTeam(victim.name() + "SB")
                    .setPrefix(ChatColor.RESET.toString());
            victim.getNexus().damage(plugin.getPhase() == 5 ? 2 : 1);

            plugin.getStatsManager().incrementStat(StatType.NEXUS_DAMAGE,
                    breaker, plugin.getPhase() == 5 ? 2 : 1);


            for (Player p : attacker.getPlayers())
                p.playSound(p.getLocation(), Sound.NOTE_PLING, 10, 1);

            plugin.getScoreboardHandler().scores.get(victim.name()).setScore(
                    victim.getNexus().getHealth());
            Bukkit.getServer()
                    .getPluginManager()
                    .callEvent(
                            new NexusDamageEvent(breaker, victim, victim
                                    .getNexus().getHealth()));

            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                public void run() {
                    plugin.getScoreboardHandler().sb.getTeam(
                            victim.name() + "SB").setPrefix(
                            victim.color().toString());
                }
            }, 2L);

            Random r = new Random();
            float pitch = 0.5F + r.nextFloat() * 0.5F;
            victim.getNexus()
                    .getLocation()
                    .getWorld()
                    .playSound(victim.getNexus().getLocation(),
                            Sound.ANVIL_LAND, 1F, pitch);

            Location nexus = victim.getNexus().getLocation().clone();
            nexus.add(0.5, 0, 0.5);
            Util.ParticleEffects.sendToLocation(
                    Util.ParticleEffects.LAVA_SPARK, nexus, 1F, 1F, 1F, 0, 20);
            Util.ParticleEffects.sendToLocation(
                    Util.ParticleEffects.LARGE_SMOKE, nexus, 1F, 1F, 1F, 0, 20);

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

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.getWorld().playSound(player.getLocation(),
                            Sound.EXPLODE, 1F, 1.25F);
                }

                for (final Location spawn : victim.getSpawns()) {
                    Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                        public void run() {
                            Util.spawnFirework(spawn,
                                    attacker.getColor(attacker),
                                    attacker.getColor(attacker));
                        }
                    }, new Random().nextInt(20));
                }

                Util.ParticleEffects.sendToLocation(
                        Util.ParticleEffects.LARGE_EXPLODE, nexus, 1F, 1F, 1F,
                        0, 20);

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {
                        Location nexus = victim.getNexus().getLocation()
                                .clone();
                        boolean found = false;
                        int y = 0;

                        while (!found) {
                            y++;

                            Block b = nexus.add(0, 1, 0).getBlock();

                            if (b != null && b.getType() == Material.BEACON)
                                b.setType(Material.AIR);

                            if (y > 10)
                                found = true;
                        }
                    }
                });
            }

            plugin.getSignHandler().updateSigns(victim);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity().getWorld().getName().equals("lobby")) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory inv = e.getInventory();
        Player player = (Player) e.getWhoClicked();
        if (inv.getTitle().startsWith("Select Class")) {
            if (e.getCurrentItem().getType() == Material.AIR)
                return;
            player.closeInventory();
            e.setCancelled(true);
            String name = e.getCurrentItem().getItemMeta().getDisplayName();
            PlayerMeta meta = PlayerMeta.getMeta(player);

            if (!Kit.valueOf(ChatColor.stripColor(name).toUpperCase())
                    .isOwnedBy(player)) {
                player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.RED + "You do not own this class.");
                return;
            }

            player.sendMessage(ChatColor.GREEN +
            "You will recieve this class when you respawn.");
            meta.setKit(Kit.getKit(ChatColor.stripColor(name)));
            player.sendMessage(ChatColor.GOLD + get("ANNIHILATION_PREFIX") + ChatColor.DARK_AQUA + "Selected class " + ChatColor.stripColor(name));
        }
    }
}