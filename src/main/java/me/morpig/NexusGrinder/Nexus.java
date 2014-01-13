package me.morpig.NexusGrinder;

import org.bukkit.Location;
import org.bukkit.Material;

public class Nexus {
	private final NexusGrinderTeam team;
	private final Location location;
	private int health;

	public Nexus(NexusGrinderTeam team, Location location, int health) {
		this.team = team;
		this.location = location;
		this.health = health;

		location.getBlock().setType(Material.ENDER_STONE);
	}

	public NexusGrinderTeam getTeam() {
		return team;
	}

	public Location getLocation() {
		return location;
	}

	public int getHealth() {
		return health;
	}

	public void damage(int amount) {
		health -= amount;
		if (health <= 0) {
			health = 0;
			location.getBlock().setType(Material.BEDROCK);
		}
	}

	public boolean isAlive() {
		return health > 0;
	}
}
