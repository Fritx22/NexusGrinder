package net.coasterman10.Annihilation;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

public class SignHandler {
	private Annihilation plugin;

	private HashMap<AnnihilationTeam, ArrayList<Location>> signs = new HashMap<AnnihilationTeam, ArrayList<Location>>();

	public SignHandler(Annihilation instance) {
		this.plugin = instance;
	}

	public void loadSigns() {
		ConfigurationSection config = plugin.getConfigManager().getConfig(
				"maps.yml");
		for (AnnihilationTeam team : AnnihilationTeam.teams()) {
			signs.put(team, new ArrayList<Location>());
			String name = team.name().toLowerCase();
			for (String l : config.getStringList("lobby.signs." + name)) {
				Location loc = Annihilation.Util.parseLocation(
						Bukkit.getWorld("lobby"), l);
				if (loc != null)
					addTeamSign(team, loc);
			}
		}
	}

	public void addTeamSign(AnnihilationTeam team, Location loc) {
		Block b = loc.getBlock();
		if (b == null)
			return;

		Material m = b.getType();
		if (m == Material.SIGN_POST || m == Material.WALL_SIGN) {
			signs.get(team).add(loc);
			updateSigns(team);
		}
	}

	public void updateSigns(AnnihilationTeam t) {
		if (t == AnnihilationTeam.NONE)
			return;

		for (Location l : signs.get(t)) {
			Block b = l.getBlock();
			if (b == null)
				return;

			Material m = b.getType();
			if (m == Material.SIGN_POST || m == Material.WALL_SIGN) {
				Sign s = (Sign) b.getState();
				s.setLine(0, ChatColor.DARK_PURPLE + "[Team]");
				s.setLine(1, t.coloredName());
				s.setLine(2, ChatColor.UNDERLINE.toString() + t.getPlayers().size()
						+ (t.getPlayers().size() == 1 ? " Player" : " Players"));
				if (t.getNexus() != null && plugin.getPhase() > 0)
					s.setLine(3, ChatColor.BOLD.toString() + "Nexus: "
							+ t.getNexus().getHealth());
				else
					s.setLine(3, " ");
				s.update(true);
			}
		}
	}
}
