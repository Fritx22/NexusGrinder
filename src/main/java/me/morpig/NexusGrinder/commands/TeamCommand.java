package me.morpig.NexusGrinder.commands;

import me.morpig.NexusGrinder.NexusGrinder;
import me.morpig.NexusGrinder.object.GameTeam;
import me.morpig.NexusGrinder.object.PlayerMeta;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeamCommand implements CommandExecutor {
	private final NexusGrinder plugin;

	public TeamCommand(NexusGrinder plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args.length == 0)
			listTeams(sender);
		else {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players can join teams");
			} else {
				joinTeam((Player) sender, args[0]);
			}
		}
		return true;
	}

	private void joinTeam(Player player, String team) {
		PlayerMeta meta = PlayerMeta.getMeta(player);
		if (meta.getTeam() != GameTeam.NONE) {
			GameTeam currentTeam = meta.getTeam();
			player.sendMessage(ChatColor.DARK_AQUA + "You are already on "
					+ currentTeam.coloredName());
			return;
		}

		GameTeam target;
		try {
			target = GameTeam.valueOf(team.toUpperCase());
		} catch (IllegalArgumentException e) {
			player.sendMessage(ChatColor.RED + "\"" + team
					+ "\" is not a valid team name!");
			listTeams(player);
			return;
		}

		if (target.getNexus() != null) {
			if (target.getNexus().getHealth() == 0 && plugin.getPhase() > 1) {
				player.sendMessage(ChatColor.RED + "You cannot join a team without a Nexus!");
				return;
			}
		}
		
		player.sendMessage(ChatColor.DARK_AQUA + "You joined "
				+ target.coloredName());
		meta.setTeam(target);

		plugin.getScoreboardHandler().teams.get(team.toUpperCase()).addPlayer(player);
		
		if (plugin.getPhase() > 0) {
			NexusGrinder.Util.sendPlayerToGame(player);
		}
		
		plugin.getSignHandler().updateSigns(GameTeam.RED);
		plugin.getSignHandler().updateSigns(GameTeam.BLUE);
		plugin.getSignHandler().updateSigns(GameTeam.GREEN);
		plugin.getSignHandler().updateSigns(GameTeam.YELLOW);
	}

	private void listTeams(CommandSender sender) {
		sender.sendMessage(ChatColor.GRAY + "============[ "
				+ ChatColor.DARK_AQUA + "Teams" + ChatColor.GRAY
				+ " ]============");
		for (GameTeam t : GameTeam.teams()) {
			int size = 0;

			for (Player p : Bukkit.getOnlinePlayers()) {
				PlayerMeta meta = PlayerMeta.getMeta(p);
				if (meta.getTeam() == t)
					size++;
			}

			if (size != 1) {
				sender.sendMessage(t.coloredName() + " - " + size + " players");
			} else {
				sender.sendMessage(t.coloredName() + " - " + size + " player");
			}
		}
		sender.sendMessage(ChatColor.GRAY + "===============================");
	}
}
