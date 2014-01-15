package me.morpig.NexusGrinder.api;

import me.morpig.NexusGrinder.object.Boss;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BossSpawnEvent extends Event {

	private Boss b;
	
	public BossSpawnEvent(Boss b) {
		this.b = b;
	}
	
	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Boss getBoss() {
		return b;
	}
}
