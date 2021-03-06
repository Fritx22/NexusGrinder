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
import net.coasterman10.Annihilation.chat.ChatUtil;
import net.coasterman10.Annihilation.object.Boss;
import net.coasterman10.Annihilation.object.PlayerMeta;
import org.bukkit.Bukkit;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

public class BossListener implements Listener {

    private Annihilation plugin;

    public BossListener(Annihilation instance) {
        this.plugin = instance;
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onHit(EntityDamageEvent event) {
        if (event.getEntity() instanceof IronGolem) {

            final IronGolem g = (IronGolem) event.getEntity();
            if (g.getCustomName() == null)
                return;

            final Boss b = plugin.getBossManager().bossNames.get(g
                    .getCustomName());
            if (b == null)
                return;

            if (event.getCause() == DamageCause.VOID) {
                event.getEntity().remove();

                Bukkit.getScheduler().runTask(plugin, new Runnable() {
                    public void run() {
                        Boss n = plugin.getBossManager().newBoss(b);
                        plugin.getBossManager().spawn(n);
                    }
                });
                return;
            }

            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                public void run() {
                    plugin.getBossManager().update(b, g);
                }
            });
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof IronGolem) {
            if (!(e.getDamager() instanceof Player)) {
                e.setCancelled(true);

                final IronGolem g = (IronGolem) e.getEntity();
                if (g.getCustomName() == null)
                    return;

                final Boss b = plugin.getBossManager().bossNames.get(g
                        .getCustomName());

                if (b == null)
                    return;
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof IronGolem) {
            IronGolem g = (IronGolem) event.getEntity();
            if (g.getCustomName() == null)
                return;

            Boss b = plugin.getBossManager().bossNames.get(g.getCustomName());
            if (b == null)
                return;

            event.getDrops().clear();
            b.spawnLootChest();

            if (g.getKiller() != null) {
                Player killer = g.getKiller();
                ChatUtil.bossDeath(b, killer, PlayerMeta.getMeta(killer)
                        .getTeam());
                respawn(b);
                Util.spawnFirework(event.getEntity().getLocation(), PlayerMeta.getMeta(killer).getTeam().getColor(PlayerMeta.getMeta(killer).getTeam()), PlayerMeta.getMeta(killer).getTeam().getColor(PlayerMeta.getMeta(killer).getTeam()));
            } else {
                g.teleport(b.getSpawn());
            }
        }
    }

    private void respawn(final Boss b) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                Boss n = plugin.getBossManager().newBoss(b);
                ChatUtil.bossRespawn(b);
                plugin.getBossManager().spawn(n);
            }
        }, 20 * plugin.respawn * 60);
    }
}
