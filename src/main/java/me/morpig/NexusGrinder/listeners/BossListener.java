package me.morpig.NexusGrinder.listeners;

import me.morpig.NexusGrinder.NexusGrinder;
import me.morpig.NexusGrinder.chat.ChatUtil;
import me.morpig.NexusGrinder.object.Boss;
import me.morpig.NexusGrinder.object.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

public class BossListener implements Listener {

	private NexusGrinder plugin;
	
	public BossListener(NexusGrinder instance) {
		this.plugin = instance;
	}

	@EventHandler
	public void onHit(EntityDamageEvent event) {
		if (event.getEntity() instanceof IronGolem) {
			if (event.getCause() == DamageCause.FALL
				|| event.getCause() == DamageCause.DROWNING
				|| event.getCause() == DamageCause.FIRE
				|| event.getCause() == DamageCause.FIRE_TICK) {
				event.setCancelled(true);
				return;
			}
			
			final IronGolem g = (IronGolem) event.getEntity();
			if (g.getCustomName() == null) return;
			
			final Boss b = plugin.getBossManager().bossNames.get(g.getCustomName());
			if (b == null) return;
			
			if (event.getCause() == DamageCause.VOID) {
				event.setCancelled(true);
				g.teleport(b.getSpawn());
				return;
			}
			
			Bukkit.getScheduler().runTask(plugin, new Runnable() {
				@Override
				public void run() {
					plugin.getBossManager().update(b, g);
				}
			});
		}
	}
	
	@EventHandler
	public void onDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof IronGolem) {
			IronGolem g = (IronGolem) event.getEntity();
			if (g.getCustomName() == null) return;
			
			Boss b = plugin.getBossManager().bossNames.get(g.getCustomName());
			if (b == null) return;
			
			event.getDrops().clear();
			
			if (g.getKiller() != null) {
				Player killer = g.getKiller();
				ChatUtil.bossDeath(b, killer, PlayerMeta.getMeta(killer).getTeam());
				respawn(b);
			} else {
				g.teleport(b.getSpawn());
			}
		}
	}

	private void respawn(final Boss b) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			@Override
			public void run() {
				Boss n = plugin.getBossManager().newBoss(b);
				ChatUtil.bossRespawn(b);
				plugin.getBossManager().spawn(n);
			}
		}, 20 * plugin.respawn * 60);
	}
}
