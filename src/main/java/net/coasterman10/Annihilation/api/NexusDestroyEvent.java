package net.coasterman10.Annihilation.api;

import net.coasterman10.Annihilation.AnnihilationTeam;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NexusDestroyEvent extends Event {

	private Player p;
	private AnnihilationTeam t;
	
	public NexusDestroyEvent(Player p, AnnihilationTeam t) {
		this.p = p;
		this.t = t;
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
	
	public AnnihilationTeam getTeam() {
		return t;
	}
}
