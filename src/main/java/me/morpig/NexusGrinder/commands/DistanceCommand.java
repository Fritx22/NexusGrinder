package me.morpig.NexusGrinder.commands;

import me.morpig.NexusGrinder.NexusGrinder;
import me.morpig.NexusGrinder.NexusGrinderTeam;
import me.morpig.NexusGrinder.PlayerMeta;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DistanceCommand implements CommandExecutor {
	private NexusGrinder plugin;

	public DistanceCommand(NexusGrinder instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			
			if (plugin.getPhase() == 0) {
				p.sendMessage(ChatColor.RED + "The game hasn't started yet!");
				return false;
			}
			
			if (PlayerMeta.getMeta(p).getTeam() == NexusGrinderTeam.NONE) {
				p.sendMessage(ChatColor.RED + "The have to be ingame to use this command!");
				return false;
			}
			
			p.sendMessage(ChatColor.GRAY + "=========[ " + ChatColor.DARK_AQUA.toString() + "Distances"
				+ ChatColor.GRAY + " ]=========");
			
			for (NexusGrinderTeam t : NexusGrinderTeam.values()) {
				if (t != NexusGrinderTeam.NONE) {
					showTeam(p, t);
				}
			}
			
			p.sendMessage(ChatColor.GRAY + "============================");
		} else {
			sender.sendMessage(ChatColor.RED + "Can only be run by a player!");
		}

		return true;
	}

	private void showTeam(Player p, NexusGrinderTeam t) {
		try {
			if (t.getNexus() != null && t.getNexus().getHealth() > 0)
				p.sendMessage(t.coloredName() + ChatColor.GRAY + " Nexus Distance: " + ChatColor.WHITE + ((int) p.getLocation().distance(t.getNexus().getLocation())) + ChatColor.GRAY + " Blocks");
		} catch (IllegalArgumentException ex) {
			
		}
	}
}
