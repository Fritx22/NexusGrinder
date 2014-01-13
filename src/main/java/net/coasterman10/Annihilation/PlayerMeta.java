package net.coasterman10.Annihilation;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class PlayerMeta {
	private static HashMap<String, PlayerMeta> metaTable = new HashMap<String, PlayerMeta>();

	public static PlayerMeta getMeta(Player player) {
		return getMeta(player.getName());
	}

	public static PlayerMeta getMeta(String username) {
		if (!metaTable.containsKey(username))
			metaTable.put(username, new PlayerMeta());
		return metaTable.get(username);
	}

	private AnnihilationTeam team;
	private Kit kit;
	private boolean alive;

	public PlayerMeta() {
		team = AnnihilationTeam.NONE;
		kit = Kit.CIVILIAN;
		alive = false;
	}

	public void setTeam(AnnihilationTeam t) {
		if (team != null)
			team = t;
		else
			team = AnnihilationTeam.NONE;
	}

	public AnnihilationTeam getTeam() {
		return team;
	}
	
	public void setKit(Kit k) {
		if (k != null)
			kit = k;
		else
			kit = Kit.CIVILIAN;
	}
	
	public Kit getKit() {
		return kit;
	}

	public void setAlive(boolean b) {
		alive = b;
	}

	public boolean isAlive() {
		return alive;
	}
}
