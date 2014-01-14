package net.coasterman10.Annihilation.stats;

import java.sql.ResultSet;
import java.sql.SQLException;

import net.coasterman10.Annihilation.Annihilation;
import net.coasterman10.Annihilation.manager.ConfigManager;

import org.bukkit.entity.Player;

public class StatsManager {
	private Annihilation plugin;
	private ConfigManager config;
	public static final int UNDEF_STAT = -42;

	public StatsManager(Annihilation instance, ConfigManager config) {
		this.plugin = instance;
		this.config = config;
	}

	public int getStat(StatType s, Player p) {
		if (!plugin.useMysql) {
			return config.getConfig("stats.yml").getInt(
					p.getName() + "." + s.name());
		} else {
			try {
				int stat = UNDEF_STAT;
				ResultSet rs = plugin
						.getDatabaseHandler()
						.query("SELECT * FROM `annihilation` WHERE `username`='"
								+ p.getName() + "'").getResultSet();

				while (rs.next())
					stat = rs.getInt(s.name().toLowerCase());

				return stat;
			} catch (SQLException ex) {
				ex.printStackTrace();
				return UNDEF_STAT;
			}
		}
	}

	public void setValue(StatType s, Player p, int value) {
		if (!plugin.useMysql) {
			config.getConfig("stats.yml").set(p.getName() + "." + s.name(),
					value);
			config.save("stats.yml");
		} else {
			plugin.getDatabaseHandler().query(
					"UPDATE `annihilation` SET `" + s.name().toLowerCase()
							+ "`='" + value + "' WHERE `username`='"
							+ p.getName() + "';");
		}
	}

	public void incrementStat(StatType s, Player p) {
		incrementStat(s, p, 1);
	}

	public void incrementStat(StatType s, Player p, int amount) {
		setValue(s, p, getStat(s, p) + amount);
	}
}
