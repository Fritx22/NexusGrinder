package me.morpig.NexusGrinder.commands;

import me.morpig.NexusGrinder.maps.VotingManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteCommand implements CommandExecutor {
	private final VotingManager manager;

	public VoteCommand(VotingManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (!manager.isRunning())
			sender.sendMessage(ChatColor.RED + "Map voting is over.");
		else if (args.length == 0)
			listMaps(sender);
		else if (!manager.vote(sender, args[0])) {
			sender.sendMessage(ChatColor.RED + "Invalid selection!");
			listMaps(sender);
		}
		return true;
	}

	private void listMaps(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_AQUA + "Maps up for voting:");
		for (String map : manager.getMaps())
			sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.GRAY
					+ map);
	}
}
