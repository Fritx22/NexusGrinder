package me.morpig.NexusGrinder.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import me.morpig.NexusGrinder.stats.StatType;
import me.morpig.NexusGrinder.stats.StatsManager;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatsCommand implements CommandExecutor {
	private StatsManager manager;

	public StatsCommand(StatsManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			if (args.length > 0) {
				List<StatType> types = new LinkedList<StatType>(
						Arrays.asList(StatType.values()));
				Iterator<StatType> iterator = types.iterator();
				while (iterator.hasNext()) {
					StatType type = (StatType) iterator.next();
					boolean keep = false;
					for (String arg : args) {
						if (type.name().toLowerCase()
								.contains(arg.toLowerCase()))
							keep = true;
					}
					if (!keep)
						iterator.remove();
				}
				listStats((Player) sender,
						types.toArray(new StatType[types.size()]));
			} else {
				listStats((Player) sender);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Can only be run by a player!");
		}

		return true;
	}

	private void listStats(Player player) {
		listStats(player, StatType.values());
	}

	private void listStats(Player player, StatType[] stats) {
		String GRAY = ChatColor.GRAY.toString();
		String DARK_AQUA = ChatColor.DARK_AQUA.toString();
		String AQUA = ChatColor.AQUA.toString();

		player.sendMessage(GRAY + "=========[ " + DARK_AQUA + "Stats" + GRAY
				+ " ]=========");

		for (StatType stat : stats) {
			if (stat == null)
				continue;
			String name = WordUtils.capitalize(stat.name().toLowerCase()
					.replace('_', ' '));

			player.sendMessage(DARK_AQUA + name + ": " + AQUA
					+ manager.getStat(stat, player));
		}
		player.sendMessage(GRAY + "=========================");
	}
}