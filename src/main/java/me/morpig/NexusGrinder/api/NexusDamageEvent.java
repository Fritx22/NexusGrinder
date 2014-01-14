package me.morpig.NexusGrinder.api;

import me.morpig.NexusGrinder.object.GameTeam;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NexusDamageEvent extends Event {

	private Player p;
	private GameTeam t;
	private int h;
	
	public NexusDamageEvent(Player p, GameTeam t, int h) {
		this.p = p;
		this.t = t;
		this.h = h;
	}
	
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Player getPlayer() {
		return p;
	}
	
	public GameTeam getTeam() {
		return t;
	}

	public int getNexusDamage() {
		return h;
	}
}
