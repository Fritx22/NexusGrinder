package net.coasterman10.Annihilation.object;

import org.bukkit.Location;

public class Boss {
	private String configName;
	private int health;
	private String bossName;
	private Location spawn;
	private Location chest;
	private boolean alive;
	
	public Boss(String configName, int health, String bossName, Location spawn, Location chest) {
		this.configName = configName;
		this.health = health;
		this.bossName = bossName;
		this.spawn = spawn;
		this.chest = chest;
		this.setAlive(false);
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public String getBossName() {
		return bossName;
	}

	public void setBossName(String bossName) {
		this.bossName = bossName;
	}

	public Location getSpawn() {
		return spawn;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}

	public Location getChest() {
		return chest;
	}

	public void setChest(Location chest) {
		this.chest = chest;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
