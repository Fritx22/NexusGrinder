package net.coasterman10.Annihilation;

import org.bukkit.Location;
import org.bukkit.Material;

public class Nexus {
	private final AnnihilationTeam team;
	private final Location location;
	private int health;

	public Nexus(AnnihilationTeam team, Location location, int health) {
		this.team = team;
		this.location = location;
		this.health = health;

		location.getBlock().setType(Material.ENDER_STONE);
	}

	public AnnihilationTeam getTeam() {
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
