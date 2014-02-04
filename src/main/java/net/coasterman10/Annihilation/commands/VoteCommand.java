package net.coasterman10.Annihilation.commands;

import net.coasterman10.Annihilation.manager.VotingManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteCommand implements CommandExecutor {
	private final VotingManager manager;

	public VoteCommand(VotingManager manager) {
		this.manager = manager;
	}

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
		int count = 0;
		for (String map : manager.getMaps().values()) {
			count ++;
		    sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.AQUA + "[" + count + "] " + ChatColor.GRAY + map);
		}
	}
}
